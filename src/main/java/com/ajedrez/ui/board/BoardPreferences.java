package com.ajedrez.ui.board;

import com.ajedrez.ui.theme.BoardTheme;

import java.util.ArrayList;
import java.util.List;

/**
 * Preferencias visuales compartidas por todos los tableros. Cada {@link BoardPanel} se registra
 * (Observer) y se actualiza solo cuando el usuario cambia el tema desde el menú.
 */
public final class BoardPreferences {

    private final List<BoardPanel> boards = new ArrayList<>();
    private BoardTheme theme = BoardTheme.GREEN;
    private boolean showCoordinates = true;

    public BoardPanel attach(BoardPanel board) {
        boards.add(board);
        board.setTheme(theme);
        board.setShowCoordinates(showCoordinates);
        return board;
    }

    public BoardPanel newBoard() {
        return attach(new BoardPanel());
    }

    public BoardTheme theme() {
        return theme;
    }

    public boolean showCoordinates() {
        return showCoordinates;
    }

    public void setTheme(BoardTheme newTheme) {
        theme = newTheme;
        boards.forEach(b -> b.setTheme(newTheme));
    }

    public void setShowCoordinates(boolean show) {
        showCoordinates = show;
        boards.forEach(b -> b.setShowCoordinates(show));
    }
}
