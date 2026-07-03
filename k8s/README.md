# Motus sur Minikube (Kubernetes)

Déployer les 4 microservices + MySQL sur un cluster **Kubernetes local (Minikube)**.
C'est la version « cloud-native » de `docker-compose` : en plus, Kubernetes sait
**scaler automatiquement** et **redémarrer un service qui tombe** (design for failure).

> Le front n'est pas modifié : on rebranche `localhost:8081..8084` sur le cluster
> avec `kubectl port-forward`, donc `http://localhost:5500` marche comme d'habitude.

---

## 0. Pré-requis (une fois)

```bash
brew install minikube kubectl     # si pas déjà installés
docker --version                  # Docker Desktop doit tourner
```

## 1. Tout déployer (une commande)

```bash
bash k8s/deploy.sh
```

Ce script : démarre Minikube, active le metrics-server, **construit les 4 images
dans le Docker de Minikube**, applique les manifests, attend que tout soit prêt.

## 2. Rendre l'appli accessible et jouer

Dans un **2ᵉ terminal** :

```bash
bash k8s/port-forward.sh
```

Puis ouvre **http://localhost:5500** et joue normalement. Ctrl+C pour arrêter les tunnels.

---

## Les commandes utiles (à connaître pour la soutenance)

```bash
kubectl get pods                 # l'état de tous les pods
kubectl get svc                  # les services (adresses internes)
kubectl get deployments          # les déploiements
kubectl logs deploy/dict-service # les logs d'un service
kubectl describe pod <nom-du-pod># le détail (events, santé) d'un pod
```

### Démo 1 — Scalabilité horizontale (le cœur du cours)

```bash
kubectl scale deployment/dict-service --replicas=3
kubectl get pods -l app=dict-service      # -> 3 pods de dict-service !
```

### Démo 2 — Auto-réparation (design for failure)

```bash
kubectl get pods
kubectl delete pod <un-pod-au-hasard>     # on "casse" un pod
kubectl get pods                          # Kubernetes en recrée un tout seul
```

### Démo 3 — Auto-scaling (HPA)

```bash
kubectl get hpa                           # game-service-hpa : cible 70% CPU, 1->5 pods
# Sous forte charge, le nombre de pods de game-service monte automatiquement.
```

---

## Tout arrêter / nettoyer

```bash
kubectl delete -f k8s/     # supprime tous les objets Motus
minikube stop              # éteint le cluster (le garde pour la prochaine fois)
minikube delete            # supprime complètement le cluster
```

---

## Ce que chaque fichier démontre (mapping avec le cours)

| Fichier | Notion du cours |
|---|---|
| `01-mysql.yaml` | Base de données (Secret, **PVC** = stockage persistant, Service DNS) |
| `02..05-*-service.yaml` | **Deployment** (replicas, self-healing) + **Service** (découverte par nom) + **sondes de santé** (`/actuator/health`) |
| `06-autoscaling.yaml` | **Scalabilité / élasticité** automatique (HPA) |
| `deploy.sh` | Build des images + déploiement (Infrastructure as Code) |
| `port-forward.sh` | Accès au cluster depuis le front local |

> **Phrase clé** : « docker-compose orchestre nos conteneurs en local ; Minikube fait
> pareil avec Kubernetes, en ajoutant le scaling automatique, l'auto-réparation des
> pods et la découverte de services — ce qui rend l'architecture réellement cloud-native. »
