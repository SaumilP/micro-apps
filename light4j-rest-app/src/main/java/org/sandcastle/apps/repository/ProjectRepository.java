package org.sandcastle.apps.repository;

import org.sandcastle.apps.DatabaseService;
import org.sandcastle.apps.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Project database operations
 * Uses prepared statements for security and performance
 */
public class ProjectRepository {
    private static final Logger logger = LoggerFactory.getLogger(ProjectRepository.class);
    private final DatabaseService databaseService;

    public ProjectRepository() {
        this.databaseService = DatabaseService.getInstance();
        initializeSchema();
    }

    /**
     * Initialize database schema if not exists
     */
    private void initializeSchema() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS projects (
                id UUID PRIMARY KEY,
                user_id VARCHAR(255) NOT NULL,
                name VARCHAR(255) NOT NULL,
                description TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;

        try (Connection conn = databaseService.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            logger.info("Database schema initialized");
        } catch (SQLException e) {
            logger.error("Failed to initialize database schema", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    /**
     * Create a new project
     */
    public Project create(Project project) {
        String sql = "INSERT INTO projects (id, user_id, name, description) VALUES (?, ?, ?, ?)";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            UUID id = UUID.randomUUID();
            stmt.setObject(1, id);
            stmt.setString(2, project.getUserId());
            stmt.setString(3, project.getName());
            stmt.setString(4, project.getDescription());

            stmt.executeUpdate();
            project.setId(id);

            logger.debug("Project created: {}", id);
            return project;
        } catch (SQLException e) {
            logger.error("Failed to create project", e);
            throw new RuntimeException("Failed to create project", e);
        }
    }

    /**
     * Find project by ID
     */
    public Optional<Project> findById(UUID id) {
        String sql = "SELECT id, user_id, name, description FROM projects WHERE id = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to find project by id: {}", id, e);
        }

        return Optional.empty();
    }

    /**
     * Find all projects by user ID
     */
    public List<Project> findByUserId(String userId) {
        String sql = "SELECT id, user_id, name, description FROM projects WHERE user_id = ?";
        List<Project> projects = new ArrayList<>();

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    projects.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to find projects by user id: {}", userId, e);
        }

        return projects;
    }

    /**
     * Delete project by ID
     */
    public boolean delete(UUID id) {
        String sql = "DELETE FROM projects WHERE id = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);
            int rowsAffected = stmt.executeUpdate();

            logger.debug("Project deleted: {}", id);
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Failed to delete project: {}", id, e);
            return false;
        }
    }

    /**
     * Get all projects
     */
    public List<Project> findAll() {
        String sql = "SELECT id, user_id, name, description FROM projects";
        List<Project> projects = new ArrayList<>();

        try (Connection conn = databaseService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                projects.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Failed to find all projects", e);
        }

        return projects;
    }

    /**
     * Map ResultSet to Project object
     */
    private Project mapResultSet(ResultSet rs) throws SQLException {
        Project project = new Project();
        project.setId((UUID) rs.getObject("id"));
        project.setUserId(rs.getString("user_id"));
        project.setName(rs.getString("name"));
        project.setDescription(rs.getString("description"));
        return project;
    }
}
