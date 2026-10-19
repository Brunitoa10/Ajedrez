package com.ajedrez.application.port;

import com.ajedrez.application.GameRecord;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida (arquitectura hexagonal): la aplicación define qué necesita guardar
 * y la infraestructura decide cómo (SQLite, PostgreSQL, memoria para tests...).
 */
public interface GameRepository {

    /** Guarda la partida y devuelve una copia con el id asignado. */
    public GameRecord save(GameRecord record);

    /** Partidas ordenadas de la más reciente a la más antigua. */
    public List<GameRecord> findAll();

    public Optional<GameRecord> findById(long id);

    public void delete(long id);
}
