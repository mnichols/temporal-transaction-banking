# Deployment Guide

## Local Development

### Prerequisites

- Java 21
- Maven 3.8.1+
- Docker & Docker Compose
- Temporal CLI (optional, for debugging)

### Step 1: Start Temporal Server

```bash
cd /path/to/temporal-transaction-banking

# Start Temporal server and UI
docker-compose up -d
```

Verify:
```bash
# Temporal server ready
curl http://localhost:7233/

# UI available
open http://localhost:8080
```

### Step 2: Build Java Project

```bash
cd java

# Clean build
mvn clean install -DskipTests

# This compiles:
# - initiations-core
# - initiations-api
# - initiations-workers
```

Output:
```
java/initiations/initiations-api/target/initiations-api-1.0.0.jar
java/initiations/initiations-workers/target/initiations-workers-1.0.0.jar
```

### Step 3: Run API Service

```bash
cd java/initiations/initiations-api

java -jar target/initiations-api-1.0.0.jar
```

Output:
```
2026-01-25 10:15:30.123  INFO 12345 --- [main] com.temporal.initiations.api.Application
Started InitiationsApiApplication in 3.456 seconds
```

Verify:
```bash
# Health check
curl http://localhost:8080/actuator/health

# Expected response
{"status":"UP"}
```

### Step 4: Run Workers Service (New Terminal)

```bash
cd java/initiations/initiations-workers

java -jar target/initiations-workers-1.0.0.jar
```

Output:
```
2026-01-25 10:15:35.123  INFO 12346 --- [main] com.temporal.initiations.workers.Application
Started InitiationsWorkersApplication in 2.123 seconds
Temporal workers started on task queue: initiations
```

### Step 5: Submit Test File

Submit a file initiation request to the API. The endpoint initiates a File workflow asynchronously and returns 202 Accepted with the workflow ID.

See the API documentation in the controller source code for request/response format details.

### Step 6: Monitor in Temporal UI

1. Open http://localhost:8080
2. Navigate to **Workflows** tab
3. Filter by namespace: `initiations`
4. Find your file workflow (ID: `test-file-001`)
5. Click to view execution details

### Step 7: Approve File (Trigger Signal)

Send an approval signal to the file workflow using the Temporal CLI or via a REST endpoint (if implemented).

### Step 8: View Results

In Temporal UI:
1. Refresh the workflow view
2. See workflow history with activities
3. View batch workflows created
4. Monitor batch processing

### Stop Services

```bash
# Stop API service
# (Press Ctrl+C in the terminal)

# Stop Workers service
# (Press Ctrl+C in the other terminal)

# Stop Temporal server
docker-compose down
```

---

## Docker-Based Local Development

### Build Container Images

```bash
cd /path/to/temporal-transaction-banking

# Build API image
docker build \
  -f docker/Dockerfile.api \
  -t temporal-transaction-banking/initiations-api:latest .

# Build Workers image
docker build \
  -f docker/Dockerfile.workers \
  -t temporal-transaction-banking/initiations-workers:latest .
```

### Run with Docker Compose (Full Stack)

A `docker-compose.yml` file is provided in the project root for running the full stack locally.

Run all services:
```bash
docker-compose up -d

# Verify
docker-compose ps
```

This starts:
- Temporal Server (port 7233, UI on port 8080)
- API Service (port 8081)
- Workers Service (port 8082)

Consult `docker-compose.yml` in the project root for full configuration details.

---

## Kubernetes Deployment (Minikube)

### Prerequisites

- Minikube installed
- kubectl installed
- Docker images pushed to registry

### Step 1: Start Minikube

```bash
minikube start --cpus=4 --memory=8192
```

### Step 2: Create Namespace

```bash
kubectl create namespace temporal-initiations
kubectl config set-context --current --namespace=temporal-initiations
```

### Step 3: Deploy Temporal Server

Use Temporal Helm chart or manual deployment:

```bash
# Option A: Using Helm (recommended)
helm repo add temporal https://temporal-helm.herokuapp.com
helm install temporal temporal/temporal \
  --namespace temporal-initiations \
  --values k8s/temporal-values.yaml

# Option B: Manual YAML
kubectl apply -f k8s/temporal-server.yaml
```

### Step 4: Deploy API Service

```bash
kubectl apply -f k8s/initiations-api/deployment.yaml
kubectl apply -f k8s/initiations-api/service.yaml
```

Verify:
```bash
kubectl get pods -l app=initiations-api
kubectl get svc initiations-api
```

### Step 5: Deploy Workers

```bash
kubectl apply -f k8s/initiations-workers/deployment.yaml
```

Verify:
```bash
kubectl get pods -l app=initiations-workers
```

### Step 6: Port Forward (Local Access)

```bash
# API service
kubectl port-forward svc/initiations-api 8080:8080

# Temporal UI (in another terminal)
kubectl port-forward svc/temporal-ui 8080:8080
```

### Step 7: Deploy Temporal Worker Controller (Auto-scaling)

```bash
# Install Temporal Worker Controller
kubectl apply -f k8s/temporal-worker-controller/

# Configure for initiations workers
kubectl apply -f k8s/temporal-worker-controller-config.yaml
```

This enables auto-scaling of workers based on task queue depth.

### Monitor Kubernetes Deployment

```bash
# Watch pods
kubectl get pods -w

# View logs
kubectl logs -f deployment/initiations-api
kubectl logs -f deployment/initiations-workers

# Describe resources
kubectl describe pod <pod-name>

# Access shell
kubectl exec -it <pod-name> -- bash
```

### Clean Up

```bash
# Delete all resources
kubectl delete namespace temporal-initiations

# Or stop Minikube
minikube stop
```

---

## Production Deployment

### Infrastructure Requirements

- Temporal Server (managed service or self-hosted)
  - 3+ server nodes for HA
  - PostgreSQL or MySQL database
  - Cassandra or other persistence layer (optional)
  - gRPC ports (7233)

- Kubernetes Cluster
  - 2+ worker nodes
  - CPU: 4+ cores per node
  - Memory: 8GB+ per node
  - Persistent storage for database

- Container Registry
  - Docker Hub, ECR, GCR, or private registry
  - Images: `initiations-api:v1.0.0`, `initiations-workers:v1.0.0`

### Deployment Process

#### 1. Build & Push Images

```bash
# Build
docker build -f docker/Dockerfile.api \
  -t your-registry/initiations-api:v1.0.0 .
docker build -f docker/Dockerfile.workers \
  -t your-registry/initiations-workers:v1.0.0 .

# Push
docker push your-registry/initiations-api:v1.0.0
docker push your-registry/initiations-workers:v1.0.0
```

#### 2. Update Kubernetes Manifests

Update image references and environment variables in the Kubernetes manifests under `k8s/` directory to point to your container registry and configure Temporal server address.

#### 3. Deploy to Production Cluster

```bash
# Using kubectl
kubectl apply -f k8s/ --context=prod-cluster

# OR using Helm (if available)
helm upgrade --install initiations ./helm/initiations \
  --kubeconfig=/path/to/prod-kubeconfig
```

#### 4. Verify Deployment

```bash
# Check pods are running
kubectl get pods -l app=initiations-api -l app=initiations-workers

# Check services
kubectl get svc

# Check logs
kubectl logs -f deployment/initiations-api
kubectl logs -f deployment/initiations-workers

# Health check
kubectl exec -it $(kubectl get pod -l app=initiations-api -o jsonpath='{.items[0].metadata.name}') \
  -- curl localhost:8080/actuator/health
```

### Configuration Management

Configuration is managed through environment variables and Kubernetes ConfigMaps/Secrets:

**Environment Variables**: `TEMPORAL_ADDRESS`, `TEMPORAL_NAMESPACE`, `LOG_LEVEL`
**Kubernetes**: Use ConfigMaps for configuration and Secrets for sensitive values

Environment-specific configurations are managed through deployment manifests in `k8s/` directory.

### Monitoring & Alerts

**Metrics** (Prometheus):
Metrics are exposed via the actuator endpoint at `/actuator/prometheus`

**Key Metrics to Monitor**:
- Workflow execution duration
- Activity execution time
- Workflow start/completion counts
- Workflow/activity failure rates
- Worker heartbeat status
- API response times

**Alert Conditions**:
- Workflow failure rate > 5%
- Activity timeout rate > 2%
- Workers unhealthy (no heartbeat)
- API response time > 5s

### Disaster Recovery

**Backup Strategy**:
- Temporal database: Daily backups, 30-day retention
- Workflow state: Maintained in Temporal (no manual backup needed)
- Configuration: Version controlled in Git

**Recovery Procedure**:
1. Restore Temporal database from backup
2. Redeploy API and workers services
3. Resubmit failed files via API
4. Monitor reconciliation

---

## Troubleshooting

### API Service Issues

- Check connectivity to Temporal server
- Review application logs
- Check health endpoint: `http://localhost:8080/actuator/health`

### Workers Service Issues

- Verify workers are registered with the task queue
- Check worker health endpoint
- Review activity execution history in Temporal UI

### Temporal Server Issues

- Verify Temporal server is running and accessible
- Check namespace exists and is configured
- Verify task queue is registered

### File Processing Stalled

- Review workflow execution in Temporal UI
- Check for failed activities
- Send approval signal if workflow is awaiting approval

---

## Performance Tuning

Performance tuning can be configured through `application.yaml` in each service:

**API Service**: Configure thread pool sizes for the HTTP server
**Workers**: Configure concurrent activity and workflow execution limits
**Temporal Server**: Configure history shards, hosts, and dynamic config update intervals

See the source code `application.yaml` files in each module for tuning parameters.

---

## Rollback Plan

If deployment fails:

Using Helm:
- `helm history initiations` - View previous releases
- `helm rollback initiations <revision>` - Rollback to previous version

Using kubectl:
- Update image references in deployments to previous version
- Apply changes to revert to previous state
