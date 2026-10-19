package com.ajedrez.ui.board;

import com.ajedrez.domain.model.Piece;

import javax.swing.Icon;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** Pieza como {@link Icon} de Swing (botones, listas, diálogo de coronación). */
public final class PieceIcon implements Icon {

    private static final PieceRenderer RENDERER = new PieceRenderer();

    private final Piece piece;
    private final int size;

    public PieceIcon(Piece piece, int size) {
        this.piece = piece;
        this.size = size;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        RENDERER.paint((Graphics2D) g, piece, x, y, size);
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }
}
