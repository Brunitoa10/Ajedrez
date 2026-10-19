package com.ajedrez.ai;

/**
 * Parámetros de búsqueda.
 *
 * @param maxDepth         profundidad máxima en medias jugadas (la búsqueda iterativa puede cortar antes por tiempo)
 * @param timeLimitMillis  tiempo máximo de pensamiento
 * @param noiseCentipawns  ruido aleatorio sumado a la puntuación de cada jugada raíz (0 = juega lo mejor que ve)
 * @param blunderChance    probabilidad (0..1) de jugar una jugada al azar
 */
public record SearchSettings(int maxDepth, long timeLimitMillis, int noiseCentipawns, double blunderChance) {

    public SearchSettings {
        if (maxDepth < 1) {
            throw new IllegalArgumentException("La profundidad mínima es 1");
        }
        if (blunderChance < 0 || blunderChance > 1) {
            throw new IllegalArgumentException("blunderChance debe estar entre 0 y 1");
        }
    }

    public boolean hasPersonality() {
        return noiseCentipawns > 0 || blunderChance > 0;
    }
}
