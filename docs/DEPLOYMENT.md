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
2026-01-25 10:15:30.123  INFO 12345 --- [main] com.temporal.initiations.api.InitiationsApiApplication
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
2026-01-25 10:15:35.123  INFO 12346 --- [main] com.temporal.initiations.workers.InitiationsWorkersApplication
Started InitiationsWorkersApplication in 2.123 seconds
Temporal workers started on task queue: initiations
```

### Step 5: Submit Test File

```bash
cd files

# Generate test file (100 records)
python3 generate_pain_113.py 100

# Submit to API
curl -X PUT http://localhost:8080/api/v1/files/test-file-001 \
  -H "Content-Type: application/xml" \
  --data-binary @generated/pain-113-100.xml

# Expected response
HTTP/1.1 202 Accepted
Location: /api/v1/files/test-file-001
```

### Step 6: Monitor in Temporal UI

1. Open http://localhost:8080
2. Navigate to **Workflows** tab
3. Filter by namespace: `initiations`
4. Find your file workflow (ID: `test-file-001`)
5. Click to view execution details

### Step 7: Approve File (Trigger Signal)

```bash
# Send approval signal via Temporal CLI
temporal workflow signal \
  --address localhost:7233 \
  --namespace initiations \
  --workflow-id test-file-001 \
  --type approve

# OR via curl (if you implement signal endpoint)
curl -X POST http://localhost:8080/api/v1/files/test-file-001/approve
```

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

Create `docker-compose.yml` in project root (if not already present):

```yaml
version: '3.8'

services:
  temporal:
    image: temporalio/auto-setup:latest
    ports:
      - "7233:7233"
      - "8080:8080"
    environment:
      - DB=sqlite
      - SQLITE_PATH=/etc/temporal/temporal.db
    volumes:
      - temporal_data:/etc/temporal

  api:
    build:
      context: .
      dockerfile: docker/Dockerfile.api
    ports:
      - "8080:8080"
    environment:
      - TEMPORAL_ADDRESS=temporal:7233
      - TEMPORAL_NAMESPACE=initiations
      - TEMPORAL_TASK_QUEUE=initiations
    depends_on:
      - temporal
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  workers:
    build:
      context: .
      dockerfile: docker/Dockerfile.workers
    environment:
      - TEMPORAL_ADDRESS=temporal:7233
      - TEMPORAL_NAMESPACE=initiations
      - TEMPORAL_TASK_QUEUE=initiations
    depends_on:
      - temporal
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8081/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  temporal_data:
```

Run all services:
```bash
docker-compose up -d

# Verify
docker-compose ps
```

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

```yaml
# k8s/initiations-api/deployment.yaml
spec:
  containers:
    - image: your-registry/initiations-api:v1.0.0
      imagePullPolicy: Always
      env:
        - name: TEMPORAL_ADDRESS
          value: temporal-server.temporal:7233
        - name: TEMPORAL_NAMESPACE
          value: initiations
```

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

**Environment Variables** (per environment):

```bash
# dev
TEMPORAL_ADDRESS=temporal-dev.temporal:7233
TEMPORAL_NAMESPACE=initiations
LOG_LEVEL=DEBUG

# staging
TEMPORAL_ADDRESS=temporal-staging.temporal:7233
TEMPORAL_NAMESPACE=initiations
LOG_LEVEL=INFO

# production
TEMPORAL_ADDRESS=temporal-prod.temporal:7233
TEMPORAL_NAMESPACE=initiations
LOG_LEVEL=WARN
```

**ConfigMaps** (Kubernetes):

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: temporal-config
data:
  temporal.address: temporal-prod.temporal:7233
  temporal.namespace: initiations
```

### Monitoring & Alerts

**Metrics** (Prometheus):
```bash
# Scrape configuration
- job_name: 'initiations-api'
  static_configs:
    - targets: ['localhost:8080']
  metrics_path: '/actuator/prometheus'
```

**Key Metrics**:
- `workflow.execution.duration` - Time to process file
- `activity.execution.duration` - Activity timing
- `temporal.workflow.start.count` - Files submitted
- `temporal.workflow.complete.count` - Files completed

**Alerts**:
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

```bash
# Check connectivity to Temporal
curl -v temporal-server:7233/

# View logs
docker logs <api-container>
kubectl logs -f deployment/initiations-api

# Check configuration
curl http://localhost:8080/actuator/configprops
```

### Workers Service Issues

```bash
# Verify workers are registered
temporal task-queue list-partitions --task-queue initiations

# Check worker health
curl http://localhost:8081/actuator/health

# View activity execution
temporal activity describe --activity-id <activity-id>
```

### Temporal Server Issues

```bash
# Check server health
curl http://localhost:7233/

# View namespaces
temporal namespace list

# Check task queue
temporal task-queue describe --task-queue initiations
```

### File Processing Stalled

```bash
# View workflow in Temporal UI
# Check for failed activities (red highlights)
# Send approval signal if awaiting approval

temporal workflow signal \
  --namespace initiations \
  --workflow-id <file-id> \
  --type approve
```

---

## Performance Tuning

### API Service

```yaml
# application.yaml
server:
  tomcat:
    threads:
      max: 200
      min-spare: 10
```

### Workers

```yaml
temporal:
  worker:
    max-concurrent-activity-execution-size: 200
    max-concurrent-workflow-task-execution-size: 200
    max-task-queue-activities-per-second: 1000
```

### Temporal Server

```yaml
# Tuning for high throughput
dynamicConfigUpdateInterval: 5s
numHistoryShards: 256
numHistoryHosts: 6
```

---

## Rollback Plan

If deployment fails:

```bash
# View previous release
helm history initiations

# Rollback to previous version
helm rollback initiations <revision>

# OR manual rollback
kubectl set image deployment/initiations-api \
  initiations-api=your-registry/initiations-api:v<previous-version>
```
