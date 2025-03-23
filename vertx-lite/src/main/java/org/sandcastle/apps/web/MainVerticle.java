package org.sandcastle.apps.web;

import java.util.Properties;

import org.hibernate.cfg.Configuration;
import org.hibernate.reactive.provider.ReactiveServiceRegistryBuilder;
import org.hibernate.reactive.stage.Stage;
import org.sandcastle.apps.entity.Project;
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
import io.vertx.core.net.impl.pool.Task;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        // 1. Create properties with config data
        var hibernateProps = new Properties();
        hibernateProps.put("hibernate.connection.url", "jdbc:mysql://localhost:3306/planner?serverTimezone=UTC");
        hibernateProps.put("hibernate.connection.username", "root");
        hibernateProps.put("hibernate.connection.password", "root");
        hibernateProps.put("jakarta.persistence.schema-generation.database.action", "update");
        hibernateProps.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        hibernateProps.put("hibernate.show_sql", true);
        hibernateProps.put("hibernate.format_sql", true);
        // hibernateProps.put("hibernate.generate_statistics", true);

        // 2. Create hibernate configs
        var hibernateConfig = new Configuration();
        hibernateConfig.setProperties(hibernateProps);
        hibernateConfig.addAnnotatedClass(Project.class);

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

        // 5. Deploy Verticle
        vertx.deployVerticle(new HelloVerticle());
        vertx.deployVerticle(new ProjectVerticle(projectService));

        // 6. Setup API routes
        var router = Router.router(vertx);
        router.get("/api/v1/hello").handler(this::helloVertx);
        router.get("/api/v1/hello/:name").handler(this::helloName);

        // 6. setting type, format and path of configuration file.
        ConfigStoreOptions defaultConfig = new ConfigStoreOptions()
                .setType("file")
                .setFormat("json")
                .setConfig(new JsonObject().put("path", "config/application-local.json"));

        ConfigRetrieverOptions opts = new ConfigRetrieverOptions().addStore(defaultConfig);
        ConfigRetriever configRetriever = ConfigRetriever.create(vertx, opts);
        Handler<AsyncResult<JsonObject>> handler = asyncResult -> this.handleConfigResults(startPromise, router,
                asyncResult);
        configRetriever.getConfig(handler);
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
