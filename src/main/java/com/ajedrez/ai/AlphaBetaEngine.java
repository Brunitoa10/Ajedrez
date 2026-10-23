
package com.ajedrez.ai;

import com.ajedrez.domain.model.Move;
import com.ajedrez.domain.model.Position;
import com.ajedrez.domain.rules.ChessRules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.random.RandomGenerator;

/**
 * Motor de ajedrez basado en negamax y poda alfa-beta.
 *
 * Incluye profundización iterativa, límite temporal, extensiones de jaque
 * y búsqueda de quiescencia.
 *
 * Cada búsqueda dispone de su propio contexto mutable.
 */
public final class AlphaBetaEngine implements ChessEngine {

    private static final int MATE_SCORE = 100_000;
    private static final int INFINITY = 1_000_000;
    private static final int MATE_THRESHOLD = MATE_SCORE - 1_000;
    private static final int MAX_QUIESCENCE_DEPTH = 8;
    private static final int TIME_CHECK_MASK = 1023;

    private final ChessRules rules;
    private final Evaluator evaluator;
    private final SearchSettings settings;
    private final RandomGenerator random;
    private final MoveOrderer orderer = new MoveOrderer();

    public AlphaBetaEngine(
            ChessRules rules,
            Evaluator evaluator,
            SearchSettings settings,
            RandomGenerator random
    ) {
        this.rules = Objects.requireNonNull(rules, "rules");
        this.evaluator = Objects.requireNonNull(evaluator, "evaluator");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override
    public String name() {
        String engineName = "Alfa-Beta (prof. " + settings.maxDepth() + ")";
        return engineName;
    }

    @Override
    public Optional<Move> chooseMove(Position position) {
        Objects.requireNonNull(position, "position");

        List<Move> moves = rules.legalMoves(position);
        Optional<Move> selectedMove = Optional.empty();

        if (!moves.isEmpty()) {
            if (moves.size() == 1) {
                selectedMove = Optional.of(moves.get(0));
            } else if (random.nextDouble() < settings.blunderChance()) {
                int randomIndex = random.nextInt(moves.size());
                selectedMove = Optional.of(moves.get(randomIndex));
            } else {
                List<ScoredMove> scored = iterativeDeepening(position, moves);
                selectedMove = Optional.of(pickMove(scored));
            }
        }

        return selectedMove;
    }

    /**
     * Busca iterativamente desde profundidad 1 hasta la profundidad máxima.
     * Conserva el último resultado completado si se interrumpe una búsqueda.
     */
    private List<ScoredMove> iterativeDeepening(
            Position root,
            List<Move> moves
    ) {
        long deadline = System.nanoTime()
                + settings.timeLimitMillis() * 1_000_000L;

        SearchContext context = new SearchContext(deadline);

        List<ScoredMove> best = moves.stream()
                .map(move -> new ScoredMove(move, 0))
                .toList();

        int depth = 1;
        boolean searching = true;

        while (depth <= settings.maxDepth() && searching) {
            try {
                best = searchRoot(root, best, depth, context);
                context.allowInterruption();

                boolean mateFound =
                        Math.abs(best.get(0).score()) >= MATE_THRESHOLD;

                if (mateFound) {
                    searching = false;
                } else {
                    depth++;
                }
            } catch (SearchInterrupted interrupted) {
                searching = false;
            }
        }

        return best;
    }

    /**
     * Evalúa las jugadas raíz y las ordena por puntuación descendente.
     */
    private List<ScoredMove> searchRoot(
            Position root,
            List<ScoredMove> previous,
            int depth,
            SearchContext context
    ) {
        boolean exactScores = settings.hasPersonality();

        context.limitExtensions(depth * 2 + 2);

        int alpha = -INFINITY;

        List<ScoredMove> results = new ArrayList<>(previous.size());

        for (ScoredMove candidate : previous) {
            Position child = root.apply(candidate.move());

            int windowAlpha = exactScores ? -INFINITY : alpha;

            int score = -negamax(
                    child,
                    depth - 1,
                    -INFINITY,
                    -windowAlpha,
                    1,
                    context
            );

            results.add(new ScoredMove(candidate.move(), score));

            alpha = Math.max(alpha, score);
        }

        results.sort(
                Comparator.comparingInt(ScoredMove::score).reversed()
        );

        return results;
    }

    /**
     * Ejecuta negamax con poda alfa-beta y extensiones de jaque.
     */
    private int negamax(
            Position position,
            int depth,
            int alpha,
            int beta,
            int ply,
            SearchContext context
    ) {
        context.tick();

        int result = 0;
        boolean searching = true;

        if (position.halfmoveClock() >= 100) {
            searching = false;
        }

        boolean inCheck = false;

        if (searching) {
            inCheck = rules.isInCheck(position);

            if (inCheck && ply < context.extensionPlyLimit()) {
                depth++;
            }

            if (depth <= 0) {
                result = quiescence(position, alpha, beta, 0, context);
                searching = false;
            }
        }

        List<Move> moves = List.of();

        if (searching) {
            moves = rules.legalMoves(position);

            if (moves.isEmpty()) {
                result = inCheck ? -MATE_SCORE + ply : 0;
                searching = false;
            }
        }

        if (searching) {
            moves = orderer.order(position, moves, null);
        }

        int index = 0;

        while (searching && index < moves.size()) {
            Move move = moves.get(index);

            int score = -negamax(
                    position.apply(move),
                    depth - 1,
                    -beta,
                    -alpha,
                    ply + 1,
                    context
            );

            if (score >= beta) {
                result = beta;
                searching = false;
            } else {
                alpha = Math.max(alpha, score);
            }

            index++;
        }

        if (searching) {
            result = alpha;
        }

        return result;
    }

    /**
     * Evalúa posiciones tácticas para reducir el efecto horizonte.
     * Considera capturas y promociones.
     */
    private int quiescence(
            Position position,
            int alpha,
            int beta,
            int qDepth,
            SearchContext context
    ) {
        context.tick();

        int result = 0;
        int standPat = evaluator.evaluate(position);

        boolean searching = true;

        if (standPat >= beta) {
            result = beta;
            searching = false;
        } else {
            alpha = Math.max(alpha, standPat);
        }

        List<Move> tactical = List.of();

        if (searching && qDepth < MAX_QUIESCENCE_DEPTH) {
            tactical = rules.legalMoves(position).stream()
                    .filter(move ->
                            position.isCapture(move) || move.isPromotion()
                    )
                    .toList();

            tactical = orderer.order(position, tactical, null);
        } else if (searching) {
            result = alpha;
            searching = false;
        }

        int index = 0;

        while (searching && index < tactical.size()) {
            Move move = tactical.get(index);

            int score = -quiescence(
                    position.apply(move),
                    -beta,
                    -alpha,
                    qDepth + 1,
                    context
            );

            if (score >= beta) {
                result = beta;
                searching = false;
            } else {
                alpha = Math.max(alpha, score);
            }

            index++;
        }

        if (searching) {
            result = alpha;
        }

        return result;
    }

    /**
     * Selecciona la jugada final aplicando el ruido configurado.
     * No introduce ruido cuando se ha encontrado un mate ganador.
     */
    private Move pickMove(List<ScoredMove> scored) {
        ScoredMove best = scored.get(0);

        boolean applyNoise =
                settings.noiseCentipawns() != 0
                && best.score() < MATE_THRESHOLD;

        if (applyNoise) {
            int bestNoisy = Integer.MIN_VALUE;

            for (ScoredMove candidate : scored) {
                int noise = random.nextInt(
                        -settings.noiseCentipawns(),
                        settings.noiseCentipawns() + 1
                );

                int noisyScore = candidate.score() + noise;

                if (noisyScore > bestNoisy) {
                    bestNoisy = noisyScore;
                    best = candidate;
                }
            }
        }

        return best.move();
    }

    private record ScoredMove(Move move, int score) {
    }

    /**
     * Señal interna para interrumpir una búsqueda.
     */
    private static final class SearchInterrupted extends RuntimeException {

        private SearchInterrupted() {
            super(null, null, false, false);
        }
    }

    /**
     * Contexto mutable e independiente para cada búsqueda.
     */
    private static final class SearchContext {

        private final long deadlineNanos;

        private long nodes;
        private boolean canInterrupt;
        private int extensionPlyLimit;

        private SearchContext(long deadlineNanos) {
            this.deadlineNanos = deadlineNanos;
        }

        /**
         * Habilita interrupciones después de completar la primera iteración.
         */
        public void allowInterruption() {
            canInterrupt = true;
        }

        /**
         * Limita la profundidad de las extensiones de jaque.
         */
        public void limitExtensions(int plyLimit) {
            extensionPlyLimit = plyLimit;
        }

        public int extensionPlyLimit() {
            return extensionPlyLimit;
        }

        /**
         * Comprueba periódicamente el plazo y la interrupción del hilo.
         */
        public void tick() {
            nodes++;

            boolean shouldCheck =
                    canInterrupt && (nodes & TIME_CHECK_MASK) == 0;

            if (shouldCheck) {
                boolean deadlineExceeded =
                        System.nanoTime() > deadlineNanos;

                boolean threadInterrupted =
                        Thread.currentThread().isInterrupted();

                if (deadlineExceeded || threadInterrupted) {
                    throw new SearchInterrupted();
                }
            }
        }
    }
}
