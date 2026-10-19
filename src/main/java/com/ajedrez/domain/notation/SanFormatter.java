package com.ajedrez.domain.notation;

import com.ajedrez.domain.model.Move;
import com.ajedrez.domain.model.Piece;
import com.ajedrez.domain.model.PieceType;
import com.ajedrez.domain.model.Position;
import com.ajedrez.domain.rules.ChessRules;

import java.util.List;

/**
 * Convierte jugadas a notación algebraica estándar (SAN): "Nf3", "exd5", "O-O", "e8=Q#".
 */
public final class SanFormatter {

    private final ChessRules rules;

    public SanFormatter(ChessRules rules) {
        this.rules = rules;
    }

    /** @param before posición previa a la jugada (la jugada debe ser legal en ella). */
    public String format(Position before, Move move) {
        String body = before.isCastling(move) ? castlingNotation(move) : standardNotation(before, move);
        return body + checkSuffix(before.apply(move));
    }

    private String castlingNotation(Move move) {
        return move.to().file() > move.from().file() ? "O-O" : "O-O-O";
    }

    private String standardNotation(Position before, Move move) {
        Piece piece = before.board().getOrNull(move.from());
        boolean capture = before.isCapture(move);
        StringBuilder san = new StringBuilder();

        if (piece.type() == PieceType.PAWN) {
            if (capture) {
                san.append(move.from().fileChar()).append('x');
            }
        } else {
            san.append(piece.type().sanLetter()).append(disambiguation(before, move, piece));
            if (capture) {
                san.append('x');
            }
        }
        san.append(move.to());
        move.promotion().ifPresent(type -> san.append('=').append(type.sanLetter()));
        return san.toString();
    }

    /** Agrega columna, fila o ambas si otra pieza igual puede ir a la misma casilla. */
    private String disambiguation(Position before, Move move, Piece piece) {
        List<Move> rivals = rules.legalMoves(before).stream()
                .filter(m -> m.to().equals(move.to()))
                .filter(m -> !m.from().equals(move.from()))
                .filter(m -> before.board().getOrNull(m.from()) == piece)
                .toList();
        if (rivals.isEmpty()) {
            return "";
        }
        boolean sameFile = rivals.stream().anyMatch(m -> m.from().file() == move.from().file());
        boolean sameRank = rivals.stream().anyMatch(m -> m.from().rank() == move.from().rank());
        if (!sameFile) {
            return String.valueOf(move.from().fileChar());
        }
        if (!sameRank) {
            return String.valueOf(move.from().rankChar());
        }
        return move.from().toString();
    }

    private String checkSuffix(Position after) {
        if (!rules.isInCheck(after)) {
            return "";
        }
        return rules.legalMoves(after).isEmpty() ? "#" : "+";
    }
}
