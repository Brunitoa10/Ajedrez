package com.ajedrez.domain.learning;

/**
 * Problema táctico con dificultad aproximada (rating estilo Elo) y una pista textual.
 */
public record Puzzle(String id, String title, String theme, int rating, String goal, String hint,
                     Exercise exercise) {
}
