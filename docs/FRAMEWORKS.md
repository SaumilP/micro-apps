# Framework Catalog

This guide covers all 11 Java frameworks we've benchmarked. Each framework has different strengths and trade-offs, so we've included detailed information to help you understand which one might work best for your use case.

**Note**: JAR sizes marked with `~` are estimates based on typical builds. Actual sizes will vary depending on your dependencies and build configuration.

## Quick Comparison Matrix

| Framework | Throughput | Latency | JAR Size | Best For | Ranking |
|-----------|-----------|---------|----------|----------|---------|
| **Undertow** | 37,808 req/s | 379μs | 7.1 MB | Maximum performance | ⭐⭐⭐ |
| **Armeria** | 35,810 req/s | 435μs | 28 MB | Async RPC/REST | ⭐⭐⭐ |
| **Light4J** | 29,583 req/s | 514μs | 12 MB | Production APIs | ⭐⭐ |
| **Micronaut** | 28,130 req/s | 675μs | ~14 MB | Cloud-native | ⭐⭐ |
| **Vert.x** | 28,073 req/s | 603μs | ~9 MB | Reactive systems | ⭐⭐ |
| **Quarkus** | 26,608 req/s | 736μs | ~15 MB | Kubernetes-native | ⭐⭐ |
| **Helidon** | 23,340 req/s | 950μs | ~11 MB | MicroProfile | ⭐ |
| **Javalin** | 19,853 req/s | 1.30ms | 9.2 MB | Developer experience | ⭐ |
| **Netty** | 18,159 req/s | 728μs | 8.5 MB | Educational baseline | ⭐ |
| **ActiveJ** | Testing | - | 5.3 MB | Minimal footprint | - |
| **Spring Lite** | N/A | N/A | 9.5 KB | Learning DI concepts | Educational |

---

## 1. Undertow Baseline

### Overview
Pure Undertow implementation demonstrating exceptional raw performance. Built by JBoss/Red Hat, Undertow is a flexible, high-performance web server that powers WildFly and is used in production by many Fortune 500 companies.

### Key Metrics
- **Throughput**: 37,808 req/s (medium concurrency) ⭐ **Fastest**
- **Latency**: 379μs avg (low concurrency) ⭐ **Lowest**
- **Database Throughput**: 14,177 req/s
- **JAR Size**: 7.1 MB
- **Memory**: 180 MB
- **Startup Time**: ~2 seconds

### Features
- Undertow embedded server
- Exceptional throughput and ultra-low latency
- Lightweight deployment
- Production-grade stability
- PostgreSQL + HikariCP integration
- Zero reflection overhead
- Non-blocking I/O

### Technology Stack
- **Java**: 21
- **Build**: Maven
- **Database**: PostgreSQL
- **Connection Pool**: HikariCP
- **Docker Image**: 232 MB

### Best Use Cases
- ✅ Maximum performance requirements
- ✅ Ultra-low latency APIs
- ✅ High-throughput data processing
- ✅ Cost-optimized cloud deployments
- ✅ Microservices requiring minimal resources

### Trade-offs
- ⚠️ Manual observability setup required
- ⚠️ Less built-in cloud-native features
- ⚠️ Lower-level API (more code to write)

### Quick Start
```bash
cd undertow-baseline
mvn clean package
docker compose up -d
curl http://localhost:8088/health
# Response: {"status":"UP"}
```

[Full Documentation](../undertow-baseline/README.md)

---

## 2. Armeria REST

### Overview
High-performance asynchronous RPC/REST framework built on Netty by Line Corporation. Proven in production handling billions of requests per day at Line, one of Asia's largest messaging platforms.

### Key Metrics
- **Throughput**: 35,810 req/s ⭐ **2nd Fastest**
- **Latency**: 435μs avg (low), 3.21ms (medium)
- **JAR Size**: 28 MB
- **Memory**: 210 MB
- **Startup Time**: ~2.5 seconds

### Features
- Netty-based async I/O
- HTTP/1.1, HTTP/2, gRPC support
- Circuit breaker patterns built-in
- Distributed tracing (Brave/Zipkin)
- Production-proven at massive scale
- Service mesh integration
- Advanced retry and timeout policies

### Technology Stack
- **Java**: 21
- **Build**: Maven
- **Database**: PostgreSQL
- **Connection Pool**: HikariCP
- **Protocols**: REST, gRPC, Thrift

### Best Use Cases
- ✅ High-performance async APIs
- ✅ Multi-protocol services (REST + gRPC)
- ✅ Microservices with circuit breakers
- ✅ Services requiring distributed tracing
- ✅ High-concurrency scenarios (10k+ connections)

### Trade-offs
- ⚠️ Larger JAR size (28 MB)
- ⚠️ Async programming complexity
- ⚠️ Steeper learning curve

### Quick Start
```bash
cd armeria-rest
mvn clean package
docker compose up -d
curl http://localhost:8085/health
```

[Full Documentation](../armeria-rest/README.md)

---

## 3. Light4J REST

### Overview
Lightweight, high-performance REST framework designed for production workloads. Built by NetworkNT, Light4J emphasizes simplicity, performance, and production readiness with proven stability in enterprise environments.

### Key Metrics
- **Throughput**: 29,583 req/s ⭐ **Best Balanced**
- **Latency**: 514μs avg (low), 4.00ms (medium)
- **JAR Size**: 12 MB
- **Memory**: 165 MB
- **Startup Time**: ~1.8 seconds

### Features
- RESTful API with excellent performance
- HikariCP connection pooling
- Multi-stage Docker build
- JVM container optimizations
- Proven production stability
- Security built-in (OAuth2, JWT)
- Plugin architecture

### Technology Stack
- **Java**: 21
- **Build**: Maven
- **Database**: PostgreSQL
- **Connection Pool**: HikariCP
- **Docker Image**: 232 MB

### Best Use Cases
- ✅ Production REST APIs
- ✅ Balanced performance and features
- ✅ Enterprise applications
- ✅ Microservices platforms
- ✅ Services requiring security built-in

### Trade-offs
- ⚠️ Smaller ecosystem than Spring
- ⚠️ Less community support than major frameworks
- ⚠️ Manual configuration required

### Quick Start
```bash
cd light4j-rest-app
mvn clean package
docker compose up -d
curl http://localhost:8084/health
```

[Full Documentation](../light4j-rest-app/README.md) | [Performance Report](../light4j-rest-app/PERFORMANCE.md)

---

## 4. Micronaut REST

### Overview
Cloud-native framework with compile-time dependency injection and minimal reflection. Built by Object Computing, Micronaut is designed as a modern alternative to Spring Boot with superior performance and lower memory usage.

### Key Metrics
- **Throughput**: 28,130 req/s
- **Latency**: 675μs avg (low), 3.94ms (medium)
- **JAR Size**: ~14 MB (typical)
- **Memory**: 130 MB - most memory efficient (331 req/s/MB)
- **Startup Time**: ~1.2 seconds
- **Cloud Cost**: ~$0.03 per 1M requests (estimated)

### Features
- Compile-time dependency injection (no runtime reflection)
- Native cloud support (AWS, GCP, Azure)
- GraalVM ready for native compilation
- Low memory footprint
- Fast startup time
- Built-in service discovery
- Configuration management

### Technology Stack
- **Java**: 21
- **Build**: Maven
- **Database**: PostgreSQL
- **ORM**: Micronaut Data / JPA
- **Cloud**: AWS, GCP, Azure SDKs

### Best Use Cases
- ✅ Cloud-native applications ⭐
- ✅ Cost optimization (lowest memory)
- ✅ Kubernetes deployments
- ✅ Spring Boot migrations (90% similar)
- ✅ Serverless / FaaS
- ✅ Resource-constrained environments

### Trade-offs
- ⚠️ Smaller ecosystem than Spring
- ⚠️ Compile-time DI learning curve
- ⚠️ Fewer third-party integrations

### Quick Start
```bash
cd micronaut-rest
mvn clean package
docker compose up -d
curl http://localhost:8082/health
```

### Migration Path
**From Spring Boot**: 1-2 days migration time, 90% code similarity
- See [Migration Guides](../MIGRATION_GUIDES.md#spring-boot--micronaut)

[Full Documentation](../micronaut-rest/README.md)

---

## 5. Vert.x Lite

### Overview
Async, event-driven toolkit for building reactive applications on the JVM. Part of the Eclipse Foundation, Vert.x excels at handling massive concurrency with minimal resources through its event-loop architecture.

### Key Metrics
- **Throughput**: 28,073 req/s
- **Latency**: 603μs avg (low), 3.67ms (medium)
- **JAR Size**: ~9 MB (typical)
- **Memory**: 240 MB
- **Startup Time**: ~2.1 seconds
- **Database Performance**: 2x better in reactive scenarios

### Features
- Reactive/async architecture
- Event bus communication
- Hibernate Reactive ORM
- TestContainers integration
- Non-blocking I/O
- Polyglot support (Java, Kotlin, Groovy, JavaScript)
- Clustered event bus

### Technology Stack
- **Java**: 21
- **Build**: Maven
- **Database**: MySQL
- **ORM**: Hibernate Reactive
- **Architecture**: Event-driven

### Best Use Cases
- ✅ Real-time applications ⭐
- ✅ High-concurrency (10k+ connections)
- ✅ Event-driven architectures
- ✅ WebSocket services
- ✅ Real-time analytics pipelines
- ✅ Streaming data processing

### Trade-offs
- ⚠️ Async complexity (callback hell)
- ⚠️ Steeper learning curve
- ⚠️ Debugging challenges
- ⚠️ Manual observability setup

### Quick Start
```bash
cd vertx-lite
mvn clean package
docker compose up -d
curl http://localhost:8081/health
```

[Full Documentation](../vertx-lite/README.md)

---

## 6. Quarkus REST

### Overview
Kubernetes-native "Supersonic Subatomic Java" framework by Red Hat. Designed specifically for containers and cloud deployments with exceptional developer experience through live reload and extensive extensions ecosystem.

### Key Metrics
- **Throughput**: 26,608 req/s
- **Latency**: 736μs avg (low), 3.84ms (medium)
- **JAR Size**: ~15 MB (JVM), ~50 MB (native executable)
- **Memory**: 140 MB (JVM), 40 MB (native)
- **Startup Time**: 2.5s (JVM), <100ms (native)
- **Native Image**: 15ms startup, 40 MB memory

### Features
- Container-first design
- Live reload in dev mode ⭐
- Native compilation support (GraalVM)
- 1000+ extensions ecosystem
- Developer productivity focus
- Built-in Kubernetes integration
- Unified reactive and imperative programming

### Technology Stack
- **Java**: 21
- **Build**: Maven
- **Database**: PostgreSQL
- **ORM**: Hibernate ORM with Panache
- **Native**: GraalVM

### Best Use Cases
- ✅ Kubernetes-native apps ⭐
- ✅ Serverless/FaaS (native mode)
- ✅ Developer productivity
- ✅ Microservices platforms
- ✅ Spring Boot migrations
- ✅ Fast auto-scaling requirements

### Trade-offs
- ⚠️ Native build time (2-5 minutes)
- ⚠️ Slightly lower JVM performance vs raw frameworks
- ⚠️ Extension compatibility in native mode

### Quick Start
```bash
cd quarkus-rest
mvn clean package
docker compose up -d
curl http://localhost:8083/health
```

### Native Mode
```bash
mvn package -Pnative
# Startup: <100ms, Memory: 40 MB
```

[Full Documentation](../quarkus-rest/README.md)

---

## 7. Helidon REST

### Overview
Oracle's lightweight microservices framework with reactive foundations. Available in two flavors (SE and MP), this implementation uses Helidon SE for maximum performance with functional programming style.

### Key Metrics
- **Throughput**: 23,340 req/s
- **Latency**: 950μs avg (low), 5.10ms (medium)
- **JAR Size**: ~11 MB (typical)
- **Memory**: ~200 MB
- **Startup Time**: ~2.5 seconds

### Features
- Functional programming style
- Reactive streams
- MicroProfile compatible (MP flavor)
- Cloud-native patterns
- Minimal dependencies
- GraalVM native support
- Health checks and metrics built-in

### Technology Stack
- **Java**: 21
- **Build**: Maven
- **Database**: PostgreSQL
- **Architecture**: Reactive (Helidon SE)

### Best Use Cases
- ✅ MicroProfile standardization
- ✅ Oracle ecosystem integration
- ✅ Functional programming style
- ✅ Reactive microservices
- ✅ Cloud-native applications

### Trade-offs
- ⚠️ Smaller community than Quarkus/Micronaut
- ⚠️ Lower performance than top-tier frameworks
- ⚠️ Less extensive ecosystem

### Quick Start
```bash
cd helidon-rest
mvn clean package
docker compose up -d
curl http://localhost:8086/health
```

[Full Documentation](../helidon-rest/README.md)

---

## 8. Javalin REST

### Overview
Simple, lightweight web framework built on Jetty with a focus on developer experience. Kotlin-first but Java-friendly, Javalin provides an expressive API that's easy to learn and productive to use.

### Key Metrics
- **Throughput**: 19,853 req/s
- **Latency**: 1.30ms avg (low), 6.34ms (medium)
- **JAR Size**: 9.2 MB
- **Memory**: ~180 MB
- **Learning Curve**: ⭐ Easiest

### Features
- Simple, expressive API ⭐
- OpenAPI/Swagger support
- WebSocket support built-in
- Low learning curve
- Kotlin-first design (Java compatible)
- Excellent documentation
- Plugin ecosystem

### Technology Stack
- **Java**: 21
- **Build**: Maven
- **Server**: Jetty
- **Database**: PostgreSQL
- **ORM**: JPA/Hibernate

### Best Use Cases
- ✅ Rapid development ⭐
- ✅ Startups and MVPs
- ✅ Small to medium APIs
- ✅ Developer experience priority
- ✅ Kotlin projects
- ✅ Learning web frameworks

### Trade-offs
- ⚠️ Lower performance (19k req/s)
- ⚠️ Thread-pool model (not reactive)
- ⚠️ Performance degrades at 500+ connections

### Quick Start
```bash
cd javalin-rest
mvn clean package
docker compose up -d
curl http://localhost:8087/health
```

[Full Documentation](../javalin-rest/README.md)

---

## 9. Netty Baseline

### Overview
Bare-metal Netty implementation demonstrating raw framework performance without abstractions. Educational implementation showing the foundation that powers many high-performance frameworks.

### Key Metrics
- **Throughput**: 18,159 req/s
- **Latency**: 728μs avg (low), 2.93ms (medium)
- **JAR Size**: 8.5 MB
- **Memory**: ~160 MB

### Features
- Pure Netty event loops
- Zero-copy networking
- Minimal overhead
- Educational baseline
- Maximum control
- Non-blocking I/O

### Technology Stack
- **Java**: 21
- **Build**: Maven
- **Server**: Pure Netty
- **Database**: None (HTTP only)

### Best Use Cases
- ✅ Learning async I/O
- ✅ Understanding framework internals
- ✅ Custom protocol implementations
- ✅ Performance baseline comparison
- ✅ Educational purposes

### Trade-offs
- ⚠️ Manual everything (routing, parsing, etc.)
- ⚠️ Not production-ready (no features)
- ⚠️ High development effort

### Quick Start
```bash
cd netty-baseline
mvn clean package
docker compose up -d
curl http://localhost:8089/health
```

[Full Documentation](../netty-baseline/README.md)

---

## 10. ActiveJ REST

### Overview
Ultra-lightweight async framework focusing on minimal dependencies and maximum efficiency. Built for high-performance applications with zero-allocation design principles.

### Key Metrics
- **JAR Size**: 5.3 MB ⭐ **Smallest**
- **Throughput**: Testing in progress
- **Memory**: Extremely low
- **Dependencies**: Minimal

### Features
- Minimal JAR size (5.3 MB)
- Async/reactive architecture
- Zero-allocation design
- High performance focus
- Compact footprint
- Promise-based async
- Dependency injection

### Technology Stack
- **Java**: 21
- **Build**: Maven
- **Database**: PostgreSQL
- **Architecture**: Async, zero-allocation

### Best Use Cases
- ✅ Size-constrained environments
- ✅ High-performance requirements
- ✅ Minimal dependency needs
- ✅ Custom async applications

### Trade-offs
- ⚠️ Very small community
- ⚠️ Limited documentation
- ⚠️ Steep learning curve

### Quick Start
```bash
cd activej-rest
mvn clean package
docker compose up -d
curl http://localhost:8090/health
```

[Full Documentation](../activej-rest/README.md)

---

## 11. Spring Lite

### Overview
Minimal educational implementation of a Spring-like dependency injection framework. Built from scratch with zero dependencies to demonstrate DI container internals and framework design principles.

### Key Metrics
- **JAR Size**: 9.5 KB ⭐ **Smallest Possible**
- **Dependencies**: Zero
- **Purpose**: Educational only

### Features
- Custom DI container
- Component scanning
- Zero external dependencies
- Learning-focused
- Framework internals study
- Annotation-based configuration

### Technology Stack
- **Java**: 19
- **Build**: Gradle
- **Dependencies**: None
- **Database**: None

### Best Use Cases
- ✅ Learning DI concepts ⭐
- ✅ Understanding framework internals
- ✅ Educational purposes
- ✅ Job interviews preparation
- ✅ Framework design study

### Trade-offs
- ⚠️ Not production-ready
- ⚠️ Very limited features
- ⚠️ Educational only

### Quick Start
```bash
cd spring-lite
gradle build
java -jar build/libs/spring-lite.jar
```

[Full Documentation](../spring-lite/README.md)

---

## Framework Selection Guide

### By Performance Tier

#### Top Tier (35k+ req/s)
- **Undertow** - Maximum performance, lowest latency
- **Armeria** - Async RPC/REST, production-proven

#### Production Tier (26-30k req/s)
- **Light4J** - Balanced performance and features
- **Micronaut** - Cloud-native, lowest memory
- **Vert.x** - Reactive, high concurrency
- **Quarkus** - Kubernetes-native, developer experience

#### Solid Tier (18-24k req/s)
- **Helidon** - MicroProfile, Oracle ecosystem
- **Javalin** - Developer experience, rapid development
- **Netty** - Educational baseline

### By Use Case

#### Cloud-Native & Kubernetes
1. **Quarkus** - Container-first, live reload, native compilation
2. **Micronaut** - Compile-time DI, low memory, GraalVM ready
3. **Helidon** - MicroProfile compliance, cloud patterns

#### Maximum Performance
1. **Undertow** - 37,808 req/s, 379μs latency
2. **Armeria** - 35,810 req/s, async architecture
3. **Light4J** - 29,583 req/s, proven production stability

#### Reactive & High Concurrency
1. **Vert.x** - Event-driven, 10k+ connections
2. **Armeria** - Async RPC, circuit breakers
3. **ActiveJ** - Minimal footprint, zero-allocation

#### Developer Experience
1. **Javalin** - Simple API, low learning curve
2. **Quarkus** - Live reload, extensive extensions
3. **Micronaut** - Spring-like, easy migration

#### Learning & Education
1. **Spring Lite** - DI framework internals
2. **Netty Baseline** - Understanding async I/O
3. **Undertow Baseline** - Raw HTTP server mechanics

---

## Performance Summary

### Throughput Rankings
1. Undertow: 37,808 req/s
2. Armeria: 35,810 req/s
3. Light4J: 29,583 req/s
4. Micronaut: 28,130 req/s
5. Vert.x: 28,073 req/s
6. Quarkus: 26,608 req/s
7. Helidon: 23,340 req/s
8. Javalin: 19,853 req/s
9. Netty: 18,159 req/s

### Memory Efficiency (req/s per MB)
1. Micronaut: 331 req/s/MB ⭐
2. Quarkus: 221 req/s/MB
3. Undertow: 210 req/s/MB
4. Light4J: 179 req/s/MB
5. Armeria: 171 req/s/MB

### Startup Time Rankings
1. Quarkus Native: <100ms ⭐
2. Micronaut: 1.2s
3. Light4J: 1.8s
4. Undertow: 2.0s
5. Vert.x: 2.1s

---

## Technology Stack Summary

### Build Tools & Java Versions
- **Java**: 19-21 across all applications
- **Build**: Maven (most), Gradle (Spring Lite)
- **Container**: Docker multi-stage builds
- **Base Image**: eclipse-temurin Alpine JRE

### Database Integration

| Framework | Database | Connection Pool | ORM |
|-----------|----------|----------------|-----|
| Light4J, Undertow, Armeria, ActiveJ | PostgreSQL | HikariCP | JDBC |
| Micronaut, Quarkus, Helidon, Javalin | PostgreSQL | HikariCP | JPA/Hibernate |
| Vert.x | MySQL | Vert.x Pool | Hibernate Reactive |
| Netty, Spring Lite | None | - | - |

---

## Further Reading

- **[Benchmarks](BENCHMARKS.md)** - Detailed performance analysis with charts
- **[Decision Guide](DECISION_GUIDE.md)** - Choose the right framework for your needs
- **[Migration Guides](../MIGRATION_GUIDES.md)** - Step-by-step framework migration
- **[Getting Started](GETTING_STARTED.md)** - Quick start tutorials
- **[Deployment](DEPLOYMENT.md)** - Production deployment guides

---

**Last Updated**: August 25, 2026
