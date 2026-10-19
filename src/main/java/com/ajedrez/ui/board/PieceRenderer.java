package com.ajedrez.ui.board;

import com.ajedrez.domain.model.Piece;
import com.ajedrez.domain.model.PieceColor;
import com.ajedrez.domain.model.PieceType;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Dibuja piezas vectoriales a partir de los glifos Unicode de ajedrez (♔♕♖♗♘♙ / ♚♛♜♝♞♟).
 * <p>
 * Las fuentes no son consistentes (en DejaVu, por ejemplo, ambos alfiles son macizos y ambos reyes
 * son de contorno), así que al iniciar se mide la tinta de cada glifo, se calcula la silueta
 * rellenando los huecos geométricamente y se decide cómo dibujar los detalles. Resultado: piezas
 * correctas con cualquier fuente (Segoe UI Symbol en Windows, DejaVu en Linux...) y a cualquier tamaño.
 */
public final class PieceRenderer {

    private static final List<String> FONT_CANDIDATES = List.of(
            "Segoe UI Symbol", "DejaVu Sans", "Noto Sans Symbols2", "Arial Unicode MS", "FreeSerif", "Symbola");
    private static final float REFERENCE_SIZE = 100f;
    private static final double KING_COVERAGE = 0.80;
    /** Si el glifo de contorno cubre menos de este % de la silueta, es "dibujo de líneas" utilizable. */
    private static final double LINE_ART_MAX_INK = 0.6;

    private static final Color WHITE_FILL = new Color(0xFB, 0xFB, 0xF8);
    private static final Color WHITE_LINES = new Color(0x1E, 0x1E, 0x1E);
    private static final Color BLACK_FILL = new Color(0x26, 0x26, 0x2A);
    private static final Color BLACK_LINES = new Color(0xF0, 0xF0, 0xF0, 140);
    private static final Color SHADOW = new Color(0, 0, 0, 55);

    private final Font font;
    private final Map<PieceType, GlyphSet> glyphs = new EnumMap<>(PieceType.class);
    private final double fontScale;

    public PieceRenderer() {
        this.font = pickFont();
        FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
        Font reference = font.deriveFont(REFERENCE_SIZE);
        for (PieceType type : PieceType.values()) {
            glyphs.put(type, GlyphSet.create(reference, frc, type));
        }
        Rectangle2D king = glyphs.get(PieceType.KING).bounds();
        this.fontScale = KING_COVERAGE * REFERENCE_SIZE / Math.max(king.getWidth(), king.getHeight());
    }

    public void paint(Graphics2D g, Piece piece, double x, double y, double size) {
        GlyphSet set = glyphs.get(piece.type());
        double fontSize = size * fontScale;
        double k = fontSize / REFERENCE_SIZE;
        Rectangle2D b = set.bounds();
        double tx = x + (size - b.getWidth() * k) / 2 - b.getX() * k;
        double ty = y + (size - b.getHeight() * k) / 2 - b.getY() * k;
        AffineTransform placement = new AffineTransform(k, 0, 0, k, tx, ty);

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            Shape silhouette = placement.createTransformedShape(set.silhouette());
            double shadowOffset = Math.max(1, size / 40);
            g2.setColor(SHADOW);
            g2.fill(AffineTransform.getTranslateInstance(shadowOffset, shadowOffset).createTransformedShape(silhouette));

            Font sized = font.deriveFont((float) fontSize);
            FontRenderContext frc = g2.getFontRenderContext();
            Shape solidContours = sized.createGlyphVector(frc, String.valueOf(set.solidGlyph()))
                    .getOutline((float) tx, (float) ty);
            if (piece.color() == PieceColor.WHITE) {
                g2.setColor(WHITE_FILL);
                g2.fill(silhouette);
                g2.setColor(WHITE_LINES);
                if (set.hasLineArt()) {
                    g2.drawGlyphVector(sized.createGlyphVector(frc, String.valueOf(set.outlineGlyph())),
                            (float) tx, (float) ty);
                } else {
                    g2.setStroke(new BasicStroke((float) Math.max(1, size / 48), BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND));
                    g2.draw(solidContours);
                }
            } else {
                g2.setColor(BLACK_FILL);
                g2.fill(silhouette);
                g2.setColor(BLACK_LINES);
                g2.setStroke(new BasicStroke((float) Math.max(0.8, size / 60)));
                g2.draw(solidContours);
            }
        } finally {
            g2.dispose();
        }
    }

    /** Glifos y geometría de referencia (a 100 pt) de un tipo de pieza. */
    private record GlyphSet(char solidGlyph, char outlineGlyph, boolean hasLineArt, Shape silhouette,
                            Rectangle2D bounds) {

        static GlyphSet create(Font reference, FontRenderContext frc, PieceType type) {
            char whiteGlyph = (char) ('♔' + glyphOffset(type));
            char blackGlyph = (char) ('♚' + glyphOffset(type));
            int blackInk = inkOf(reference, frc, blackGlyph);
            int whiteInk = inkOf(reference, frc, whiteGlyph);
            boolean blackIsSolid = blackInk >= whiteInk;
            char solid = blackIsSolid ? blackGlyph : whiteGlyph;
            char outline = blackIsSolid ? whiteGlyph : blackGlyph;
            GlyphVector vector = reference.createGlyphVector(frc, String.valueOf(solid));
            Shape filled = fillHoles(vector.getOutline());
            double lineArtRatio = (double) Math.min(blackInk, whiteInk) / Math.max(1, inkOf(filled));
            return new GlyphSet(solid, outline, lineArtRatio < LINE_ART_MAX_INK, filled, vector.getVisualBounds());
        }

        /** Píxeles con tinta de una silueta (para comparar con la de los glifos). */
        private static int inkOf(Shape shape) {
            BufferedImage image = canvas();
            Graphics2D g = image.createGraphics();
            g.setColor(Color.BLACK);
            g.translate(image.getWidth() / 4.0, image.getHeight() * 0.75);
            g.fill(shape);
            g.dispose();
            return countInk(image);
        }

        /** Cantidad de píxeles con tinta: el glifo macizo siempre tiene más. */
        private static int inkOf(Font font, FontRenderContext frc, char glyph) {
            BufferedImage image = canvas();
            Graphics2D g = image.createGraphics();
            g.setColor(Color.BLACK);
            g.drawGlyphVector(font.createGlyphVector(frc, String.valueOf(glyph)),
                    image.getWidth() / 4f, image.getHeight() * 0.75f);
            g.dispose();
            return countInk(image);
        }

        private static BufferedImage canvas() {
            int size = (int) REFERENCE_SIZE * 2;
            return new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        }

        private static int countInk(BufferedImage image) {
            int ink = 0;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    if ((image.getRGB(x, y) >>> 24) > 128) {
                        ink++;
                    }
                }
            }
            return ink;
        }

        /** Une cada contorno como figura sólida: el resultado es la silueta sin huecos. */
        private static Shape fillHoles(Shape outline) {
            Area result = new Area();
            Path2D current = null;
            double[] c = new double[6];
            for (PathIterator it = outline.getPathIterator(null); !it.isDone(); it.next()) {
                switch (it.currentSegment(c)) {
                    case PathIterator.SEG_MOVETO -> {
                        if (current != null) {
                            result.add(new Area(current));
                        }
                        current = new Path2D.Double(Path2D.WIND_NON_ZERO);
                        current.moveTo(c[0], c[1]);
                    }
                    case PathIterator.SEG_LINETO -> current.lineTo(c[0], c[1]);
                    case PathIterator.SEG_QUADTO -> current.quadTo(c[0], c[1], c[2], c[3]);
                    case PathIterator.SEG_CUBICTO -> current.curveTo(c[0], c[1], c[2], c[3], c[4], c[5]);
                    case PathIterator.SEG_CLOSE -> current.closePath();
                    default -> { }
                }
            }
            if (current != null) {
                result.add(new Area(current));
            }
            return result;
        }
    }

    /** Orden de los glifos Unicode: rey, dama, torre, alfil, caballo, peón. */
    private static int glyphOffset(PieceType type) {
        return switch (type) {
            case KING -> 0;
            case QUEEN -> 1;
            case ROOK -> 2;
            case BISHOP -> 3;
            case KNIGHT -> 4;
            case PAWN -> 5;
        };
    }

    private static Font pickFont() {
        Set<String> installed = Arrays.stream(GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames()).collect(Collectors.toSet());
        for (String name : FONT_CANDIDATES) {
            if (installed.contains(name)) {
                Font candidate = new Font(name, Font.PLAIN, 48);
                if (candidate.canDisplay('♚') && candidate.canDisplay('♔')) {
                    return candidate;
                }
            }
        }
        return new Font(Font.DIALOG, Font.PLAIN, 48);
    }
}
