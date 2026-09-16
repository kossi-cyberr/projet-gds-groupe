# 02 — Architecture technique

## 1. Vue d'ensemble

```
┌──────────────────┐     ┌──────────────────┐
│  Angular :4200   │     │ Next.js :3009    │
│  (Nginx static)  │     │  (Node server)   │
└────────┬─────────┘     └────────┬─────────┘
         │        HTTP/JSON       │
         └───────────┬────────────┘
                     ▼
          ┌─────────────────────┐
          │  Backend :8089      │
          │  Spring Boot 3.3    │
          │  /gestiondestock    │
          └──────────┬──────────┘
                     ▼
          ┌─────────────────────┐     ┌──────────────┐
          │  PostgreSQL :5436   │     │ /photos/**   │
          │  (inventory_db)     │     │ volume local │
          └─────────────────────┘     └──────────────┘
```

## 2. Stack technique

| Couche | Technologie | Version |
|---|---|---|
| Backend | Java (Temurin) | 17 |
| Framework | Spring Boot | 3.3.5 |
| Sécurité | Spring Security + JWT (jjwt) | 0.11.5 |
| ORM | Hibernate (via Spring Data JPA) | 6.5 |
| Migrations | Flyway (+ flyway-database-postgresql) | — |
| Base de données | PostgreSQL | 15 (alpine) |
| Documentation API | springdoc-openapi (Swagger UI) | 2.5.0 |
| PDF | OpenPDF | 2.0.3 |
| Excel/CSV | Apache POI (poi-ooxml) | 5.2.5 |
| Frontend 1 | Angular + Material + Bootstrap | 15.1 |
| Frontend 2 | Next.js + React + Tailwind + Recharts | 16 / 19 / 4 |
| Conteneurisation | Docker + Docker Compose | — |

## 3. Organisation du backend

```
backend/versuion/src/main/java/com/example/versuion/
├── Dto/                  # DTOs (objets d'échange JSON) + mappers fromEntity/toEntity
│   └── dashboard/        # DTOs du tableau de bord
├── config/               # Configuration Spring
│   ├── SecurityConfiguration.java   # Filtre de sécurité HTTP (routes publiques/protégées)
│   ├── WebMvcConfig.java            # Service des fichiers /photos/**
│   └── SwaggerConfiguration.java
├── controller/           # Contrôleurs REST (implémentent les interfaces api/)
│   ├── auth/AuthenticationControlleur.java   # /auth/authentification, /auth/me
│   ├── ArticleControler.java
│   ├── ClientControlleur.java
│   ├── FournisseurControlleur.java
│   ├── CommandeClinetControlleur.java
│   ├── CommandeFournisseurControlleur.java
│   ├── VentesControlleur.java
│   ├── MvtStkController.java
│   ├── CategoryControlleur.java
│   ├── UtilisateurControlleur.java
│   ├── EntrepriseControlleur.java
│   ├── PhotoControlleur.java        # Upload photos (articles)
│   ├── DashboardControlleur.java
│   └── ExportControlleur.java       # Exports Excel/CSV
├── controller/api/       # Interfaces déclaratives des endpoints (paths, @PreAuthorize)
├── exception/            # Exceptions métier + ErrorCodes + RestExceptionHandler
├── jwt/                  # JwtUtil (génération/validation), JwtRequestFiltre (décodage Bearer)
├── models/               # Entités JPA (tables PostgreSQL)
├── repository/           # Repositories Spring Data (+ méthodes multi-tenant par défaut)
├── services/             # Interfaces métier
│   └── impl/             # Implémentations (toute la logique métier)
├── utiles/               # Constants (routes), CurrentEntreprise (MDC tenant), PaginationUtils
└── validator/            # Validateurs métier (champs obligatoires, formats)
```

### Conventions importantes
- **DTO ≠ Entité** : les contrôleurs ne manipulent que des DTOs ; les mappers statiques
  `fromEntity()` / `toEntity()` assurent la conversion. Le mot de passe est marqué
  `@JsonProperty(WRITE_ONLY)` pour ne jamais ressortir en JSON.
- **Interfaces d'API** : chaque contrôleur implémente une interface `*Api` qui porte les
  annotations de routes et de sécurité — une seule source de vérité pour l'URL et le rôle requis.
- **Validation** : chaque service appelle son validateur (`ClientValidator`, `MvtStkValidator`…)
  avant toute écriture, et lève `InvalidEntityException` (→ HTTP 400 avec la liste des erreurs).

## 4. Architecture multi-tenant (multi-entreprises)

Principe : **toutes les tables métier portent une colonne `identreprise`**, remplie
automatiquement à partir du token JWT.

```
Token JWT ──(claim "idEntreprise")──▶ JwtRequestFiltre ──▶ MDC.put("idEntreprise", ...)
                                                                │
                                              CurrentEntreprise.getId()
                                                                │
                         ┌──────────────────────────────────────┘
                         ▼
   Repository.default findByIdTenant(id)  →  WHERE id = ? AND identreprise = ?
   Repository.default findAllTenant()     →  WHERE identreprise = ?
```

- `JwtRequestFiltre` extrait le claim `idEntreprise` du JWT et le place dans le MDC (contexte de thread).
- `CurrentEntreprise.getId()` le relit de façon statique, sans paramètre de méthode.
- Les repositories exposent des méthodes **default** (`findByIdTenant`, `findAllTenant`) qui
  délèguent vers la requête filtrée si un tenant est présent, sinon vers la requête globale.
- À l'écriture, les services **écrasent** l'`idEntreprise` du client avec celui du token
  (impossible de créer des données pour une autre entreprise).

Conséquence : le filtrage est **systématique et centralisé** — aucun contrôleur n'a besoin
d'implémenter la logique tenant lui-même.

## 5. Sécurité (résumé)

- Session **stateless** : pas de cookie de session, seul le header `Authorization: Bearer <jwt>` compte.
- Le JWT contient : `sub` (email), `iat`, `exp` (8 h par défaut), `idEntreprise`.
- `JwtRequestFiltre` s'exécute avant le filtre d'authentification Spring et peuple le
  `SecurityContextHolder` à partir de la base (`ApplicationUserDetailsService`).
- Routes publiques (voir [08. Sécurité](./08-securite-roles.md)) : santé, login, inscription
  entreprise, catégories, Swagger, fichiers `/photos/**`.
- Autorisations fines par `@PreAuthorize` (`ADMIN`, `MANAGER`, `VENDEUR`).

## 6. Stockage des photos

- Service `PhotoStorageService` : écrit les fichiers dans le dossier `app.photos.dir`
  (`/app/photos` en Docker, volume `photos_data`).
- Formats acceptés : `jpg`, `jpeg`, `png`, `webp` — 5 Mo maximum.
- Nom généré en UUID → collision impossible ; l'ancienne photo est supprimée lors du remplacement.
- Servies publiquement sous `/photos/**` via `WebMvcConfig` (ressource statique `file:`).
- Deux usages : photo des **articles** (`POST /photos/article/{id}`) et
  **photo de profil** (`POST /utilisateurs/me/photo`).

## 7. Le fil de données du stock (cœur métier)

Le stock réel n'est **jamais stocké** : c'est la somme signée des mouvements.

| Événement | Mouvement généré | Type | Signe |
|---|---|---|---|
| Création d'une vente | `source = VENTE` | SORTIE | négatif |
| Commande client passée à `LIVREE` | `source = COMMANDE_CLIENT` | SORTIE | négatif |
| Commande fournisseur passée à `LIVREE` | `source = COMMANDE_FOURNISSEUR` | ENTREE | positif |
| Action manuelle (MANAGER/ADMIN) | `source = null` | ENTREE / SORTIE / CORRECTION_POS / CORRECTION_NEG | ± |

Le calcul : `stockReel = SUM(quantite)` sur `mvt_stk` pour l'article (les sorties sont
stockées négativement). Voir [05. Flux utilisateurs](./05-flux-utilisateurs.md) § 4.

## 8. Gestion des erreurs

`RestExceptionHandler` centralise les exceptions et renvoie un JSON homogène :

```json
{
  "httpCode": 400,
  "code": 20001,
  "message": "Le client n'est pas valide",
  "errors": ["Veuillez renseigner le nom du client", "Veuillez renseigner l'adresse 1"]
}
```

Codes métier principaux (extrait) : `ARTICLE_NOT_VALID`, `CLIENT_ALREADY_IN_USE`,
`COMMANDE_CLIENT_NOT_FOUND`, `UTILISATEUR_NOT_FOUND`, `UTILISATEUR_ALREADY_EXISTS`…
(détail complet dans `exception/ErrorCodes.java`).

## 9. Gestion de la pagination

`PageResponse<T>` enveloppe les réponses paginées :

```json
{ "content": [...], "page": 0, "size": 10, "totalElements": 42, "totalPages": 5, "last": false }
```

Paramètres standards : `?page=0&size=10&sortBy=nom&sortDir=asc&search=terme`
(whitelist des champs triables dans `PaginationUtils`).

## 10. Organisation des frontends

### Angular (`frontend/my-first-project`)
```
src/app/
├── Models/               # Interfaces TypeScript alignées sur les DTOs backend
├── services/             # Un service par ressource (HttpClient + intercepteur JWT)
├── auth/intercptor/      # AuthInterceptor : ajoute le Bearer token à chaque requête
└── _Dashboard/
    ├── dashboard/        # Layout + page d'accueil (KPI, graphiques CSS)
    ├── components/       # Menu, header (recherche + avatar + déco), détail lignes, modals
    └── _Pages/           # articles, clients, fournisseurs, commandes, stock, profil…
```

### Next.js (`frontend/stock-ui`)
```
app/
├── login/page.tsx        # Connexion + inscription entreprise
└── (app)/                # Groupe de routes protégées (layout avec Sidebar/Topbar)
    ├── dashboard/page.tsx
    ├── articles/page.tsx
    ├── clients/page.tsx  # → components/PartnersPage.tsx (réutilisé fournisseurs)
    ├── commandes/page.tsx, ventes/page.tsx, stock/page.tsx
    ├── utilisateurs/page.tsx, profil/page.tsx
components/               # UI kit (Button, Modal, DataTable, charts, Sidebar, Topbar)
lib/api.ts                # Client HTTP fetch (JWT, upload, download, gestion d'erreurs)
lib/auth.tsx              # AuthProvider (login/logout/refreshUser/roles)
lib/types.ts              # Types TypeScript alignés sur les DTOs backend
```

## 11. Pourquoi deux frontends ?

- Le projet Angular est le **back-office historique** : complet mais au design classique.
- StockFlow (Next.js) est la **nouvelle interface** : même API, UX modernisée, composants réutilisables.
- Ils cohabitent volontairement : le backend ne fait la distinction d'aucune façon, ce qui
  permet de migrer progressivement page par page sans rupture.
