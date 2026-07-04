#!/usr/bin/env bash
# =====================================================================
#  Rend les 4 services du cluster accessibles sur localhost:8081..8084,
#  EXACTEMENT comme docker-compose. Le front (http://localhost:8090)
#  fonctionne alors SANS aucune modification.
#
#  Usage :  bash k8s/port-forward.sh   (laisse la fenetre ouverte)
#           Ctrl+C pour tout arreter.
# =====================================================================
echo "Ouverture des tunnels localhost -> cluster (Ctrl+C pour arreter)…"
kubectl port-forward svc/player-service 8081:8081 &
kubectl port-forward svc/game-service   8082:8082 &
kubectl port-forward svc/dict-service   8083:8083 &
kubectl port-forward svc/score-service  8084:8084 &

# Sert aussi le front comme en demo docker :
( cd "$(dirname "$0")/../frontend" && python3 -m http.server 8090 >/tmp/motus-front.log 2>&1 & )
echo "Front sur http://localhost:8090  |  services sur 8081-8084"

trap 'echo; echo "Arret des tunnels…"; kill $(jobs -p) 2>/dev/null; pkill -f "http.server 8090" 2>/dev/null' INT
wait
