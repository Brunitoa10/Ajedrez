package com.ajedrez.infrastructure.persistence;

import com.ajedrez.infrastructure.config.DatabaseType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Conexiones a un archivo SQLite (embebido, sin servidor: ideal para la versión portable). */
final class SqliteConnectionFactory implements ConnectionFactory {

    private static final String PREFIX = "jdbc:sqlite:";

    private final String jdbcUrl;

    public SqliteConnectionFactory(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        createParentDirectory(jdbcUrl);
    }

    @Override
    public Connection open() throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 3000");
        }
        return connection;
    }

    @Override
    public DatabaseType type() {
        return DatabaseType.SQLITE;
    }

    private static void createParentDirectory(String jdbcUrl) {
        if (!jdbcUrl.startsWith(PREFIX) || jdbcUrl.contains(":memory:")) {
            return;
        }
        Path parent = Paths.get(jdbcUrl.substring(PREFIX.length())).toAbsolutePath().getParent();
        try {
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            throw new PersistenceException("No se pudo crear la carpeta de la base de datos: " + parent, e);
        }
    }
}
