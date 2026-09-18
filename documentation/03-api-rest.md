# 03 — API REST

Toutes les routes sont préfixées par **`/gestiondestock`** et servies sur le port **8089**.
Base locale : `http://localhost:8089/gestiondestock`

- Authentification : header `Authorization: Bearer <token>` (sauf routes publiques).
- Format : JSON sauf mention contraire (upload multipart, PDF, Excel, CSV).
- Réponses paginées : voir format `PageResponse` dans [02. Architecture](./02-architecture-technique.md) § 9.

---

## 1. Authentification

| Méthode | Route | Accès | Description |
|---|---|---|---|
| POST | `/auth/authentification` | public | Connexion → `{ accessToken }` |
| GET | `/auth/me` | connecté | Profil complet de l'utilisateur connecté |

```bash
# Connexion
curl -X POST http://localhost:8089/gestiondestock/auth/authentification \
  -H 'Content-Type: application/json' \
  -d '{"login":"admin@stock-hub-demo.com","password":"Admin123!"}'
```

---

## 2. Entreprises

| Méthode | Route | Accès | Description |
|---|---|---|---|
| POST | `/entreprises/create` | public | Inscription → crée l'entreprise + un compte ADMIN |
| GET | `/entreprises/{id}` | connecté | Fiche entreprise |
| GET | `/entreprises/all` | connecté | Liste |
| GET | `/entreprises/message` | connecté | Endpoint utilitaire |
| DELETE | `/entreprises/delete/{id}` | ADMIN | Suppression |

> À la création, la réponse contient `motDePasse` : le mot de passe temporaire de l'ADMIN
> (par défaut `Admin123!`, surchargeable via `DEFAULT_ADMIN_PASSWORD`).

---

## 3. Articles

| Méthode | Route | Accès | Description |
|---|---|---|---|
| POST | `/articles/create` | public* | Créer un article |
| GET | `/articles/{id}` | connecté | Détail |
| GET | `/articles/all` | connecté | Liste complète |
| GET | `/articles/paged` | connecté | Liste paginée (`page,size,sortBy,sortDir,search`) |
| GET | `/articles/sous-seuil` | connecté | Articles dont le stock est ≤ seuil d'alerte |
| GET | `/articles/filter/category/{id}` | connecté | Articles d'une catégorie |
| GET | `/articles/historique/vente/{id}` | connecté | Historique des ventes de l'article |
| GET | `/articles/historique/commandeclient/{id}` | connecté | Historique des commandes clients |
| GET | `/articles/historique/commandefournisseur/{id}` | connecté | Historique des commandes fournisseurs |
| DELETE | `/articles/delete/{id}` | ADMIN/MANAGER | Suppression |

\* la route `create` est publique pour permettre le premier article lors d'une inscription ;
l'entreprise est de toute façon forcée côté serveur à partir du token.

---

## 4. Catégories

| Méthode | Route | Accès |
|---|---|---|
| POST | `/categories/create` | public |
| PUT | `/categories/update/{id}` | connecté |
| GET | `/categories/{id}` · `/categories/all` | public |
| DELETE | `/categories/delete/{id}` | connecté |

---

## 5. Clients

| Méthode | Route | Accès | Description |
|---|---|---|---|
| POST | `/clients/create` | connecté | Créer (entreprise forcée par le token) |
| PUT | `/clients/update/{id}` | connecté | Modifier |
| GET | `/clients/{id}` · `/clients/all` | connecté | Détail / liste |
| GET | `/clients/paged` | connecté | Liste paginée + recherche (`search` sur le nom) |
| DELETE | `/clients/delete/{id}` | ADMIN/MANAGER | Refusé si le client a des commandes |

---

## 6. Fournisseurs

Mêmes routes que les clients, préfixées `/fournisseurs` :

`POST /create` · `PUT /update/{id}` · `GET /{id}` · `GET /all` · `GET /paged` ·
`DELETE /delete/{id}` (ADMIN/MANAGER, refusé si commandes existantes)

---

## 7. Commandes clients

| Méthode | Route | Description |
|---|---|---|
| POST | `/commandesclients/create` | Créer avec lignes (`ligneComandeClientList`) |
| GET | `/commandesclients/{id}` | Détail (sans lignes) |
| GET | `/commandesclients/filter/{code}` | Par code |
| GET | `/commandesclients/all` | Liste complète |
| GET | `/commandesclients/paged` | Paginée |
| GET | `/commandesclients/lignesCommande/{id}` | **Lignes de la commande** |
| PATCH | `/commandesclients/update/etat/{id}/{etat}` | `EN_PREPARATION`, `VALIDEE`, `LIVREE` |
| PATCH | `/commandesclients/update/quantite/{idCmd}/{idLigne}/{qte}` | Modifier une quantité |
| PATCH | `/commandesclients/update/client/{idCmd}/{idClient}` | Réaffecter le client |
| PATCH | `/commandesclients/update/article/{idCmd}/{idLigne}/{idArticle}` | Changer l'article d'une ligne |
| DELETE | `/commandesclients/delete/article/{idCmd}/{idLigne}` | Retirer une ligne |
| GET | `/commandesclients/{id}/facture/pdf` | **Facture PDF** (`application/pdf`) |
| DELETE | `/commandesclients/delete/{id}` | ADMIN/MANAGER |

> Passer une commande client à `LIVREE` génère automatiquement une **sortie de stock**
> (`source = COMMANDE_CLIENT`) pour chaque ligne. Voir [05. Flux](./05-flux-utilisateurs.md).

---

## 8. Commandes fournisseurs

Préfixe `/commandesfournisseurs` — mêmes opérations que les commandes clients
(create, all, paged, lignesCommande, update/etat, update/quantite, update/fournisseur,
update/article, delete/article, delete). La livraison génère une **entrée de stock**.

---

## 9. Ventes

| Méthode | Route | Accès | Description |
|---|---|---|---|
| POST | `/ventes/create` | connecté | Créer avec `ligneVentes` → **sortie de stock immédiate** |
| GET | `/ventes/{id}` · `/ventes/all` · `/ventes/paged` | connecté | Lecture |
| GET | `/ventes/filter/{code}` | connecté | Par code |
| DELETE | `/ventes/delete/{id}` | ADMIN/MANAGER | Suppression |

---

## 10. Mouvements de stock

| Méthode | Route | Accès | Description |
|---|---|---|---|
| GET | `/mvtstk/all` | connecté | Tous les mouvements de l'entreprise (tri par id desc) |
| GET | `/mvtstk/filter/article/{idArticle}` | connecté | Historique d'un article |
| GET | `/mvtstk/stockreel/{idArticle}` | connecté | Stock réel (nombre) |
| POST | `/mvtstk/entree` | ADMIN/MANAGER | Entrée |
| POST | `/mvtstk/sortie` | ADMIN/MANAGER | Sortie |
| POST | `/mvtstk/correctionpos` | ADMIN/MANAGER | Correction positive |
| POST | `/mvtstk/correctionneg` | ADMIN/MANAGER | Correction négative |

Corps attendu pour les écritures : `{ "article": {"id": 1}, "quantite": 10 }`
(la date et le type sont posés par le serveur).

---

## 11. Utilisateurs

| Méthode | Route | Accès | Description |
|---|---|---|---|
| POST | `/utilisateurs/create` | ADMIN | Créer un utilisateur |
| GET | `/utilisateurs/{id}` · `/utilisateurs/find/{email}` · `/utilisateurs/all` | connecté | Lecture |
| PUT | `/utilisateurs/me` | connecté | **Modifier son propre profil** (nom, prénom, date naissance, adresse) |
| POST | `/utilisateurs/me/photo` | connecté | **Photo de profil** (multipart `file`, jpg/png/webp ≤ 5 Mo) |
| POST | `/utilisateurs/update/password` | connecté | Changer le mot de passe (soi-même, ou ADMIN pour tous) |
| PUT | `/utilisateurs/roles/{id}/{role}` | ADMIN | Affecter `ADMIN`, `MANAGER` ou `VENDEUR` |
| DELETE | `/utilisateurs/delete/{id}` | ADMIN | Supprimer |

---

## 12. Photos (articles)

| Méthode | Route | Accès | Description |
|---|---|---|---|
| POST | `/photos/article/{idArticle}` | ADMIN/MANAGER | Upload photo article (multipart `file`) |

Les fichiers sont servis publiquement sur **`/photos/<uuid>.<ext>`** (sans JWT) afin de
pouvoir être affichés par les balises `<img>` des deux frontends.

---

## 13. Dashboard

| Méthode | Route | Description |
|---|---|---|
| GET | `/dashboard` | Indicateurs consolidés de l'entreprise courante |

Réponse (extrait) :
```json
{
  "chiffreAffairesTotal": 0,
  "chiffreAffairesMoisEnCours": 0,
  "chiffreAffairesJourEnCours": 0,
  "nombreClients": 4, "nombreFournisseurs": 3, "nombreArticles": 12,
  "nombreCommandesClient": 2, "nombreCommandesFournisseur": 0, "nombreVentes": 2,
  "stockSousSeuil": 12, "valeurStock": 0.00, "margeMoyenne": 9130.5,
  "topArticles": [...], "ventesParMois": [...],
  "ventesParCategorie": [...], "commandesParClient": [...]
}
```

---

## 14. Exports (Excel / CSV)

| Route | Format |
|---|---|
| `/exports/articles/excel` · `/exports/articles/csv` | Liste des articles |
| `/exports/clients/excel` · `/exports/clients/csv` | Liste des clients |
| `/exports/fournisseurs/excel` · `/exports/fournisseurs/csv` | Liste des fournisseurs |
| `/exports/commandesclients/excel` · `/exports/commandesclients/csv` | Commandes clients |
| `/exports/ventes/excel` · `/exports/ventes/csv` | Ventes |

Toutes ces routes sont réservées **ADMIN / MANAGER** et renvoient le fichier en pièce jointe
(`Content-Disposition: attachment`).

---

## 15. Health & documentation

| Route | Description |
|---|---|
| `GET /actuator/health` | Statut de l'application (`UP`/`DOWN`) — public |
| `GET /actuator/info` · `/actuator/metrics` | Infos & métriques |
| `/v3/api-docs/**` · `/swagger-ui/**` | OpenAPI 3 + Swagger UI |
