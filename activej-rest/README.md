# ActiveJ REST Microservice

High-performance REST API implementation using ActiveJ framework - showcasing minimal JAR size and efficient resource usage.

## Overview

ActiveJ is a modern Java framework designed for building high-performance applications with minimal dependencies. This implementation demonstrates:

- **Ultra-small JAR size**: 5.3 MB (smallest in benchmark suite)
- **Event-driven architecture**: Non-blocking I/O for high concurrency
- **Zero reflection**: Compile-time dependency injection
- **Minimal dependencies**: Focused core framework

## Key Metrics

| Metric | Value | Ranking |
|--------|-------|---------|
| **Throughput (High Concurrency)** | 25,429 req/s | 7th of 11 |
| **JAR Size** | 5.3 MB | **#1 Smallest** |
| **Memory Usage** | ~170 MB | Moderate |
| **P50 Latency** | 18.89ms | - |
| **P99 Latency** | 35.71ms | - |

## Quick Start

### Prerequisites
- Java 21+
- Docker & Docker Compose
- Maven

### Running Locally

```bash
# Build the project
mvn clean package

# Start with Docker Compose
docker compose up -d

# Test the health endpoint
curl http://localhost:8087/health
# Response: OK

# Test the API
curl http://localhost:8087/api/projects
```

### API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/health` | GET | Health check |
| `/api/projects` | GET | List all projects |
| `/api/projects?userId=X` | GET | List projects by user |
| `/api/projects/{id}` | GET | Get project by ID |
| `/api/projects` | POST | Create new project |
| `/api/projects/{id}` | DELETE | Delete project |

## Technology Stack

- **Framework**: ActiveJ 6.0-rc2
- **Java**: 21
- **Build Tool**: Maven
- **Database**: PostgreSQL 16
- **Connection Pool**: HikariCP 5.1.0
- **JSON**: Jackson 2.18.1

## Performance Characteristics

### Strengths
✅ **Smallest JAR**: 5.3 MB - ideal for containers and serverless
✅ **Zero reflection**: Fast startup and runtime
✅ **Event-driven**: Efficient handling of concurrent connections
✅ **Minimal dependencies**: Reduced attack surface and maintenance

### Trade-offs
⚠️ Less mature ecosystem compared to Spring/Quarkus
⚠️ Smaller community and fewer third-party integrations
⚠️ Requires understanding of async/event-driven patterns

## Best Use Cases

- **Microservices** with strict size constraints
- **Serverless/FaaS** deployments requiring minimal cold start
- **Container-optimized** applications (Docker, K8s)
- **Performance-critical** services with simple requirements
- **Learning** event-driven and reactive programming patterns

## Configuration

The application can be configured through environment variables:

```bash
DB_HOST=postgres        # Database host
DB_PORT=5432           # Database port
DB_NAME=projectdb      # Database name
DB_USER=postgres       # Database user
DB_PASSWORD=postgres   # Database password
```

Server listen address is configured in `src/main/resources/http-server.properties`:
```properties
http.listenAddresses=0.0.0.0:8080
```

## Building

```bash
# Standard build
mvn clean package

# Skip tests
mvn clean package -DskipTests

# View JAR size
ls -lh target/activej-rest-1.0.0.jar
```

## Docker

```bash
# Build image
docker compose build

# Run container
docker compose up -d

# View logs
docker logs activej-rest-app

# Stop container
docker compose down
```

## Comparison with Other Frameworks

ActiveJ positions itself uniquely in the benchmark suite:

| Aspect | ActiveJ | Typical Framework |
|--------|---------|------------------|
| JAR Size | 5.3 MB | 10-30 MB |
| Dependencies | Minimal | Comprehensive |
| Startup Time | Fast | Varies |
| Learning Curve | Moderate | Easy to Steep |
| Ecosystem | Small | Large |

## Additional Resources

- [ActiveJ Official Website](https://activej.io/)
- [ActiveJ GitHub](https://github.com/activej/activej)
- [ActiveJ Documentation](https://activej.io/documentation)
- [Framework Comparison](../docs/FRAMEWORKS.md)
- [Performance Benchmarks](../docs/BENCHMARKS.md)

## License

See main repository [LICENSE](../LICENSE) file.
