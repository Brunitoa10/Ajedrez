package com.ajedrez.ui.common;

import com.ajedrez.ui.theme.Theme;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

/**
 * Íconos vectoriales dibujados con Java2D. Se evitan glifos Unicode decorativos (✓, ⏮...)
 * porque no todas las fuentes de Windows/Linux los incluyen.
 */
public final class Icons {

    private static final int SIZE = 14;

    private Icons() {
    }

    public static Icon done() {
        return new PaintedIcon(SIZE, (g, s) -> {
            g.setColor(Theme.SUCCESS);
            g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D check = new Path2D.Double();
            check.moveTo(s * 0.15, s * 0.55);
            check.lineTo(s * 0.42, s * 0.8);
            check.lineTo(s * 0.88, s * 0.22);
            g.draw(check);
        });
    }

    public static Icon failed() {
        return new PaintedIcon(SIZE, (g, s) -> {
            g.setColor(Theme.ERROR);
            g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine((int) (s * 0.2), (int) (s * 0.2), (int) (s * 0.8), (int) (s * 0.8));
            g.drawLine((int) (s * 0.8), (int) (s * 0.2), (int) (s * 0.2), (int) (s * 0.8));
        });
    }

    public static Icon pending() {
        return new PaintedIcon(SIZE, (g, s) -> {
            g.setColor(Theme.TEXT_MUTED);
            g.setStroke(new BasicStroke(1.6f));
            g.drawOval(2, 2, s - 5, s - 5);
        });
    }

    /** Flechas de navegación: dirección -1 (atrás) o 1 (adelante); "toEnd" agrega la barra de extremo. */
    public static Icon navigation(int direction, boolean toEnd) {
        return new PaintedIcon(16, (g, s) -> {
            g.setColor(Theme.TEXT);
            Path2D triangle = new Path2D.Double();
            double tip = direction > 0 ? s * 0.75 : s * 0.25;
            double back = direction > 0 ? s * 0.3 : s * 0.7;
            triangle.moveTo(tip, s * 0.5);
            triangle.lineTo(back, s * 0.2);
            triangle.lineTo(back, s * 0.8);
            triangle.closePath();
            g.fill(triangle);
            if (toEnd) {
                int barX = (int) (direction > 0 ? s * 0.78 : s * 0.12);
                g.fillRect(barX, (int) (s * 0.2), 2, (int) (s * 0.6) + 1);
            }
        });
    }

    @FunctionalInterface
    private interface Painter {
        public void paint(Graphics2D g, int size);
    }

    private record PaintedIcon(int size, Painter painter) implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.translate(x, y);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                painter.paint(g2, size);
            } finally {
                g2.dispose();
            }
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
}
