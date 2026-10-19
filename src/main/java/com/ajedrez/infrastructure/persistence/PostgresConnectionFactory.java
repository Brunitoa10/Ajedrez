package com.ajedrez.infrastructure.persistence;

import com.ajedrez.infrastructure.config.DatabaseType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Conexiones a un servidor PostgreSQL. */
final class PostgresConnectionFactory implements ConnectionFactory {

    private static final String CONNECT_TIMEOUT_SECONDS = "5";

    private final String jdbcUrl;
    private final Properties properties = new Properties();

    public PostgresConnectionFactory(String jdbcUrl, String user, String password) {
        this.jdbcUrl = jdbcUrl;
        properties.setProperty("user", user == null ? "" : user);
        properties.setProperty("password", password == null ? "" : password);
        properties.setProperty("connectTimeout", CONNECT_TIMEOUT_SECONDS);
        properties.setProperty("ApplicationName", "Ajedrez");
    }

    @Override
    public Connection open() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, properties);
    }

    @Override
    public DatabaseType type() {
        return DatabaseType.POSTGRESQL;
    }
}
