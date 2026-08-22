# Micro-Apps - Tiny Microservices Showcase

A collection of minimal, high-performance microservice implementations demonstrating various Java frameworks and architectural patterns. This repository showcases how to build production-ready API servers with minimal size and maximum performance.

## Applications

### 1. Light4J REST App
**Framework**: Light4J + Undertow
**Java**: 21
**Build**: Maven
**Database**: PostgreSQL + HikariCP
**Size**: 12 MB JAR, 232 MB Docker image

High-performance REST API with PostgreSQL integration, optimized for minimal footprint and maximum throughput.

**Features**:
- RESTful API endpoints (CRUD operations)
- PostgreSQL database integration
- HikariCP connection pooling
- Docker & docker-compose setup
- Multi-stage Docker build
- JVM container optimizations
- 10k+ requests/second throughput

**Quick Start**:
```bash
cd light4j-rest-app
docker compose up -d
curl http://localhost:8080/health
```

[Full Documentation](light4j-rest-app/README.md) | [Performance Report](light4j-rest-app/PERFORMANCE.md)

---

### 2. Vert.x Lite
**Framework**: Vert.x (Reactive)
**Java**: 21
**Build**: Maven
**Database**: MySQL + Hibernate Reactive

Async, event-driven microservice for project management with reactive database access.

**Features**:
- Reactive/async architecture
- Hibernate Reactive ORM
- Event bus communication
- TestContainers for testing
- Vertical slice architecture

**Quick Start**:
```bash
cd vertx-lite
mvn clean package
java -jar target/vertx-lite-1.0.0-SNAPSHOT-fat.jar
```

---

### 3. Spring Lite
**Framework**: Custom (Spring-like)
**Java**: 19
**Build**: Gradle
**Dependencies**: Zero (educational framework)

Minimal educational implementation of a Spring-like dependency injection framework.

**Features**:
- Custom DI container
- Component scanning
- Annotation-based configuration
- Request mapping
- Zero external dependencies

**Quick Start**:
```bash
cd spring-lite
gradle clean assemble
java -jar build/libs/spring-lite-1.0.0.jar
```

---

## Comparison Matrix

| App | Framework | Size (JAR) | Image Size | DB | Startup | Focus |
|-----|-----------|------------|------------|-----|---------|-------|
| **light4j-rest-app** | Light4J | 12 MB | 232 MB | PostgreSQL | < 2s | Performance |
| vertx-lite | Vert.x | ~3 MB | N/A | MySQL | < 2s | Reactive |
| spring-lite | Custom | 7 KB | N/A | None | Instant | Education |

## Use Cases

### Light4J REST App
- Production microservices
- High-throughput APIs
- Container deployments
- Database-backed services

### Vert.x Lite
- Event-driven architectures
- Reactive data processing
- Non-blocking I/O workloads
- Message-driven systems

### Spring Lite
- Learning DI concepts
- Framework internals study
- Minimal overhead demos
- Educational projects

## Getting Started

### Prerequisites
- Java 19-21
- Maven 3.9+ or Gradle 8+
- Docker (optional, for light4j-rest-app)
- PostgreSQL/MySQL (if running locally)

### Running with Docker (Light4J)
```bash
cd light4j-rest-app
docker compose up -d
```

### Local Development
Each application has its own build and run instructions in their respective README files.

## Repository Structure
```
micro-apps/
├── light4j-rest-app/      # Light4J + PostgreSQL
│   ├── src/
│   ├── Dockerfile
│   ├── docker-compose.yml
│   ├── pom.xml
│   └── README.md
├── vertx-lite/            # Vert.x + MySQL
│   ├── src/
│   ├── pom.xml
│   └── README.md
└── spring-lite/           # Custom framework
    ├── src/
    ├── build.gradle
    └── README.md
```

## Performance Highlights

### Light4J REST App
- **Throughput**: 10,000+ req/s
- **Latency (p99)**: < 5ms
- **Memory**: ~150 MB
- **Startup**: < 2 seconds

See [PERFORMANCE.md](light4j-rest-app/PERFORMANCE.md) for detailed metrics.

## Technologies

- **Frameworks**: Light4J, Vert.x, Custom DI
- **Databases**: PostgreSQL, MySQL
- **Connection Pools**: HikariCP, Vert.x MySQL Client
- **ORM**: Hibernate Reactive
- **Build Tools**: Maven, Gradle
- **Containers**: Docker, Docker Compose
- **Java Versions**: 19, 21

## Contributing

This is a showcase repository. Feel free to:
- Study the implementations
- Use as reference for your projects
- Suggest optimizations
- Report issues

## License

MIT License - See individual application directories for details.