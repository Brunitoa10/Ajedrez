package com.ajedrez.domain.learning;

/** Categorías del apartado de aprendizaje, en orden pedagógico. */
public enum LessonCategory {
    FUNDAMENTALS("Fundamentos"),
    SPECIAL_MOVES("Movimientos especiales"),
    PRINCIPLES("Principios de apertura"),
    BASIC_MATES("Mates básicos"),
    TACTICS("Táctica");

    private final String displayName;

    private LessonCategory(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
