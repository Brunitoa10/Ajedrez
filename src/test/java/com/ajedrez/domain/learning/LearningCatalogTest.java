package com.ajedrez.domain.learning;

import com.ajedrez.domain.model.Move;
import com.ajedrez.domain.model.Position;
import com.ajedrez.domain.rules.AttackDetector;
import com.ajedrez.domain.rules.ChessRules;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Valida TODO el contenido didáctico: si alguien agrega una lección con una jugada ilegal
 * o un "mate" que no es mate, el build falla.
 */
public class LearningCatalogTest {

    private static final Pattern MATE_GOAL = Pattern.compile("\\bmate\\b", Pattern.CASE_INSENSITIVE);

    private final ChessRules rules = ChessRules.standard();
    private final LearningCatalog catalog = new BuiltInLearningCatalog();

    @TestFactory
    public Stream<DynamicTest> everyExerciseIsPlayableAndSolvable() {
        Stream<DynamicTest> lessons = catalog.lessons().stream()
                .map(l -> DynamicTest.dynamicTest("Lección: " + l.title(), () -> verify(l.exercise(), l.goal())));
        Stream<DynamicTest> puzzles = catalog.puzzles().stream()
                .map(p -> DynamicTest.dynamicTest("Problema: " + p.title(), () -> verify(p.exercise(), p.goal())));
        return Stream.concat(lessons, puzzles);
    }

    private void verify(Exercise exercise, String goal) {
        Position start = exercise.startPosition();
        assertFalse(rules.isCheckmate(start), "La posición inicial ya es mate");
        assertFalse(isSideNotToMoveInCheck(start), "El bando que no juega está en jaque: posición imposible");

        for (int i = 0; i < exercise.stepCount(); i++) {
            List<Move> accepted = exercise.isUserStep(i) ? exercise.acceptedMoves(i) : List.of(exercise.opponentReply(i));
            for (Move move : accepted) {
                assertTrue(rules.isLegal(start, move), "Jugada ilegal en el paso " + i + ": " + move);
            }
            start = start.apply(accepted.get(0));
        }
        if (MATE_GOAL.matcher(goal).find()) {
            assertTrue(rules.isCheckmate(start), "Debería terminar en mate: " + exercise.fen());
        }

        ExerciseSession session = new ExerciseSession(rules, exercise);
        for (int i = 0; i < exercise.stepCount(); i += 2) {
            ExerciseFeedback feedback = session.attempt(exercise.acceptedMoves(i).get(0));
            assertTrue(feedback.isPositive(), "La solución fue rechazada en el paso " + i);
        }
        assertTrue(session.isSolved());
    }

    private boolean isSideNotToMoveInCheck(Position position) {
        return new AttackDetector().isKingInCheck(position.board(), position.sideToMove().opposite());
    }

    @Test
    public void idsAreUnique() {
        Set<String> ids = new HashSet<>();
        catalog.lessons().forEach(l -> assertTrue(ids.add(l.id()), "id repetido " + l.id()));
        catalog.puzzles().forEach(p -> assertTrue(ids.add(p.id()), "id repetido " + p.id()));
    }

    @Test
    public void wrongMoveIsReportedAndAlternativeMateIsAccepted() {
        Exercise twoRooks = new Exercise("6k1/5ppp/8/8/8/8/8/R3R1K1 w - - 0 1", List.of("a1a8"));
        ExerciseSession session = new ExerciseSession(rules, twoRooks);
        assertEquals(ExerciseFeedback.Kind.ILLEGAL, session.attempt(Move.fromUci("a1h8")).kind());
        assertEquals(ExerciseFeedback.Kind.WRONG, session.attempt(Move.fromUci("a1a2")).kind());
        assertEquals(ExerciseFeedback.Kind.SOLVED, session.attempt(Move.fromUci("e1e8")).kind());
        assertEquals(1, session.mistakes());
    }
}
