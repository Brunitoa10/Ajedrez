package com.ajedrez.application;

public enum GameMode {
    VS_COMPUTER("Contra la computadora"),
    LOCAL_TWO_PLAYERS("Dos jugadores (mismo equipo)");

    private final String displayName;

    GameMode(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
