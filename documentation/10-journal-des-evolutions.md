# 10 — Journal des évolutions

Historique technique des chantiers menés sur le projet (contexte : remise à niveau du code
historique 2023 et industrialisation). Classé du plus ancien au plus récent.

---

## 1. Remise à niveau du backend (migrations)

- Migration de `WebSecurityConfigurerAdapter` (déprécié) vers **`SecurityFilterChain`**
  (Spring Security 6) avec `requestMatchers`, `sessionCreationPolicy(STATELESS)`,
  CSRF désactivé, CORS activé.
- Activation `@EnableMethodSecurity` → remplacement des `@PreAuthorize` fonctionnels.
- Suppression de `NoOpPasswordEncoder` → **BCrypt** partout.
- Externalisation de la configuration (`application.properties`) : variables
  `SPRING_DATASOURCE_*`, `JWT_SECRET`, `JWT_EXPIRATION`, `PHOTOS_DIR`, `SMTP_*`,
  `DEFAULT_ADMIN_PASSWORD`, `STOCK_ALERT_CRON`.
- Mise à jour Lombok/JUnit et du port applicatif (8089).
- Dockerfile multi-étapes (build Maven → JRE 17 slim) + healthcheck PostgreSQL.
- Actuator exposé (`health,info,metrics`) ; check SMTP désactivé en dev pour un health `UP`.

## 2. Remplacement de Flickr par le stockage local des photos

- Nouveau `PhotoStorageService` : UUID + extension contrôlée (jpg/jpeg/png/webp),
  5 Mo max, suppression de l'ancienne photo au remplacement.
- `WebMvcConfig` : service public des fichiers sous `/photos/**`.
- `POST /photos/article/{id}` (ADMIN/MANAGER) : rattache une photo à un article.
- Volume Docker `photos_data` → persistance des fichiers.

## 3. Architecture multi-tenant

- Claim `idEntreprise` ajouté au JWT.
- `JwtRequestFiltre` : alimente le MDC à chaque requête (nettoyé en `finally`).
- `CurrentEntreprise` : lecture statique du tenant.
- Méthodes `*Tenant` (default methods) sur tous les repositories :
  `findByIdTenant`, `findAllTenant` (avec variantes paginées et recherche).
- Écrasement systématique de l'`identreprise` à l'écriture côté services.
- `PageResponse<T>` : format de pagination homogène + `PaginationUtils` (whitelist de tri).

## 4. Endpoints ajoutés (chantiers récents)

| Endpoint | Rôle |
|---|---|
| `PUT /utilisateurs/me` | Modifier **son propre** profil (identifié par le token, pas un id client) |
| `POST /utilisateurs/me/photo` | Upload de la **photo de profil** (réutilise PhotoStorageService) |
| `PUT /clients/update/{id}` | Modification d'un client (validation + conservation du tenant) |
| `PUT /fournisseurs/update/{id}` | Modification d'un fournisseur |
| `GET /mvtstk/all` | Tous les mouvements de stock de l'entreprise (tri desc) |
| `GET /auth/me` (déjà existant, largement utilisé) | Profil de l'utilisateur connecté |

Sécurité : `/photos/**` ajouté aux routes publiques (affichage par `<img>` sans JWT).

## 5. Frontend Angular — pages converties de maquettes statiques à l'API réelle

### Profil
- Remplacement des textes en dur par `GET /auth/me` (rôles, entreprise, photo affichés).
- Édition complète (nom, prénom, date de naissance, adresse) → `PUT /utilisateurs/me`.
- Upload photo avec aperçu local (FileReader) → `POST /utilisateurs/me/photo`.
- Header : avatar réel (photo ou favicon), bouton **déconnexion**.

### Clients & fournisseurs
- Listes réelles (photo/initiales, contacts, adresse complète).
- Composant réutilisable `details-cl-frs` (`@Input origin`, `@Input partenaire`).
- Modification pré-remplie (routes `newclient/:id`, `newfournisseur/:id`) → `PUT .../update/{id}`.
- Suppression avec modal Bootstrap + messages d'erreur métier (client utilisé dans commandes…).
- Services `ClientService` / `FournisseurService` (CRUD complet).

### Utilisateurs
- Liste réelle avec badges de rôles.
- Création fonctionnelle (mot de passe + rôle initial) → `POST create` puis `PUT roles/{id}/{role}`.
- Suppression (ADMIN) avec confirmation.
- `UserService` enrichi : `findAllUtilisateurs`, `creerUtilisateur`, `assignerRole`, `deleteUtilisateur`.

### Commandes clients / fournisseurs
- Liste accordéon réelle : client/fournisseur, code, date, badge d'état coloré.
- Lignes chargées à l'ouverture (`GET /lignesCommande/{id}`) + **total calculé**.
- Changement d'état (`PATCH update/etat`), suppression, **facture PDF** téléchargeable.
- Formulaire de création : sélection client/fournisseur, **autocomplétion d'articles**,
  prix TTC pré-rempli, lignes dynamiques, total en direct.

### Mouvements de stock
- Articles avec **stock réel** (rouge si sous seuil) et historique par article.
- **Correction de stock** positive/négative fonctionnelle (modal).
- Nouveau service `MvtStkService` (all, par article, stock réel, corrections, entrée/sortie).

### Articles
- Photo fonctionnelle : bouton image branché (aperçu + upload après enregistrement).
- Modèle `Article` aligné (ajout `seuilAlerte`).

### Divers
- Recherche globale d'articles dans le header (suggestions + redirection `/articles?q=`).
- Dashboard d'accueil enrichi (KPI, graphiques CSS, top articles/clients, alertes).

## 6. Frontend Next.js (StockFlow)

- Page **`/profil`** complète : lecture `GET /auth/me`, édition `PUT /utilisateurs/me`,
  upload photo (`uploadFile` dans `lib/api.ts`), toasts de confirmation.
- Sidebar : bloc utilisateur cliquable vers `/profil`, avatar réel.
- Topbar : avatar avec photo réelle (ou initiales).
- `lib/api.ts` enrichi : `uploadFile()`, `photoUrl()` (URL absolue des photos backend).
- Types `Utilisateur`/`Adresse` alignés (photo, adresse).

## 7. Corrigés au fil de l'eau

- 403 sur `/actuator/health` (jar obsolète par rapport aux sources) → discipline de rebuild :
  `docker compose build <service>` après chaque modification.
- Health `DOWN` à cause du SMTP absent → `management.health.mail.enabled=false`.
- Frontends servis avec du code antérieur aux sources → rebuild systématique des 3 images.
- Erreurs TypeScript (union types Observable, `seuilAlerte` manquant, échappements) → build OK.

## 8. État final vérifié (end-to-end)

```
health API            : UP
Authentification JWT  : OK (8h)
Profil + photo        : OK (édition + upload + affichage public)
Clients/Fournisseurs  : CRUD complet OK
Commandes client      : création, états, lignes, PDF, suppression OK
Commandes fournisseur : création, états, entrées de stock OK
Ventes                : création + sorties de stock OK
Mouvements de stock   : historique + corrections OK
Utilisateurs/rôles    : création, rôles, suppression OK (ADMIN)
Exports Excel/CSV     : OK
Angular 4200          : 200
Next.js 3009          : 200 (toutes pages)
```

## 9. Idées d'évolution (backlog)

- Refresh token + expiration courte / révocation.
- Édition complète des commandes (lignes) côté frontends (les endpoints PATCH existent).
- Soft delete (`deleted_at`) au lieu de suppressions physiques.
- Tests d'intégration élargis (parcours commande → livraison → stock).
- CI/CD (build + tests + build images) — un workflow GitHub Actions existe à amorcer
  (`backend/versuion/.github/workflows/ci.yml`).
- Pagination côté Angular (l'app `app-pagination` est encore décorative sur certaines listes).
