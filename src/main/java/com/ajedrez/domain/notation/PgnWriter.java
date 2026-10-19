package com.ajedrez.domain.notation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Genera texto PGN (Portable Game Notation) a partir de encabezados y jugadas en SAN.
 * Patrón Builder: {@code new PgnWriter().header("White", "Ana").moves(...).result("1-0").write()}.
 */
public final class PgnWriter {

    private static final int LINE_WIDTH = 80;

    private final Map<String, String> headers = new LinkedHashMap<>();
    private List<String> sanMoves = List.of();
    private int firstMoveNumber = 1;
    private boolean blackStarts;
    private String result = "*";

    public PgnWriter header(String key, String value) {
        headers.put(key, value == null ? "?" : value.replace("\"", "'"));
        return this;
    }

    public PgnWriter moves(List<String> sanMoves) {
        this.sanMoves = List.copyOf(sanMoves);
        return this;
    }

    /** Para partidas que empiezan desde un FEN con jugada distinta de 1 o con turno negro. */
    public PgnWriter startingAt(int moveNumber, boolean blackToMove) {
        this.firstMoveNumber = moveNumber;
        this.blackStarts = blackToMove;
        return this;
    }

    public PgnWriter result(String result) {
        this.result = result;
        return this;
    }

    public String write() {
        StringBuilder pgn = new StringBuilder();
        headers.putIfAbsent("Result", result);
        headers.forEach((key, value) -> pgn.append('[').append(key).append(" \"").append(value).append("\"]\n"));
        pgn.append('\n').append(wrap(movetext())).append('\n');
        return pgn.toString();
    }

    private String movetext() {
        StringBuilder text = new StringBuilder();
        int number = firstMoveNumber;
        boolean whiteTurn = !blackStarts;
        for (int i = 0; i < sanMoves.size(); i++) {
            if (whiteTurn) {
                text.append(number).append(". ");
            } else if (i == 0) {
                text.append(number).append("... ");
            }
            text.append(sanMoves.get(i)).append(' ');
            if (!whiteTurn) {
                number++;
            }
            whiteTurn = !whiteTurn;
        }
        return text.append(result).toString();
    }

    private String wrap(String text) {
        StringBuilder wrapped = new StringBuilder();
        int lineLength = 0;
        for (String token : text.split(" ")) {
            if (lineLength + token.length() + 1 > LINE_WIDTH && lineLength > 0) {
                wrapped.append('\n');
                lineLength = 0;
            } else if (lineLength > 0) {
                wrapped.append(' ');
                lineLength++;
            }
            wrapped.append(token);
            lineLength += token.length();
        }
        return wrapped.toString();
    }
}
