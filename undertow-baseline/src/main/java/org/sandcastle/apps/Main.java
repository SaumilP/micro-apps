package org.sandcastle.apps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import io.undertow.util.StatusCodes;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static DataSource dataSource;
    private static final ExecutorService dbExecutor = Executors.newFixedThreadPool(20);

    public static void main(String[] args) {
        int port = Integer.parseInt(System.getenv().getOrDefault("SERVER_PORT", "8088"));
        dataSource = createDataSource();

        RoutingHandler routes = new RoutingHandler()
            .get("/health", exchange -> {
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                exchange.getResponseSender().send("OK");
            })
            .get("/api/projects", Main::handleListProjects)
            .get("/api/projects/{id}", Main::handleGetProject)
            .post("/api/projects", Main::handleCreateProject)
            .delete("/api/projects/{id}", Main::handleDeleteProject);

        Undertow server = Undertow.builder()
            .addHttpListener(port, "0.0.0.0")
            .setHandler(routes)
            .build();

        server.start();
        System.out.println("Undertow server started on port " + port);
    }

    private static void handleListProjects(HttpServerExchange exchange) {
        if (exchange.isInIoThread()) {
            exchange.dispatch(() -> handleListProjects(exchange));
            return;
        }

        try {
            Deque<String> userIdParam = exchange.getQueryParameters().get("userId");
            String userId = (userIdParam != null && !userIdParam.isEmpty()) ? userIdParam.getFirst() : null;

            List<Project> projects = new ArrayList<>();
            try (Connection conn = dataSource.getConnection()) {
                String sql = userId != null
                    ? "SELECT id, user_id, name, description FROM project WHERE user_id = ?"
                    : "SELECT id, user_id, name, description FROM project";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    if (userId != null) {
                        stmt.setString(1, userId);
                    }

                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            projects.add(new Project(
                                UUID.fromString(rs.getString("id")),
                                rs.getString("user_id"),
                                rs.getString("name"),
                                rs.getString("description")
                            ));
                        }
                    }
                }
            }

            String json = objectMapper.writeValueAsString(projects);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseSender().send(json);
        } catch (Exception e) {
            exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
            exchange.getResponseSender().send(e.getMessage());
        }
    }

    private static void handleGetProject(HttpServerExchange exchange) {
        if (exchange.isInIoThread()) {
            exchange.dispatch(() -> handleGetProject(exchange));
            return;
        }

        try {
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String id = pathMatch.getParameters().get("id");

            Project project = null;
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id, user_id, name, description FROM project WHERE id = ?")) {

                stmt.setObject(1, UUID.fromString(id));

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        project = new Project(
                            UUID.fromString(rs.getString("id")),
                            rs.getString("user_id"),
                            rs.getString("name"),
                            rs.getString("description")
                        );
                    }
                }
            }

            if (project != null) {
                String json = objectMapper.writeValueAsString(project);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseSender().send(json);
            } else {
                exchange.setStatusCode(StatusCodes.NOT_FOUND);
            }
        } catch (Exception e) {
            exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
            exchange.getResponseSender().send(e.getMessage());
        }
    }

    private static void handleCreateProject(HttpServerExchange exchange) {
        if (exchange.isInIoThread()) {
            exchange.dispatch(() -> handleCreateProject(exchange));
            return;
        }

        exchange.getRequestReceiver().receiveFullBytes((ex, data) -> {
            try {
                Project project = objectMapper.readValue(data, Project.class);

                try (Connection conn = dataSource.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO project (user_id, name, description) VALUES (?, ?, ?) RETURNING id")) {

                    stmt.setString(1, project.getUserId());
                    stmt.setString(2, project.getName());
                    stmt.setString(3, project.getDescription());

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            project.setId(UUID.fromString(rs.getString("id")));
                        }
                    }
                }

                String json = objectMapper.writeValueAsString(project);
                ex.setStatusCode(StatusCodes.CREATED);
                ex.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                ex.getResponseSender().send(json);
            } catch (Exception e) {
                ex.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
                ex.getResponseSender().send(e.getMessage());
            }
        });
    }

    private static void handleDeleteProject(HttpServerExchange exchange) {
        if (exchange.isInIoThread()) {
            exchange.dispatch(() -> handleDeleteProject(exchange));
            return;
        }

        try {
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String id = pathMatch.getParameters().get("id");

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM project WHERE id = ?")) {

                stmt.setObject(1, UUID.fromString(id));
                stmt.executeUpdate();
            }

            exchange.setStatusCode(StatusCodes.NO_CONTENT);
        } catch (Exception e) {
            exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
            exchange.getResponseSender().send(e.getMessage());
        }
    }

    private static DataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(System.getenv().getOrDefault("JDBC_URL",
            "jdbc:postgresql://" + System.getenv().getOrDefault("DB_HOST", "localhost") +
            ":" + System.getenv().getOrDefault("DB_PORT", "5432") +
            "/" + System.getenv().getOrDefault("DB_NAME", "projectdb")));
        config.setUsername(System.getenv().getOrDefault("DB_USER", "postgres"));
        config.setPassword(System.getenv().getOrDefault("DB_PASSWORD", "postgres"));
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        return new HikariDataSource(config);
    }
}
