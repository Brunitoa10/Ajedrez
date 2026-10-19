package com.ajedrez.ai;

import com.ajedrez.domain.model.Move;
import com.ajedrez.domain.model.Position;

import java.util.Optional;

/**
 * Patrón Strategy: cualquier motor (alfa-beta propio, aleatorio, o un adaptador UCI a Stockfish)
 * se usa igual desde la aplicación.
 */
public interface ChessEngine {

    /** Devuelve la jugada elegida, o vacío si no hay jugadas legales. Debe ser seguro llamarlo desde otro hilo. */
    public Optional<Move> chooseMove(Position position);

    public String name();
}
