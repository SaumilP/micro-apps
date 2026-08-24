package org.sandcastle.apps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.annotation.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Main {

    private static DataSource dataSource;
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        dataSource = createDataSource();

        Server server = Server.builder()
            .http(getPort())
            .annotatedService(new ProjectService())
            .annotatedService(new HealthService())
            .build();

        server.start().join();
        System.out.println("Armeria server started on port: " + getPort());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop().join();
        }));
    }

    public static class HealthService {
        @Get("/health")
        public HttpResponse health() {
            return HttpResponse.of("OK");
        }
    }

    public static class ProjectService {

        @Get("/api/projects")
        @ProducesJson
        public HttpResponse listProjects(@Param("userId") @Default("") String userId) {
            List<Project> projects = new ArrayList<>();

            try (Connection conn = dataSource.getConnection()) {
                String sql = userId != null && !userId.isEmpty()
                    ? "SELECT id, user_id, name, description FROM project WHERE user_id = ?"
                    : "SELECT id, user_id, name, description FROM project";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    if (userId != null && !userId.isEmpty()) {
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

                String json = objectMapper.writeValueAsString(projects);
                return HttpResponse.of(HttpStatus.OK, com.linecorp.armeria.common.MediaType.JSON, json);
            } catch (Exception e) {
                return HttpResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                    com.linecorp.armeria.common.MediaType.PLAIN_TEXT_UTF_8, e.getMessage());
            }
        }

        @Get("/api/projects/{id}")
        @ProducesJson
        public HttpResponse getProject(@Param("id") String id) {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id, user_id, name, description FROM project WHERE id = ?")) {

                stmt.setObject(1, UUID.fromString(id));

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Project project = new Project(
                            UUID.fromString(rs.getString("id")),
                            rs.getString("user_id"),
                            rs.getString("name"),
                            rs.getString("description")
                        );
                        String json = objectMapper.writeValueAsString(project);
                        return HttpResponse.of(HttpStatus.OK, com.linecorp.armeria.common.MediaType.JSON, json);
                    } else {
                        return HttpResponse.of(HttpStatus.NOT_FOUND);
                    }
                }
            } catch (Exception e) {
                return HttpResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                    com.linecorp.armeria.common.MediaType.PLAIN_TEXT_UTF_8, e.getMessage());
            }
        }

        @Post("/api/projects")
        @ConsumesJson
        @ProducesJson
        public HttpResponse createProject(String jsonBody) {
            try {
                Project project = objectMapper.readValue(jsonBody, Project.class);

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
                return HttpResponse.of(HttpStatus.CREATED, com.linecorp.armeria.common.MediaType.JSON, json);
            } catch (Exception e) {
                return HttpResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                    com.linecorp.armeria.common.MediaType.PLAIN_TEXT_UTF_8, e.getMessage());
            }
        }

        @Delete("/api/projects/{id}")
        public HttpResponse deleteProject(@Param("id") String id) {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM project WHERE id = ?")) {

                stmt.setObject(1, UUID.fromString(id));
                stmt.executeUpdate();

                return HttpResponse.of(HttpStatus.NO_CONTENT);
            } catch (SQLException e) {
                return HttpResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                    com.linecorp.armeria.common.MediaType.PLAIN_TEXT_UTF_8, e.getMessage());
            }
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

    private static int getPort() {
        String port = System.getenv("SERVER_PORT");
        return port != null ? Integer.parseInt(port) : 8085;
    }
}
