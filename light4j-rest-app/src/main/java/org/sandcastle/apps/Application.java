package org.sandcastle.apps;

import com.networknt.server.Server;
import com.networknt.server.ServerConfig;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.util.Methods;
import org.sandcastle.apps.handler.HealthHandler;
import org.sandcastle.apps.handler.ProjectsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Light4J REST Application
 * High-performance, minimal-size microservice for API endpoints with PostgreSQL integration
 */
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        logger.info("Starting Light4J REST Application...");

        // Initialize database connection pool
        DatabaseService.getInstance();

        // Create routing handler
        RoutingHandler router = Handlers.routing()
            .add(Methods.GET, "/health", new HealthHandler())
            .add(Methods.GET, "/api/projects", new ProjectsHandler())
            .add(Methods.GET, "/api/projects/{id}", new ProjectsHandler())
            .add(Methods.POST, "/api/projects", new ProjectsHandler())
            .add(Methods.DELETE, "/api/projects/{id}", new ProjectsHandler());

        // Build and start server
        Undertow server = Undertow.builder()
            .addHttpListener(8080, "0.0.0.0")
            .setHandler(router)
            .build();

        server.start();

        logger.info("Light4J REST Application started on port 8080");
    }
}
