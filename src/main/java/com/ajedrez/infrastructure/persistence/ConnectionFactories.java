package com.ajedrez.infrastructure.persistence;

import com.ajedrez.infrastructure.config.DatabaseConfig;

/** Patrón Factory Method: elige la implementación de {@link ConnectionFactory} según la configuración. */
public final class ConnectionFactories {

    private ConnectionFactories() {
    }

    public static ConnectionFactory create(DatabaseConfig config) {
        return switch (config.type()) {
            case SQLITE -> new SqliteConnectionFactory(config.jdbcUrl());
            case POSTGRESQL -> new PostgresConnectionFactory(config.jdbcUrl(), config.user(), config.password());
        };
    }
}
