package com.ajedrez.infrastructure.persistence;

import com.ajedrez.infrastructure.config.DatabaseType;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Abstracción para obtener conexiones. Los repositorios dependen de esta interfaz y no de un
 * motor concreto (Dependency Inversion), por eso el mismo código funciona con SQLite y PostgreSQL.
 */
public interface ConnectionFactory {

    public Connection open() throws SQLException;

    public DatabaseType type();
}
