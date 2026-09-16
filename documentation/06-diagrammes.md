# 06 — Diagrammes

Tous les diagrammes sont en **Mermaid** : ils s'affichent nativement sur GitHub/GitLab,
dans VS Code (extension Mermaid Preview) et sur [mermaid.live](https://mermaid.live).

---

## 1. Diagramme de cas d'utilisation

```mermaid
flowchart LR
    subgraph Acteurs
        A((Visiteur))
        V((VENDEUR))
        M((MANAGER))
        AD((ADMIN))
    end

    subgraph Système [Application Gestion de Stock]
        UC1([S'inscrire entreprise])
        UC2([Se connecter / déconnecter])
        UC3([Consulter le dashboard])
        UC4([Gérer les articles])
        UC5([Gérer les catégories])
        UC6([Gérer les clients])
        UC7([Gérer les fournisseurs])
        UC8([Créer une commande client])
        UC9([Faire évoluer l'état d'une commande])
        UC10([Télécharger une facture PDF])
        UC11([Créer une commande fournisseur])
        UC12([Enregistrer une vente])
        UC13([Entrées / sorties / corrections de stock])
        UC14([Consulter l'historique des mouvements])
        UC15([Gérer les utilisateurs])
        UC16([Affecter les rôles])
        UC17([Modifier son profil et sa photo])
        UC18([Exporter Excel / CSV])
    end

    A --> UC1
    A --> UC2
    V --> UC2
    V --> UC3
    V --> UC4
    V --> UC6
    V --> UC7
    V --> UC8
    V --> UC12
    V --> UC14
    V --> UC17
    M --> UC5
    M --> UC9
    M --> UC10
    M --> UC11
    M --> UC13
    M --> UC18
    AD --> UC15
    AD --> UC16
```

---

## 2. Diagramme de classes (cœur métier)

```mermaid
classDiagram
    class Entreprise {
        +Long id
        +String nom
        +String email
        +String codefiscale
    }
    class Utilisateurs {
        +Long id
        +String nom
        +String prenom
        +String email
        +String motDePasse
        +String photo
    }
    class Roles {
        +Long id
        +String rolename
    }
    class Article {
        +Long id
        +String codeArticle
        +String designation
        +BigDecimal prixUnitaire
        +BigDecimal tauxTva
        +BigDecimal prixUnitaireTTc
        +BigDecimal seuilAlerte
        +String photo
    }
    class Category {
        +Long id
        +String codeCategory
        +String designation
    }
    class Client {
        +Long id
        +String nom
        +String prenom
        +String mail
        +String numTel
    }
    class Fournisseur {
        +Long id
        +String nom
        +String prenom
    }
    class CommandeClient {
        +Long id
        +String code
        +Instant dateComande
        +EtatCommande etatCommande
    }
    class ComandeFournisseur {
        +Long id
        +String code
        +EtatCommande etatCommande
    }
    class LigneComandeClient {
        +Long id
        +BigDecimal quantite
        +BigDecimal prixUnitaire
    }
    class LigneComandeFournisseur {
        +Long id
        +BigDecimal quantite
        +BigDecimal prixUnitaire
    }
    class Ventes {
        +Long id
        +String code
        +Instant dateVente
    }
    class LigneVente {
        +Long id
        +BigDecimal quantite
        +BigDecimal prixUnitaire
    }
    class MvtStk {
        +Long id
        +Instant dateMvt
        +BigDecimal quantite
        +TypeMvtStk typeMvt
        +SourceMvtStk sourceMvt
    }

    Entreprise "1" --> "0..*" Utilisateurs
    Utilisateurs "1" --> "0..*" Roles
    Entreprise "1" --> "0..*" Article
    Category "1" --> "0..*" Article
    Entreprise "1" --> "0..*" Client
    Entreprise "1" --> "0..*" Fournisseur
    Client "1" --> "0..*" CommandeClient
    CommandeClient "1" *-- "1..*" LigneComandeClient
    Article "1" --> "0..*" LigneComandeClient
    Fournisseur "1" --> "0..*" ComandeFournisseur
    ComandeFournisseur "1" *-- "1..*" LigneComandeFournisseur
    Article "1" --> "0..*" LigneComandeFournisseur
    Ventes "1" *-- "1..*" LigneVente
    Article "1" --> "0..*" LigneVente
    Article "1" --> "0..*" MvtStk
```

---

## 3. Diagramme de séquence — Connexion et appel authentifié

```mermaid
sequenceDiagram
    actor U as Utilisateur
    participant FE as Frontend
    participant API as API Spring Boot
    participant DB as PostgreSQL

    U->>FE: email + mot de passe
    FE->>API: POST /auth/authentification
    API->>DB: findByEmail + vérification BCrypt
    DB-->>API: utilisateur + rôles
    API-->>FE: { accessToken (JWT, 8h, idEntreprise) }
    FE->>FE: localStorage.setItem(token)
    FE->>API: GET /auth/me (Bearer token)
    API-->>FE: profil + rôles + entreprise
    FE-->>U: interface personnalisée

    U->>FE: action métier (ex. liste clients)
    FE->>API: GET /clients/all (Bearer token)
    API->>API: JwtRequestFiltre → MDC idEntreprise
    API->>DB: SELECT ... WHERE identreprise = ?
    DB-->>API: lignes du tenant
    API-->>FE: JSON filtré
```

---

## 4. Diagramme de séquence — Livraison d'une commande client (impact stock)

```mermaid
sequenceDiagram
    actor M as MANAGER
    participant FE as Frontend
    participant API as API
    participant DB as PostgreSQL

    M->>FE: commande CMD-001 → état LIVREE
    FE->>API: PATCH /commandesclients/update/etat/{id}/LIVREE
    API->>DB: UPDATE commande_client SET etatcommande='LIVREE'
    loop Pour chaque ligne de commande
        API->>API: création du mouvement (quantité négative)
        API->>DB: INSERT mvt_stk (SORTIE, source=COMMANDE_CLIENT)
    end
    API-->>FE: commande mise à jour
    M->>FE: consulte le stock de l'article
    FE->>API: GET /mvtstk/stockreel/{idArticle}
    API->>DB: SELECT SUM(quantite) FROM mvt_stk WHERE article_id = ?
    DB-->>API: stock réel
    API-->>FE: nouveau niveau de stock
```

---

## 5. Machine à états — Cycle de vie d'une commande

```mermaid
stateDiagram-v2
    [*] --> EN_PREPARATION : création (POST /create)
    EN_PREPARATION --> VALIDEE : PATCH etat/VALIDEE
    VALIDEE --> LIVREE : PATCH etat/LIVREE
    EN_PREPARATION --> LIVREE : PATCH etat/LIVREE
    LIVREE --> [*]

    note right of LIVREE
        Client : génère les SORTIES de stock
        Fournisseur : génère les ENTRÉES de stock
    end note
```

---

## 6. Diagramme d'activité — Vente directe avec contrôle de stock

```mermaid
flowchart TD
    A[Vendeur ouvre Nouvelle vente] --> B[Sélectionne articles + quantités]
    B --> C{Lignes valides ?}
    C -- Non --> B
    C -- Oui --> D[POST /ventes/create]
    D --> E[Backend enregistre la vente]
    E --> F[Pour chaque ligne : génère un mvt SORTIE]
    F --> G[Stock réel recalculé = SUM quantités]
    G --> H{Stock ≤ seuil d'alerte ?}
    H -- Oui --> I[Article signalé sous seuil dashboard + alerte]
    H -- Non --> J[Tout est OK]
    I --> K[Liste des ventes actualisée]
    J --> K
```

---

## 7. Diagramme d'activité — Upload d'une photo de profil

```mermaid
flowchart TD
    A[Utilisateur clique sur sa photo] --> B[Choix du fichier]
    B --> C{Format jpg/png/webp et ≤ 5 Mo ?}
    C -- Non --> D[Erreur affichée] --> B
    C -- Oui --> E[Aperçu local instantané FileReader]
    E --> F[POST /utilisateurs/me/photo multipart]
    F --> G[Backend : nom UUID + écriture disque photos/]
    G --> H[Remplacement du champ photo en base]
    H --> I[Ancienne photo supprimée du disque]
    I --> J[GET /auth/me → avatar actualisé dans le header]
```

---

## 8. Diagramme de déploiement

```mermaid
flowchart TB
    subgraph HOTE["Machine hôte (Docker Compose)"]
        subgraph NG["Nginx :4200 (gds_frontend)"]
            ANG["Bundle Angular (statique)"]
        end
        subgraph NODE["Node :3009 (gds_frontend_next)"]
            NEXT["Serveur Next.js standalone"]
        end
        subgraph SPRING["Tomcat :8089 (gds_backend)"]
            JAR["app.jar — Spring Boot 3.3"]
            subgraph SPRING_INT
                SEC[Spring Security + JWT]
                FLY[Flyway]
                JPA[Hibernate / JPA]
            end
        end
        subgraph PG["PostgreSQL :5432 (gds_db)"]
            DB[(inventory_db)]
        end
        VOL_PHOTOS[("Volume photos_data")]
        VOL_DB[("Volume db_data")]
    end

    Navigateur -->|:4200| ANG
    Navigateur -->|:3009| NEXT
    ANG -->|REST /gestiondestock| JAR
    NEXT -->|REST /gestiondestock| JAR
    JAR --> DB
    JAR --> VOL_PHOTOS
    DB --- VOL_DB
```

---

## 9. Diagramme entité-relation

Voir [04. Modèle de données](./04-modele-de-donnees.md) § 1 (diagramme ERD Mermaid).

---

## 10. Vue du flux multi-tenant (pipeline d'une requête)

```mermaid
flowchart LR
    A[Requête HTTP + Bearer JWT] --> B[JwtRequestFiltre]
    B --> C{Token valide ?}
    C -- Non --> D[401 Unauthorized]
    C -- Oui --> E[Extraction idEntreprise → MDC]
    E --> F[Spring Security : rôles]
    F --> G{Endpoint protégé ?}
    G -- Rôle insuffisant --> H[403 Forbidden]
    G -- OK --> I[Contrôleur → Service]
    I --> J[Repository *Tenant : WHERE identreprise = ?]
    J --> K[(PostgreSQL)]
    K --> L[Réponse JSON filtrée]
```
