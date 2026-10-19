package com.ajedrez.infrastructure.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

/** Crea las tablas si no existen, con el script del dialecto correspondiente. */
public final class SchemaInitializer {

    private final ConnectionFactory connections;

    public SchemaInitializer(ConnectionFactory connections) {
        this.connections = connections;
    }

    public void initialize() {
        String script = readScript(connections.type().schemaResource());
        try (Connection connection = connections.open(); Statement statement = connection.createStatement()) {
            for (String sql : splitStatements(script)) {
                statement.execute(sql);
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo inicializar la base de datos", e);
        }
    }

    private static String[] splitStatements(String script) {
        return Arrays.stream(script.split(";"))
                .map(String::trim)
                .filter(sql -> !sql.isEmpty())
                .toArray(String[]::new);
    }

    private static String readScript(String resource) {
        try (InputStream in = SchemaInitializer.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("No se encontró el script " + resource);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el script " + resource, e);
        }
    }
}
