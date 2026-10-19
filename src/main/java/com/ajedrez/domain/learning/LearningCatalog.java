package com.ajedrez.domain.learning;

import java.util.List;
import java.util.Optional;

/**
 * Fuente del contenido didáctico. La implementación actual está en memoria; podría leerse de la
 * base de datos o de archivos JSON sin cambiar a quien la consume.
 */
public interface LearningCatalog {

    public List<Lesson> lessons();

    public List<Puzzle> puzzles();

    public default Optional<Lesson> lesson(String id) {
        return lessons().stream().filter(l -> l.id().equals(id)).findFirst();
    }

    public default Optional<Puzzle> puzzle(String id) {
        return puzzles().stream().filter(p -> p.id().equals(id)).findFirst();
    }
}
