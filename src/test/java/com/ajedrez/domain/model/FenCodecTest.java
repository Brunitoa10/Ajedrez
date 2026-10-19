package com.ajedrez.domain.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FenCodecTest {

    @ParameterizedTest
    @ValueSource(strings = {
            Position.STARTING_FEN,
            "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1",
            "4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 2",
            "8/8/8/8/8/8/8/K6k b - - 37 80"
    })
    void roundTrip(String fen) {
        assertEquals(fen, FenCodec.format(FenCodec.parse(fen)));
    }

    @Test
    void invalidFenIsRejected() {
        assertThrows(InvalidFenException.class, () -> FenCodec.parse("8/8/8 w - - 0 1"));
        assertThrows(InvalidFenException.class, () -> FenCodec.parse("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNX w KQkq - 0 1"));
    }

    @Test
    void doublePawnPushSetsEnPassantSquare() {
        Position after = Position.initial().apply(Move.fromUci("e2e4"));
        assertEquals("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1", after.toFen());
    }
}
