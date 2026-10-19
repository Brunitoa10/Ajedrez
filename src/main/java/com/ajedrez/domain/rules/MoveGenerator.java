package com.ajedrez.domain.rules;

import com.ajedrez.domain.model.Move;
import com.ajedrez.domain.model.Position;
import com.ajedrez.domain.model.Square;

import java.util.List;

/**
 * Abstracción de la generación de jugadas legales (permite reemplazarla por una de bitboards
 * sin tocar a quienes la usan: Dependency Inversion).
 */
public interface MoveGenerator {

    public List<Move> legalMoves(Position position);

    public List<Move> legalMovesFrom(Position position, Square from);
}
