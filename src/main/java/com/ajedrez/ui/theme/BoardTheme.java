package com.ajedrez.ui.theme;

import java.awt.Color;
import java.util.List;

/**
 * Paleta del tablero. Es un valor intercambiable (Strategy de presentación): agregar un tema
 * es agregar una constante, sin tocar el componente que dibuja.
 */
public record BoardTheme(String name, Color light, Color dark, Color lastMoveLight, Color lastMoveDark,
                         Color selection, Color hint) {

    public static final BoardTheme GREEN = new BoardTheme("Clásico verde",
            new Color(0xEE, 0xEE, 0xD2), new Color(0x76, 0x96, 0x56),
            new Color(0xF6, 0xF6, 0x69), new Color(0xBA, 0xCA, 0x2B),
            new Color(0x60, 0x9E, 0xD8), new Color(0xE8, 0x7B, 0x2E));

    public static final BoardTheme WOOD = new BoardTheme("Madera",
            new Color(0xF0, 0xD9, 0xB5), new Color(0xB5, 0x88, 0x63),
            new Color(0xF7, 0xEC, 0x74), new Color(0xDA, 0xC3, 0x4B),
            new Color(0x60, 0x9E, 0xD8), new Color(0x2E, 0x7D, 0xE8));

    public static final BoardTheme BLUE = new BoardTheme("Azul",
            new Color(0xDE, 0xE3, 0xE6), new Color(0x8C, 0xA2, 0xAD),
            new Color(0xC3, 0xD8, 0x87), new Color(0x93, 0xB1, 0x66),
            new Color(0x4F, 0x8E, 0xC9), new Color(0xE8, 0x7B, 0x2E));

    public static final List<BoardTheme> ALL = List.of(GREEN, WOOD, BLUE);
}
