package com.example.versuion;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static com.example.versuion.utiles.Constants.APP_ROOT;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration de bout en bout : une vraie base PostgreSQL (Testcontainers),
 * le contexte Spring complet (Flyway + JPA + Security 6 + Springdoc) et l'API REST
 * authentifiée par JWT.
 *
 * Ignorés automatiquement lorsqu'aucun environnement Docker n'est disponible
 * (ex. poste de dev sans Docker) afin de ne pas bloquer `mvn test`.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class VersuionApplicationTests {

    private static final String MOT_DE_PASSE_ADMIN_DEFAUT = "Admin123!";

    /** Racine d'URL utilisée par MockMvc (doit commencer par '/'). */
    private static final String URL = "/" + APP_ROOT;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("inventory_db")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Désactive la tâche planifiée d'alertes pendant les tests
        registry.add("app.stock-alert.cron", () -> "-");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void accesSansTokenRefuse() throws Exception {
        mockMvc.perform(get(URL + "/articles/paged"))
                .andExpect(status().isForbidden());
    }

    @Test
    void cycleCompletAuthentificationEtApi() throws Exception {
        String email = uniqueEmail();

        // 1. Création d'une entreprise (endpoint public) -> crée l'utilisateur ADMIN
        MvcResult entResult = mockMvc.perform(post(URL + "/entreprises/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(entrepriseJson(email)))
                .andExpect(status().isOk())
                .andReturn();
        long entrepriseId = objectMapper.readTree(entResult.getResponse().getContentAsString())
                .get("id").asLong();

        // 2. Authentification de l'admin
        String token = login(email);

        // 3. Endpoint /auth/me avec le token
        mockMvc.perform(get(URL + "/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.roles[0].rolename").value("ADMIN"));

        // 4. Création d'une catégorie
        long categoryId = createCategory(token, "CAT-001");

        // 5. Création d'un article avec seuil d'alerte
        long articleId = createArticle(token, "ART-001", "Article de test", categoryId);

        // 6. Liste paginée avec le token -> l'article est visible (filtrage entreprise)
        mockMvc.perform(get(URL + "/articles/paged")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "designation")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));

        // 7. Recherche par designation
        mockMvc.perform(get(URL + "/articles/paged")
                        .header("Authorization", "Bearer " + token)
                        .param("search", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        // 8. Articles sous le seuil (aucun mouvement de stock -> stock 0 < seuil 5)
        mockMvc.perform(get(URL + "/articles/sous-seuil")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        // 9. Dashboard de base
        mockMvc.perform(get(URL + "/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreArticles").value(1))
                .andExpect(jsonPath("$.nombreClients").value(0))
                .andExpect(jsonPath("$.stockSousSeuil").value(1))
                .andExpect(jsonPath("$.valeurStock").isNumber());

        // 10. Facture PDF d'une commande inexistante -> 404 (endpoint câblé)
        mockMvc.perform(get(URL + "/commandesclients/999/facture/pdf")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        // 11. Enregistrement d'une vente (déduit le stock via MvtStk)
        String venteJson = objectMapper.writeValueAsString(Map.of(
                "code", "V-" + entrepriseId,
                "dateVente", "2026-08-12T08:00:00Z",
                "commentaire", "Vente de test",
                "ligneVentes", List.of(Map.of(
                        "article", Map.of("id", articleId),
                        "quantite", 2,
                        "prixUnitaire", 1000
                ))
        ));
        mockMvc.perform(post(URL + "/ventes/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(venteJson))
                .andExpect(status().isOk());

        // 12. Dashboard avec données réelles -> exerce les projections JPQL (topArticles, ventesParMois, ventesParCategorie)
        mockMvc.perform(get(URL + "/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreVentes").value(1))
                .andExpect(jsonPath("$.chiffreAffairesTotal").isNumber())
                .andExpect(jsonPath("$.topArticles.length()").value(1))
                .andExpect(jsonPath("$.topArticles[0].designation").value("Article de test"))
                .andExpect(jsonPath("$.ventesParMois.length()").value(1))
                .andExpect(jsonPath("$.ventesParMois[0].annee").value(2026))
                .andExpect(jsonPath("$.ventesParCategorie.length()").value(1));

        // 13. Export Excel des articles (rôle ADMIN autorisé)
        mockMvc.perform(get(URL + "/exports/articles/excel")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void permissionsParRole() throws Exception {
        String adminEmail = uniqueEmail();
        MvcResult entResult = mockMvc.perform(post(URL + "/entreprises/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(entrepriseJson(adminEmail)))
                .andExpect(status().isOk())
                .andReturn();
        long entrepriseId = objectMapper.readTree(entResult.getResponse().getContentAsString())
                .get("id").asLong();
        String adminToken = login(adminEmail);
        long categoryId = createCategory(adminToken, "CAT-002");
        long articleId = createArticle(adminToken, "ART-002", "Article role", categoryId);

        // Création d'un utilisateur VENDEUR (réservé à l'ADMIN) : l'entreprise est
        // rattachée automatiquement à partir du token de l'admin.
        String vendeurEmail = uniqueEmail();
        String userJson = objectMapper.writeValueAsString(Map.of(
                "nom", "Vendeur",
                "prenom", "Test",
                "email", vendeurEmail,
                "motDePasse", MOT_DE_PASSE_ADMIN_DEFAUT,
                "dateDeNaissance", "1990-01-01",
                "adresse", Map.of(
                        "addresse1", "1 rue des Vendeurs",
                        "Ville", "Lomé",
                        "codePostale", "00000",
                        "pays", "Togo")
        ));
        MvcResult userResult = mockMvc.perform(post(URL + "/utilisateurs/create")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andReturn();

        // L'admin récupère l'ID du vendeur (réponse JSON) puis lui affecte le rôle VENDEUR
        long vendeurId = objectMapper.readTree(userResult.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(put(URL + "/utilisateurs/roles/" + vendeurId + "/VENDEUR")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Connexion du vendeur
        String vendeurToken = login(vendeurEmail);

        // Un VENDEUR peut consulter mais pas supprimer un article (403)
        mockMvc.perform(delete(URL + "/articles/delete/" + articleId)
                        .header("Authorization", "Bearer " + vendeurToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get(URL + "/articles/all")
                        .header("Authorization", "Bearer " + vendeurToken))
                .andExpect(status().isOk());

        // Les exports sont réservés aux ADMIN/MANAGER
        mockMvc.perform(get(URL + "/exports/articles/excel")
                        .header("Authorization", "Bearer " + vendeurToken))
                .andExpect(status().isForbidden());

        // L'admin, lui, peut supprimer l'article
        mockMvc.perform(delete(URL + "/articles/delete/" + articleId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private String login(String email) throws Exception {
        MvcResult loginResult = mockMvc.perform(post(URL + "/auth/authentification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"" + email + "\",\"password\":\"" + MOT_DE_PASSE_ADMIN_DEFAUT + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("accessToken").asText();
        assertNotNull(token, "Le token JWT ne doit pas être null");
        return token;
    }

    private long createCategory(String token, String code) throws Exception {
        MvcResult result = mockMvc.perform(post(URL + "/categories/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codeCategory\":\"" + code + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long createArticle(String token, String code, String designation, long categoryId) throws Exception {
        String articleJson = objectMapper.writeValueAsString(Map.of(
                "codeArticle", code,
                "designation", designation,
                "prixUnitaire", 1000,
                "tauxTva", 18,
                "prixUnitaireTTc", 1180,
                "seuilAlerte", 5,
                "category", Map.of("id", categoryId)
        ));
        MvcResult result = mockMvc.perform(post(URL + "/articles/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(articleJson))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private String entrepriseJson(String email) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "nom", "Test Corp",
                "description", "Entreprise de test",
                "codefiscale", "CF-2024-001",
                "email", email,
                "numTel", "+22890000000",
                "adresse", Map.of(
                        "addresse1", "1 rue des Tests",
                        "Ville", "Lomé",
                        "codePostale", "00000",
                        "pays", "Togo")
        ));
    }

    private String uniqueEmail() {
        return "admin" + System.nanoTime() + "@test.com";
    }
}
