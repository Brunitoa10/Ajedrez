package com.ajedrez.infrastructure.config;

/** Motores de base de datos soportados, con su script de esquema. */
public enum DatabaseType {
    SQLITE("/db/schema-sqlite.sql"),
    POSTGRESQL("/db/schema-postgresql.sql");

    private final String schemaResource;

    DatabaseType(String schemaResource) {
        this.schemaResource = schemaResource;
    }

    public String schemaResource() {
        return schemaResource;
    }

    public static DatabaseType fromConfig(String value) {
        if (value == null) {
            return SQLITE;
        }
        return switch (value.trim().toLowerCase()) {
            case "postgres", "postgresql", "pg" -> POSTGRESQL;
            default -> SQLITE;
        };
    }
}
