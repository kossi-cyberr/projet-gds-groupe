# 07 — Installation & exécution

## 1. Prérequis

| Outil | Version minimale | Vérification |
|---|---|---|
| Docker + Docker Compose | 24 / v2 | `docker compose version` |
| Java (mode local) | 17 | `java -version` |
| Maven (mode local) | 3.9 | `mvn -version` |
| Node.js (mode local) | 18+ | `node --version` |

> Le mode **Docker Compose** est la méthode recommandée : tout est isolé et reproductible.

---

## 2. Démarrage rapide (Docker Compose)

À la racine du dépôt (le `docker-compose.yml` racine orchestre les 4 services) :

```bash
docker compose build        # construit les images (première fois : plusieurs minutes)
docker compose up -d        # démarre en arrière-plan
docker compose ps           # vérifie l'état
docker compose logs -f backend
```

Services démarrés :

| Conteneur | Port hôte | Rôle |
|---|---|---|
| `gds_db` | 5436 → 5432 | PostgreSQL 15, base `inventory_db` |
| `gds_backend` | 8089 | API Spring Boot (`/gestiondestock`) |
| `gds_frontend` | 4200 → 80 | Angular servi par Nginx |
| `gds_frontend_next` | 3009 → 3000 | Next.js (serveur Node standalone) |
| `gds_adminer` | 8081 → 8080 | **Adminer** — interface web d'administration PostgreSQL |

Vérification :
```bash
curl http://localhost:8089/actuator/health   # {"status":"UP",...}
curl -o /dev/null -w '%{http_code}' http://localhost:4200   # 200
curl -o /dev/null -w '%{http_code}' http://localhost:3009    # 200
curl -o /dev/null -w '%{http_code}' http://localhost:8081    # 200 (Adminer)
```

### Adminer — accès à la base de données

Adminer est intégré à la stack et pré-branché sur le conteneur PostgreSQL
(`ADMINER_DEFAULT_SERVER: db`). Ouvrir **http://localhost:8081** puis :

| Champ | Valeur |
|---|---|
| Système | PostgreSQL |
| Serveur | `db` (pré-rempli) |
| Utilisateur | `postgres` |
| Mot de passe | `postgres` |
| Base | `inventory_db` |

Permet d'explorer les tables (`article`, `commande_client`, `mvt_stk`…), exécuter des
requêtes SQL, importer/exporter des dumps — sans outil externe.

> ⚠ Par défaut les identifiants sont ceux du développement (`postgres`/`postgres`).
> En production, changer les variables et ne pas exposer Adminer sur Internet.

### Arrêt / remise à zéro
```bash
docker compose down                 # arrête (les volumes sont conservés)
docker compose down -v              # ⚠ supprime aussi les données (db + photos)
```

### Rebuild après modification du code
```bash
docker compose build backend        # cible uniquement le service modifié
docker compose up -d backend
```

---

## 3. Variables d'environnement (backend)

Définies dans `docker-compose.yml` (ou `application.properties` en local). Toutes ont
une valeur par défaut adaptée au développement :

| Variable | Défaut | Rôle |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://db:5432/inventory_db` | Connexion PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` / `_PASSWORD` | `postgres` / `postgres` | Identifiants DB |
| `JWT_SECRET` | (valeur de dev) | Clé HMAC des tokens — **à changer en production** |
| `JWT_EXPIRATION` | `28800000` (8 h) | Durée de vie du token (ms) |
| `DEFAULT_ADMIN_PASSWORD` | `Admin123!` | Mot de passe admin créé avec l'entreprise |
| `ALERT_EMAIL` | vide | Destinataire des alertes de stock (vide = désactivé) |
| `SMTP_HOST` / `SMTP_PORT` | `localhost` / `587` | Serveur mail pour les alertes |
| `PHOTOS_DIR` | `./photos` (→ `/app/photos` en Docker) | Dossier des images |
| `STOCK_ALERT_CRON` | `0 0 8 * * *` | Cadence des alertes e-mail |

Note santé : le check SMTP est désactivé (`management.health.mail.enabled=false`) pour que
`/actuator/health` reste `UP` en développement sans serveur mail.

---

## 4. Mode local (sans Docker)

### 4.1 Base de données
PostgreSQL doit écouter sur le port **5436** avec la base `inventory_db` :
```bash
docker run -d --name gds_db -p 5436:5432 \
  -e POSTGRES_DB=inventory_db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres \
  postgres:15-alpine
```

### 4.2 Backend
```bash
cd backend/versuion
mvn spring-boot:run
# ou : mvn clean package -DskipTests && java -jar target/versuion-0.0.1-SNAPSHOT.jar
```
Le backend démarre sur **8089**, applique les migrations Flyway puis le `ddl-auto=update`.

### 4.3 Frontend Angular
```bash
cd frontend/my-first-project
npm install
npm start          # ng serve → http://localhost:4200 (proxy vers l'API via environment.ts)
```

### 4.4 Frontend Next.js
```bash
cd frontend/stock-ui
npm install
npm run dev        # http://localhost:3000 (adapter NEXT_PUBLIC_API_BASE si besoin)
# build production : npm run build && npm start
```

---

## 5. Premier lancement : jeu de données

1. Ouvrir http://localhost:3009/login → **Créer mon entreprise**
   (ou `POST /entreprises/create`) → un compte ADMIN est créé automatiquement.
2. Alternativement, un seed de démonstration est disponible :
   ```bash
   bash scripts/seed-demo.sh
   ```
   Il crée l'entreprise *StockFlow Démo SARL* (`demo@stockflow.com` / `Admin123!`),
   5 catégories, 12 articles, 4 clients, 3 fournisseurs, commandes, ventes et mouvements.
3. Compte démo utilisé dans la documentation :
   - **admin@stockflow-demo.com** / **Admin123!** (entreprise 152)

---

## 6. Configuration de l'URL d'API des frontends

| Frontend | Mécanisme |
|---|---|
| Angular dev | `src/environments/environment.ts` → `apiUrl: 'http://localhost:8089/gestiondestock'` |
| Angular Docker | surchargé au build via l'argument `NG_APP_API_URL` (Dockerfile) |
| Next.js | variable `NEXT_PUBLIC_API_BASE` (défaut `http://localhost:8089/gestiondestock`) |

---

## 7. Dépannage courant

| Symptôme | Cause probable | Solution |
|---|---|---|
| `health` → `DOWN` | PostgreSQL non prêt / SMTP | attendre le healthy ; SMTP déjà désactivé |
| 403 sur tous les endpoints | token absent/expiré | se reconnecter ; vérifier le header Bearer |
| 401 en boucle après login | `JWT_SECRET` différent entre 2 instances | même valeur partout |
| Images `/photos/**` en 404 | volume photos recréé | re-uploader ; vérifier `PHOTOS_DIR` |
| Port 3000 occupé (Metabase…) | conflit local | Next.js déjà remappé sur **3009** dans le compose |
| Backend relancé mais vieux code | image non reconstruite | `docker compose build backend` puis `up -d` |
| `mvn test` échoue sur Testcontainers | API Docker | `api.version=1.43` déjà configuré dans le POM |
