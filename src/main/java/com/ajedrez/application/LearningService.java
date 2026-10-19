package com.ajedrez.application;

import com.ajedrez.application.port.ProgressRepository;
import com.ajedrez.domain.learning.Exercise;
import com.ajedrez.domain.learning.ExerciseSession;
import com.ajedrez.domain.learning.LearningCatalog;
import com.ajedrez.domain.learning.Lesson;
import com.ajedrez.domain.learning.Puzzle;
import com.ajedrez.domain.model.Position;
import com.ajedrez.domain.rules.ChessRules;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Caso de uso "aprender": lecciones, problemas y registro del progreso. */
public final class LearningService {

    private final LearningCatalog catalog;
    private final ProgressRepository progress;
    private final ChessRules rules;

    public LearningService(LearningCatalog catalog, ProgressRepository progress, ChessRules rules) {
        this.catalog = catalog;
        this.progress = progress;
        this.rules = rules;
    }

    public List<Lesson> lessons() {
        return catalog.lessons();
    }

    public List<Puzzle> puzzles() {
        return catalog.puzzles();
    }

    public Set<String> completedLessons() {
        return progress.completedLessons();
    }

    public Map<String, PuzzleProgress> puzzleProgress() {
        return progress.puzzleProgress();
    }

    public boolean isInCheck(Position position) {
        return rules.isInCheck(position);
    }

    public ExerciseSession startExercise(Exercise exercise) {
        return new ExerciseSession(rules, exercise);
    }

    public void completeLesson(Lesson lesson) {
        progress.markLessonCompleted(lesson.id());
    }

    /** Registra un intento terminado (resuelto o abandonado/fallido). */
    public void recordPuzzleAttempt(Puzzle puzzle, boolean solved) {
        progress.recordPuzzleAttempt(puzzle.id(), solved);
    }
}
