package com.ajedrez.ai;

import com.ajedrez.domain.model.Move;
import com.ajedrez.domain.model.Position;
import com.ajedrez.domain.rules.ChessRules;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlphaBetaEngineTest {

    private final ChessRules rules = ChessRules.standard();

    private ChessEngine engine(Difficulty difficulty) {
        return new AlphaBetaEngine(rules, new PieceSquareEvaluator(), difficulty.settings(), new Random(42));
    }

    @Test
    void findsMateInOne() {
        Position position = Position.fromFen("6k1/5ppp/8/8/8/8/5PPP/4R1K1 w - - 0 1");
        assertEquals(Move.fromUci("e1e8"), engine(Difficulty.MEDIUM).chooseMove(position).orElseThrow());
    }

    @Test
    void findsMateInTwo() {
        Position position = Position.fromFen("4r2k/6pp/7N/3Q4/8/8/8/6K1 w - - 0 1");
        assertEquals(Move.fromUci("d5g8"), engine(Difficulty.HARD).chooseMove(position).orElseThrow());
    }

    @Test
    void capturesHangingQueen() {
        Position position = Position.fromFen("4k3/8/8/3q4/8/8/8/3RK3 w - - 0 1");
        assertEquals(Move.fromUci("d1d5"), engine(Difficulty.MEDIUM).chooseMove(position).orElseThrow());
    }

    @Test
    void everyLevelReturnsALegalMoveQuickly() {
        Position position = Position.initial();
        for (Difficulty level : Difficulty.values()) {
            long start = System.currentTimeMillis();
            Move move = engine(level).chooseMove(position).orElseThrow();
            long elapsed = System.currentTimeMillis() - start;
            assertTrue(rules.isLegal(position, move), level + " devolvió una jugada ilegal");
            assertTrue(elapsed < level.settings().timeLimitMillis() + 2_000, level + " tardó " + elapsed + " ms");
        }
    }
}
