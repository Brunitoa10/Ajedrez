package com.ajedrez.ai;

import com.ajedrez.domain.rules.ChessRules;

import java.util.Random;

/**
 * Patrón Factory: crea el motor adecuado para cada nivel. Si en el futuro se agrega un
 * adaptador UCI (Stockfish), solo cambia esta clase.
 */
public final class EngineFactory {

    private final ChessRules rules;
    private final Evaluator evaluator;

    public EngineFactory(ChessRules rules, Evaluator evaluator) {
        this.rules = rules;
        this.evaluator = evaluator;
    }

    public ChessEngine create(Difficulty difficulty) {
        return new AlphaBetaEngine(rules, evaluator, difficulty.settings(), new Random());
    }
}
