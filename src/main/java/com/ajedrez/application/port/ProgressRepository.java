package com.ajedrez.application.port;

import com.ajedrez.application.PuzzleProgress;

import java.util.Map;
import java.util.Set;

/** Progreso del alumno en lecciones y problemas. */
public interface ProgressRepository {

    public void markLessonCompleted(String lessonId);

    public Set<String> completedLessons();

    public void recordPuzzleAttempt(String puzzleId, boolean solved);

    public Map<String, PuzzleProgress> puzzleProgress();
}
