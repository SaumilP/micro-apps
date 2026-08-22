package org.sandcastle.apps.web;

import java.util.Properties;

import org.hibernate.cfg.Configuration;
import org.hibernate.reactive.provider.ReactiveServiceRegistryBuilder;
import org.hibernate.reactive.stage.Stage;
import org.sandcastle.apps.entity.Project;
import org.sandcastle.apps.entity.Task;
import org.sandcastle.apps.repository.ProjectRepositoryImpl;
import org.sandcastle.apps.service.ProjectServiceImpl;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        // Execute Hibernate initialization on a worker thread to avoid blocking the event loop
        vertx.<ProjectServiceImpl>executeBlocking(promise -> {
            try {
                // 1. Create properties with config data from environment variables
                String dbHost = System.getenv().getOrDefault("DB_HOST", "localhost");
                String dbPort = System.getenv().getOrDefault("DB_PORT", "3306");
                String dbName = System.getenv().getOrDefault("DB_NAME", "planner");
                String dbUser = System.getenv().getOrDefault("DB_USER", "root");
                String dbPassword = System.getenv().getOrDefault("DB_PASSWORD", "root");

                var hibernateProps = new Properties();
                hibernateProps.put("hibernate.connection.url",
                    String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC", dbHost, dbPort, dbName));
                hibernateProps.put("hibernate.connection.username", dbUser);
                hibernateProps.put("hibernate.connection.password", dbPassword);
                hibernateProps.put("jakarta.persistence.schema-generation.database.action", "update");
                hibernateProps.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
                hibernateProps.put("hibernate.show_sql", false);
                hibernateProps.put("hibernate.format_sql", false);

                // 2. Create hibernate configs
                var hibernateConfig = new Configuration();
                hibernateConfig.setProperties(hibernateProps);
                hibernateConfig.addAnnotatedClass(Project.class);
                hibernateConfig.addAnnotatedClass(Task.class);

                // 3.create service registry
                var serviceRegistry = new ReactiveServiceRegistryBuilder()
                        .applySettings(hibernateConfig.getProperties())
                        .build();

                // 4. Create session-factory
                var sessionFactory = hibernateConfig
                        .buildSessionFactory(serviceRegistry)
                        .unwrap(Stage.SessionFactory.class);

                var projectRepo = new ProjectRepositoryImpl(sessionFactory);
                var projectService = new ProjectServiceImpl(projectRepo);

                promise.complete(projectService);
            } catch (Exception e) {
                promise.fail(e);
            }
        }).onSuccess(projectService -> {
            // 5. Deploy Verticles
            vertx.deployVerticle(new HelloVerticle());
            vertx.deployVerticle(new ProjectVerticle(projectService));

            // 6. Setup API routes
            var router = Router.router(vertx);
            router.get("/api/v1/hello").handler(this::helloVertx);
            router.get("/api/v1/hello/:name").handler(this::helloName);

            // 7. setting type, format and path of configuration file.
            ConfigStoreOptions defaultConfig = new ConfigStoreOptions()
                    .setType("file")
                    .setFormat("json")
                    .setConfig(new JsonObject().put("path", "application-local.json"));

            ConfigRetrieverOptions opts = new ConfigRetrieverOptions().addStore(defaultConfig);
            ConfigRetriever configRetriever = ConfigRetriever.create(vertx, opts);
            Handler<AsyncResult<JsonObject>> handler = asyncResult -> this.handleConfigResults(startPromise, router,
                    asyncResult);
            configRetriever.getConfig(handler);
        }).onFailure(err -> {
            System.err.println("Failed to initialize Hibernate: " + err.getMessage());
            startPromise.fail(err);
        });
    }

    void handleConfigResults(Promise<Void> startPromise, Router router, AsyncResult<JsonObject> asyncResult) {
        if (asyncResult.succeeded()) {
            JsonObject config = asyncResult.result();
            JsonObject httpKey = config.getJsonObject("http");
            int httpPort = httpKey.getInteger("port");

            // created server here and set port number.
            vertx.createHttpServer().requestHandler(router).listen(httpPort, http -> {
                if (http.succeeded()) {
                    startPromise.complete();
                    System.out.println("HTTP server started on port " + httpPort);
                }
            });
        } else {
            // Other stuff here
            startPromise.fail("Unable to load configurations.");
        }
    }

    void helloVertx(RoutingContext ctx) {
        vertx.eventBus().request("hello.vertx.addr", "", reply -> {
            ctx.request().response().end((String) reply.result().body());
        });
    }

    void helloName(RoutingContext ctx) {
        String name = ctx.pathParam("name");
        vertx.eventBus().request("hello.named.addr", name, reply -> {
            ctx.request().response().end((String) reply.result().body());
        });
    }

}
