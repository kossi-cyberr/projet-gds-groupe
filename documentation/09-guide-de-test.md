# 09 — Guide de test

## 1. Stratégie

| Niveau | Outils | Périmètre |
|---|---|---|
| Unitaires backend | JUnit 5 (`src/test/java`) | validateurs, utilitaires JWT |
| Intégration backend | Testcontainers (PostgreSQL) | repositories, services |
| End-to-end API | scripts **curl** (ce document) | tous les parcours métier |
| Interface | navigateur (4200 / 3009) | parcours utilisateurs complets |

Lancer les tests unitaires/intégration :
```bash
cd backend/versuion && mvn test
```

## 2. Comptes de test

| Compte | Mot de passe | Entreprise | Rôle |
|---|---|---|---|
| `admin@stockflow-demo.com` | `Admin123!` | 152 — StockFlow Démo SARL | ADMIN |
| `achille.mballa@novatra-distribution.cm` | (seed E2E) | 900 — NOVATRA DISTRIBUTION SA | ADMIN |

> Les données étant isolées par entreprise, connectez-vous avec le bon compte pour voir
> les données correspondantes.

## 3. Pré-requis commun

```bash
BASE=http://localhost:8089/gestiondestock
TOKEN=$(curl -s -X POST $BASE/auth/authentification \
  -H 'Content-Type: application/json' \
  -d '{"login":"admin@stockflow-demo.com","password":"Admin123!"}' \
  | python3 -c "import sys,json;print(json.load(sys.stdin)['accessToken'])")
AUTH="Authorization: Bearer $TOKEN"
```

## 4. Scénario complet end-to-end (curl)

### 4.1 Santé & authentification
```bash
curl -s http://localhost:8089/actuator/health                 # status UP
curl -s $BASE/auth/me -H "$AUTH"                              # profil connecté
```

### 4.2 Référentiels
```bash
curl -s -X POST $BASE/categories/create -H "$AUTH" -H 'Content-Type: application/json' \
  -d '{"codeCategory":"CAT-TEST","designation":"Catégorie de test"}'

curl -s -X POST $BASE/articles/create -H "$AUTH" -H 'Content-Type: application/json' \
  -d '{"codeArticle":"ART-TEST","designation":"Article de test","prixUnitaire":1000,
       "tauxTva":18,"prixUnitaireTTc":1180,"seuilAlerte":5,"category":{"id":1}}'
```

### 4.3 Clients & fournisseurs
```bash
curl -s -X POST $BASE/clients/create -H "$AUTH" -H 'Content-Type: application/json' \
  -d '{"nom":"Doe","prenom":"John","mail":"john@doe.com","numTel":"+22890000001",
       "adresse":{"addresse1":"1 rue Test","Ville":"Lomé","codePostale":"00000","pays":"Togo"}}'

# Modification
curl -X PUT $BASE/clients/update/{id} -H "$AUTH" -H 'Content-Type: application/json' \
  -d '{"nom":"Doe","prenom":"John-Modifié","mail":"john@doe.com",
       "adresse":{"addresse1":"1 rue Test","Ville":"Lomé","codePostale":"00000","pays":"Togo"}}'
```

### 4.4 Commande client de bout en bout (avec impact stock)
```bash
# 1. Créer la commande
curl -s -X POST $BASE/commandesclients/create -H "$AUTH" -H 'Content-Type: application/json' \
  -d '{"code":"CMD-TEST-001","client":{"id":1},
       "ligneComandeClientList":[{"article":{"id":1},"quantite":2,"prixUnitaire":1180}]}'

# 2. Stock avant livraison
curl -s $BASE/mvtstk/stockreel/1 -H "$AUTH"

# 3. Passer à LIVREE → génère les sorties de stock
curl -X PATCH $BASE/commandesclients/update/etat/{idCmd}/LIVREE -H "$AUTH"

# 4. Vérifier que le stock a diminué de 2
curl -s $BASE/mvtstk/stockreel/1 -H "$AUTH"

# 5. Facture PDF
curl -s -o facture.pdf $BASE/commandesclients/{idCmd}/facture/pdf -H "$AUTH"
file facture.pdf   # PDF document
```

### 4.5 Vente directe
```bash
curl -s -X POST $BASE/ventes/create -H "$AUTH" -H 'Content-Type: application/json' \
  -d '{"code":"V-TEST-001","commentaire":"Test","ligneVentes":[{"article":{"id":2},"quantite":3,"prixUnitaire":29500}]}'
# → le stock de l'article 2 est immédiatement déduit de 3
```

### 4.6 Mouvements de stock
```bash
curl -s $BASE/mvtstk/all -H "$AUTH"                     # historique entreprise
curl -s $BASE/mvtstk/filter/article/1 -H "$AUTH"        # historique article
curl -s -X POST $BASE/mvtstk/correctionpos -H "$AUTH" -H 'Content-Type: application/json' \
  -d '{"article":{"id":1},"quantite":4}'                # correction +4
```

### 4.7 Profil & photo
```bash
# Modification du profil de l'utilisateur connecté
curl -X PUT $BASE/utilisateurs/me -H "$AUTH" -H 'Content-Type: application/json' \
  -d '{"nom":"NouveauNom","prenom":"NouveauPrenom"}'

# Upload photo (multipart)
curl -X POST $BASE/utilisateurs/me/photo -H "$AUTH" -F "file=@/chemin/photo.png"
# → renvoie {"photo":"/photos/<uuid>.png"} ; l'URL doit être accessible SANS JWT
```

### 4.8 Utilisateurs & rôles (ADMIN)
```bash
curl -s -X POST $BASE/utilisateurs/create -H "$AUTH" -H 'Content-Type: application/json' \
  -d '{"nom":"Vendeur","prenom":"Test","email":"vendeur@demo.com","motDePasse":"Test1234!",
       "dateDeNaissance":"1995-01-01","adresse":{"addresse1":"rue 1","Ville":"Lomé",
       "codePostale":"00000","pays":"Togo"}}'
curl -X PUT $BASE/utilisateurs/roles/{id}/VENDEUR -H "$AUTH"
```

### 4.9 Exports
```bash
curl -s -o articles.xlsx $BASE/exports/articles/excel -H "$AUTH"
curl -s -o articles.csv  $BASE/exports/articles/csv  -H "$AUTH"
```

## 5. Matrice de vérification rapide

| # | Vérification | Résultat attendu |
|---|---|---|
| 1 | `GET /actuator/health` | 200, `status: UP` |
| 2 | Login avec mauvais mot de passe | 401 BadCredentials |
| 3 | `GET /clients/all` sans token | 403 |
| 4 | Données de l'entreprise 900 avec compte 152 | invisibles (liste vide) |
| 5 | Créer un client sans nom | 400 + liste d'erreurs métier |
| 6 | Supprimer un client avec commandes | 400 `CLIENT_ALREADY_IN_USE` |
| 7 | `PUT /utilisateurs/roles/{id}/ADMIN` avec compte VENDEUR | 403 |
| 8 | Photo > 5 Mo ou format interdit | 400 InvalidOperationException |
| 9 | Livraison commande client | sortie de stock créée automatiquement |
| 10 | Livraison commande fournisseur | entrée de stock créée automatiquement |

## 6. Tests de l'interface (manuel)

### Frontend Angular (4200)
- [ ] Connexion / déconnexion (icône rouge dans le header)
- [ ] Recherche globale d'articles dans le header (suggestions + redirection)
- [ ] Articles : création avec photo, modification, calcul TTC automatique
- [ ] Clients / fournisseurs : liste réelle, édition, suppression, erreurs de suppression
- [ ] Commandes client : accordéon, lignes, changement d'état, facture PDF, suppression
- [ ] Commande fournisseur : création avec sélection fournisseur + articles
- [ ] Mouvements de stock : stock réel, historique, correction ±
- [ ] Profil : données réelles, édition, photo avec aperçu, avatar header mis à jour
- [ ] Utilisateurs : liste, création avec rôle, suppression (compte ADMIN requis)

### Frontend Next.js (3009)
- [ ] Connexion + création d'entreprise (mot de passe admin affiché)
- [ ] Dashboard : KPI, graphiques, alertes, compteurs
- [ ] Toutes les pages : pagination, tri, recherche serveur
- [ ] Commandes : création, états, lignes, PDF, suppression
- [ ] Stock : sélection article, entrée/sortie/corrections, historique
- [ ] Profil : édition + photo, avatar sidebar/topbar actualisé
- [ ] Déconnexion (sidebar)

## 7. Seed de démonstration

`scripts/seed-demo.sh` crée un jeu de données complet (entreprise, catégories, articles,
clients, fournisseurs, commandes, ventes, mouvements, utilisateur VENDEUR) via l'API.
Pratique pour démontrer l'application sur une base vide :

```bash
bash scripts/seed-demo.sh
```
