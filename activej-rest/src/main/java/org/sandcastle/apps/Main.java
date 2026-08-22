package org.sandcastle.apps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.activej.http.*;
import io.activej.inject.annotation.Provides;
import io.activej.launcher.Launcher;
import io.activej.launchers.http.HttpServerLauncher;
import io.activej.promise.Promise;
import io.activej.reactor.Reactor;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static io.activej.http.HttpMethod.*;

public class Main extends HttpServerLauncher {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static DataSource dataSource;
    private static final Executor dbExecutor = Executors.newFixedThreadPool(20);

    @Provides
    AsyncServlet servlet(Reactor reactor) {
        return RoutingServlet.builder(reactor)
            .with(GET, "/health", request ->
                HttpResponse.ok200().withPlainText("OK").toPromise())
            .with(GET, "/api/projects", this::handleListProjects)
            .with(GET, "/api/projects/:id", this::handleGetProject)
            .with(POST, "/api/projects", this::handleCreateProject)
            .with(DELETE, "/api/projects/:id", this::handleDeleteProject)
            .build();
    }

    private Promise<HttpResponse> handleListProjects(HttpRequest request) {
        Map<String, String> params = request.getQueryParameters();
        String userId = params.get("userId");

        return Promise.ofBlocking(dbExecutor, () -> {
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
            return objectMapper.writeValueAsString(projects);
        }).map(json -> HttpResponse.ok200()
            .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .withBody(json.getBytes())
            .build());
    }

    private Promise<HttpResponse> handleGetProject(HttpRequest request) {
        String id = request.getPathParameter("id");

        return Promise.ofBlocking(dbExecutor, () -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id, user_id, name, description FROM project WHERE id = ?")) {

                stmt.setObject(1, UUID.fromString(id));

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new Project(
                            UUID.fromString(rs.getString("id")),
                            rs.getString("user_id"),
                            rs.getString("name"),
                            rs.getString("description")
                        );
                    }
                }
                return null;
            }
        }).map(project -> {
            if (project != null) {
                try {
                    String json = objectMapper.writeValueAsString(project);
                    return HttpResponse.ok200()
                        .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withBody(json.getBytes())
                        .build();
                } catch (Exception e) {
                    return HttpResponse.ofCode(500).withPlainText(e.getMessage()).build();
                }
            } else {
                return HttpResponse.ofCode(404).build();
            }
        });
    }

    private Promise<HttpResponse> handleCreateProject(HttpRequest request) {
        return Promise.ofBlocking(dbExecutor, () -> {
            byte[] body = request.getBody().asArray();
            Project project = objectMapper.readValue(body, Project.class);

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
            return project;
        }).map(project -> {
            try {
                String json = objectMapper.writeValueAsString(project);
                return HttpResponse.ofCode(201)
                    .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .withBody(json.getBytes())
                    .build();
            } catch (Exception e) {
                return HttpResponse.ofCode(500).withPlainText(e.getMessage()).build();
            }
        });
    }

    private Promise<HttpResponse> handleDeleteProject(HttpRequest request) {
        String id = request.getPathParameter("id");

        return Promise.ofBlocking(dbExecutor, () -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM project WHERE id = ?")) {

                stmt.setObject(1, UUID.fromString(id));
                stmt.executeUpdate();
            }
            return null;
        }).map(v -> HttpResponse.ofCode(204).build());
    }

    @Override
    protected void onStart() {
        dataSource = createDataSource();
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

    public static void main(String[] args) throws Exception {
        Launcher launcher = new Main();
        launcher.launch(args);
    }
}
