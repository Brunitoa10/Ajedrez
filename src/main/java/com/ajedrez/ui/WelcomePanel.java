package com.ajedrez.ui;

import com.ajedrez.domain.model.Piece;
import com.ajedrez.ui.board.PieceRenderer;
import com.ajedrez.ui.theme.Theme;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.font.TextAttribute;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;

/**
 * Pantalla de bienvenida: portada sobria con un único botón "Jugar".
 * Todo se dibuja con Java2D (sin imágenes externas) para que escale a cualquier resolución.
 */
public final class WelcomePanel extends JComponent {

    private static final Color BACKGROUND_TOP = new Color(0x14, 0x16, 0x1C);
    private static final Color BACKGROUND_BOTTOM = new Color(0x0B, 0x0C, 0x10);
    private static final Color GOLD = new Color(0xC9, 0xA9, 0x61);
    private static final Color GOLD_LIGHT = new Color(0xE4, 0xC9, 0x8A);
    private static final Color TEXT_SOFT = new Color(0xA8, 0xAD, 0xB8);
    private static final Color SQUARE_TINT = new Color(255, 255, 255, 6);
    private static final String TITLE = "AJEDREZ";
    private static final String TAGLINE = "Estrategia  ·  Aprendizaje  ·  5 niveles de inteligencia artificial";
    private static final int BUTTON_WIDTH = 240;
    private static final int BUTTON_HEIGHT = 56;

    private final PieceRenderer pieceRenderer = new PieceRenderer();
    private final JButton playButton;

    public WelcomePanel(Runnable onPlay) {
        setLayout(null);
        setPreferredSize(new Dimension(1280, 800));
        playButton = new PlayButton(onPlay);
        add(playButton);

        String key = "jugar";
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), key);
        getActionMap().put(key, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isShowing()) {
                    onPlay.run();
                }
            }
        });
    }

    @Override
    public void doLayout() {
        int x = (getWidth() - BUTTON_WIDTH) / 2;
        int y = (int) (getHeight() * 0.68);
        playButton.setBounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            paintBackground(g);
            paintPieces(g);
            paintTitle(g);
            paintFooter(g);
        } finally {
            g.dispose();
        }
    }

    private void paintBackground(Graphics2D g) {
        int w = getWidth();
        int h = getHeight();
        g.setPaint(new GradientPaint(0, 0, BACKGROUND_TOP, 0, h, BACKGROUND_BOTTOM));
        g.fillRect(0, 0, w, h);

        // Tablero sutil en perspectiva plana, apenas visible.
        int square = Math.max(48, w / 18);
        g.setColor(SQUARE_TINT);
        for (int row = 0; row * square < h; row++) {
            for (int col = 0; col * square < w; col++) {
                if ((row + col) % 2 == 0) {
                    g.fillRect(col * square, row * square, square, square);
                }
            }
        }

        // Halo dorado detrás del título y viñeta en los bordes.
        Point2D center = new Point2D.Double(w / 2.0, h * 0.38);
        g.setPaint(new RadialGradientPaint(center, Math.max(w, h) * 0.45f, new float[]{0f, 1f},
                new Color[]{new Color(201, 169, 97, 38), new Color(201, 169, 97, 0)}));
        g.fillRect(0, 0, w, h);
        g.setPaint(new RadialGradientPaint(new Point2D.Double(w / 2.0, h / 2.0), Math.max(w, h) * 0.75f,
                new float[]{0.55f, 1f}, new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 170)}));
        g.fillRect(0, 0, w, h);
    }

    private void paintPieces(Graphics2D g) {
        int h = getHeight();
        double size = h * 0.22;
        double y = h * 0.14;
        double centerX = getWidth() / 2.0;
        Graphics2D faded = (Graphics2D) g.create();
        faded.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.92f));
        pieceRenderer.paint(faded, Piece.BLACK_KING, centerX - size * 0.95, y + size * 0.05, size * 0.9);
        pieceRenderer.paint(faded, Piece.WHITE_KING, centerX + size * 0.05, y + size * 0.05, size * 0.9);
        faded.dispose();
        pieceRenderer.paint(g, Piece.WHITE_KNIGHT, centerX - size / 2, y - size * 0.02, size);
    }

    private void paintTitle(Graphics2D g) {
        int w = getWidth();
        int h = getHeight();
        Font titleFont = Theme.font(Font.BOLD, Math.max(44, h / 11f))
                .deriveFont(Map.of(TextAttribute.TRACKING, 0.28));
        g.setFont(titleFont);
        FontMetrics fm = g.getFontMetrics();
        int titleY = (int) (h * 0.50);
        int titleX = (w - fm.stringWidth(TITLE)) / 2;
        g.setPaint(new GradientPaint(0, titleY - fm.getAscent(), GOLD_LIGHT, 0, titleY, GOLD));
        g.drawString(TITLE, titleX, titleY);

        int lineWidth = Math.min(320, w / 4);
        int lineY = titleY + fm.getDescent() + 14;
        g.setStroke(new BasicStroke(1.2f));
        g.setPaint(new GradientPaint((w - lineWidth) / 2f, 0, new Color(201, 169, 97, 0),
                w / 2f, 0, GOLD, true));
        g.drawLine((w - lineWidth) / 2, lineY, (w + lineWidth) / 2, lineY);

        g.setFont(Theme.font(Font.PLAIN, Math.max(14, h / 52f)));
        g.setColor(TEXT_SOFT);
        FontMetrics small = g.getFontMetrics();
        g.drawString(TAGLINE, (w - small.stringWidth(TAGLINE)) / 2, lineY + 22 + small.getAscent());
    }

    private void paintFooter(Graphics2D g) {
        String footer = "Versión 1.0  ·  Presioná Enter para comenzar";
        g.setFont(Theme.font(Font.PLAIN, 12));
        g.setColor(new Color(0x6B, 0x70, 0x7C));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(footer, (getWidth() - fm.stringWidth(footer)) / 2, getHeight() - 28);
    }

    /** Botón tipo "píldora" con borde dorado que se rellena al pasar el mouse. */
    private static final class PlayButton extends JButton {

        public PlayButton(Runnable onPlay) {
            super("JUGAR");
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(Theme.font(Font.BOLD, 17).deriveFont(Map.of(TextAttribute.TRACKING, 0.2)));
            addActionListener(e -> onPlay.run());
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                boolean hover = getModel().isRollover();
                boolean pressed = getModel().isPressed();
                RoundRectangle2D pill = new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2,
                        getHeight(), getHeight());
                if (hover || pressed) {
                    g.setPaint(new GradientPaint(0, 0, pressed ? GOLD : GOLD_LIGHT, 0, getHeight(), GOLD));
                    g.fill(pill);
                } else {
                    g.setColor(new Color(201, 169, 97, 28));
                    g.fill(pill);
                }
                g.setColor(GOLD);
                g.setStroke(new BasicStroke(1.6f));
                g.draw(pill);

                g.setFont(getFont());
                FontMetrics fm = g.getFontMetrics();
                String text = getText();
                g.setColor(hover || pressed ? BACKGROUND_BOTTOM : GOLD_LIGHT);
                g.drawString(text, (getWidth() - fm.stringWidth(text)) / 2,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            } finally {
                g.dispose();
            }
        }
    }
}
