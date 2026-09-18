# 08 — Sécurité & rôles

## 1. Modèle d'authentification

- **Stateless JWT** : aucune session serveur. Chaque requête doit transporter
  `Authorization: Bearer <token>`.
- Algorithme **HMAC-SHA256** (clé `jwt.secret`, ≥ 32 caractères).
- Claims du token :

| Claim | Contenu | Exemple |
|---|---|---|
| `sub` | email de l'utilisateur | `admin@stock-hub-demo.com` |
| `iat` | émis à | `1789554883` |
| `exp` | expire à (8 h par défaut) | `1789583683` |
| `idEntreprise` | **tenant** de l'utilisateur | `152` |

> Le claim `idEntreprise` est la clé de l'isolation multi-tenant : il ne peut pas être
> falsifié sans invalider la signature HMAC.

## 2. Chaîne de filtrage (Spring Security)

```
Requête
  └─▶ JwtRequestFiltre (OncePerRequestFilter)
        ├─ token absent           → requête anonyme (les routes publiques passent)
        ├─ token invalide/expiré  → log WARN, requête anonyme (→ 401 sur route protégée)
        └─ token valide           → loadUserByUsername → SecurityContextHolder
                                     + MDC["idEntreprise"] (isolation des données)
  └─▶ AuthorizeHttpRequests
        ├─ routes publiques       → permitAll
        └─ toute autre route      → authenticated() + @PreAuthorize éventuel
```

Configuration : `SecurityConfiguration` (session STATELESS, CSRF désactivé — inutile
sans cookies, CORS activé pour les deux frontends).

## 3. Routes publiques (permitAll)

| Route | Motif |
|---|---|
| `/actuator/health` | Supervision |
| `/gestiondestock/auth/authentification` | Login (évidemment) |
| `/gestiondestock/entreprises/create` | Inscription d'une nouvelle entreprise |
| `/gestiondestock/articles/create` | Premier article lors d'une inscription (tenant forcé par le token si présent) |
| `/gestiondestock/categories/**` | Référentiel consultable avant connexion |
| `/photos/**` | Fichiers images (nécessaire aux balises `<img>`) |
| `/v3/api-docs/**`, `/swagger-ui/**`, swagger legacy | Documentation API |

**Toute autre route** exige un JWT valide.

## 4. Contrôle d'accès par rôle (RBAC)

Trois rôles hiérarchiques portés par la table `roles` :

| Endpoint (pattern) | Rôle requis |
|---|---|
| Création/modification des données de base (clients, articles, commandes, ventes…) | connecté (VENDEUR suffit) |
| `DELETE /clients/*`, `/fournisseurs/*`, `/commandesclients/*`, `/ventes/*` | `ADMIN` ou `MANAGER` |
| `POST /mvtstk/entree|sortie|correctionpos|correctionneg` | `ADMIN` ou `MANAGER` |
| `POST /photos/article/{id}` | `ADMIN` ou `MANAGER` |
| `POST /utilisateurs/create` | `ADMIN` |
| `PUT /utilisateurs/roles/{id}/{role}` | `ADMIN` |
| `DELETE /utilisateurs/delete/{id}` | `ADMIN` |
| `GET /exports/**` (Excel/CSV) | `ADMIN` ou `MANAGER` |

Mécanismes : `@PreAuthorize("hasAuthority('ADMIN')")` sur les interfaces d'API
(une seule source de vérité route + rôle).

### Règles métier complémentaires (au-delà des rôles)

| Règle | Implémentation |
|---|---|
| Changer le mot de passe de **quelqu'un d'autre** | réservé ADMIN (`UtilisateurServiceImpl.changerMotDePasse`) |
| Modifier son profil (`PUT /utilisateurs/me`) | l'utilisateur est identifié par son **token**, pas par un id client |
| Créer des données pour une autre entreprise | impossible : `identreprise` écrasé par le serveur |

## 5. Isolation multi-tenant — défense en profondeur

1. **Extraction** : `JwtRequestFiltre` lit le claim et le place dans le MDC du thread.
2. **Lecture** : méthodes repository `*Tenant` (ex. `findAllTenant()`) → `WHERE identreprise = ?`.
3. **Écriture** : les services réécrivent systématiquement l'`identreprise` depuis
   `CurrentEntreprise.getId()`.
4. **Nettoyage** : `finally { MDC.remove(...) }` — aucune fuite entre threads.

Toute requête est donc bornée à l'entreprise du token, y compris si le client HTTP envoie
volontairement l'id d'une autre entreprise dans le corps JSON.

## 6. Protection des mots de passe

- Hachage **BCrypt** (`PasswordEncoder` bean) à la création et à chaque changement.
- Le hash n'est **jamais** renvoyé par l'API (`@JsonProperty(access = WRITE_ONLY)`).
- Validation côté service : confirmation obligatoire, non-nullité des champs
  (`ChangerMotDePasseUtilisateurDto`).

## 7. Fichiers et upload

| Contrôle | Détail |
|---|---|
| Extensions | `jpg`, `jpeg`, `png`, `webp` uniquement |
| Taille | 5 Mo maximum |
| Nom de fichier | UUID généré serveur (pas de nom client → pas de path traversal) |
| Servis | publiquement sous `/photos/**` en lecture seule (ressource statique) |
| Remplacement | l'ancienne photo est supprimée du disque |

## 8. Recommandations pour la production

1. **Changer `JWT_SECRET`** (variable `JWT_SECRET`) — et ne jamais le committer.
2. Changer les identifiants PostgreSQL par défaut.
3. Activer **HTTPS** devant les frontends (reverse proxy) : le token circule en clair en HTTP.
4. Configurer `ALERT_EMAIL` + `SMTP_*` avec des identifiants réels et restreindre `FROM`.
5. Envisager une **expiration courte + refresh token** (actuellement 8 h fixes).
6. Ajouter une politique de complexité des mots de passe (aujourd'hui : validation présence).
7. Mettre `spring.jpa.show-sql=false` et `show-details` de health sur `when-authorized`.
