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
 * Motor propio: negamax con poda alfa-beta, profundización iterativa con límite de tiempo,
 * extensión de jaque y búsqueda de quiescencia (solo capturas) para evitar el "efecto horizonte".
 * <p>
 * El motor no guarda estado entre llamadas: cada búsqueda usa su propio {@link SearchContext},
 * por lo que es seguro usarlo desde un hilo en segundo plano.
 */
public final class AlphaBetaEngine implements ChessEngine {

    static final int MATE_SCORE = 100_000;
    private static final int INFINITY = 1_000_000;
    private static final int MATE_THRESHOLD = MATE_SCORE - 1_000;
    private static final int MAX_QUIESCENCE_DEPTH = 8;
    private static final int TIME_CHECK_MASK = 1023;

    private final ChessRules rules;
    private final Evaluator evaluator;
    private final SearchSettings settings;
    private final RandomGenerator random;
    private final MoveOrderer orderer = new MoveOrderer();

    public AlphaBetaEngine(ChessRules rules, Evaluator evaluator, SearchSettings settings, RandomGenerator random) {
        this.rules = Objects.requireNonNull(rules, "rules");
        this.evaluator = Objects.requireNonNull(evaluator, "evaluator");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override
    public String name() {
        return "Alfa-Beta (prof. " + settings.maxDepth() + ")";
    }

    @Override
    public Optional<Move> chooseMove(Position position) {
        List<Move> moves = rules.legalMoves(position);
        if (moves.isEmpty()) {
            return Optional.empty();
        }
        if (moves.size() == 1) {
            return Optional.of(moves.get(0));
        }
        if (random.nextDouble() < settings.blunderChance()) {
            return Optional.of(moves.get(random.nextInt(moves.size())));
        }
        List<ScoredMove> scored = iterativeDeepening(position, moves);
        return Optional.of(pickMove(scored));
    }

    private List<ScoredMove> iterativeDeepening(Position root, List<Move> moves) {
        SearchContext context = new SearchContext(System.nanoTime() + settings.timeLimitMillis() * 1_000_000L);
        List<ScoredMove> best = moves.stream().map(m -> new ScoredMove(m, 0)).toList();
        for (int depth = 1; depth <= settings.maxDepth(); depth++) {
            try {
                best = searchRoot(root, best, depth, context);
            } catch (SearchInterrupted interrupted) {
                break;
            }
            context.allowInterruption();
            if (Math.abs(best.get(0).score()) >= MATE_THRESHOLD) {
                break;
            }
        }
        return best;
    }

    /**
     * Puntúa las jugadas raíz. Con "personalidad" (niveles bajos) se calcula el valor exacto de cada
     * jugada para poder elegir entre varias buenas; en niveles altos se usa la ventana alfa-beta completa.
     */
    private List<ScoredMove> searchRoot(Position root, List<ScoredMove> previous, int depth, SearchContext context) {
        boolean exactScores = settings.hasPersonality();
        context.limitExtensions(depth * 2 + 2);
        int alpha = -INFINITY;
        List<ScoredMove> results = new ArrayList<>(previous.size());
        for (ScoredMove candidate : previous) {
            Position child = root.apply(candidate.move());
            int windowAlpha = exactScores ? -INFINITY : alpha;
            int score = -negamax(child, depth - 1, -INFINITY, -windowAlpha, 1, context);
            results.add(new ScoredMove(candidate.move(), score));
            alpha = Math.max(alpha, score);
        }
        results.sort(Comparator.comparingInt(ScoredMove::score).reversed());
        return results;
    }

    private int negamax(Position position, int depth, int alpha, int beta, int ply, SearchContext context) {
        context.tick();
        if (position.halfmoveClock() >= 100) {
            return 0;
        }
        boolean inCheck = rules.isInCheck(position);
        if (inCheck && ply < context.extensionPlyLimit()) {
            depth++;
        }
        if (depth <= 0) {
            return quiescence(position, alpha, beta, 0, context);
        }
        List<Move> moves = rules.legalMoves(position);
        if (moves.isEmpty()) {
            return inCheck ? -MATE_SCORE + ply : 0;
        }
        for (Move move : orderer.order(position, moves, null)) {
            int score = -negamax(position.apply(move), depth - 1, -beta, -alpha, ply + 1, context);
            if (score >= beta) {
                return beta;
            }
            alpha = Math.max(alpha, score);
        }
        return alpha;
    }

    private int quiescence(Position position, int alpha, int beta, int qDepth, SearchContext context) {
        context.tick();
        int standPat = evaluator.evaluate(position);
        if (standPat >= beta) {
            return beta;
        }
        alpha = Math.max(alpha, standPat);
        if (qDepth >= MAX_QUIESCENCE_DEPTH) {
            return alpha;
        }
        List<Move> tactical = rules.legalMoves(position).stream()
                .filter(m -> position.isCapture(m) || m.isPromotion())
                .toList();
        for (Move move : orderer.order(position, tactical, null)) {
            int score = -quiescence(position.apply(move), -beta, -alpha, qDepth + 1, context);
            if (score >= beta) {
                return beta;
            }
            alpha = Math.max(alpha, score);
        }
        return alpha;
    }

    /** Elige la jugada final aplicando el ruido del nivel (nunca ignora un mate encontrado). */
    private Move pickMove(List<ScoredMove> scored) {
        if (settings.noiseCentipawns() == 0 || scored.get(0).score() >= MATE_THRESHOLD) {
            return scored.get(0).move();
        }
        ScoredMove best = null;
        int bestNoisy = Integer.MIN_VALUE;
        for (ScoredMove candidate : scored) {
            int noise = random.nextInt(-settings.noiseCentipawns(), settings.noiseCentipawns() + 1);
            int noisy = candidate.score() + noise;
            if (noisy > bestNoisy) {
                bestNoisy = noisy;
                best = candidate;
            }
        }
        return best.move();
    }

    private record ScoredMove(Move move, int score) {
    }

    /** Señal interna para abortar la búsqueda por tiempo o por cancelación del hilo. */
    private static final class SearchInterrupted extends RuntimeException {
        public SearchInterrupted() {
            super(null, null, false, false);
        }
    }

    /** Estado mutable de una búsqueda concreta (nodos visitados, plazo). */
    private static final class SearchContext {
        private final long deadlineNanos;
        private long nodes;
        private boolean canInterrupt;
        private int extensionPlyLimit;

        public SearchContext(long deadlineNanos) {
            this.deadlineNanos = deadlineNanos;
        }

        /** La primera iteración (profundidad 1) siempre se completa para tener una jugada válida. */
        public void allowInterruption() {
            canInterrupt = true;
        }

        /** Evita que las extensiones de jaque (jaques perpetuos) disparen la búsqueda. */
        public void limitExtensions(int plyLimit) {
            extensionPlyLimit = plyLimit;
        }

        public int extensionPlyLimit() {
            return extensionPlyLimit;
        }

        public void tick() {
            nodes++;
            if (!canInterrupt || (nodes & TIME_CHECK_MASK) != 0) {
                return;
            }
            if (System.nanoTime() > deadlineNanos || Thread.currentThread().isInterrupted()) {
                throw new SearchInterrupted();
            }
        }
    }
}
