package org.sandcastle.apps;

import io.helidon.http.Status;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProjectService implements HttpService {

    private final DataSource dataSource;

    public ProjectService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void routing(HttpRules rules) {
        rules
            .get("/health", this::health)
            .get("/api/projects", this::list)
            .get("/api/projects/{id}", this::get)
            .post("/api/projects", this::create)
            .delete("/api/projects/{id}", this::delete);
    }

    private void health(ServerRequest request, ServerResponse response) {
        response.send("OK");
    }

    private void list(ServerRequest request, ServerResponse response) {
        String userId = request.query().first("userId").orElse(null);
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
            response.send(projects);
        } catch (SQLException e) {
            response.status(Status.INTERNAL_SERVER_ERROR_500).send(e.getMessage());
        }
    }

    private void get(ServerRequest request, ServerResponse response) {
        String id = request.path().pathParameters().get("id");

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
                    response.send(project);
                } else {
                    response.status(Status.NOT_FOUND_404).send();
                }
            }
        } catch (SQLException e) {
            response.status(Status.INTERNAL_SERVER_ERROR_500).send(e.getMessage());
        }
    }

    private void create(ServerRequest request, ServerResponse response) {
        try {
            Project project = request.content().as(Project.class);

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

                response.status(Status.CREATED_201).send(project);
            }
        } catch (SQLException e) {
            response.status(Status.INTERNAL_SERVER_ERROR_500).send(e.getMessage());
        }
    }

    private void delete(ServerRequest request, ServerResponse response) {
        String id = request.path().pathParameters().get("id");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM project WHERE id = ?")) {

            stmt.setObject(1, UUID.fromString(id));
            stmt.executeUpdate();

            response.status(Status.NO_CONTENT_204).send();
        } catch (SQLException e) {
            response.status(Status.INTERNAL_SERVER_ERROR_500).send(e.getMessage());
        }
    }
}
