package com.ajedrez.ai;

import com.ajedrez.domain.model.Position;

/**
 * Función de evaluación estática. Devuelve centipeones desde el punto de vista del bando que mueve
 * (positivo = bueno para quien tiene el turno), convención necesaria para negamax.
 */
public interface Evaluator {

    public int evaluate(Position position);
}
