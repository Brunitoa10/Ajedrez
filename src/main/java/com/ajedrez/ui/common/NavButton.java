package com.ajedrez.ui.common;

import com.ajedrez.ui.theme.Theme;

import javax.swing.JToggleButton;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Botón plano de la barra lateral: resaltado con el color de acento cuando está seleccionado. */
public final class NavButton extends JToggleButton {

    private static final int ARC = 10;

    public NavButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setFocusable(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setForeground(Theme.TEXT);
        setFont(Theme.font(Font.BOLD, 14));
        setHorizontalAlignment(LEFT);
        setBorder(Theme.padding(9, 14));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (isSelected()) {
                g2.setColor(Theme.ACCENT_DARK);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
            } else if (getModel().isRollover()) {
                g2.setColor(Theme.SURFACE_ALT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
            }
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
