package com.ajedrez.domain.model;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Casilla del tablero. file = columna (0=a .. 7=h), rank = fila (0=1 .. 7=8).
 * Las 64 instancias se cachean (Flyweight), así que {@link #of} no genera basura.
 */
public record Square(int file, int rank) {

    private static final Square[] CACHE = IntStream.range(0, 64)
            .mapToObj(i -> new Square(i % 8, i / 8))
            .toArray(Square[]::new);

    /** Todas las casillas, de a1 a h8. */
    public static final List<Square> ALL = List.of(CACHE);

    public Square {
        if (!isOnBoard(file, rank)) {
            throw new IllegalArgumentException("Casilla fuera del tablero: " + file + "," + rank);
        }
    }

    public static boolean isOnBoard(int file, int rank) {
        return file >= 0 && file < 8 && rank >= 0 && rank < 8;
    }

    public static Square of(int file, int rank) {
        if (!isOnBoard(file, rank)) {
            throw new IllegalArgumentException("Casilla fuera del tablero: " + file + "," + rank);
        }
        return CACHE[rank * 8 + file];
    }

    public static Square fromIndex(int index) {
        return CACHE[index];
    }

    /** Convierte notación algebraica ("e4") a casilla. */
    public static Square parse(String algebraic) {
        Objects.requireNonNull(algebraic, "algebraic");
        if (algebraic.length() != 2) {
            throw new IllegalArgumentException("Casilla inválida: " + algebraic);
        }
        int file = algebraic.charAt(0) - 'a';
        int rank = algebraic.charAt(1) - '1';
        return of(file, rank);
    }

    public int index() {
        return rank * 8 + file;
    }

    public boolean canOffset(int df, int dr) {
        return isOnBoard(file + df, rank + dr);
    }

    public Square offset(int df, int dr) {
        return of(file + df, rank + dr);
    }

    public boolean isLight() {
        return (file + rank) % 2 != 0;
    }

    public char fileChar() {
        return (char) ('a' + file);
    }

    public char rankChar() {
        return (char) ('1' + rank);
    }

    @Override
    public String toString() {
        return "" + fileChar() + rankChar();
    }
}
