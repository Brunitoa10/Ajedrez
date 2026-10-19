package com.ajedrez.domain.game;

import com.ajedrez.domain.model.Move;
import com.ajedrez.domain.model.Position;

/** Jugada ya realizada, con su notación y las posiciones antes y después. */
public record MoveRecord(Move move, String san, Position before, Position after) {
}
