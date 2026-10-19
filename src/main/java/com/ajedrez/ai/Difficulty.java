package com.ajedrez.ai;

/**
 * Niveles de dificultad. Cada uno combina profundidad, tiempo y "personalidad"
 * (ruido en la evaluación y probabilidad de error) para que los niveles bajos jueguen
 * de forma humana en lugar de simplemente lenta.
 */
public enum Difficulty {
    BEGINNER("Principiante", "Comete errores a propósito, ideal para aprender.",
            new SearchSettings(1, 400, 150, 0.30)),
    EASY("Fácil", "Ve capturas simples, a veces se equivoca.",
            new SearchSettings(2, 700, 60, 0.10)),
    MEDIUM("Intermedio", "Calcula combinaciones cortas.",
            new SearchSettings(3, 1_500, 20, 0.0)),
    HARD("Difícil", "Juega sólido y castiga los errores.",
            new SearchSettings(4, 3_000, 0, 0.0)),
    EXPERT("Experto", "Búsqueda profunda con más tiempo.",
            new SearchSettings(64, 6_000, 0, 0.0));

    private final String displayName;
    private final String description;
    private final SearchSettings settings;

    private Difficulty(String displayName, String description, SearchSettings settings) {
        this.displayName = displayName;
        this.description = description;
        this.settings = settings;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }

    public SearchSettings settings() {
        return settings;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
