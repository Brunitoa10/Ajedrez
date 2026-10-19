package com.ajedrez.infrastructure.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Mini plantilla JDBC (idea del patrón Template Method): centraliza abrir/cerrar recursos y
 * traducir excepciones, así cada repositorio solo escribe SQL y mapeo.
 */
final class JdbcTemplate {

    private final ConnectionFactory connections;

    JdbcTemplate(ConnectionFactory connections) {
        this.connections = connections;
    }

    @FunctionalInterface
    interface ParameterSetter {
        public void set(PreparedStatement statement) throws SQLException;
    }

    @FunctionalInterface
    interface RowMapper<T> {
        public T map(ResultSet row) throws SQLException;
    }

    <T> List<T> query(String sql, ParameterSetter params, RowMapper<T> mapper) {
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            params.set(statement);
            try (ResultSet rows = statement.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rows.next()) {
                    result.add(mapper.map(rows));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error consultando la base de datos", e);
        }
    }

    int update(String sql, ParameterSetter params) {
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            params.set(statement);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("Error actualizando la base de datos", e);
        }
    }

    /** Inserta y devuelve la clave generada (columna "id"). */
    long insert(String sql, ParameterSetter params) {
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(sql, new String[]{"id"})) {
            params.set(statement);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
                throw new PersistenceException("La base no devolvió el id generado", null);
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error insertando en la base de datos", e);
        }
    }
}
