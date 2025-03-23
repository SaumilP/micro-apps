package org.sandcastle.apps;

import org.hibernate.cfg.Configuration;
import org.hibernate.reactive.provider.ReactiveServiceRegistryBuilder;
import org.hibernate.reactive.stage.Stage;
import org.sandcastle.apps.entity.Project;
import org.sandcastle.apps.entity.Task;
import org.sandcastle.apps.repository.ProjectRepositoryImpl;
import org.sandcastle.apps.service.ProjectServiceImpl;
import org.sandcastle.apps.web.ProjectVerticle;

import io.vertx.core.Vertx;

import java.util.Properties;

public class Application {
    public static void main(String[] args) {
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
        hibernateConfig.addAnnotatedClass(Task.class);
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
        Vertx.vertx().deployVerticle(new ProjectVerticle(projectService));
    }
}
