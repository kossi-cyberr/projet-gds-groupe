# 📦 Gestion de Stock — Backend REST API

API REST complète de gestion de stock multi-entreprise (Spring Boot 3).

> **Stack** : Java 17 · Spring Boot 3.3 · Spring Security 6 (JWT) · Spring Data JPA ·
> PostgreSQL · Flyway · Springdoc OpenAPI 3 · Apache POI · OpenPDF · Testcontainers · Docker

---

## ✨ Fonctionnalités

### Gestion des données
- **Articles** (code, désignation, prix HT/TTC, TVA, **seuil d'alerte**, photo, catégorie)
- **Catégories**, **Clients**, **Fournisseurs**, **Entreprises**
- **Commandes client & fournisseur** (création, changement d'état, modification des lignes, historique)
- **Ventes** avec déduction automatique du stock (mouvements de stock)
- **Mouvements de stock** : entrée, sortie, corrections (positives/négatives)

### 🔔 Alertes & Dashboard
- **Seuil d'alerte** par article : endpoint `GET /articles/sous-seuil`
- **Email automatique quotidien** (cron configurable) listant les articles sous le seuil
- **Tableau de bord agrégé** `GET /dashboard` :
  - CA total / mois en cours / jour en cours, marge moyenne
  - Compteurs (clients, fournisseurs, articles, commandes, ventes)
  - Valeur du stock, nombre d'articles sous seuil
  - **Top 10 articles**, CA par mois, par catégorie, commandes par client

### 📄 Exports & documents
- **Facture PDF** d'une commande client (`GET /commandesclients/{id}/facture/pdf`)
- **Exports Excel (XLSX)** : articles, clients, fournisseurs, commandes, ventes
- **Exports CSV** : articles

### 🔐 Sécurité & multi-entreprise
- Authentification **JWT** (clé externalisée via `JWT_SECRET`)
- **Permissions par rôle** : `ADMIN`, `MANAGER`, `VENDEUR` (`@PreAuthorize`)
  - `ADMIN` : gestion entreprise, utilisateurs, affectation des rôles
  - `ADMIN`/`MANAGER` : suppressions, mouvements de stock, exports
  - Tous : consultation, création de ventes/commandes
- **Filtrage strict par entreprise** (tenant) sur toutes les requêtes (l'`idEntreprise` est lu dans le token)
- Endpoint `GET /auth/me` : utilisateur courant + rôles

---

## 🚀 Démarrage rapide

### Option 1 — Docker Compose (recommandé)

```bash
docker compose up --build
```

L'API est disponible sur http://localhost:8089 (Swagger UI : http://localhost:8089/swagger-ui.html).

### Option 2 — En local

Prérequis : JDK 17, Maven 3.9+, PostgreSQL (par défaut sur le port **5436**).

```bash
mvn clean package
export JWT_SECRET="ta-cle-secrete-tres-longue-au-moins-32-caracteres"
mvn spring-boot:run
```

### Comptes fournis par la migration V5/V6 (NOVATRA DISTRIBUTION SA)

Les migrations Flyway **V5** (seed) et **V6** (correction du signe des sorties de stock) insèrent
automatiquement une entreprise complète et réaliste : **NOVATRA DISTRIBUTION SA** (Yaoundé, Cameroun) —
5 catégories, 8 articles (FCFA, TVA 19,25 %), 5 clients, 3 fournisseurs, 3 commandes clients,
2 commandes fournisseurs, 6 ventes étalées sur 5 mois et 16 mouvements de stock (dont 3 articles
sous le seuil d'alerte pour tester les alertes).

| Rôle | Email | Mot de passe |
|---|---|---|
| ADMIN | `achille.mballa@novatra-distribution.cm` | `Novatra@2026` |
| MANAGER | `clarisse.ndongo@novatra-distribution.cm` | `Novatra@2026` |
| VENDEUR | `emmanuel.fotso@novatra-distribution.cm` | `Novatra@2026` |
| VENDEUR | `sandrine.tchoumi@novatra-distribution.cm` | `Novatra@2026` |

Le seed est **idempotent** (garde sur l'entreprise id 900) : il ne s'exécute que si l'entreprise
n'existe pas déjà, et sait créer le schéma si la base est vierge.

### Création d'une autre entreprise

`POST /gestiondestock/entreprises/create` crée l'entreprise **et** son utilisateur `ADMIN`
(email = email de l'entreprise). Un **mot de passe temporaire** est renvoyé dans la réponse de création
(`Admin123!` par défaut, surchargeable via la variable d'environnement `DEFAULT_ADMIN_PASSWORD`).
Pensez à le changer après la première connexion via `POST /gestiondestock/utilisateurs/update/password`.

### Création d'utilisateurs (réservée à l'ADMIN)

`POST /gestiondestock/utilisateurs/create` (avec un token ADMIN) crée un utilisateur rattaché
à l'entreprise de l'administrateur connecté. Le rôle se affecte ensuite via
`PUT /gestiondestock/utilisateurs/roles/{id}/{roleName}`. Dans l'interface, le bouton
**« Nouvel utilisateur »** (page Utilisateurs) combine les deux appels.

---

## 🔑 Variables d'environnement

| Variable | Défaut | Description |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5436/inventory_db` | URL PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Utilisateur DB |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Mot de passe DB |
| `JWT_SECRET` | *(valeur de dev)* | **Clé secrète JWT ≥ 32 caractères — à changer en prod** |
| `JWT_EXPIRATION` | `6000000` | Durée de validité (ms) |
| `SMTP_HOST` / `SMTP_PORT` / `SMTP_USERNAME` / `SMTP_PASSWORD` | vide | Serveur SMTP (alertes email) |
| `ALERT_EMAIL` | vide | Destinataire des alertes de stock faible |
| `STOCK_ALERT_CRON` | `0 0 8 * * *` | Cadence de la vérification des stocks |

---

## 📚 Principaux endpoints

| Méthode | URL | Description |
|---|---|---|
| `POST` | `/gestiondestock/auth/authentification` | Connexion → JWT |
| `GET` | `/gestiondestock/auth/me` | Utilisateur courant + rôles |
| `GET` | `/gestiondestock/dashboard` | Statistiques du tableau de bord |
| `GET` | `/gestiondestock/articles/paged?page=0&size=10&sortBy=designation&search=foo` | Articles paginés + tri + recherche |
| `GET` | `/gestiondestock/articles/sous-seuil` | Articles sous le seuil d'alerte |
| `GET` | `/gestiondestock/commandesclients/{id}/facture/pdf` | Facture PDF d'une commande |
| `GET` | `/gestiondestock/exports/articles/excel` | Export Excel des articles |
| `GET` | `/gestiondestock/exports/articles/csv` | Export CSV des articles |
| `PUT` | `/gestiondestock/utilisateurs/roles/{id}/{roleName}` | Affecter un rôle (ADMIN) |
| `GET` | `/actuator/health` | Santé de l'application |

La **documentation OpenAPI complète** (avec bouton « Authorize » pour le JWT) est disponible sur `/swagger-ui.html`.

---

## 🧪 Tests

```bash
# Tous les tests (unitaires Mockito + intégration Testcontainers avec vraie PostgreSQL)
mvn test

# Build complet + rapport de couverture JaCoCo (target/site/jacoco)
mvn verify
```

Les tests d'intégration (Testcontainers) démarrent automatiquement une base PostgreSQL jetable
— **aucune base locale nécessaire**, seul Docker doit être actif.

## 🤖 CI/CD

Pipeline GitHub Actions (`.github/workflows/ci.yml`) : build + tests + couverture à chaque push/PR sur `main`.

---

## 🗺️ Architecture

```
controller/          Contrôleurs REST + interfaces API (annotations OpenAPI)
  ├── api/           Déclaration des endpoints (springdoc)
services/            Couche métier (interfaces + impl)
repository/          Spring Data JPA (méthodes tenant-aware par défaut)
models/              Entités JPA
Dto/                 Objets de transfert (mapping fromEntity/toEntity)
jwt/                 JwtUtil + JwtRequestFiltre (filtre d'authentification)
config/              Security (SecurityFilterChain), Springdoc, alertes planifiées
utiles/              Constants, CurrentEntreprise (tenant depuis le token/MDC)
validator/           Validations métier
handler/             Gestion centralisée des exceptions
```

### Multi-entreprise
1. Au login, l'`idEntreprise` est intégré au token JWT.
2. `JwtRequestFiltre` le place dans le **MDC** (contexte de requête).
3. Les repositories exposent des méthodes `...Tenant()` (méthodes `default`) qui filtrent
   systématiquement par `idEntreprise` — chaque entreprise ne voit que ses données.

### Migrations de base de données
Le schéma est géré par **Flyway** (`src/main/resources/db/migration`) avec
`baseline-on-migrate=true` pour les bases existantes + `ddl-auto=update` (compromis assumé,
à remplacer par des migrations complètes à terme).
