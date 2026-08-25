# Production Deployment Guide

This guide covers deployment options for these microservices to production environments. We'll cover Docker, Kubernetes, and the major cloud platforms (AWS, GCP, Azure).

**Note**: All cost estimates in this guide are rough approximations based on current pricing and typical usage patterns. Your actual costs will vary significantly based on region, usage, reserved capacity, and other factors.

---

## Table of Contents

- [Docker Deployment](#docker-deployment)
- [Kubernetes Deployment](#kubernetes-deployment)
- [Cloud Platforms](#cloud-platforms)
- [Observability](#observability)
- [Security Best Practices](#security-best-practices)
- [Performance Tuning](#performance-tuning)

---

## Docker Deployment

### Basic Docker Deployment

All frameworks include production-ready Dockerfiles with multi-stage builds.

#### Build and Run

```bash
# Navigate to framework directory
cd undertow-baseline

# Build image
docker build -t undertow-app:latest .

# Run container
docker run -d \
  --name undertow-app \
  -p 8088:8088 \
  -e JAVA_OPTS="-Xmx512m -Xms256m" \
  undertow-app:latest

# Check health
curl http://localhost:8088/health

# View logs
docker logs -f undertow-app
```

#### Multi-Stage Dockerfile Example

```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# JVM optimizations
ENV JAVA_OPTS="-XX:+UseG1GC \
               -XX:MaxGCPauseMillis=200 \
               -XX:+UseContainerSupport \
               -Xmx512m -Xms256m"

EXPOSE 8088
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### Docker Compose for Local Development

```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8088:8088"
    environment:
      - JAVA_OPTS=-Xmx512m
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=appdb
      - DB_USER=postgres
      - DB_PASSWORD=postgres
    depends_on:
      postgres:
        condition: service_healthy

  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: appdb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 5s
      timeout: 5s
      retries: 5

volumes:
  postgres_data:
```

### Docker Best Practices

✅ **Use multi-stage builds** - Reduce image size
✅ **Use Alpine base images** - Smaller images, faster pulls
✅ **Set resource limits** - Prevent container OOM
✅ **Use health checks** - Enable Docker to monitor health
✅ **Don't run as root** - Security best practice
✅ **Use .dockerignore** - Faster builds

**.dockerignore**:
```
target/
.git/
.idea/
*.md
.env
```

---

## Kubernetes Deployment

### Production-Ready Kubernetes Manifests

All manifests available in [k8s/](../k8s/) directory with framework-specific configurations.

#### Quick Deploy to Kubernetes

```bash
# Deploy Undertow with all resources
kubectl apply -f k8s/undertow-baseline/

# Includes:
# - Deployment (replicas, health checks, resources)
# - Service (ClusterIP)
# - ConfigMap (environment configuration)
# - HPA (auto-scaling)
# - PDB (pod disruption budget)
# - Ingress (external access)
# - ServiceMonitor (Prometheus metrics)
```

### Deployment Manifest

**k8s/undertow-baseline/deployment.yaml**:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: undertow-baseline
  namespace: micro-apps
  labels:
    app: undertow-baseline
    framework: undertow
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: undertow-baseline
  template:
    metadata:
      labels:
        app: undertow-baseline
        framework: undertow
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8088"
        prometheus.io/path: "/metrics"
    spec:
      containers:
      - name: app
        image: undertow-baseline:1.0.0
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 8088
          name: http
          protocol: TCP
        env:
        - name: JAVA_OPTS
          value: "-Xmx512m -Xms256m -XX:+UseG1GC"
        - name: DB_HOST
          valueFrom:
            configMapKeyRef:
              name: undertow-config
              key: db.host
        resources:
          requests:
            cpu: 250m
            memory: 512Mi
          limits:
            cpu: 1000m
            memory: 1Gi
        livenessProbe:
          httpGet:
            path: /health
            port: 8088
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /health
            port: 8088
          initialDelaySeconds: 10
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 2
```

### Horizontal Pod Autoscaler (HPA)

**k8s/undertow-baseline/hpa.yaml**:

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: undertow-baseline-hpa
  namespace: micro-apps
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: undertow-baseline
  minReplicas: 3
  maxReplicas: 20
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  behavior:
    scaleUp:
      stabilizationWindowSeconds: 60
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
      - type: Pods
        value: 2
        periodSeconds: 60
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 10
        periodSeconds: 60
```

### Service & Ingress

**Service (k8s/undertow-baseline/service.yaml)**:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: undertow-baseline
  namespace: micro-apps
  labels:
    app: undertow-baseline
spec:
  type: ClusterIP
  selector:
    app: undertow-baseline
  ports:
  - port: 80
    targetPort: 8088
    protocol: TCP
    name: http
```

**Ingress (k8s/undertow-baseline/ingress.yaml)**:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: undertow-baseline
  namespace: micro-apps
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    cert-manager.io/cluster-issuer: letsencrypt-prod
spec:
  ingressClassName: nginx
  tls:
  - hosts:
    - api.yourdomain.com
    secretName: api-tls
  rules:
  - host: api.yourdomain.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: undertow-baseline
            port:
              number: 80
```

### Resource Sizing by Framework

| Framework | CPU Request | CPU Limit | Memory Request | Memory Limit | Replicas (Min) |
|-----------|-------------|-----------|----------------|--------------|----------------|
| Undertow | 250m | 1000m | 256Mi | 512Mi | 2 |
| Armeria | 250m | 1000m | 256Mi | 768Mi | 2 |
| Micronaut | 200m | 800m | 192Mi | 384Mi | 3 |
| Quarkus | 200m | 800m | 192Mi | 512Mi | 3 |
| Vert.x | 250m | 1000m | 320Mi | 768Mi | 2 |
| Javalin | 250m | 1000m | 256Mi | 512Mi | 3 |

**Note**: Adjust based on actual load testing results.

---

## Cloud Platforms

### AWS Deployment

#### AWS ECS Fargate

**Task Definition** (JSON):

```json
{
  "family": "undertow-baseline",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "containerDefinitions": [
    {
      "name": "app",
      "image": "your-registry/undertow-baseline:latest",
      "portMappings": [
        {
          "containerPort": 8088,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "JAVA_OPTS",
          "value": "-Xmx512m -Xms256m"
        }
      ],
      "healthCheck": {
        "command": [
          "CMD-SHELL",
          "curl -f http://localhost:8088/health || exit 1"
        ],
        "interval": 30,
        "timeout": 5,
        "retries": 3
      },
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/undertow-baseline",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
```

#### AWS EKS (Kubernetes)

```bash
# Create EKS cluster
eksctl create cluster \
  --name micro-apps \
  --region us-east-1 \
  --nodes 3 \
  --node-type t3.medium

# Deploy application
kubectl apply -f k8s/undertow-baseline/
```

#### AWS Lambda (Serverless - Quarkus Native only)

```bash
# Build native image
cd quarkus-rest
mvn package -Pnative -Dquarkus.native.container-build=true

# Deploy with SAM or Serverless Framework
sam deploy --guided
```

**Estimated Cost (10k req/s sustained)** - These are rough estimates and will vary:
- ECS Fargate: ~$15.50/month (1 task, 0.5 vCPU, 1 GB)
- EKS: ~$73/month (cluster) + ~$30/month (nodes)
- Lambda (Quarkus Native): ~$5/month (1M invocations)

### Google Cloud Platform (GCP)

#### GCP Cloud Run

```bash
# Build and push image
gcloud builds submit --tag gcr.io/PROJECT_ID/undertow-baseline

# Deploy to Cloud Run
gcloud run deploy undertow-baseline \
  --image gcr.io/PROJECT_ID/undertow-baseline \
  --platform managed \
  --region us-central1 \
  --memory 512Mi \
  --cpu 1 \
  --min-instances 1 \
  --max-instances 10 \
  --allow-unauthenticated
```

#### GKE (Google Kubernetes Engine)

```bash
# Create GKE cluster
gcloud container clusters create micro-apps \
  --zone us-central1-a \
  --num-nodes 3 \
  --machine-type n1-standard-2

# Deploy
kubectl apply -f k8s/undertow-baseline/
```

**Estimated Cost**: Cloud Run ~$12/month (1M requests), GKE ~$90/month (estimates only)

### Azure

#### Azure Container Apps

```bash
# Create container app
az containerapp create \
  --name undertow-baseline \
  --resource-group micro-apps-rg \
  --environment micro-apps-env \
  --image your-registry.azurecr.io/undertow-baseline:latest \
  --target-port 8088 \
  --ingress external \
  --min-replicas 1 \
  --max-replicas 10 \
  --cpu 0.5 \
  --memory 1.0Gi
```

#### AKS (Azure Kubernetes Service)

```bash
# Create AKS cluster
az aks create \
  --resource-group micro-apps-rg \
  --name micro-apps \
  --node-count 3 \
  --node-vm-size Standard_D2s_v3

# Deploy
kubectl apply -f k8s/undertow-baseline/
```

**Estimated Cost**: Container Apps ~$15/month, AKS ~$100/month (rough estimates)

---

## Observability

Complete observability stack available in [observability/](../observability/).

### Quick Setup

```bash
cd observability

# Start Prometheus, Grafana, Jaeger, Loki
docker compose up -d

# Access dashboards:
# - Grafana: http://localhost:3000 (admin/admin)
# - Prometheus: http://localhost:9090
# - Jaeger: http://localhost:16686
```

### Prometheus Metrics

**ServiceMonitor for Kubernetes**:

```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: undertow-baseline
  namespace: micro-apps
spec:
  selector:
    matchLabels:
      app: undertow-baseline
  endpoints:
  - port: http
    path: /metrics
    interval: 30s
```

### Grafana Dashboard

Pre-built dashboard showing:
- ✅ Requests per second by framework
- ✅ Latency (p50, p95, p99)
- ✅ Error rates
- ✅ Memory usage
- ✅ CPU usage
- ✅ JVM metrics (heap, GC)

**Import**: `observability/grafana/dashboards/micro-apps-overview.json`

### Distributed Tracing (Jaeger)

For frameworks with built-in tracing (Armeria, Quarkus, Micronaut):

```yaml
# Add to deployment
env:
- name: JAEGER_ENDPOINT
  value: "http://jaeger-collector:14268/api/traces"
- name: JAEGER_SERVICE_NAME
  value: "undertow-baseline"
```

### Alerting Rules

**prometheus/alerts.yml**:

```yaml
groups:
- name: micro-apps
  rules:
  - alert: HighLatency
    expr: http_request_duration_seconds{quantile="0.99"} > 0.1
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "High p99 latency on {{ $labels.app }}"

  - alert: HighErrorRate
    expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.05
    for: 5m
    labels:
      severity: critical
    annotations:
      summary: "High error rate on {{ $labels.app }}"
```

---

## Security Best Practices

### Container Security

```dockerfile
# Don't run as root
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -g 1000 appuser && \
    adduser -u 1000 -G appuser -s /bin/sh -D appuser

USER appuser
WORKDIR /app
COPY --chown=appuser:appuser target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Kubernetes Security

```yaml
# Pod Security
spec:
  securityContext:
    runAsNonRoot: true
    runAsUser: 1000
    fsGroup: 1000
  containers:
  - name: app
    securityContext:
      allowPrivilegeEscalation: false
      readOnlyRootFilesystem: true
      capabilities:
        drop:
        - ALL
```

### Network Policies

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: undertow-baseline
  namespace: micro-apps
spec:
  podSelector:
    matchLabels:
      app: undertow-baseline
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx
    ports:
    - protocol: TCP
      port: 8088
  egress:
  - to:
    - namespaceSelector:
        matchLabels:
          name: postgres
    ports:
    - protocol: TCP
      port: 5432
```

### Secrets Management

```yaml
# Use Kubernetes Secrets
apiVersion: v1
kind: Secret
metadata:
  name: db-credentials
  namespace: micro-apps
type: Opaque
stringData:
  username: postgres
  password: secure-password

# Reference in deployment
env:
- name: DB_PASSWORD
  valueFrom:
    secretKeyRef:
      name: db-credentials
      key: password
```

---

## Performance Tuning

### JVM Tuning

#### For Low Latency (Undertow, Armeria)

```bash
JAVA_OPTS="
  -Xmx512m -Xms512m
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=50
  -XX:+ParallelRefProcEnabled
  -XX:+UseStringDeduplication
  -XX:+AlwaysPreTouch
  -XX:+DisableExplicitGC
"
```

#### For High Throughput

```bash
JAVA_OPTS="
  -Xmx1g -Xms1g
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=200
  -XX:+UseContainerSupport
  -XX:MaxRAMPercentage=75.0
"
```

#### For Low Memory (Micronaut, Quarkus)

```bash
JAVA_OPTS="
  -Xmx256m -Xms128m
  -XX:+UseSerialGC
  -XX:MaxRAMPercentage=70.0
  -XX:+TieredCompilation
  -XX:TieredStopAtLevel=1
"
```

### Database Connection Pooling

**HikariCP Configuration**:

```properties
# Size based on concurrent requests
hikari.maximum-pool-size=20
hikari.minimum-idle=5
hikari.connection-timeout=30000
hikari.idle-timeout=600000
hikari.max-lifetime=1800000

# Performance tuning
hikari.leak-detection-threshold=60000
hikari.validation-timeout=5000
```

**Rule of Thumb**:
```
Pool Size = (Core Count × 2) + Disk Count
For 4 cores: 10-12 connections
```

### Kubernetes Resource Tuning

```yaml
# Based on actual usage (use metrics)
resources:
  requests:
    cpu: 250m      # 25% of 1 core
    memory: 256Mi  # Guaranteed memory
  limits:
    cpu: 1000m     # Max 1 core (burst)
    memory: 512Mi  # Hard limit (OOM if exceeded)
```

---

## Deployment Checklist

### Pre-Deployment

- [ ] Run performance tests locally
- [ ] Configure proper resource limits
- [ ] Set up health checks (liveness + readiness)
- [ ] Configure logging (JSON format for aggregation)
- [ ] Set up monitoring and alerts
- [ ] Review security settings
- [ ] Test auto-scaling behavior
- [ ] Document runbooks

### Post-Deployment

- [ ] Verify health endpoints responding
- [ ] Check pod/container logs for errors
- [ ] Verify metrics in Prometheus/Grafana
- [ ] Test auto-scaling triggers
- [ ] Perform load testing
- [ ] Verify database connections
- [ ] Test rollback procedure
- [ ] Update documentation

---

## Further Reading

- **[Kubernetes Manifests](../k8s/)** - Production-ready configs for all frameworks
- **[Observability Stack](../observability/)** - Prometheus, Grafana, Jaeger setup
- **[GraalVM Native](../GRAALVM_NATIVE.md)** - Native compilation for serverless
- **[Memory Analysis](../MEMORY_ANALYSIS.md)** - Optimize memory usage
- **[Benchmarks](BENCHMARKS.md)** - Performance analysis for sizing

---

**Last Updated**: August 25, 2026
