# Motus — Jeu de mots en microservices

Projet **Applications Web orientées Services** · M2 MIAGE SITN · Université Paris-Dauphine · 2025-2026
Binôme : **Liya** & **Steven**

---

## Prérequis

- **Docker Desktop** (démarré)
- **Python 3** (pour servir le frontend statique)
- Ports libres : `3306`, `8081`, `8082`, `8083`, `8084`, `8090`

## Lancement

```bash
git clone https://github.com/Steven-1105/M2_SITN_Motus.git
cd M2_SITN_Motus
docker compose up --build -d          # MySQL + les 4 microservices
cd frontend && python3 -m http.server 8090
# puis ouvre http://localhost:8090
```

Le dictionnaire (~132 000 mots) charge en environ 30 secondes au premier démarrage.

## Comptes de démonstration

| Identifiant | Mot de passe | Rôle |
|-------------|--------------|------|
| `admin`     | `admin123`   | ADMIN — accès au panneau d'administration |
| _(inscription libre)_ | _(6 caractères min.)_ | PLAYER |

Douze comptes joueurs (Liya, Steven, Amelie, Nathan…) peuplent aussi le classement de démo.

Le mode **invité** est disponible depuis l'écran de connexion : les scores ne sont pas enregistrés.

## Services

| Service         | Port | Description                                        |
|-----------------|------|----------------------------------------------------|
| player-service  | 8081 | Inscription, connexion, gestion des joueurs        |
| game-service    | 8082 | Création de partie, feedback lettre par lettre     |
| dict-service    | 8083 | Dictionnaire français, tirage aléatoire, validation |
| score-service   | 8084 | Historique, classement, administration             |

Chaque service a sa propre base MySQL (`motus_players`, `motus_games`, `motus_dictionary`, `motus_scores`).

## Arrêter

```bash
docker compose down          # arrête les services
docker compose down -v       # arrête et supprime les données (reseed complet au prochain démarrage)
```

## Déploiement Kubernetes

Un déploiement Minikube est fourni dans `k8s/`. Lancer `bash k8s/deploy.sh` puis
`bash k8s/port-forward.sh` pour exposer les services sur les mêmes ports que
`docker compose`.

## Dépannage

- **Front qui affiche « dict-service injoignable »** : attends 30-60 s après le
  démarrage, le dictionnaire finit son chargement.
- **Docker refuse de démarrer** : vérifie que Docker Desktop tourne.
- **Port occupé** : `docker compose down` puis relance.
