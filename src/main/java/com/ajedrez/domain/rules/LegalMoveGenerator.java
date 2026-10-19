package com.ajedrez.domain.rules;

import com.ajedrez.domain.model.Move;
import com.ajedrez.domain.model.Piece;
import com.ajedrez.domain.model.PieceColor;
import com.ajedrez.domain.model.Position;
import com.ajedrez.domain.model.Square;

import java.util.ArrayList;
import java.util.List;

/**
 * Genera jugadas pseudo-legales con las estrategias de cada pieza y descarta las que dejan
 * al propio rey en jaque.
 */
public final class LegalMoveGenerator implements MoveGenerator {

    private final MoveStrategyRegistry registry;
    private final AttackDetector attackDetector;

    public LegalMoveGenerator(MoveStrategyRegistry registry, AttackDetector attackDetector) {
        this.registry = registry;
        this.attackDetector = attackDetector;
    }

    @Override
    public List<Move> legalMoves(Position position) {
        List<Move> pseudo = new ArrayList<>(48);
        for (Square from : position.board().squaresOf(position.sideToMove())) {
            addPseudoLegalFrom(position, from, pseudo);
        }
        return filterLegal(position, pseudo);
    }

    @Override
    public List<Move> legalMovesFrom(Position position, Square from) {
        if (!position.board().isOccupiedBy(from, position.sideToMove())) {
            return List.of();
        }
        List<Move> pseudo = new ArrayList<>(16);
        addPseudoLegalFrom(position, from, pseudo);
        return filterLegal(position, pseudo);
    }

    private void addPseudoLegalFrom(Position position, Square from, List<Move> out) {
        Piece piece = position.board().getOrNull(from);
        registry.forType(piece.type()).addPseudoLegalMoves(position, from, out);
    }

    private List<Move> filterLegal(Position position, List<Move> pseudo) {
        PieceColor mover = position.sideToMove();
        List<Move> legal = new ArrayList<>(pseudo.size());
        for (Move move : pseudo) {
            Position after = position.apply(move);
            if (!attackDetector.isKingInCheck(after.board(), mover)) {
                legal.add(move);
            }
        }
        return legal;
    }
}
