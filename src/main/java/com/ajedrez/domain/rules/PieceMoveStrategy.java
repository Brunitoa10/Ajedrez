package com.ajedrez.domain.rules;

import com.ajedrez.domain.model.Move;
import com.ajedrez.domain.model.Position;
import com.ajedrez.domain.model.Square;

import java.util.List;

/**
 * Patrón Strategy: cada tipo de pieza sabe generar sus movimientos pseudo-legales
 * (respetan cómo se mueve la pieza, pero todavía no verifican si dejan al propio rey en jaque).
 * Agregar una pieza nueva (variantes de ajedrez) no obliga a tocar el generador: Open/Closed.
 */
public interface PieceMoveStrategy {

    public void addPseudoLegalMoves(Position position, Square from, List<Move> moves);
}
