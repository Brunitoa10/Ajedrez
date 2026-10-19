package com.ajedrez.domain.learning;

import com.ajedrez.domain.model.Position;

import java.util.Comparator;
import java.util.List;

/**
 * Contenido didáctico incluido con la aplicación. Cada ejercicio está validado por
 * {@code LearningCatalogTest} (las jugadas son legales y los mates son mates).
 */
public final class BuiltInLearningCatalog implements LearningCatalog {

    private final List<Lesson> lessons = List.of(
            // ------------------------------------------------------------- Fundamentos
            lesson("pawn", LessonCategory.FUNDAMENTALS, "El peón",
                    """
                    El peón avanza una casilla hacia adelante, nunca hacia atrás.
                    Desde su casilla inicial puede avanzar una o dos casillas.
                    No puede saltar piezas: si la casilla de adelante está ocupada, queda bloqueado.""",
                    "Avanzá el peón de e2 dos casillas.",
                    "4k3/8/8/8/8/8/4P3/4K3 w - - 0 1", "e2e4"),
            lesson("pawn-capture", LessonCategory.FUNDAMENTALS, "Captura del peón",
                    """
                    El peón es la única pieza que captura distinto de como se mueve:
                    avanza recto pero captura en diagonal, una casilla hacia adelante.""",
                    "Capturá el peón negro.",
                    "4k3/8/8/3p4/4P3/8/8/4K3 w - - 0 1", "e4d5"),
            lesson("knight", LessonCategory.FUNDAMENTALS, "El caballo",
                    """
                    El caballo se mueve en forma de "L": dos casillas en una dirección y una en perpendicular.
                    Es la única pieza que puede saltar por encima de las demás.
                    Siempre cae en una casilla de color distinto a la de partida.""",
                    "Capturá la torre con el caballo.",
                    "4k3/8/8/3r4/8/4N3/8/4K3 w - - 0 1", "e3d5"),
            lesson("bishop", LessonCategory.FUNDAMENTALS, "El alfil",
                    """
                    El alfil se mueve en diagonal todas las casillas que quiera mientras el camino esté libre.
                    Cada alfil se queda toda la partida en casillas de un mismo color.""",
                    "Capturá la dama con el alfil.",
                    "4k3/8/7q/8/8/8/8/2B1K3 w - - 0 1", "c1h6"),
            lesson("rook", LessonCategory.FUNDAMENTALS, "La torre",
                    """
                    La torre se mueve en línea recta, por filas y columnas, tantas casillas como quiera.
                    Es muy fuerte en columnas abiertas y en la séptima fila rival.""",
                    "Capturá el caballo con la torre.",
                    "n3k3/8/8/8/8/8/8/R3K3 w - - 0 1", "a1a8"),
            lesson("queen", LessonCategory.FUNDAMENTALS, "La dama",
                    """
                    La dama combina la torre y el alfil: se mueve en línea recta y en diagonal.
                    Es la pieza más poderosa (vale unos 9 peones). ¡No la saques demasiado temprano!""",
                    "Capturá la torre con la dama.",
                    "4k3/8/8/7r/8/8/8/3QK3 w - - 0 1", "d1h5"),
            lesson("king", LessonCategory.FUNDAMENTALS, "El rey y el jaque",
                    """
                    El rey se mueve una casilla en cualquier dirección.
                    Cuando está atacado está en "jaque" y es obligatorio salir del jaque:
                    mover el rey, capturar la pieza atacante o interponer otra pieza.""",
                    "Tu rey está en jaque: capturá el peón que lo ataca.",
                    "4k3/8/8/8/8/8/3p4/4K3 w - - 0 1", "e1d2"),
            lesson("check", LessonCategory.FUNDAMENTALS, "Dar jaque",
                    """
                    Dar jaque obliga al rival a responder, lo que te da la iniciativa.
                    Pero un jaque sin un plan no siempre es bueno: pensá qué ganás con él.""",
                    "Dale jaque al rey negro con la torre.",
                    "4k3/8/8/8/8/8/8/R3K3 w - - 0 1", "a1a8"),
            lesson("piece-value", LessonCategory.FUNDAMENTALS, "Valor de las piezas",
                    """
                    Valores aproximados: peón 1, caballo 3, alfil 3, torre 5, dama 9.
                    El rey no tiene valor porque perderlo es perder la partida.
                    Si podés elegir qué capturar, normalmente conviene la pieza más valiosa.""",
                    "Elegí la captura que más material gana.",
                    "4k3/8/8/2q1n3/3P4/8/8/4K3 w - - 0 1", "d4c5"),

            // ------------------------------------------------------- Movimientos especiales
            lesson("castle-short", LessonCategory.SPECIAL_MOVES, "Enroque corto",
                    """
                    El enroque mueve el rey dos casillas hacia una torre y la torre salta al otro lado del rey.
                    Requisitos: ni el rey ni esa torre se movieron, no hay piezas entre ellos,
                    el rey no está en jaque y no pasa ni cae en casillas atacadas.""",
                    "Hacé el enroque corto (rey a g1).",
                    "r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1", "e1g1"),
            lesson("castle-long", LessonCategory.SPECIAL_MOVES, "Enroque largo",
                    """
                    El enroque largo es hacia el lado de la dama: el rey va a c1 (o c8) y la torre a d1 (o d8).
                    Deja al rey un poco menos protegido pero activa rápido la torre en el centro.""",
                    "Hacé el enroque largo (rey a c1).",
                    "r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1", "e1c1"),
            lesson("en-passant", LessonCategory.SPECIAL_MOVES, "Captura al paso",
                    """
                    Si un peón rival avanza dos casillas y queda al lado de tu peón, podés capturarlo
                    "al paso" como si hubiera avanzado una sola. Solo vale en la jugada inmediata.""",
                    "El peón negro acaba de jugar d7-d5. Capturalo al paso.",
                    "4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 2", "e5d6"),
            lesson("promotion", LessonCategory.SPECIAL_MOVES, "Coronación",
                    """
                    Cuando un peón llega a la última fila se transforma en dama, torre, alfil o caballo.
                    Casi siempre se elige dama, pero a veces un caballo da jaque mate o evita un ahogado.""",
                    "Coroná el peón en dama.",
                    "4k3/1P6/8/8/8/8/8/4K3 w - - 0 1", "b7b8q"),
            lesson("stalemate", LessonCategory.SPECIAL_MOVES, "Mate, no ahogado",
                    """
                    Si el rival no tiene jugadas legales y su rey NO está en jaque, es "rey ahogado": tablas.
                    Con mucha ventaja hay que tener cuidado de no ahogar al rey y dejarle siempre una salida
                    hasta dar el mate.""",
                    "Da jaque mate en una jugada (¡ojo con Df7, que es ahogado!).",
                    "7k/8/6K1/8/8/8/8/5Q2 w - - 0 1", "f1f8"),

            // ------------------------------------------------------- Principios de apertura
            lesson("center", LessonCategory.PRINCIPLES, "Controlá el centro",
                    """
                    Las casillas centrales (e4, d4, e5, d5) son las más importantes:
                    las piezas ahí controlan más casillas y se mueven más fácil a cualquier flanco.""",
                    "Ocupá el centro con un peón.",
                    Position.STARTING_FEN, "e2e4|d2d4"),
            lesson("develop", LessonCategory.PRINCIPLES, "Desarrollá las piezas",
                    """
                    En la apertura sacá rápido caballos y alfiles hacia el centro.
                    Regla práctica: no muevas la misma pieza dos veces sin motivo y no saques la dama temprano.""",
                    "Desarrollá una pieza menor.",
                    "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq e6 0 2", "g1f3|b1c3|f1c4|f1b5"),
            lesson("castle-early", LessonCategory.PRINCIPLES, "Enrocá temprano",
                    """
                    Con las piezas desarrolladas, enrocá: el rey queda protegido detrás de los peones
                    y la torre se conecta con el juego.""",
                    "Poné a salvo tu rey.",
                    "r1bqk2r/pppp1ppp/2n2n2/2b1p3/2B1P3/5N2/PPPP1PPP/RNBQK2R w KQkq - 6 5", "e1g1"),

            // ------------------------------------------------------- Mates básicos
            lesson("back-rank", LessonCategory.BASIC_MATES, "Mate del pasillo",
                    """
                    Cuando el rey está encerrado por sus propios peones en la última fila,
                    una torre o dama en esa fila puede dar mate.""",
                    "Da jaque mate en una jugada.",
                    "6k1/5ppp/8/8/8/8/8/R5K1 w - - 0 1", "a1a8"),
            lesson("queen-mate", LessonCategory.BASIC_MATES, "Mate con dama y rey",
                    """
                    La dama no puede dar mate sola: necesita que el rey propio cubra las casillas de escape.
                    Llevá al rey rival al borde y acercá tu rey.""",
                    "Da jaque mate en una jugada.",
                    "k7/8/1K6/8/8/8/8/6Q1 w - - 0 1", "g1g8"),
            lesson("ladder-mate", LessonCategory.BASIC_MATES, "Mate de la escalera",
                    """
                    Dos torres (o torre y dama) se turnan para empujar al rey fila por fila
                    hasta el borde: una corta el paso y la otra da jaque.""",
                    "Da jaque mate en una jugada.",
                    "k7/7R/8/8/8/8/8/6RK w - - 0 1", "g1g8"),
            lesson("scholar-mate", LessonCategory.BASIC_MATES, "Mate del pastor",
                    """
                    La casilla f7 (f2 para blancas) solo está defendida por el rey al inicio.
                    Dama y alfil coordinados pueden dar mate allí si el rival se descuida.
                    Aprenderlo sirve sobre todo para saber defenderse.""",
                    "Da jaque mate en f7.",
                    "r1bqkb1r/pppp1ppp/2n2n2/4p2Q/2B1P3/8/PPPP1PPP/RNB1K1NR w KQkq - 4 4", "h5f7"),

            // ------------------------------------------------------- Táctica
            lesson("fork", LessonCategory.TACTICS, "Ataque doble (horquilla)",
                    """
                    Una pieza que ataca dos objetivos a la vez es un ataque doble.
                    El caballo es especialista: puede atacar rey y dama al mismo tiempo.""",
                    "Ganá la dama con un ataque doble de caballo.",
                    "q3k3/8/8/1N6/8/8/8/4K3 w - - 0 1", "b5c7", "e8e7", "c7a8"),
            lesson("skewer", LessonCategory.TACTICS, "Ataque en rayos X (pincho)",
                    """
                    Atacás una pieza valiosa que, al moverse, deja expuesta otra que está detrás en la misma línea.
                    Es como una clavada pero al revés: la pieza de adelante es la más valiosa.""",
                    "Dá jaque al rey y ganá la dama que está detrás.",
                    "8/8/q2k4/8/8/8/8/4K2R w - - 0 1", "h1h6", "d6d7", "h6a6")
    );

    private final List<Puzzle> puzzles = List.of(
            puzzle("p-fools", "Mate del loco", "Mate en 1", 400,
                    "Juegan negras y dan mate en 1.",
                    "El rey blanco quedó expuesto en la diagonal e1-h4.",
                    "rnbqkbnr/pppp1ppp/8/4p3/6P1/5P2/PPPPP2P/RNBQKBNR b KQkq g3 0 2", "d8h4"),
            puzzle("p-backrank-black", "Pasillo para negras", "Mate en 1", 500,
                    "Juegan negras y dan mate en 1.",
                    "Los peones blancos encierran a su propio rey.",
                    "3r2k1/5ppp/8/8/8/8/5PPP/6K1 b - - 0 1", "d8d1"),
            puzzle("p-backrank", "La última fila", "Mate en 1", 600,
                    "Juegan blancas y dan mate en 1.",
                    "¿Qué fila no puede cubrir el rey negro?",
                    "6k1/5ppp/8/8/8/8/5PPP/4R1K1 w - - 0 1", "e1e8"),
            puzzle("p-smothered", "Mate ahogado", "Mate en 1", 800,
                    "Juegan blancas y dan mate en 1.",
                    "El rey está rodeado de sus propias piezas: solo un caballo puede atacarlo.",
                    "6rk/6pp/8/6N1/8/8/8/6K1 w - - 0 1", "g5f7"),
            puzzle("p-fork", "Horquilla real", "Ataque doble", 900,
                    "Juegan blancas y ganan material.",
                    "Buscá un jaque de caballo que también ataque a la dama.",
                    "2q3k1/5ppp/8/3N4/8/8/5PPP/6K1 w - - 0 1", "d5e7", "g8h8", "e7c8"),
            puzzle("p-ladder", "Escalera en dos", "Mate en 2", 1000,
                    "Juegan blancas y dan mate en 2.",
                    "Primero cortá la séptima fila con una torre.",
                    "6k1/8/8/8/8/8/R7/1R4K1 w - - 0 1", "a2a7", "g8h8", "b1b8"),
            puzzle("p-philidor", "El legado de Philidor", "Mate en 2", 1200,
                    "Juegan blancas y dan mate en 2.",
                    "Sacrificá la dama en g8: después el caballo hace el resto.",
                    "4r2k/6pp/7N/3Q4/8/8/8/6K1 w - - 0 1", "d5g8", "e8g8", "h6f7"),
            puzzle("p-anastasia", "Mate de Anastasia", "Mate en 2", 1400,
                    "Juegan blancas y dan mate en 2.",
                    "El caballo de e7 ya controla g8 y g6. Abrí la columna h.",
                    "5r1k/4Nppp/8/3R4/7Q/8/5PPP/6K1 w - - 0 1", "h4h7", "h8h7", "d5h5")
    );

    @Override
    public List<Lesson> lessons() {
        return lessons;
    }

    @Override
    public List<Puzzle> puzzles() {
        return puzzles.stream().sorted(Comparator.comparingInt(Puzzle::rating)).toList();
    }

    private static Lesson lesson(String id, LessonCategory category, String title, String theory,
                                 String goal, String fen, String... steps) {
        return new Lesson(id, category, title, theory, goal, new Exercise(fen, List.of(steps)));
    }

    private static Puzzle puzzle(String id, String title, String theme, int rating, String goal, String hint,
                                 String fen, String... steps) {
        return new Puzzle(id, title, theme, rating, goal, hint, new Exercise(fen, List.of(steps)));
    }
}
