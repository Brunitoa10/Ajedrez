package com.ajedrez.domain.rules;

import com.ajedrez.domain.model.Move;
import com.ajedrez.domain.model.Position;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * "Perft" cuenta todas las hojas del árbol de jugadas legales hasta cierta profundidad y lo compara
 * con valores de referencia conocidos. Es la prueba estándar para validar un generador de jugadas:
 * si enroques, al paso, coronaciones o clavadas fallan, los números no coinciden.
 * Valores de https://www.chessprogramming.org/Perft_Results
 */
class PerftTest {

    private final ChessRules rules = ChessRules.standard();

    @ParameterizedTest(name = "{0} profundidad {1} = {2}")
    @CsvSource(delimiter = ';', value = {
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1; 1; 20",
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1; 2; 400",
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1; 3; 8902",
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1; 4; 197281",
            // Kiwipete: enroques, clavadas, al paso y coronaciones
            "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1; 1; 48",
            "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1; 2; 2039",
            "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1; 3; 97862",
            // Posición 3: finales con al paso que descubre jaque
            "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1; 1; 14",
            "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1; 2; 191",
            "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1; 3; 2812",
            "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1; 4; 43238",
            // Posición 4: coronaciones con captura y enroques negros
            "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1; 1; 6",
            "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1; 2; 264",
            "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1; 3; 9467",
            // Posición 5
            "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8; 1; 44",
            "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8; 2; 1486",
            "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8; 3; 62379"
    })
    void perftMatchesReference(String fen, int depth, long expected) {
        assertEquals(expected, perft(Position.fromFen(fen), depth));
    }

    private long perft(Position position, int depth) {
        var moves = rules.legalMoves(position);
        if (depth == 1) {
            return moves.size();
        }
        long nodes = 0;
        for (Move move : moves) {
            nodes += perft(position.apply(move), depth - 1);
        }
        return nodes;
    }
}
