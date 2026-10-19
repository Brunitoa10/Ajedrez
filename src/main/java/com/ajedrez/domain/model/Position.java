package com.ajedrez.domain.model;

import java.util.Objects;
import java.util.Optional;

/**
 * Estado completo e inmutable de una partida en un instante: tablero, turno, enroques,
 * casilla al paso y relojes. Cada jugada produce una posición nueva ({@link #apply(Move)}),
 * por lo que una posición funciona como "memento" para deshacer y como nodo del árbol de búsqueda.
 */
public final class Position {

    public static final String STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private final Board board;
    private final PieceColor sideToMove;
    private final CastlingRights castlingRights;
    private final Square enPassantTarget;
    private final int halfmoveClock;
    private final int fullmoveNumber;

    public Position(Board board, PieceColor sideToMove, CastlingRights castlingRights,
                    Square enPassantTarget, int halfmoveClock, int fullmoveNumber) {
        this.board = Objects.requireNonNull(board, "board");
        this.sideToMove = Objects.requireNonNull(sideToMove, "sideToMove");
        this.castlingRights = Objects.requireNonNull(castlingRights, "castlingRights");
        this.enPassantTarget = enPassantTarget;
        this.halfmoveClock = halfmoveClock;
        this.fullmoveNumber = fullmoveNumber;
    }

    public static Position initial() {
        return FenCodec.parse(STARTING_FEN);
    }

    public static Position fromFen(String fen) {
        return FenCodec.parse(fen);
    }

    public Board board() {
        return board;
    }

    public PieceColor sideToMove() {
        return sideToMove;
    }

    public CastlingRights castlingRights() {
        return castlingRights;
    }

    public Optional<Square> enPassantTarget() {
        return Optional.ofNullable(enPassantTarget);
    }

    public int halfmoveClock() {
        return halfmoveClock;
    }

    public int fullmoveNumber() {
        return fullmoveNumber;
    }

    public String toFen() {
        return FenCodec.format(this);
    }

    /**
     * Clave de repetición: tablero + turno + enroques + al paso (sin relojes).
     * Dos posiciones con la misma clave cuentan como la "misma posición" para la triple repetición.
     */
    public String repetitionKey() {
        String fen = toFen();
        int cut = nthIndexOf(fen, ' ', 4);
        return cut < 0 ? fen : fen.substring(0, cut);
    }

    public boolean isCapture(Move move) {
        return !board.isEmpty(move.to()) || isEnPassant(move);
    }

    public boolean isEnPassant(Move move) {
        Piece piece = board.getOrNull(move.from());
        return piece != null
                && piece.type() == PieceType.PAWN
                && move.to().equals(enPassantTarget)
                && move.from().file() != move.to().file()
                && board.isEmpty(move.to());
    }

    public boolean isCastling(Move move) {
        Piece piece = board.getOrNull(move.from());
        return piece != null
                && piece.type() == PieceType.KING
                && Math.abs(move.to().file() - move.from().file()) == 2;
    }

    /** Pieza capturada por la jugada, si la hay (contempla captura al paso). */
    public Optional<Piece> capturedPiece(Move move) {
        if (isEnPassant(move)) {
            return Optional.of(Piece.of(sideToMove.opposite(), PieceType.PAWN));
        }
        return board.pieceAt(move.to());
    }

    /**
     * Aplica una jugada y devuelve la posición resultante. No valida legalidad:
     * eso es responsabilidad de {@code ChessRules}.
     */
    public Position apply(Move move) {
        Piece moving = board.getOrNull(move.from());
        if (moving == null) {
            throw new IllegalArgumentException("No hay pieza en " + move.from());
        }
        boolean capture = isCapture(move);
        boolean enPassant = isEnPassant(move);
        boolean castling = isCastling(move);

        Board.Builder next = board.toBuilder();
        next.remove(move.from());
        next.put(move.to(), move.isPromotion() ? Piece.of(sideToMove, move.promotionPiece()) : moving);

        if (enPassant) {
            next.remove(Square.of(move.to().file(), move.from().rank()));
        }
        if (castling) {
            moveRookForCastling(next, move);
        }

        CastlingRights rights = castlingRights;
        if (moving.type() == PieceType.KING) {
            rights = rights.revokeAll(sideToMove);
        }
        rights = rights.revokeForCorner(move.from()).revokeForCorner(move.to());

        Square newEnPassant = null;
        if (moving.type() == PieceType.PAWN && Math.abs(move.to().rank() - move.from().rank()) == 2) {
            newEnPassant = Square.of(move.from().file(), (move.from().rank() + move.to().rank()) / 2);
        }

        int newHalfmove = (moving.type() == PieceType.PAWN || capture) ? 0 : halfmoveClock + 1;
        int newFullmove = sideToMove == PieceColor.BLACK ? fullmoveNumber + 1 : fullmoveNumber;

        return new Position(next.build(), sideToMove.opposite(), rights, newEnPassant, newHalfmove, newFullmove);
    }

    private void moveRookForCastling(Board.Builder next, Move kingMove) {
        int rank = kingMove.from().rank();
        boolean kingSide = kingMove.to().file() > kingMove.from().file();
        Square rookFrom = Square.of(kingSide ? 7 : 0, rank);
        Square rookTo = Square.of(kingSide ? 5 : 3, rank);
        Piece rook = next.get(rookFrom);
        next.remove(rookFrom);
        next.put(rookTo, rook);
    }

    private static int nthIndexOf(String text, char c, int n) {
        int index = -1;
        for (int i = 0; i < n; i++) {
            index = text.indexOf(c, index + 1);
            if (index < 0) {
                return -1;
            }
        }
        return index;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Position that)) return false;
        return halfmoveClock == that.halfmoveClock
                && fullmoveNumber == that.fullmoveNumber
                && board.equals(that.board)
                && sideToMove == that.sideToMove
                && castlingRights.equals(that.castlingRights)
                && Objects.equals(enPassantTarget, that.enPassantTarget);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, sideToMove, castlingRights, enPassantTarget, halfmoveClock, fullmoveNumber);
    }

    @Override
    public String toString() {
        return toFen();
    }
}
