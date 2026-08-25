# Getting Started

This guide will help you get up and running with the benchmarks in just a few minutes. We'll walk through running your first framework and understanding the results.

---

## Prerequisites

Before you begin, ensure you have the following installed:

### Required
- **Java**: 19-21 (Eclipse Temurin or OpenJDK)
  ```bash
  # Check Java version
  java -version

  # Should show: openjdk version "21.x.x" or similar
  ```

- **Docker**: For containerized deployments
  ```bash
  # Check Docker
  docker --version
  docker compose version
  ```

### Optional (for benchmarking)
- **wrk**: HTTP benchmarking tool
  ```bash
  # Install on Ubuntu/Debian
  sudo apt install wrk

  # Install on macOS
  brew install wrk
  ```

- **Build Tools**: Maven 3.9+ or Gradle 8+ (optional, Docker builds included)
  ```bash
  # Check Maven
  mvn --version
  ```

---

## Quick Start (2 Minutes)

### 1. Clone the Repository

```bash
# Clone repository
git clone https://github.com/your-org/java-framework-showdown.git
cd java-framework-showdown
```

### 2. Run the Fastest Framework (Undertow)

```bash
# Navigate to Undertow
cd undertow-baseline

# Start with Docker Compose
docker compose up -d

# Wait 10 seconds for startup, then test
curl http://localhost:8088/health
```

**Expected Output**:
```json
{"status":"UP"}
```

That's it! You've just run the fastest framework from our benchmarks (37,808 req/s in our tests).

### 3. Quick Benchmark

```bash
# Run a quick performance test (if wrk installed)
wrk -t2 -c10 -d10s http://localhost:8088/health

# Expected: ~35k-38k requests/second
```

### 4. Cleanup

```bash
# Stop the container
docker compose down
```

---

## Try Different Frameworks

### Undertow (Fastest - 37,808 req/s)

```bash
cd undertow-baseline
docker compose up -d
curl http://localhost:8088/health
docker compose down
```

**Why choose**: Maximum performance, lowest latency

### Armeria (Feature-Rich - 35,810 req/s)

```bash
cd armeria-rest
docker compose up -d
curl http://localhost:8085/health
docker compose down
```

**Why choose**: Async RPC/REST, circuit breakers, distributed tracing

### Micronaut (Cloud-Native - 28,130 req/s)

```bash
cd micronaut-rest
docker compose up -d
curl http://localhost:8082/health
docker compose down
```

**Why choose**: Lowest memory (130 MB), best cloud cost efficiency

### Quarkus (Kubernetes-Native - 26,608 req/s)

```bash
cd quarkus-rest
docker compose up -d
curl http://localhost:8083/health
docker compose down
```

**Why choose**: Live reload, native compilation (<100ms startup)

### Javalin (Best Developer Experience - 19,853 req/s)

```bash
cd javalin-rest
docker compose up -d
curl http://localhost:8087/health
docker compose down
```

**Why choose**: Simple API, fastest time to market

---

## Running Performance Tests

### Test All Frameworks

```bash
cd performance-tests

# Run comprehensive benchmarks on all frameworks
./run-all-tests.sh

# This will:
# 1. Start each framework one by one
# 2. Run wrk benchmarks (low, medium, high concurrency)
# 3. Collect results in results/ directory
# 4. Generate comparison report

# View results
cat results/comparison-report.txt
```

### Test Individual Framework

```bash
cd performance-tests

# Test specific framework
./test-app.sh undertow-baseline 8088

# Arguments: <app-name> <port>
```

### Analyze Results

```bash
# Generate detailed analysis
./analyze-results.sh

# Clean up all test results
./cleanup.sh
```

---

## Exploring the Frameworks

### 1. Check Out the Code

Each framework directory contains:
```
framework-name/
├── src/                  # Source code
├── pom.xml              # Maven configuration (or build.gradle)
├── docker-compose.yml   # Docker deployment
├── Dockerfile           # Multi-stage build
└── README.md            # Framework-specific docs
```

**Example**: Explore Undertow
```bash
cd undertow-baseline
cat src/main/java/com/example/UndertowServer.java
```

### 2. Build from Source

```bash
# Navigate to any framework
cd undertow-baseline

# Build with Maven
mvn clean package

# Run the JAR
java -jar target/*.jar

# Test in another terminal
curl http://localhost:8088/health
```

### 3. Inspect Docker Images

```bash
# Build image
docker build -t undertow-test .

# Check image size
docker images undertow-test

# Run container
docker run -p 8088:8088 undertow-test
```

---

## Understanding the Results

### What to Look For

When comparing frameworks, consider:

1. **Throughput** (req/s): Higher is better
   - Top tier: 35k+ (Undertow, Armeria)
   - Production: 26-30k (Light4J, Micronaut, Vert.x, Quarkus)
   - Solid: 18-24k (Helidon, Javalin, Netty)

2. **Latency** (milliseconds): Lower is better
   - Excellent: <500μs (Undertow, Armeria)
   - Good: <1ms (Light4J, Vert.x, Micronaut, Quarkus)
   - Acceptable: <2ms (All frameworks at low concurrency)

3. **Memory** (MB): Lower is better (cloud cost)
   - Most efficient: Micronaut (130 MB), Quarkus (140 MB)
   - Standard: 165-210 MB (most frameworks)
   - Higher: Vert.x (240 MB) - offset by reactive performance

4. **JAR Size** (MB): Smaller is better (CI/CD speed)
   - Tiny: ActiveJ (5.3 MB), Undertow (7.1 MB)
   - Small: 8-12 MB (most frameworks)
   - Large: Armeria (28 MB) - includes gRPC, HTTP/2

### Reading Benchmark Output

```bash
wrk -t4 -c100 -d30s http://localhost:8088/health
```

**Sample Output**:
```
Running 30s test @ http://localhost:8088/health
  4 threads and 100 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     3.10ms    1.52ms  28.44ms   89.67%
    Req/Sec     9.45k     1.23k   12.34k    71.25%
  1134240 requests in 30.00s, 102.34MB read
Requests/sec:  37808.00
Transfer/sec:      3.41MB
```

**Key Metrics**:
- **Requests/sec**: 37,808 (throughput)
- **Latency Avg**: 3.10ms (p50 latency)
- **Latency Max**: 28.44ms (p100 latency)
- **Throughput/Thread**: ~9,450 req/s

---

## Next Steps

### For Developers

1. **Explore Framework Details**
   - Read [Frameworks Guide](FRAMEWORKS.md) for detailed info on all 11 frameworks

2. **Study Code Examples**
   - Check [Code Examples](CODE_EXAMPLES.md) for copy-paste snippets

3. **Dive into Implementation**
   - Browse source code in each framework directory
   - Understand database integration, error handling, health checks

### For Architects & Decision Makers

1. **Compare Performance**
   - Review [Benchmarks](BENCHMARKS.md) for detailed performance analysis

2. **Make a Decision**
   - Use [Decision Guide](DECISION_GUIDE.md) to choose the right framework

3. **Calculate ROI**
   - See [Migration Guides](../MIGRATION_GUIDES.md) for effort estimates and cost savings

### For DevOps Engineers

1. **Deploy to Kubernetes**
   - Explore [k8s/](../k8s/) for production-ready manifests

2. **Set Up Monitoring**
   - Check [observability/](../observability/) for Prometheus, Grafana, Jaeger setup

3. **Read Deployment Guide**
   - See [Deployment Guide](DEPLOYMENT.md) for Docker, K8s, cloud platforms

---

## Troubleshooting

### Port Already in Use

**Problem**: `Error: bind: address already in use`

**Solution**:
```bash
# Find process using port
lsof -i :8088

# Kill process
kill -9 <PID>

# Or use different port
docker run -p 9000:8088 framework-image
```

### Docker Compose Issues

**Problem**: `docker compose: command not found`

**Solution**:
```bash
# Use older syntax
docker-compose up -d

# Or install docker compose plugin
sudo apt install docker-compose-plugin
```

### Out of Memory

**Problem**: Container crashes with OOM error

**Solution**:
```bash
# Increase Docker memory limit (Docker Desktop)
# Settings → Resources → Memory → 4GB+

# Or set Java heap size
docker run -e JAVA_OPTS="-Xmx512m" framework-image
```

### Slow Build Times

**Problem**: Maven builds taking too long

**Solution**:
```bash
# Use Docker multi-stage builds (pre-configured)
docker build -t framework .

# Or skip tests
mvn clean package -DskipTests
```

### wrk Not Available

**Problem**: `wrk: command not found`

**Solution**:
```bash
# Ubuntu/Debian
sudo apt update && sudo apt install wrk

# macOS
brew install wrk

# Or build from source
git clone https://github.com/wg/wrk.git
cd wrk && make
sudo cp wrk /usr/local/bin/
```

---

## Common Questions

### Q: Which framework should I start with?

**A**: Depends on your goal:
- **Maximum performance**: Undertow
- **Cloud-native**: Micronaut or Quarkus
- **Easy to learn**: Javalin
- **Reactive**: Vert.x
- **Production-ready**: Light4J or Armeria

### Q: Can I use these in production?

**A**: Absolutely! Frameworks used in production:
- **Undertow**: Powers WildFly, used by Fortune 500
- **Armeria**: Handles billions of requests/day at Line
- **Quarkus**: Used by Red Hat customers globally
- **Micronaut**: Deployed at major enterprises
- **Light4J**: Proven in financial services

### Q: How do I migrate from Spring Boot?

**A**: See [Migration Guides](../MIGRATION_GUIDES.md):
- **Spring Boot → Micronaut**: 1-2 days, 90% similar
- **Spring Boot → Quarkus**: 2-3 days, 85% similar
- Both guides include code comparisons and examples

### Q: Are these benchmarks realistic?

**A**: Yes, because:
- ✅ Tested on identical hardware
- ✅ Real-world endpoints (/health, database queries)
- ✅ Controlled environment (Docker)
- ✅ Multiple concurrency levels
- ✅ Reproducible (run tests yourself!)

### Q: Can I contribute?

**A**: Absolutely! See [Contributing Guide](../CONTRIBUTING.md) for:
- Adding new frameworks
- Improving benchmarks
- Fixing bugs
- Enhancing documentation

---

## Quick Reference

### Framework Ports

| Framework | Port |
|-----------|------|
| Vert.x | 8081 |
| Micronaut | 8082 |
| Quarkus | 8083 |
| Light4J | 8084 |
| Armeria | 8085 |
| Helidon | 8086 |
| Javalin | 8087 |
| Undertow | 8088 |
| Netty | 8089 |
| ActiveJ | 8090 |

### Essential Commands

```bash
# Start framework
docker compose up -d

# View logs
docker compose logs -f

# Test health endpoint
curl http://localhost:<PORT>/health

# Stop framework
docker compose down

# Run benchmark
wrk -t4 -c100 -d30s http://localhost:<PORT>/health

# Build from source
mvn clean package

# Run JAR
java -jar target/*.jar
```

---

## Resources

### Documentation
- [Frameworks Guide](FRAMEWORKS.md) - All 11 frameworks in detail
- [Benchmarks](BENCHMARKS.md) - Performance analysis with charts
- [Decision Guide](DECISION_GUIDE.md) - Choose the right framework
- [Deployment](DEPLOYMENT.md) - Production deployment guides

### Advanced Topics
- [GraalVM Native](../GRAALVM_NATIVE.md) - Native compilation (100x faster startup)
- [Memory Analysis](../MEMORY_ANALYSIS.md) - Memory usage deep dive
- [Migration Guides](../MIGRATION_GUIDES.md) - Step-by-step migrations

### Production Resources
- [Kubernetes Manifests](../k8s/) - Production K8s configs
- [Observability Stack](../observability/) - Prometheus, Grafana, Jaeger
- [Performance Tests](../performance-tests/) - Benchmark methodology

---

## Need Help?

- 💬 **Discussions**: [GitHub Discussions](https://github.com/your-org/micro-apps/discussions)
- 🐛 **Issues**: [Report bugs or request features](https://github.com/your-org/micro-apps/issues)
- 📧 **Contact**: For business inquiries or collaborations

---

---

**Last Updated**: August 25, 2026
