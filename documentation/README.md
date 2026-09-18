# 📚 Documentation — Gestion de Stock (STOCK-HUB)

Bienvenue dans la documentation complète du projet **Gestion de Stock** (a.k.a. *STOCK-HUB*),
une application de gestion d'inventaire, de ventes et de commandes **multi-entreprises**.

## 🗂️ Contenu du dossier

| # | Document | Description |
|---|----------|-------------|
| 01 | [Présentation générale](./01-presentation-generale.md) | Le projet, ses objectifs, ses fonctionnalités, les acteurs |
| 02 | [Architecture technique](./02-architecture-technique.md) | Stack, organisation du code, multi-tenant, sécurité, stockage |
| 03 | [API REST](./03-api-rest.md) | Toutes les routes HTTP du backend avec exemples |
| 04 | [Modèle de données](./04-modele-de-donnees.md) | Tables, relations, énumérations, migrations Flyway |
| 05 | [Flux utilisateurs](./05-flux-utilisateurs.md) | Parcours de bout en bout pour chaque acteur |
| 06 | [Diagrammes](./06-diagrammes.md) | Cas d'utilisation, séquence, classes, états, déploiement (Mermaid) |
| 07 | [Installation & exécution](./07-installation-execution.md) | Démarrage avec Docker ou en local, variables d'environnement |
| 08 | [Sécurité & rôles](./08-securite-roles.md) | JWT, RBAC, règles de protection des endpoints |
| 09 | [Guide de test](./09-guide-de-test.md) | Tests end-to-end curl, jeu de données de démonstration |
| 10 | [Journal des évolutions](./10-journal-des-evolutions.md) | Changelog technique du projet (migrations, corrections) |

## 🎯 Par où commencer ?

- **Découvrir le projet** → [01. Présentation générale](./01-presentation-generale.md)
- **Installer et lancer** → [07. Installation & exécution](./07-installation-execution.md)
- **Développer / intégrer** → [02. Architecture](./02-architecture-technique.md) + [03. API REST](./03-api-rest.md)
- **Comprendre la base de données** → [04. Modèle de données](./04-modele-de-donnees.md)
- **Comprendre les parcours métier** → [05. Flux utilisateurs](./05-flux-utilisateurs.md) + [06. Diagrammes](./06-diagrammes.md)

## 🔗 Accès rapides

| Service | URL |
|---|---|
| Frontend Angular | http://localhost:4200 |
| Frontend Next.js (STOCK-HUB) | http://localhost:3009 |
| API Backend | http://localhost:8089/gestiondestock |
| **Adminer (gestion BDD)** | http://localhost:8081 |
| Swagger UI | http://localhost:8089/swagger-ui/index.html |
| Health check | http://localhost:8089/actuator/health |

> Les diagrammes sont écrits en [Mermaid](https://mermaid.js.org/) : ils se rendent nativement
> dans GitHub, GitLab, VS Code (extension Mermaid) et la plupart des outils de documentation.
