package com.ajedrez.infrastructure.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

/**
 * Configuración de la base de datos leída de {@code config/ajedrez.properties}.
 * Si el archivo no existe se crea con SQLite por defecto y un ejemplo comentado de PostgreSQL.
 */
public record DatabaseConfig(DatabaseType type, String jdbcUrl, String user, String password) {

    public static final String FILE_NAME = "ajedrez.properties";
    private static final String DEFAULT_TEMPLATE = "/config/ajedrez.default.properties";

    public DatabaseConfig {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(jdbcUrl, "jdbcUrl");
    }

    public static DatabaseConfig sqlite(Path databaseFile) {
        return new DatabaseConfig(DatabaseType.SQLITE, "jdbc:sqlite:" + databaseFile.toAbsolutePath(), null, null);
    }

    public static DatabaseConfig load(AppPaths paths) {
        Path file = paths.configDir().resolve(FILE_NAME);
        if (Files.notExists(file)) {
            writeDefaultTemplate(file);
        }
        Properties props = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            props.load(reader);
        } catch (IOException e) {
            return defaultSqlite(paths);
        }
        DatabaseType type = DatabaseType.fromConfig(props.getProperty("db.type"));
        if (type == DatabaseType.POSTGRESQL) {
            return new DatabaseConfig(type,
                    props.getProperty("db.postgres.url", "jdbc:postgresql://localhost:5432/ajedrez"),
                    props.getProperty("db.postgres.user", "postgres"),
                    props.getProperty("db.postgres.password", ""));
        }
        String relative = props.getProperty("db.sqlite.file", "data/ajedrez.db");
        Path dbFile = paths.home().resolve(relative);
        return sqlite(dbFile);
    }

    public static DatabaseConfig defaultSqlite(AppPaths paths) {
        return sqlite(paths.dataDir().resolve("ajedrez.db"));
    }

    /** Descripción para mostrar al usuario, sin la contraseña. */
    public String describe() {
        return type == DatabaseType.SQLITE
                ? "SQLite (" + jdbcUrl.substring("jdbc:sqlite:".length()) + ")"
                : "PostgreSQL (" + jdbcUrl + ")";
    }

    private static void writeDefaultTemplate(Path file) {
        try (InputStream template = DatabaseConfig.class.getResourceAsStream(DEFAULT_TEMPLATE)) {
            if (template != null) {
                Files.copy(template, file);
            }
        } catch (IOException e) {
            // Si no se puede escribir la plantilla se sigue con los valores por defecto.
        }
    }
}
