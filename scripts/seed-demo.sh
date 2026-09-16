#!/usr/bin/env bash
set -e
BASE="http://localhost:8089/gestiondestock"
PW='som3R@nd0mP@$$word'
CT="Content-Type: application/json"

jget() { python3 -c "import sys,json;d=json.load(sys.stdin);print(d$1 if d$1 is not None else '')" 2>/dev/null || echo ""; }

post() { curl -s -X POST "$BASE$1" -H "$CT" ${3:+-H "Authorization: Bearer $3"} -d "$2"; }
postA() { curl -s -X POST "$BASE$1" -H "$CT" -H "Authorization: Bearer $TOKEN" -d "$2"; }
putA() { curl -s -X PUT "$BASE$1" -H "$CT" -H "Authorization: Bearer $TOKEN"; }

echo "== 1. Entreprise =="
post /entreprises/create '{
  "nom":"StockFlow Démo SARL","description":"Entreprise de démonstration","codefiscale":"CF-2026-001",
  "email":"demo@stockflow.com","numTel":"+22890000001","siteWeb":"https://stockflow.app",
  "adresse":{"addresse1":"12 Boulevard du 13 Janvier","addresse2":"Immeuble Kossi","Ville":"Lomé","codePostale":"00000","pays":"Togo"}}' | jget "['id']"

echo "== 2. Login =="
TOKEN=$(curl -s -X POST "$BASE/auth/authentification" -H "$CT" -d '{"login":"demo@stockflow.com","password":"'$PW'"}' | python3 -c "import sys,json;print(json.load(sys.stdin)['accessToken'])")
echo "Token OK: ${TOKEN:0:16}..."

C() { postA /categories/create "{\"codeCategory\":\"$1\",\"designation\":\"$2\"}" | jget "['id']"; }
echo "== 3. Catégories =="
C CAT-ELEC "Électronique"
C CAT-ALIM "Alimentaire"
C CAT-VET "Vêtements"
C CAT-MOB "Mobilier"
C CAT-PAP "Papeterie"

A() { postA /articles/create "{\"codeArticle\":\"$1\",\"designation\":\"$2\",\"prixUnitaire\":$3,\"tauxTva\":18,\"prixUnitaireTTc\":$4,\"seuilAlerte\":$5,\"category\":{\"id\":$6}}" >/dev/null; }
echo "== 4. Articles =="
A ART-001 "Ordinateur portable Pro 14" 450000 531000 8 1
A ART-002 "Casque audio sans fil" 25000 29500 15 1
A ART-003 "Clavier mécanique RGB" 18000 21240 10 1
A ART-004 "Souris ergonomique" 8000 9440 20 1
A ART-005 "Riz parfumé 5kg" 3500 4130 40 2
A ART-006 "Huile végétale 1L" 1200 1416 60 2
A ART-007 "Lait concentré 12x370g" 5500 6490 30 2
A ART-008 "T-shirt coton premium" 3000 3540 25 3
A ART-009 "Jeans slim" 9000 10620 18 3
A ART-010 "Chaise de bureau ergo" 25000 29500 6 4
A ART-011 "Table bureau 120cm" 60000 70800 4 4
A ART-012 "Cahier 200 pages A4" 500 590 100 5

echo "== 5. Clients =="
postA /clients/create '{"nom":"Amewou","prenom":"Kofi","mail":"kofi.amewou@mail.com","numTel":"+22890000010","adresse":{"addresse1":"15 Rue du Marché","Ville":"Lomé","codePostale":"00000","pays":"Togo"}}' >/dev/null
postA /clients/create '{"nom":"Abla","prenom":"Ama","mail":"ama.abla@mail.com","numTel":"+22890000011","adresse":{"addresse1":"8 Avenue de la Paix","Ville":"Kara","codePostale":"00000","pays":"Togo"}}' >/dev/null
postA /clients/create '{"nom":"Soglo","prenom":"Pierre","mail":"pierre.soglo@mail.com","numTel":"+22890000012","adresse":{"addresse1":"3 Rue des Artisans","Ville":"Cotonou","codePostale":"00000","pays":"Bénin"}}' >/dev/null
postA /clients/create '{"nom":"Diallo","prenom":"Fatou","mail":"fatou.diallo@mail.com","numTel":"+22890000013","adresse":{"addresse1":"27 Rue du Port","Ville":"Dakar","codePostale":"00000","pays":"Sénégal"}}' >/dev/null

echo "== 6. Fournisseurs =="
postA /fournisseurs/create '{"nom":"TechnoWest","prenom":"Import","mail":"contact@technowest.com","numTel":"+22891000001","adresse":{"addresse1":"Zone industrielle","Ville":"Lomé","codePostale":"00000","pays":"Togo"}}' >/dev/null
postA /fournisseurs/create '{"nom":"AgroDistrib","prenom":"Import","mail":"ventes@agrodistrib.com","numTel":"+22891000002","adresse":{"addresse1":"Port autonome","Ville":"Lomé","codePostale":"00000","pays":"Togo"}}' >/dev/null
postA /fournisseurs/create '{"nom":"MobilierPlus","prenom":"Import","mail":"info@mobilierplus.com","numTel":"+22891000003","adresse":{"addresse1":"Route de Kpalimé","Ville":"Lomé","codePostale":"00000","pays":"Togo"}}' >/dev/null

CC() { postA /commandesclients/create "{\"code\":\"$1\",\"dateComande\":\"$2\",\"etatCommande\":\"$3\",\"client\":{\"id\":$4},\"ligneComandeClientList\":[{\"article\":{\"id\":$5},\"quantite\":$6,\"prixUnitaire\":$7}]}" | jget "['id']"; }
echo "== 7. Commandes clients =="
CC CMD-2026-001 "2026-07-02T09:00:00Z" "EN_PREPARATION" 1 1 1 531000
CC CMD-2026-002 "2026-07-18T10:30:00Z" "VALIDEE" 2 2 3 29500
CC CMD-2026-003 "2026-08-05T14:00:00Z" "LIVREE" 3 3 2 21240
CC CMD-2026-004 "2026-08-10T11:00:00Z" "LIVREE" 4 5 10 4130

V() { postA /ventes/create "{\"code\":\"$1\",\"dateVente\":\"$2\",\"commentaire\":\"$3\",\"ligneVentes\":[{\"article\":{\"id\":$4},\"quantite\":$5,\"prixUnitaire\":$6}]}" >/dev/null; }
echo "== 8. Ventes (historique mensuel) =="
V V-2026-02 "2026-02-12T10:00:00Z" "Vente boutique" 1 2 531000
V V-2026-03 "2026-03-08T11:30:00Z" "Vente en ligne" 2 4 29500
V V-2026-03 "2026-03-21T09:15:00Z" "Vente boutique" 5 20 4130
V V-2026-04 "2026-04-05T16:00:00Z" "Grossiste" 3 5 21240
V V-2026-04 "2026-04-19T10:45:00Z" "Vente boutique" 8 15 3540
V V-2026-05 "2026-05-09T12:00:00Z" "Vente en ligne" 9 6 10620
V V-2026-06 "2026-06-14T15:20:00Z" "Grossiste" 10 8 29500
V V-2026-06 "2026-06-27T09:40:00Z" "Vente boutique" 6 30 1416
V V-2026-07 "2026-07-11T10:10:00Z" "Vente en ligne" 11 3 70800
V V-2026-08 "2026-08-12T09:00:00Z" "Vente du jour" 2 2 29500
V V-2026-08 "2026-08-08T16:30:00Z" "Vente boutique" 7 12 6490

echo "== 9. Mouvements de stock =="
postA /mvtstk/entree '{"article":{"id":1},"quantite":10}' >/dev/null
postA /mvtstk/entree '{"article":{"id":2},"quantite":25}' >/dev/null
postA /mvtstk/entree '{"article":{"id":3},"quantite":12}' >/dev/null
postA /mvtstk/entree '{"article":{"id":4},"quantite":30}' >/dev/null
postA /mvtstk/entree '{"article":{"id":5},"quantite":100}' >/dev/null
postA /mvtstk/entree '{"article":{"id":6},"quantite":80}' >/dev/null
postA /mvtstk/entree '{"article":{"id":7},"quantite":45}' >/dev/null
postA /mvtstk/entree '{"article":{"id":8},"quantite":20}' >/dev/null
postA /mvtstk/entree '{"article":{"id":9},"quantite":15}' >/dev/null
postA /mvtstk/entree '{"article":{"id":10},"quantite":8}' >/dev/null
postA /mvtstk/entree '{"article":{"id":11},"quantite":5}' >/dev/null
postA /mvtstk/entree '{"article":{"id":12},"quantite":200}' >/dev/null

echo "== 10. Utilisateur VENDEUR =="
USERID=$(post /utilisateurs/create '{"nom":"Sena","prenom":"Afi","email":"afi@stockflow.com","motDePasse":"'$PW'","dateDeNaissance":"1992-05-14","entreprise":{"id":1},"adresse":{"addresse1":"2 Rue des Cocotiers","Ville":"Lomé","codePostale":"00000","pays":"Togo"}}' | grep -o 'id=[0-9]*' | head -1 | cut -d= -f2)
echo "Vendeur id=$USERID"
putA "/utilisateurs/roles/$USERID/VENDEUR" >/dev/null

echo "== Seeding terminé =="
