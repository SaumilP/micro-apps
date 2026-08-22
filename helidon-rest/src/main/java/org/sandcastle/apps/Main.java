package org.sandcastle.apps;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.helidon.config.Config;
import io.helidon.webserver.WebServer;

import javax.sql.DataSource;

public class Main {

    public static void main(String[] args) {
        Config config = Config.create();

        DataSource dataSource = createDataSource();

        WebServer server = WebServer.builder()
            .config(config.get("server"))
            .routing(routing -> routing
                .register(new ProjectService(dataSource)))
            .build()
            .start();

        System.out.println("Helidon server started on port: " + server.port());
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
}
