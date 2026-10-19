package com.ajedrez.domain.learning;

/**
 * Lección: teoría breve + ejercicio práctico con un objetivo concreto.
 */
public record Lesson(String id, LessonCategory category, String title, String theory, String goal,
                     Exercise exercise) {
}
