# Code Examples

Here are copy-paste code snippets for all 11 frameworks. These should give you a quick reference for implementing common patterns across the different frameworks.

---

## Table of Contents

- [Hello World / Health Endpoints](#hello-world--health-endpoints)
- [Database CRUD Operations](#database-crud-operations)
- [Error Handling](#error-handling)
- [Configuration](#configuration)
- [Middleware & Filters](#middleware--filters)
- [Testing](#testing)

---

## Hello World / Health Endpoints

### Undertow

```java
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class UndertowServer {
    public static void main(String[] args) {
        Undertow server = Undertow.builder()
            .addHttpListener(8088, "0.0.0.0")
            .setHandler(new HttpHandler() {
                @Override
                public void handleRequest(HttpServerExchange exchange) {
                    String path = exchange.getRequestPath();

                    if ("/health".equals(path)) {
                        exchange.getResponseHeaders()
                            .put(Headers.CONTENT_TYPE, "application/json");
                        exchange.getResponseSender()
                            .send("{\"status\":\"UP\"}");
                    } else {
                        exchange.setStatusCode(404);
                        exchange.getResponseSender().send("Not Found");
                    }
                }
            })
            .build();

        server.start();
        System.out.println("Server started on http://localhost:8088");
    }
}
```

### Armeria

```java
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.annotation.Get;

public class ArmeriaServer {
    public static void main(String[] args) {
        Server server = Server.builder()
            .http(8085)
            .annotatedService(new HealthService())
            .build();

        server.start().join();
    }
}

class HealthService {
    @Get("/health")
    public String health() {
        return "{\"status\":\"UP\"}";
    }
}
```

### Micronaut

```java
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller
public class HealthController {

    @Get("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
```

### Quarkus

```java
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/health")
public class HealthResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
```

### Vert.x

```java
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

public class VertxServer {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.get("/health").handler(ctx -> {
            ctx.response()
                .putHeader("content-type", "application/json")
                .end("{\"status\":\"UP\"}");
        });

        server.requestHandler(router).listen(8081);
    }
}
```

### Javalin

```java
import io.javalin.Javalin;

public class JavalinApp {
    public static void main(String[] args) {
        Javalin app = Javalin.create()
            .get("/health", ctx -> {
                ctx.json(Map.of("status", "UP"));
            })
            .start(8087);
    }
}
```

### Helidon

```java
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;

public class HelidonServer {
    public static void main(String[] args) {
        WebServer server = WebServer.builder()
            .routing(routing -> routing
                .get("/health", (req, res) -> {
                    res.send("{\"status\":\"UP\"}");
                })
            )
            .port(8086)
            .build()
            .start();
    }
}
```

### Light4J

```java
import com.networknt.server.Server;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public class Light4JServer {
    public static void main(String[] args) {
        Server.start(new HttpHandler() {
            @Override
            public void handleRequest(HttpServerExchange exchange) {
                if ("/health".equals(exchange.getRequestPath())) {
                    exchange.getResponseSender()
                        .send("{\"status\":\"UP\"}");
                }
            }
        });
    }
}
```

---

## Database CRUD Operations

### Micronaut (JPA)

```java
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;
import jakarta.persistence.*;
import java.util.List;
import java.util.Optional;

// Entity
@Entity
@Table(name = "users")
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;

    // Getters, setters, constructors
}

// Repository
@Repository
interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}

// Controller
@Controller("/users")
class UserController {

    @Inject
    UserRepository repository;

    @Get
    public List<User> findAll() {
        return repository.findAll();
    }

    @Get("/{id}")
    public Optional<User> findById(Long id) {
        return repository.findById(id);
    }

    @Post
    public User create(@Body User user) {
        return repository.save(user);
    }

    @Put("/{id}")
    public User update(Long id, @Body User user) {
        user.setId(id);
        return repository.save(user);
    }

    @Delete("/{id}")
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
```

### Quarkus (Panache)

```java
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.Entity;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

// Entity (extends PanacheEntity for auto ID)
@Entity
class User extends PanacheEntity {
    public String name;
    public String email;

    public static User findByEmail(String email) {
        return find("email", email).firstResult();
    }
}

// Repository (optional, can use static methods on Entity)
@ApplicationScoped
class UserRepository implements PanacheRepository<User> {
    // Custom methods here
}

// Resource
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class UserResource {

    @GET
    public List<User> findAll() {
        return User.listAll();
    }

    @GET
    @Path("/{id}")
    public User findById(@PathParam("id") Long id) {
        return User.findById(id);
    }

    @POST
    public User create(User user) {
        user.persist();
        return user;
    }

    @PUT
    @Path("/{id}")
    public User update(@PathParam("id") Long id, User user) {
        User entity = User.findById(id);
        if (entity != null) {
            entity.name = user.name;
            entity.email = user.email;
        }
        return entity;
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") Long id) {
        User.deleteById(id);
    }
}
```

### Vert.x (Reactive)

```java
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

public class UserVerticle extends AbstractVerticle {

    private PgPool client;

    @Override
    public void start(Promise<Void> promise) {
        client = PgPool.pool(vertx, config());

        Router router = Router.router(vertx);
        router.get("/users").handler(this::findAll);
        router.get("/users/:id").handler(this::findById);
        router.post("/users").handler(this::create);

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8081)
            .onSuccess(server -> promise.complete())
            .onFailure(promise::fail);
    }

    private void findAll(RoutingContext ctx) {
        client.query("SELECT * FROM users")
            .execute()
            .onSuccess(rows -> {
                ctx.response()
                    .putHeader("content-type", "application/json")
                    .end(rows.toJson().encode());
            })
            .onFailure(err -> ctx.fail(500, err));
    }

    private void findById(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        client.preparedQuery("SELECT * FROM users WHERE id = $1")
            .execute(io.vertx.sqlclient.Tuple.of(Long.parseLong(id)))
            .onSuccess(rows -> {
                if (rows.size() > 0) {
                    ctx.response()
                        .putHeader("content-type", "application/json")
                        .end(rows.iterator().next().toJson().encode());
                } else {
                    ctx.response().setStatusCode(404).end();
                }
            })
            .onFailure(err -> ctx.fail(500, err));
    }

    private void create(RoutingContext ctx) {
        // Parse JSON, insert into database
        ctx.body().asJsonObject();
        // Implementation details...
    }
}
```

### Javalin (Simple JDBC)

```java
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JavalinCRUD {

    private static final String DB_URL = "jdbc:postgresql://localhost/db";

    public static void main(String[] args) {
        Javalin app = Javalin.create()
            .get("/users", JavalinCRUD::findAll)
            .get("/users/{id}", JavalinCRUD::findById)
            .post("/users", JavalinCRUD::create)
            .put("/users/{id}", JavalinCRUD::update)
            .delete("/users/{id}", JavalinCRUD::delete)
            .start(8087);
    }

    private static void findAll(Context ctx) {
        List<User> users = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {

            while (rs.next()) {
                users.add(new User(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("email")
                ));
            }
            ctx.json(users);
        } catch (SQLException e) {
            ctx.status(500).result("Database error");
        }
    }

    private static void findById(Context ctx) {
        long id = Long.parseLong(ctx.pathParam("id"));
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                ctx.json(new User(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("email")
                ));
            } else {
                ctx.status(404);
            }
        } catch (SQLException e) {
            ctx.status(500).result("Database error");
        }
    }

    private static void create(Context ctx) {
        User user = ctx.bodyAsClass(User.class);
        String sql = "INSERT INTO users (name, email) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql,
                 Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                user.setId(rs.getLong(1));
            }

            ctx.status(201).json(user);
        } catch (SQLException e) {
            ctx.status(500).result("Database error");
        }
    }

    private static void update(Context ctx) {
        // Similar to create
    }

    private static void delete(Context ctx) {
        // Similar to findById
    }
}

record User(Long id, String name, String email) {
    // Java 17+ record
}
```

---

## Error Handling

### Micronaut

```java
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Controller;
import io.micronaut.context.annotation.Requires;

@Controller
public class GlobalExceptionHandler {

    @Error(global = true)
    public HttpResponse<ErrorResponse> handleException(
            HttpRequest request,
            Exception exception) {

        return HttpResponse.serverError(new ErrorResponse(
            "Internal Server Error",
            exception.getMessage()
        ));
    }

    @Error(status = HttpStatus.NOT_FOUND, global = true)
    public HttpResponse<ErrorResponse> notFound(HttpRequest request) {
        return HttpResponse.notFound(new ErrorResponse(
            "Not Found",
            "Resource not found: " + request.getUri()
        ));
    }
}

record ErrorResponse(String error, String message) {}
```

### Quarkus

```java
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionMapper
        implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {

        if (exception instanceof WebApplicationException) {
            WebApplicationException webEx =
                (WebApplicationException) exception;
            return webEx.getResponse();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(Map.of(
                "error", "Internal Server Error",
                "message", exception.getMessage()
            ))
            .build();
    }
}
```

### Javalin

```java
import io.javalin.Javalin;

public class JavalinErrorHandling {
    public static void main(String[] args) {
        Javalin app = Javalin.create()
            .exception(Exception.class, (e, ctx) -> {
                ctx.status(500).json(Map.of(
                    "error", "Internal Server Error",
                    "message", e.getMessage()
                ));
            })
            .error(404, ctx -> {
                ctx.json(Map.of(
                    "error", "Not Found",
                    "path", ctx.path()
                ));
            })
            .start(8087);
    }
}
```

---

## Configuration

### Micronaut (application.yml)

```yaml
# src/main/resources/application.yml
micronaut:
  application:
    name: micronaut-rest
  server:
    port: 8082

datasources:
  default:
    url: jdbc:postgresql://localhost:5432/db
    driverClassName: org.postgresql.Driver
    username: postgres
    password: ${DB_PASSWORD:postgres}

hikari:
  maximum-pool-size: 20
  minimum-idle: 5
```

**Accessing in Code**:

```java
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

@Singleton
public class AppConfig {

    @Value("${micronaut.application.name}")
    private String appName;

    @Value("${datasources.default.url}")
    private String dbUrl;

    public String getAppName() {
        return appName;
    }
}
```

### Quarkus (application.properties)

```properties
# src/main/resources/application.properties
quarkus.application.name=quarkus-rest
quarkus.http.port=8083

quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=postgres
quarkus.datasource.password=${DB_PASSWORD:postgres}
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/db

quarkus.hibernate-orm.database.generation=update
```

**Accessing in Code**:

```java
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Inject;

public class AppConfig {

    @ConfigProperty(name = "quarkus.application.name")
    String appName;

    @Inject
    @ConfigProperty(name = "quarkus.http.port", defaultValue = "8080")
    int port;
}
```

### Environment Variables

All frameworks support environment variables:

```bash
# Docker
docker run -e DB_PASSWORD=secret -e PORT=8088 app

# Kubernetes
env:
- name: DB_PASSWORD
  valueFrom:
    secretKeyRef:
      name: db-secret
      key: password
```

---

## Middleware & Filters

### Micronaut (HTTP Filters)

```java
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import org.reactivestreams.Publisher;

@Filter("/**")
public class LoggingFilter implements HttpServerFilter {

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(
            HttpRequest<?> request,
            ServerFilterChain chain) {

        long start = System.currentTimeMillis();

        return Flux.from(chain.proceed(request))
            .doOnNext(response -> {
                long duration = System.currentTimeMillis() - start;
                System.out.printf("%s %s - %d (%dms)%n",
                    request.getMethod(),
                    request.getPath(),
                    response.getStatus().getCode(),
                    duration
                );
            });
    }
}
```

### Quarkus (ContainerRequestFilter)

```java
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class LoggingFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        System.out.printf("Request: %s %s%n",
            requestContext.getMethod(),
            requestContext.getUriInfo().getPath()
        );
    }
}
```

### Javalin (Before/After Handlers)

```java
import io.javalin.Javalin;

public class JavalinMiddleware {
    public static void main(String[] args) {
        Javalin app = Javalin.create()
            .before(ctx -> {
                ctx.attribute("startTime", System.currentTimeMillis());
                System.out.println("Before: " + ctx.path());
            })
            .after(ctx -> {
                long start = ctx.attribute("startTime");
                long duration = System.currentTimeMillis() - start;
                System.out.printf("After: %s (%dms)%n",
                    ctx.path(), duration);
            })
            .start(8087);
    }
}
```

---

## Testing

### Micronaut (JUnit 5)

```java
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class HealthControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void testHealthEndpoint() {
        String response = client.toBlocking()
            .retrieve("/health");

        assertTrue(response.contains("UP"));
    }
}
```

### Quarkus (REST Assured)

```java
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class HealthResourceTest {

    @Test
    void testHealthEndpoint() {
        given()
            .when().get("/health")
            .then()
                .statusCode(200)
                .body("status", is("UP"));
    }
}
```

### Javalin (JUnit + HTTP Client)

```java
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class JavalinAppTest {

    @Test
    void testHealthEndpoint() {
        Javalin app = Javalin.create()
            .get("/health", ctx -> ctx.result("OK"));

        JavalinTest.test(app, (server, client) -> {
            assertThat(client.get("/health").code()).isEqualTo(200);
            assertThat(client.get("/health").body().string())
                .isEqualTo("OK");
        });
    }
}
```

---

## Further Reading

- **[Frameworks Guide](FRAMEWORKS.md)** - Detailed framework information
- **[Getting Started](GETTING_STARTED.md)** - Quick start tutorials
- **[Benchmarks](BENCHMARKS.md)** - Performance comparisons
- Individual framework READMEs in each directory

---

**Last Updated**: August 25, 2026
