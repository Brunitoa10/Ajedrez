package com.ajedrez.domain.rules;

/** Estado de una posición considerada de forma aislada (sin historial). */
public enum PositionStatus {
    IN_PROGRESS,
    CHECKMATE,
    STALEMATE,
    INSUFFICIENT_MATERIAL,
    FIFTY_MOVE_RULE;

    public boolean isTerminal() {
        return this != IN_PROGRESS;
    }
}
