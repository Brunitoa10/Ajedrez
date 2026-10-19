package com.ajedrez.application;

import com.ajedrez.application.port.GameRepository;
import com.ajedrez.domain.model.Move;
import com.ajedrez.domain.model.Position;
import com.ajedrez.domain.notation.SanFormatter;
import com.ajedrez.domain.rules.ChessRules;

import java.util.ArrayList;
import java.util.List;

/** Consulta, reproducción y borrado de partidas guardadas. */
public final class GameHistoryService {

    private final GameRepository repository;
    private final SanFormatter sanFormatter;

    public GameHistoryService(GameRepository repository, ChessRules rules) {
        this.repository = repository;
        this.sanFormatter = new SanFormatter(rules);
    }

    public List<GameRecord> allGames() {
        return repository.findAll();
    }

    public void delete(long id) {
        repository.delete(id);
    }

    /** Reconstruye todas las posiciones de una partida guardada para recorrerla jugada a jugada. */
    public Replay replay(GameRecord record) {
        List<Position> positions = new ArrayList<>();
        List<Move> moves = new ArrayList<>();
        List<String> san = new ArrayList<>();
        Position current = Position.fromFen(record.startFen());
        positions.add(current);
        for (String uci : record.uciMoves()) {
            Move move = Move.fromUci(uci);
            san.add(sanFormatter.format(current, move));
            current = current.apply(move);
            moves.add(move);
            positions.add(current);
        }
        return new Replay(List.copyOf(positions), List.copyOf(moves), List.copyOf(san));
    }

    /**
     * positions.get(0) es la posición inicial; positions.get(i) es la posición después de moves.get(i - 1).
     */
    public record Replay(List<Position> positions, List<Move> moves, List<String> sanMoves) {
    }
}
