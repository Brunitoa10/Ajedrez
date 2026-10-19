package com.ajedrez.domain.model;

/**
 * Lectura y escritura de posiciones en notación FEN (Forsyth–Edwards).
 * Es la serialización canónica de {@link Position}, por eso vive en el modelo.
 */
public final class FenCodec {

    private FenCodec() {
    }

    public static Position parse(String fen) {
        if (fen == null || fen.isBlank()) {
            throw new InvalidFenException("FEN vacío");
        }
        String[] parts = fen.trim().split("\\s+");
        if (parts.length < 4) {
            throw new InvalidFenException("FEN incompleto: " + fen);
        }
        Board board = parseBoard(parts[0]);
        PieceColor side = switch (parts[1]) {
            case "w" -> PieceColor.WHITE;
            case "b" -> PieceColor.BLACK;
            default -> throw new InvalidFenException("Turno inválido: " + parts[1]);
        };
        CastlingRights castling = CastlingRights.fromFen(parts[2]);
        Square enPassant = "-".equals(parts[3]) ? null : parseSquare(parts[3]);
        int halfmove = parts.length > 4 ? parseNumber(parts[4]) : 0;
        int fullmove = parts.length > 5 ? parseNumber(parts[5]) : 1;
        return new Position(board, side, castling, enPassant, halfmove, fullmove);
    }

    public static String format(Position position) {
        StringBuilder sb = new StringBuilder();
        Board board = position.board();
        for (int rank = 7; rank >= 0; rank--) {
            int emptyRun = 0;
            for (int file = 0; file < 8; file++) {
                Piece piece = board.getOrNull(Square.of(file, rank));
                if (piece == null) {
                    emptyRun++;
                    continue;
                }
                if (emptyRun > 0) {
                    sb.append(emptyRun);
                    emptyRun = 0;
                }
                sb.append(piece.fenChar());
            }
            if (emptyRun > 0) {
                sb.append(emptyRun);
            }
            if (rank > 0) {
                sb.append('/');
            }
        }
        sb.append(' ').append(position.sideToMove() == PieceColor.WHITE ? 'w' : 'b');
        sb.append(' ').append(position.castlingRights().toFen());
        sb.append(' ').append(position.enPassantTarget().map(Square::toString).orElse("-"));
        sb.append(' ').append(position.halfmoveClock());
        sb.append(' ').append(position.fullmoveNumber());
        return sb.toString();
    }

    private static Board parseBoard(String placement) {
        String[] ranks = placement.split("/");
        if (ranks.length != 8) {
            throw new InvalidFenException("El tablero debe tener 8 filas: " + placement);
        }
        Board.Builder builder = Board.builder();
        for (int i = 0; i < 8; i++) {
            int rank = 7 - i;
            int file = 0;
            for (char c : ranks[i].toCharArray()) {
                if (Character.isDigit(c)) {
                    file += c - '0';
                } else {
                    if (file > 7) {
                        throw new InvalidFenException("Fila demasiado larga: " + ranks[i]);
                    }
                    builder.put(Square.of(file, rank), toPiece(c));
                    file++;
                }
            }
            if (file != 8) {
                throw new InvalidFenException("Fila con " + file + " columnas: " + ranks[i]);
            }
        }
        return builder.build();
    }

    private static Piece toPiece(char c) {
        try {
            return Piece.fromFenChar(c);
        } catch (IllegalArgumentException e) {
            throw new InvalidFenException("Pieza inválida en FEN: " + c);
        }
    }

    private static Square parseSquare(String text) {
        try {
            return Square.parse(text);
        } catch (IllegalArgumentException e) {
            throw new InvalidFenException("Casilla al paso inválida: " + text);
        }
    }

    private static int parseNumber(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new InvalidFenException("Número inválido en FEN: " + text);
        }
    }
}
