# Vert.x Lite - Reactive Microservice

A high-performance, event-driven microservice built with Vert.x framework, demonstrating reactive programming patterns with MySQL database integration.

## Features

- **Reactive Architecture**: Non-blocking I/O with Vert.x event loops
- **Async Database**: Hibernate Reactive for MySQL
- **Event Bus**: Inter-component async messaging
- **Verticle Pattern**: Isolated deployment units
- **Docker-ready**: Multi-stage builds and docker-compose setup
- **Type-safe Queries**: JPA Criteria API

## Tech Stack

- **Framework**: Vert.x 4.5.13
- **Java**: 21
- **Database**: MySQL 8.0
- **ORM**: Hibernate Reactive 2.0.5
- **Build**: Maven 3.9+
- **Runtime**: Alpine Linux (JRE 21)

## API Endpoints

### Projects API
```bash
# Get project by ID
GET /api/v1/project/:id

# Get projects by user
GET /api/v1/projects/:userId

# Create project
POST /api/v1/project
Content-Type: application/json
{
  "id": 1,
  "userId": 123,
  "name": "My Project"
}

# Delete project
DELETE /api/v1/project/:id
```

### Event Bus
- `hello.vertx.addr` - General greeting handler
- `hello.named.addr` - Personalized greeting with name parameter

## Quick Start

### Using Docker Compose (Recommended)

```bash
# Start MySQL and the application
docker compose up -d

# View logs
docker compose logs -f vertx-app

# Stop services
docker compose down
```

The application will be available at `http://localhost:8081`

### Local Development

Requirements:
- Java 21
- Maven 3.9+
- MySQL 8.0 (running on localhost:3306)

```bash
# Build
mvn clean package

# Run
java -jar target/vertx-lite-1.0.0-SNAPSHOT-fat.jar
```

### Environment Variables

Configure via environment or modify `Application.java`:

- `DB_HOST`: Database host (default: `localhost`)
- `DB_PORT`: Database port (default: `3306`)
- `DB_NAME`: Database name (default: `planner`)
- `DB_USER`: Database username (default: `root`)
- `DB_PASSWORD`: Database password (default: `root`)

## Docker Image Details

**Size Optimization:**
- Multi-stage build (builder + runtime)
- Alpine-based JRE 21
- Non-root user
- Health checks included

**Metrics:**
- JAR Size: 29 MB
- Image Size: 270 MB
- Startup: 2-3 seconds

See [PERFORMANCE.md](PERFORMANCE.md) for detailed analysis.

## Project Structure

```
vertx-lite/
├── src/main/java/org/sandcastle/apps/
│   ├── Application.java              # Main entry + Hibernate config
│   ├── entity/
│   │   ├── Project.java              # JPA entity
│   │   └── Task.java                 # Related entity
│   ├── dto/
│   │   ├── ProjectDto.java           # Data transfer object
│   │   └── ProjectsList.java         # Collection wrapper
│   ├── mappers/
│   │   ├── ProjectDtoMapper.java     # Entity → DTO
│   │   └── ProjectEntityMapper.java  # DTO → Entity
│   ├── repository/
│   │   ├── ProjectRepository.java    # Interface
│   │   └── ProjectRepositoryImpl.java# Hibernate Reactive impl
│   ├── service/
│   │   ├── ProjectService.java       # Business logic interface
│   │   └── ProjectServiceImpl.java   # Implementation
│   └── web/
│       ├── MainVerticle.java         # Entry point, routing
│       ├── HelloVerticle.java        # Event bus example
│       └── ProjectVerticle.java      # REST API handler
├── src/main/resources/
│   ├── application-local.json        # HTTP port config
│   └── log4j2.properties             # Logging config
├── Dockerfile                        # Multi-stage build
├── docker-compose.yml                # Full stack setup
└── pom.xml                           # Maven dependencies
```

## Architecture

### Reactive Patterns
- **Event Loop**: Non-blocking async I/O
- **Verticles**: Actor-like deployment units
- **Event Bus**: Message-driven communication
- **Futures**: Composable async operations

### Layers
1. **Web Layer**: HTTP handlers (Verticles)
2. **Service Layer**: Business logic
3. **Repository Layer**: Database access (Hibernate Reactive)
4. **Entity Layer**: JPA entities with relationships

## Development Tips

### Running with MySQL

```bash
# Start MySQL container
docker run --name mysql-dev \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=planner \
  -p 3306:3306 \
  -d mysql:8.0

# Wait for MySQL to be ready
sleep 10

# Run the application
mvn exec:java
```

### Event Bus Examples

```java
// Send message
vertx.eventBus().request("hello.vertx.addr", "message", reply -> {
    System.out.println("Got reply: " + reply.result().body());
});

// Send with parameters
JsonObject msg = new JsonObject().put("name", "World");
vertx.eventBus().request("hello.named.addr", msg, reply -> {
    System.out.println(reply.result().body());
});
```

### Testing

```bash
# Run tests (requires TestContainers)
mvn test

# Skip tests during build
mvn package -DskipTests
```

## Performance

- **Throughput**: 8,000-12,000 requests/second
- **Latency (p99)**: < 10ms
- **Concurrency**: 10,000+ simultaneous connections
- **Memory**: ~200-280 MB

See [PERFORMANCE.md](PERFORMANCE.md) for comprehensive analysis.

## Reactive Programming

### Benefits
- High concurrency with few threads
- Resource-efficient (minimal memory per request)
- Natural backpressure handling
- Excellent scalability

### Considerations
- Steeper learning curve
- Async debugging can be challenging
- Requires reactive-compatible libraries
- Best for I/O-bound workloads

## Production Deployment

### Kubernetes
```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "500m"
  limits:
    memory: "1Gi"
    cpu: "1000m"
```

### Scaling
- Horizontal: Stateless, can scale to multiple instances
- Vertical: Event loops scale with CPU cores
- Event Bus: Supports clustering for distributed deployments

## License

MIT License - See root repository for details
