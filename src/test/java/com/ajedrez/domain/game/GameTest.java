package com.ajedrez.domain.game;

import com.ajedrez.domain.model.Move;
import com.ajedrez.domain.model.PieceColor;
import com.ajedrez.domain.model.Position;
import com.ajedrez.domain.rules.ChessRules;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameTest {

    private final ChessRules rules = ChessRules.standard();

    private Game play(String... uciMoves) {
        Game game = Game.standard(rules);
        for (String uci : uciMoves) {
            game.play(Move.fromUci(uci));
        }
        return game;
    }

    @Test
    public void foolsMateEndsInCheckmateForBlack() {
        Game game = play("f2f3", "e7e5", "g2g4", "d8h4");
        assertTrue(game.isOver());
        assertEquals(GameResult.BLACK_WINS, game.outcome().orElseThrow().result());
        assertEquals(Termination.CHECKMATE, game.outcome().orElseThrow().termination());
        assertEquals(List.of("f3", "e5", "g4", "Qh4#"), game.sanMoves());
    }

    @Test
    public void illegalMoveIsRejected() {
        Game game = Game.standard(rules);
        assertThrows(IllegalMoveException.class, () -> game.play(Move.fromUci("e2e5")));
    }

    @Test
    public void undoRestoresPreviousPositionAndReopensGame() {
        Game game = play("f2f3", "e7e5", "g2g4", "d8h4");
        game.undo();
        assertFalse(game.isOver());
        assertEquals(PieceColor.BLACK, game.sideToMove());
        assertEquals(3, game.history().size());
    }

    @Test
    public void threefoldRepetitionIsDraw() {
        Game game = play("g1f3", "g8f6", "f3g1", "f6g8", "g1f3", "g8f6", "f3g1", "f6g8");
        assertEquals(Termination.THREEFOLD_REPETITION, game.outcome().orElseThrow().termination());
    }

    @Test
    public void stalemateIsDetected() {
        Game game = new Game(rules, Position.fromFen("7k/8/6K1/8/8/8/8/5Q2 w - - 0 1"));
        game.play(Move.fromUci("f1f7"));
        assertEquals(Termination.STALEMATE, game.outcome().orElseThrow().termination());
        assertEquals(GameResult.DRAW, game.outcome().orElseThrow().result());
    }

    @Test
    public void insufficientMaterialAfterCapture() {
        Game game = new Game(rules, Position.fromFen("4k3/8/8/8/8/8/3p4/4K3 w - - 0 1"));
        game.play(Move.fromUci("e1d2"));
        assertEquals(Termination.INSUFFICIENT_MATERIAL, game.outcome().orElseThrow().termination());
    }

    @Test
    public void observersAreNotified() {
        Game game = Game.standard(rules);
        List<String> events = new ArrayList<>();
        game.addListener(new GameListener() {
            @Override
            public void onMovePlayed(Game g, MoveRecord record) {
                events.add("move:" + record.san());
            }

            @Override
            public void onMoveUndone(Game g, MoveRecord undone) {
                events.add("undo:" + undone.san());
            }
        });
        game.play(Move.fromUci("e2e4"));
        game.undo();
        assertEquals(List.of("move:e4", "undo:e4"), events);
    }

    @Test
    public void sanHandlesCastlingPromotionEnPassantAndDisambiguation() {
        Game castling = new Game(rules, Position.fromFen("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1"));
        assertEquals("O-O", castling.play(Move.fromUci("e1g1")).san());
        assertEquals("O-O-O", castling.play(Move.fromUci("e8c8")).san());

        Game promotion = new Game(rules, Position.fromFen("4k3/1P6/8/8/8/8/8/4K3 w - - 0 1"));
        assertEquals("b8=Q+", promotion.play(Move.fromUci("b7b8q")).san());

        Game enPassant = new Game(rules, Position.fromFen("4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 2"));
        assertEquals("exd6", enPassant.play(Move.fromUci("e5d6")).san());

        Game knights = new Game(rules, Position.fromFen("4k3/8/8/8/8/8/8/1N2KN2 w - - 0 1"));
        assertEquals("Nbd2", knights.play(Move.fromUci("b1d2")).san());
    }

    @Test
    public void castlingThroughAttackedSquareIsIllegal() {
        Position position = Position.fromFen("4k3/8/8/8/8/8/5r2/R3K2R w KQ - 0 1");
        assertFalse(rules.isLegal(position, Move.fromUci("e1g1")));
        assertTrue(rules.isLegal(position, Move.fromUci("e1c1")));
    }
}
