package com.ajedrez.domain.rules;

import com.ajedrez.domain.model.PieceType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Fábrica/registro que asocia cada tipo de pieza con su estrategia de movimiento.
 */
public final class MoveStrategyRegistry {

    private final Map<PieceType, PieceMoveStrategy> strategies = new EnumMap<>(PieceType.class);

    public MoveStrategyRegistry(AttackDetector attackDetector) {
        strategies.put(PieceType.PAWN, new PawnMoveStrategy());
        strategies.put(PieceType.KNIGHT, new StepMoveStrategy(Directions.KNIGHT));
        strategies.put(PieceType.BISHOP, new SlidingMoveStrategy(Directions.DIAGONAL));
        strategies.put(PieceType.ROOK, new SlidingMoveStrategy(Directions.ORTHOGONAL));
        strategies.put(PieceType.QUEEN, new SlidingMoveStrategy(Directions.ALL));
        strategies.put(PieceType.KING, new KingMoveStrategy(attackDetector));
    }

    public PieceMoveStrategy forType(PieceType type) {
        return strategies.get(type);
    }
}
