# 01 — Présentation générale

## 1. Le projet en une phrase

**Gestion de Stock** est une application web full-stack qui permet à des entreprises de gérer
leurs **articles**, leur **stock** (entrées, sorties, corrections), leurs **clients** et
**fournisseurs**, leurs **commandes** (ventes et achats) et leurs **utilisateurs**, avec une
séparation stricte des données par entreprise (*multi-tenant*).

## 2. Contexte et objectifs

| Objectif | Description |
|---|---|
| Centralisation | Une seule source de vérité pour le catalogue, le stock et les ventes |
| Multi-entreprise | Chaque entreprise ne voit que ses données (isolation au niveau API) |
| Temps réel | Le stock est mis à jour automatiquement à chaque vente / livraison |
| Traçabilité | Chaque mouvement de stock est historisé (date, type, source) |
| Double interface | Une UI Angular (classique) et une UI Next.js (moderne) sur la même API |
| Sécurité | Authentification JWT, rôles ADMIN / MANAGER / VENDEUR |

## 3. Fonctionnalités principales

### Catalogue & stock
- **Articles** : code, désignation, prix HT, taux de TVA, prix TTC (calculé), seuil d'alerte, photo
- **Catégories** : classement des articles (code + désignation)
- **Mouvements de stock** : entrées, sorties, corrections positives/négatives, tous historisés
- **Stock réel** : calculé comme la somme (signée) des mouvements d'un article
- **Alertes** : article sous le seuil d'alerte → signalé dans le dashboard et la page articles

### Relation client / fournisseur
- **Clients** : fiche complète (nom, prénom, email, téléphone, adresse, photo)
- **Fournisseurs** : même structure de fiche
- **Commandes clients** : lignes de commande (article, quantité, prix), cycle d'états
  `EN_PREPARATION → VALIDEE → LIVREE`, génération de **facture PDF**
- **Commandes fournisseurs** : même cycle ; la livraison génère une **entrée de stock**
- **Ventes** : vente directe au comptoir, déduit le stock immédiatement

### Administration
- **Utilisateurs** : création par l'ADMIN, affectation de rôles, désactivation par suppression
- **Entreprise** : création d'entreprise (inscription) → génère automatiquement un compte ADMIN
- **Profil** : chaque utilisateur peut modifier son profil (nom, prénom, adresse, date de naissance)
  et sa **photo de profil** (upload local)
- **Dashboard** : chiffre d'affaires (jour/mois/total), valeur du stock, marge moyenne,
  top articles, top clients, répartition par catégorie
- **Exports** : Excel et CSV pour articles, clients, fournisseurs, commandes, ventes

## 4. Les acteurs (rôles)

| Rôle | Permissions |
|---|---|
| **Visiteur (non connecté)** | Inscription d'une entreprise, connexion, consultation des catégories |
| **VENDEUR** | Consultation de toutes les listes, création de commandes clients et ventes |
| **MANAGER** | Tous les droits VENDEUR + suppression clients/fournisseurs/commandes, mouvements de stock, exports |
| **ADMIN** | Tous les droits MANAGER + gestion des utilisateurs (création, rôles, suppression) |

> L'ADMIN est en plus le seul à pouvoir créer des utilisateurs et à modifier le mot de passe
> de n'importe quel compte. Un utilisateur lambda ne peut modifier que **son propre** profil
> et **son propre** mot de passe.

## 5. Les deux frontends

| | Angular (port 4200) | Next.js (port 3009) |
|---|---|---|
| Style | Bootstrap 5, dashboards classiques | Tailwind 4, design "glass" sombre |
| Public | Back-office complet de gestion | Tableau de bord moderne, orienté pilotage |
| Auth | JWT en `localStorage` + intercepteur HTTP | JWT en `localStorage` + provider React |
| Particularité | Recherche globale d'articles dans le header | Création d'entreprise depuis l'écran de login |

Les deux consomment **exactement la même API** — aucune logique métier côté client.

## 6. Périmètre technique (résumé)

- **Backend** : Spring Boot 3.3 (Java 17), PostgreSQL 15, Flyway, Spring Security + JWT, OpenPDF, Apache POI
- **Frontend 1** : Angular 15 + Material + Bootstrap
- **Frontend 2** : Next.js 16 + React 19 + Tailwind 4 + Recharts
- **Déploiement** : Docker Compose (4 services : db, backend, frontend, frontend-next)

Détails → [02. Architecture technique](./02-architecture-technique.md)
