package com.ajedrez.domain.game;

import java.util.Objects;

/** Resultado final + motivo. */
public record GameOutcome(GameResult result, Termination termination) {

    public GameOutcome {
        Objects.requireNonNull(result, "result");
        Objects.requireNonNull(termination, "termination");
    }

    public String describe() {
        return result.displayName() + " por " + termination.description();
    }
}
