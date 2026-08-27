# Observability Stack for Java Frameworks

Complete monitoring, logging, and tracing solution for all Java microservice frameworks.

## Stack Components

| Component | Purpose | Port | UI |
|-----------|---------|------|-----|
| **Prometheus** | Metrics collection & storage | 9090 | http://localhost:9090 |
| **Grafana** | Metrics visualization | 3000 | http://localhost:3000 |
| **Jaeger** | Distributed tracing | 16686 | http://localhost:16686 |
| **Loki** | Log aggregation | 3100 | (via Grafana) |
| **Tempo** | Trace storage | 3200 | (via Grafana) |
| **AlertManager** | Alert routing | 9093 | http://localhost:9093 |
| **Node Exporter** | Host metrics | 9100 | - |
| **cAdvisor** | Container metrics | 8080 | http://localhost:8080 |

## Quick Start

### 1. Start Observability Stack

```bash
cd observability
docker-compose up -d

# Verify all services are running
docker-compose ps

# View logs
docker-compose logs -f
```

### 2. Access UIs

**Grafana** (Visualization):
```
URL: http://localhost:3000
User: admin
Password: admin
```

**Prometheus** (Metrics):
```
URL: http://localhost:9090
```

**Jaeger** (Tracing):
```
URL: http://localhost:16686
```

### 3. Start Applications

```bash
# Start applications to monitor
cd ../light4j-rest-app
docker compose up -d

cd ../undertow-baseline
docker compose up -d

# Prometheus will automatically discover and scrape them
```

### 4. View Dashboards

1. Open Grafana: http://localhost:3000
2. Navigate to Dashboards
3. Open "Java Framework Performance Overview"
4. See real-time metrics for all frameworks

## Architecture

```
┌─────────────────────┐
│   Java Framework    │ (Undertow, Armeria, Quarkus, etc.)
│  Applications       │
└────────┬────────────┘
         │ /metrics endpoint
         ▼
┌─────────────────┐      ┌──────────────┐
│   Prometheus    │─────▶│  Grafana     │ ◀── User
│  (Scraper)      │      │  (Viz)       │
└─────────┬───────┘      └──────────────┘
          │
          │ alerts
          ▼
┌─────────────────┐
│  AlertManager   │─────▶ Slack/Email/PagerDuty
└─────────────────┘

┌─────────────────┐      ┌──────────────┐
│  Applications   │─────▶│    Jaeger    │
│  (OpenTelemetry)│traces│  (Tracing)   │
└─────────────────┘      └──────────────┘

┌─────────────────┐      ┌──────────────┐      ┌──────────────┐
│  Applications   │─────▶│   Promtail   │─────▶│     Loki     │
│  (Logs)         │ logs │  (Shipper)   │      │  (Storage)   │
└─────────────────┘      └──────────────┘      └──────────────┘
```

## Metrics Available

### Application Metrics

All frameworks expose standard metrics:

**HTTP Metrics**:
- `http_requests_total` - Total requests (by status, method, path)
- `http_request_duration_seconds` - Request latency histogram
- `http_requests_in_flight` - Current active requests

**JVM Metrics**:
- `jvm_memory_used_bytes` - Memory usage by pool
- `jvm_gc_pause_seconds` - GC pause times
- `jvm_threads_current` - Thread count
- `jvm_classes_loaded` - Loaded classes

**Database Metrics** (HikariCP):
- `hikari_connections_active` - Active connections
- `hikari_connections_idle` - Idle connections
- `hikari_connections_pending` - Pending requests
- `hikari_connection_timeout_total` - Timeouts

### Framework-Specific Metrics

**Undertow**:
- `undertow_worker_threads_active` - Worker threads in use
- `undertow_worker_threads_max` - Max worker threads
- `undertow_requests_active` - Active requests

**Armeria**:
- `armeria_server_requests_total` - Total requests
- `armeria_server_request_duration_seconds` - Latencies
- `armeria_server_active_requests` - In-flight

**Quarkus**:
- Micrometer metrics (extensive)
- RESTEasy metrics
- Hibernate metrics (if using)

**Micronaut**:
- Micrometer metrics
- HTTP client/server metrics
- Datasource metrics

**Vert.x**:
- `vertx_eventloop_delay_seconds` - Event loop lag
- `vertx_http_server_requests_total` - Requests
- `vertx_http_server_active_connections` - Connections

## Pre-Built Dashboards

### 1. Java Framework Performance Overview

Compares all frameworks side-by-side:
- Throughput (requests/sec)
- Latency (p50, p95, p99)
- Memory usage
- CPU usage
- Error rates
- Performance ranking

### 2. Framework Deep Dive (per framework)

Detailed metrics for each:
- Request patterns
- Latency distribution
- Resource utilization
- Database performance
- JVM internals

### 3. Database Performance

- Query execution times
- Connection pool health
- Slow query detection
- Transaction rates

### 4. JVM Monitoring

- Heap usage over time
- GC frequency and duration
- Thread activity
- Class loading

### 5. SLO Dashboard

- Availability percentage
- Latency SLOs (p99 < Xms)
- Error budget tracking
- SLO breach history

## Alerting

### Alert Severity Levels

**Critical** (Page immediately):
- Application completely down
- SLO availability breach
- Connection pool exhausted
- Event loop blocked (Vert.x)

**Warning** (Investigate during business hours):
- High error rate (>5%)
- High latency (p99 > threshold)
- Approaching capacity
- Resource saturation

**Info** (Monitor):
- Deployment events
- Configuration changes
- Scaling events

### Example Alerts

#### High Latency Alert (Undertow)

```yaml
alert: UndertowHighLatency
expr: histogram_quantile(0.99, rate(http_request_duration_seconds_bucket{application="undertow-baseline"}[5m])) > 0.010
for: 5m
annotations:
  summary: "Undertow p99 latency high"
  description: "Undertow p99 latency is {{ $value }}s (threshold: 10ms)"
```

**Why**: Undertow normally has sub-millisecond latency. >10ms p99 indicates issues.

#### Near Capacity Alert (Armeria)

```yaml
alert: ArmeriaNearCapacity
expr: rate(http_requests_total{application="armeria-rest"}[1m]) > 28000
for: 2m
annotations:
  summary: "Armeria approaching capacity"
  description: "Armeria handling {{ $value }} req/s (capacity: 35k req/s)"
```

**Why**: Armeria handles 35k req/s. Alert at 80% capacity to scale proactively.

### Alert Routing (AlertManager)

```yaml
# alertmanager.yml
route:
  receiver: 'team-slack'
  group_by: ['alertname', 'application']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 12h

  routes:
    # Critical alerts to PagerDuty
    - match:
        severity: critical
      receiver: 'pagerduty'
      continue: true

    # Performance alerts to Slack
    - match:
        category: performance
      receiver: 'perf-slack'

    # Database alerts to DBA team
    - match:
        category: database
      receiver: 'dba-email'

receivers:
  - name: 'team-slack'
    slack_configs:
      - api_url: 'YOUR_SLACK_WEBHOOK'
        channel: '#alerts'
        text: '{{ range .Alerts }}{{ .Annotations.description }}{{ end }}'

  - name: 'pagerduty'
    pagerduty_configs:
      - service_key: 'YOUR_PAGERDUTY_KEY'

  - name: 'dba-email'
    email_configs:
      - to: 'dba@example.com'
```

## Distributed Tracing

### Enable Tracing in Applications

**Quarkus** (automatic with extension):
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-opentelemetry</artifactId>
</dependency>
```

```properties
quarkus.otel.exporter.otlp.traces.endpoint=http://jaeger:4317
quarkus.otel.service.name=quarkus-rest
```

**Micronaut**:
```yaml
micronaut:
  application:
    name: micronaut-rest
tracing:
  jaeger:
    enabled: true
    sampler:
      probability: 1.0
    sender:
      agentHost: jaeger
      agentPort: 6831
```

**Manual (OpenTelemetry SDK)**:
```java
// Initialize tracer
OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
    .setTracerProvider(
        SdkTracerProvider.builder()
            .addSpanProcessor(
                BatchSpanProcessor.builder(
                    OtlpGrpcSpanExporter.builder()
                        .setEndpoint("http://jaeger:4317")
                        .build()
                ).build()
            )
            .build()
    )
    .build();

Tracer tracer = openTelemetry.getTracer("undertow-baseline");

// Create spans
Span span = tracer.spanBuilder("handleRequest").startSpan();
try (Scope scope = span.makeCurrent()) {
    // Your code here
    span.setAttribute("http.method", "GET");
    span.setAttribute("http.route", "/api/users");
} finally {
    span.end();
}
```

### Viewing Traces

1. Open Jaeger UI: http://localhost:16686
2. Select service (e.g., "undertow-baseline")
3. Click "Find Traces"
4. Select trace to see:
   - Request flow across services
   - Span duration breakdown
   - Error details
   - Tags and logs

### Common Trace Patterns

**Database Query Trace**:
```
undertow-baseline: GET /api/users [150ms]
  ├─ HTTP Handler [145ms]
  │  ├─ Database Query [140ms]
  │  │  ├─ Connection acquire [2ms]
  │  │  ├─ Query execute [135ms] ← Slow!
  │  │  └─ Connection release [1ms]
  │  └─ JSON serialization [5ms]
  └─ Response write [1ms]
```

**Service-to-Service**:
```
API Gateway [200ms]
  ├─ Auth Service [50ms]
  │  └─ Token validation [45ms]
  ├─ User Service [120ms]
  │  └─ Database query [115ms]
  └─ Response formatting [5ms]
```

## Log Aggregation with Loki

### Configure Application Logging

**JSON Structured Logging**:
```java
// Logback configuration
<configuration>
    <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"app":"undertow-baseline","framework":"undertow"}</customFields>
        </encoder>
    </appender>
    <root level="INFO">
        <appender-ref ref="JSON" />
    </root>
</configuration>
```

### Query Logs in Grafana

```logql
# All logs from undertow
{app="undertow-baseline"}

# Error logs only
{app="undertow-baseline"} |= "ERROR"

# Slow requests (>1s)
{app="undertow-baseline"} | json | duration > 1000

# Logs correlated with trace ID
{app="undertow-baseline"} | json | trace_id="abc123"
```

### Log-based Metrics

Create metrics from logs:
```logql
# Count errors per minute
rate({app=~".*-rest|.*-baseline"} |= "ERROR" [1m])

# P95 request duration from logs
quantile_over_time(0.95, {app="undertow-baseline"} | json | unwrap duration [5m])
```

## Query Examples

### Prometheus PromQL

**Top 5 frameworks by throughput**:
```promql
topk(5, rate(http_requests_total[5m]))
```

**Latency comparison (p99)**:
```promql
histogram_quantile(0.99,
  sum by (application, le) (
    rate(http_request_duration_seconds_bucket[5m])
  )
)
```

**Memory usage by framework**:
```promql
container_memory_usage_bytes{name=~".*-rest|.*-baseline"} / 1024 / 1024
```

**Requests per second (1h average)**:
```promql
avg_over_time(rate(http_requests_total[5m])[1h:])
```

**Error rate percentage**:
```promql
sum(rate(http_requests_total{status=~"5.."}[5m]))
/
sum(rate(http_requests_total[5m]))
* 100
```

## Best Practices

### Metrics

1. ✅ **Use histograms for latency** (not gauges/counters)
2. ✅ **Label cardinality**: Keep labels finite (avoid user IDs, request IDs)
3. ✅ **Naming convention**: `{namespace}_{subsystem}_{name}_{unit}`
4. ✅ **Scrape interval**: 15-30s for most apps
5. ✅ **Retention**: 30 days local, longer in remote storage

### Tracing

1. ✅ **Sample rate**: 100% in dev, 1-10% in production
2. ✅ **Propagate context**: Use standard headers (W3C Trace Context)
3. ✅ **Tag everything**: Service, version, environment, region
4. ✅ **Don't log sensitive data**: PII, credentials, tokens
5. ✅ **Head-based sampling**: Sample at entry point, not per-span

### Logging

1. ✅ **Structured logs**: JSON format for parsing
2. ✅ **Log levels**: ERROR (alerts), WARN (investigate), INFO (normal), DEBUG (verbose)
3. ✅ **Include context**: Trace ID, span ID, user ID, request ID
4. ✅ **Don't log secrets**: Redact passwords, API keys, tokens
5. ✅ **Retention**: 7-30 days depending on compliance

### Alerting

1. ✅ **Alert on symptoms**, not causes (e.g., high latency, not high CPU)
2. ✅ **SLO-based alerts**: Focus on user experience
3. ✅ **Actionable alerts**: Include runbook link in description
4. ✅ **Avoid alert fatigue**: Tune thresholds, group similar alerts
5. ✅ **Test alerts**: Trigger manually to verify routing

## Troubleshooting

### Prometheus Not Scraping

```bash
# Check Prometheus targets
curl http://localhost:9090/api/v1/targets

# Verify app exposes metrics
curl http://localhost:8088/metrics

# Check Prometheus logs
docker-compose logs prometheus
```

### Grafana Shows No Data

1. Verify datasource connection: Configuration → Data Sources → Test
2. Check time range (top-right)
3. Verify metrics exist in Prometheus: http://localhost:9090/graph
4. Check dashboard queries for errors

### Jaeger No Traces

1. Verify app sends traces: Check app logs for OTLP export errors
2. Check Jaeger collector: `docker-compose logs jaeger`
3. Verify endpoint: App should send to `http://jaeger:4317` (gRPC) or `:14268` (HTTP)
4. Check sampling rate: Might be sampling out all traces

### High Cardinality Issues

**Symptom**: Prometheus using too much memory

**Fix**: Reduce label cardinality
```yaml
# Bad: User ID in label (millions of unique values)
http_requests_total{user_id="12345"}

# Good: Aggregate without user ID
http_requests_total{endpoint="/api/users"}
```

## Cost Optimization

### Metrics

- **Reduce retention**: 30d → 15d saves 50% storage
- **Remote write**: Use Cortex/Thanos for long-term cheaper storage
- **Sample less frequently**: 15s → 30s scrape interval

### Traces

- **Lower sample rate**: 100% → 1% in production (99% cost reduction)
- **Tail-based sampling**: Keep only interesting traces (errors, slow requests)
- **Shorter retention**: 7 days is usually enough

### Logs

- **Log less**: INFO → WARN in production
- **Structured logging**: More efficient than parsing
- **Retention tiers**: Hot (7d) → Warm (30d) → Cold (90d)

## Production Checklist

- [ ] Prometheus scraping all apps
- [ ] Grafana dashboards configured
- [ ] AlertManager routing configured
- [ ] PagerDuty/Slack integration tested
- [ ] SLO alerts defined (availability, latency)
- [ ] Distributed tracing enabled
- [ ] Log aggregation working
- [ ] Retention policies set
- [ ] Backup strategy for Prometheus data
- [ ] Runbooks linked in alert annotations

---

**Last Updated**: August 24, 2026
**Stack Version**: Prometheus 2.47, Grafana 10.1, Jaeger 1.50
