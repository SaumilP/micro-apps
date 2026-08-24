package org.sandcastle.apps;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.javalin.Javalin;
import io.javalin.http.Context;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Main {

    private static DataSource dataSource;

    public static void main(String[] args) {
        dataSource = createDataSource();

        Javalin app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
        }).start(getPort());

        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/api/projects", Main::listProjects);
        app.get("/api/projects/{id}", Main::getProject);
        app.post("/api/projects", Main::createProject);
        app.delete("/api/projects/{id}", Main::deleteProject);

        System.out.println("Javalin server started on port: " + getPort());
    }

    private static void listProjects(Context ctx) {
        String userId = ctx.queryParam("userId");
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
            ctx.json(projects);
        } catch (SQLException e) {
            ctx.status(500).result(e.getMessage());
        }
    }

    private static void getProject(Context ctx) {
        String id = ctx.pathParam("id");

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
                    ctx.json(project);
                } else {
                    ctx.status(404);
                }
            }
        } catch (SQLException e) {
            ctx.status(500).result(e.getMessage());
        }
    }

    private static void createProject(Context ctx) {
        Project project = ctx.bodyAsClass(Project.class);

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

            ctx.status(201).json(project);
        } catch (SQLException e) {
            ctx.status(500).result(e.getMessage());
        }
    }

    private static void deleteProject(Context ctx) {
        String id = ctx.pathParam("id");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM project WHERE id = ?")) {

            stmt.setObject(1, UUID.fromString(id));
            stmt.executeUpdate();

            ctx.status(204);
        } catch (SQLException e) {
            ctx.status(500).result(e.getMessage());
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
        return port != null ? Integer.parseInt(port) : 8084;
    }
}
