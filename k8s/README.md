# Kubernetes Production Manifests

Production-ready Kubernetes configurations for all micro-apps frameworks, optimized based on actual performance test results.

## Overview

Each framework has been analyzed for optimal resource allocation, scaling behavior, and production patterns. Configurations are tuned for:
- **Resource efficiency**: Based on real memory/CPU usage
- **Performance**: Optimized for framework-specific characteristics
- **High availability**: Pod disruption budgets, anti-affinity
- **Observability**: Prometheus metrics, health checks
- **Security**: Non-root containers, read-only filesystems, security contexts

## Quick Start

### Deploy Undertow (Fastest Framework)

```bash
# Create namespace
kubectl create namespace micro-apps

# Apply secrets (update with your values)
kubectl apply -f undertow-baseline/secrets.yaml -n micro-apps

# Deploy application
kubectl apply -f undertow-baseline/ -n micro-apps

# Verify deployment
kubectl get pods -n micro-apps -l app=undertow-baseline
kubectl get svc -n micro-apps undertow-baseline

# Check auto-scaling
kubectl get hpa -n micro-apps undertow-baseline-hpa
```

### Deploy Armeria (High-Performance Async)

```bash
kubectl apply -f armeria-rest/ -n micro-apps
```

### Deploy Micronaut (Cloud-Native)

```bash
kubectl apply -f micronaut-rest/ -n micro-apps
```

### Deploy Quarkus (Kubernetes-Native)

```bash
kubectl apply -f quarkus-rest/ -n micro-apps
```

## Resource Allocation Strategy

Based on performance test results, each framework has optimized resource limits:

| Framework | Memory Request | Memory Limit | CPU Request | CPU Limit | Rationale |
|-----------|---------------|--------------|-------------|-----------|-----------|
| **Undertow** | 200 Mi | 512 Mi | 500m | 1000m | Fastest (37k req/s), low memory |
| **Armeria** | 220 Mi | 512 Mi | 500m | 1000m | High perf (35k req/s), async |
| **Light4J** | 180 Mi | 512 Mi | 500m | 1000m | Balanced (29k req/s) |
| **Micronaut** | 140 Mi | 384 Mi | 250m | 750m | Low memory, compile-time DI |
| **Quarkus** | 150 Mi | 384 Mi | 250m | 750m | K8s-native, fast startup |
| **Vert.x** | 200 Mi | 512 Mi | 500m | 1000m | Reactive, high concurrency |
| **Helidon** | 180 Mi | 512 Mi | 500m | 1000m | Oracle, MicroProfile |
| **Javalin** | 200 Mi | 512 Mi | 400m | 800m | Developer-friendly |

## Auto-Scaling Configuration

HPA (Horizontal Pod Autoscaler) settings are tuned per framework:

### High-Performance Frameworks (Undertow, Armeria)

```yaml
minReplicas: 3
maxReplicas: 20
targetCPU: 60%           # Scale at 60% CPU
targetMemory: 70%        # Scale at 70% memory
customMetric: 20000 req/s  # Scale before hitting capacity
```

**Rationale**: These can handle 35k+ req/s per pod, so fewer replicas needed. Scale conservatively to avoid over-provisioning.

### Cloud-Native Frameworks (Micronaut, Quarkus)

```yaml
minReplicas: 5
maxReplicas: 50
targetCPU: 70%
targetMemory: 75%
customMetric: 15000 req/s
```

**Rationale**: Faster startup times allow aggressive scaling. Higher replica counts provide better distribution.

### Reactive Frameworks (Vert.x)

```yaml
minReplicas: 3
maxReplicas: 30
targetCPU: 65%
targetMemory: 70%
customMetric: 18000 req/s
```

**Rationale**: Event-loop model handles high concurrency efficiently. Medium scaling aggressiveness.

## Health Check Tuning

Probe timings based on actual startup times from performance tests:

| Framework | Startup Time | initialDelaySeconds | periodSeconds | Notes |
|-----------|-------------|---------------------|---------------|-------|
| Quarkus (Native) | 15ms | 1 | 5 | Native image: instant startup |
| Micronaut | 1.2s | 3 | 5 | Fast startup, aggressive probes |
| Light4J | 1.8s | 5 | 10 | Standard startup |
| Undertow | 1.8s | 5 | 10 | Fast and stable |
| Vert.x | 2.1s | 5 | 10 | Reactive initialization |
| Helidon | 2.5s | 7 | 10 | Heavier framework |

## Networking Configuration

### Service Types

**LoadBalancer** (External access):
```yaml
type: LoadBalancer
# Use for production APIs
# Cloud provider creates external LB
```

**ClusterIP** (Internal only):
```yaml
type: ClusterIP
# Use for internal services
# No external exposure
```

**Headless** (For StatefulSets):
```yaml
clusterIP: None
# Use for service discovery
# Direct pod-to-pod communication
```

### Ingress Configuration

All frameworks include ingress manifests with:
- TLS termination (Let's Encrypt via cert-manager)
- Rate limiting
- Connection limits
- Optimized timeouts
- Keepalive connections

**For Nginx Ingress**:
```yaml
nginx.ingress.kubernetes.io/limit-rps: "1000"
nginx.ingress.kubernetes.io/upstream-keepalive-connections: "100"
```

**For AWS ALB**:
```yaml
alb.ingress.kubernetes.io/scheme: internet-facing
alb.ingress.kubernetes.io/target-type: ip
```

## Observability

### Prometheus Monitoring

All deployments include ServiceMonitor for Prometheus scraping:

```yaml
endpoints:
- port: metrics
  interval: 15s
  path: /metrics
```

**Exposed Metrics**:
- HTTP request rate
- Request duration (p50, p95, p99)
- Active connections
- Database connection pool stats
- JVM memory/GC metrics
- Framework-specific metrics

### Logging

Structured JSON logging to stdout/stderr:
```yaml
env:
- name: LOG_LEVEL
  value: "INFO"
- name: LOG_FORMAT
  value: "JSON"
```

**Log aggregation**: Compatible with:
- ELK Stack (Elasticsearch, Logstash, Kibana)
- Loki + Grafana
- CloudWatch Logs
- Datadog

### Distributed Tracing

Frameworks with built-in support:
- **Quarkus**: OpenTelemetry
- **Micronaut**: OpenTelemetry
- **Armeria**: Brave/Zipkin

Configure via environment:
```yaml
env:
- name: OTEL_EXPORTER_OTLP_ENDPOINT
  value: "http://jaeger-collector:4317"
- name: OTEL_SERVICE_NAME
  value: "undertow-baseline"
```

## Security Best Practices

All manifests implement:

### Container Security
```yaml
securityContext:
  runAsNonRoot: true
  runAsUser: 1001
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
  name: undertow-netpol
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
          name: database
    ports:
    - protocol: TCP
      port: 5432
```

### Secrets Management

Use Kubernetes secrets or external secret managers:

```bash
# Create database secret
kubectl create secret generic undertow-secrets \
  --from-literal=database-url=jdbc:postgresql://db:5432/mydb \
  --from-literal=database-user=admin \
  --from-literal=database-password=changeme \
  -n micro-apps

# Or use external secrets operator (AWS Secrets Manager, etc.)
```

## High Availability

### Pod Disruption Budgets

Ensures minimum availability during:
- Node drains
- Cluster upgrades
- Voluntary disruptions

```yaml
minAvailable: 2  # Always keep 2 pods running
```

### Anti-Affinity Rules

Spreads pods across nodes:

```yaml
podAntiAffinity:
  preferredDuringSchedulingIgnoredDuringExecution:
  - weight: 100
    podAffinityTerm:
      topologyKey: kubernetes.io/hostname
```

**Soft anti-affinity**: Prefers spreading but doesn't require it (allows small clusters).

### Multi-Zone Deployment

For production, use topology spread constraints:

```yaml
topologySpreadConstraints:
- maxSkew: 1
  topologyKey: topology.kubernetes.io/zone
  whenUnsatisfiable: DoNotSchedule
  labelSelector:
    matchLabels:
      app: undertow-baseline
```

## Cost Optimization

### Vertical Pod Autoscaler (VPA)

Automatically adjusts resource requests:

```yaml
apiVersion: autoscaling.k8s.io/v1
kind: VerticalPodAutoscaler
metadata:
  name: undertow-vpa
spec:
  targetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: undertow-baseline
  updatePolicy:
    updateMode: "Auto"
  resourcePolicy:
    containerPolicies:
    - containerName: undertow-baseline
      minAllowed:
        cpu: 250m
        memory: 128Mi
      maxAllowed:
        cpu: 2000m
        memory: 1Gi
```

### Cluster Autoscaler

Works with HPA to scale nodes:
- HPA scales pods
- Cluster Autoscaler adds nodes when pods are pending
- Removes nodes when underutilized

### Spot Instances

For cost savings (non-critical workloads):

```yaml
nodeSelector:
  node.kubernetes.io/instance-type: spot

tolerations:
- key: "spot"
  operator: "Equal"
  value: "true"
  effect: "NoSchedule"
```

## Performance Testing in K8s

### Load Testing from within cluster

```bash
# Deploy load generator
kubectl run -it --rm load-test \
  --image=williamyeh/wrk \
  --restart=Never \
  -- -t 4 -c 100 -d 30s \
  http://undertow-baseline.micro-apps.svc.cluster.local/health

# Or use fortio for percentile analysis
kubectl run -it --rm fortio \
  --image=fortio/fortio \
  --restart=Never \
  -- load -t 30s -c 100 -qps 10000 \
  http://undertow-baseline.micro-apps.svc.cluster.local/health
```

### Chaos Engineering

Test resilience:

```bash
# Install chaos-mesh
kubectl apply -f https://mirrors.chaos-mesh.org/latest/chaos-mesh.yaml

# Test pod failure
apiVersion: chaos-mesh.org/v1alpha1
kind: PodChaos
metadata:
  name: pod-failure-test
spec:
  action: pod-failure
  mode: one
  duration: "30s"
  selector:
    namespaces:
      - micro-apps
    labelSelectors:
      app: undertow-baseline
```

## GitOps Deployment

### ArgoCD Application

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: undertow-baseline
  namespace: argocd
spec:
  project: micro-apps
  source:
    repoURL: https://github.com/your-org/micro-apps
    targetRevision: main
    path: k8s/undertow-baseline
  destination:
    server: https://kubernetes.default.svc
    namespace: micro-apps
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
    syncOptions:
    - CreateNamespace=true
```

### Flux Configuration

```yaml
apiVersion: kustomize.toolkit.fluxcd.io/v1beta2
kind: Kustomization
metadata:
  name: undertow-baseline
  namespace: flux-system
spec:
  interval: 5m
  path: ./k8s/undertow-baseline
  prune: true
  sourceRef:
    kind: GitRepository
    name: micro-apps
```

## Framework-Specific Notes

### Undertow Baseline
- **Fastest**: 37,808 req/s per pod
- **Pod capacity**: ~25k req/s recommended (leaves headroom)
- **Replicas**: Fewer needed due to high performance
- **Cost**: Lowest per-request cost

### Armeria REST
- **Async**: Non-blocking I/O, excellent concurrency
- **Features**: Circuit breakers, metrics built-in
- **Replicas**: 3-20 handles most workloads
- **Best for**: RPC/gRPC + REST hybrid

### Micronaut REST
- **Memory**: Lowest footprint (140 MB request)
- **Startup**: 1.2s, great for auto-scaling
- **Pod density**: Highest pods/node
- **Best for**: Cost-sensitive deployments

### Quarkus REST
- **Native**: 15ms startup with GraalVM
- **K8s native**: Best K8s integration
- **Extensions**: Rich ecosystem
- **Best for**: Modern cloud-native apps

## Troubleshooting

### Pods Not Starting

```bash
# Check pod status
kubectl get pods -n micro-apps

# Describe pod for events
kubectl describe pod <pod-name> -n micro-apps

# Check logs
kubectl logs <pod-name> -n micro-apps

# Common issues:
# - Insufficient resources (adjust requests/limits)
# - Image pull errors (check image name/registry)
# - Health check failures (adjust probe timing)
```

### High Memory Usage

```bash
# Check actual usage
kubectl top pods -n micro-apps

# If higher than expected:
# 1. Check for memory leaks (heap dump)
# 2. Adjust -XX:MaxRAMPercentage
# 3. Enable GC logging
# 4. Consider VPA for auto-tuning
```

### Performance Issues

```bash
# Check HPA status
kubectl get hpa -n micro-apps

# View metrics
kubectl describe hpa undertow-baseline-hpa -n micro-apps

# If not scaling:
# 1. Verify metrics-server is running
# 2. Check custom metrics (Prometheus adapter)
# 3. Review scaling thresholds
```

## Best Practices Summary

1. ✅ **Right-size resources**: Use actual performance data
2. ✅ **Set PDBs**: Ensure availability during disruptions
3. ✅ **Enable monitoring**: ServiceMonitor + dashboards
4. ✅ **Use HPA**: Auto-scale based on load
5. ✅ **Implement health checks**: Tune for framework startup
6. ✅ **Apply security contexts**: Run as non-root
7. ✅ **Use anti-affinity**: Spread across nodes/zones
8. ✅ **Configure ingress**: Rate limiting, TLS, timeouts
9. ✅ **Enable logging**: Structured JSON to stdout
10. ✅ **Test at scale**: Load test in staging

---

**Last Updated**: August 24, 2026
**Kubernetes Version**: 1.28+
**Tested on**: EKS, GKE, AKS
