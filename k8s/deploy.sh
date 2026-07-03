#!/usr/bin/env bash
# =====================================================================
#  Deploie tout Motus sur Minikube en une commande :  bash k8s/deploy.sh
#  (a lancer depuis la racine du projet)
# =====================================================================
set -e
cd "$(dirname "$0")/.."

echo "== 1) Demarrage de Minikube =="
minikube status >/dev/null 2>&1 || minikube start
minikube addons enable metrics-server   # necessaire pour l'auto-scaling (HPA)

echo "== 2) On construit les images DANS le Docker de Minikube =="
# Astuce cle : ce 'eval' fait pointer 'docker' vers le moteur interne de Minikube,
# donc les images construites ici sont directement visibles par le cluster.
eval "$(minikube docker-env)"
docker build -t player-service:1.0 ./player-service
docker build -t game-service:1.0   ./game-service
docker build -t dict-service:1.0   ./dict-service
docker build -t score-service:1.0  ./score-service

echo "== 3) On applique tous les manifests =="
kubectl apply -f k8s/

echo "== 4) On attend que tout soit pret (peut prendre 1-3 min, dict charge 132k mots) =="
kubectl rollout status deployment/mysql          --timeout=180s
kubectl rollout status deployment/dict-service   --timeout=300s
kubectl rollout status deployment/score-service  --timeout=180s
kubectl rollout status deployment/player-service --timeout=180s
kubectl rollout status deployment/game-service   --timeout=180s

echo ""
echo "OK ! Tout tourne sur Minikube.  Verifie avec :  kubectl get pods"
echo "Puis rends les services accessibles au front :  bash k8s/port-forward.sh"
