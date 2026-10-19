package com.ajedrez.domain.game;

import com.ajedrez.domain.rules.PositionStatus;

/** Motivo por el que terminó una partida. */
public enum Termination {
    CHECKMATE("jaque mate"),
    STALEMATE("rey ahogado"),
    INSUFFICIENT_MATERIAL("material insuficiente"),
    FIFTY_MOVE_RULE("regla de los 50 movimientos"),
    THREEFOLD_REPETITION("triple repetición"),
    RESIGNATION("abandono"),
    AGREEMENT("acuerdo mutuo");

    private final String description;

    private Termination(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }

    public static Termination fromStatus(PositionStatus status) {
        return switch (status) {
            case CHECKMATE -> CHECKMATE;
            case STALEMATE -> STALEMATE;
            case INSUFFICIENT_MATERIAL -> INSUFFICIENT_MATERIAL;
            case FIFTY_MOVE_RULE -> FIFTY_MOVE_RULE;
            case IN_PROGRESS -> throw new IllegalArgumentException("La partida sigue en juego");
        };
    }
}
