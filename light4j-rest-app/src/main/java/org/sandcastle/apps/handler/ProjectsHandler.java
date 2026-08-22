package org.sandcastle.apps.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.handler.LightHttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.sandcastle.apps.model.Project;
import org.sandcastle.apps.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Handler for Project CRUD operations
 * Supports GET (all/by-user/by-id), POST (create), DELETE operations
 */
public class ProjectsHandler implements LightHttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(ProjectsHandler.class);
    private final ProjectRepository repository;
    private final ObjectMapper objectMapper;

    public ProjectsHandler() {
        this.repository = new ProjectRepository();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        String method = exchange.getRequestMethod().toString();
        String path = exchange.getRelativePath();

        try {
            switch (method) {
                case "GET" -> handleGet(exchange, path);
                case "POST" -> handlePost(exchange);
                case "DELETE" -> handleDelete(exchange, path);
                default -> {
                    exchange.setStatusCode(StatusCodes.METHOD_NOT_ALLOWED);
                    exchange.getResponseSender().send("{\"error\":\"Method not allowed\"}");
                }
            }
        } catch (Exception e) {
            logger.error("Error handling request", e);
            exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseSender().send("{\"error\":\"Internal server error\"}");
        }
    }

    /**
     * Handle GET requests
     * GET /api/projects - Get all projects
     * GET /api/projects?userId=xxx - Get projects by user
     * GET /api/projects/{id} - Get project by ID
     */
    private void handleGet(HttpServerExchange exchange, String path) throws Exception {
        // Check if path has ID parameter
        if (path.matches("/api/projects/[0-9a-f-]+")) {
            String idStr = path.substring("/api/projects/".length());
            UUID id = UUID.fromString(idStr);
            Optional<Project> project = repository.findById(id);

            if (project.isPresent()) {
                exchange.setStatusCode(StatusCodes.OK);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseSender().send(objectMapper.writeValueAsString(project.get()));
            } else {
                exchange.setStatusCode(StatusCodes.NOT_FOUND);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseSender().send("{\"error\":\"Project not found\"}");
            }
        } else {
            // Check for userId query parameter
            Deque<String> userIdParam = exchange.getQueryParameters().get("userId");

            List<Project> projects;
            if (userIdParam != null && !userIdParam.isEmpty()) {
                String userId = userIdParam.getFirst();
                projects = repository.findByUserId(userId);
            } else {
                projects = repository.findAll();
            }

            exchange.setStatusCode(StatusCodes.OK);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseSender().send(objectMapper.writeValueAsString(projects));
        }
    }

    /**
     * Handle POST requests - Create new project
     * POST /api/projects
     */
    private void handlePost(HttpServerExchange exchange) throws Exception {
        exchange.getRequestReceiver().receiveFullBytes((ex, data) -> {
            try {
                Project project = objectMapper.readValue(data, Project.class);
                Project created = repository.create(project);

                ex.setStatusCode(StatusCodes.CREATED);
                ex.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                ex.getResponseSender().send(objectMapper.writeValueAsString(created));
            } catch (Exception e) {
                logger.error("Error creating project", e);
                ex.setStatusCode(StatusCodes.BAD_REQUEST);
                ex.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                ex.getResponseSender().send("{\"error\":\"Invalid request body\"}");
            }
        });
    }

    /**
     * Handle DELETE requests
     * DELETE /api/projects/{id}
     */
    private void handleDelete(HttpServerExchange exchange, String path) {
        if (path.matches("/api/projects/[0-9a-f-]+")) {
            String idStr = path.substring("/api/projects/".length());
            UUID id = UUID.fromString(idStr);
            boolean deleted = repository.delete(id);

            if (deleted) {
                exchange.setStatusCode(StatusCodes.NO_CONTENT);
                exchange.getResponseSender().send("");
            } else {
                exchange.setStatusCode(StatusCodes.NOT_FOUND);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseSender().send("{\"error\":\"Project not found\"}");
            }
        } else {
            exchange.setStatusCode(StatusCodes.BAD_REQUEST);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseSender().send("{\"error\":\"Invalid project ID\"}");
        }
    }
}
