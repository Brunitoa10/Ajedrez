package com.ajedrez.domain.game;

/**
 * Patrón Observer: la interfaz gráfica (u otros componentes) se suscriben a los cambios de la partida
 * sin que el dominio dependa de ellos. Los métodos tienen implementación vacía para que cada
 * observador implemente solo lo que le interesa (Interface Segregation pragmática).
 */
public interface GameListener {

    public default void onMovePlayed(Game game, MoveRecord record) {
    }

    public default void onMoveUndone(Game game, MoveRecord undone) {
    }

    public default void onGameOver(Game game, GameOutcome outcome) {
    }
}
