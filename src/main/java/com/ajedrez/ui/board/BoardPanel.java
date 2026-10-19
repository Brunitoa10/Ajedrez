package com.ajedrez.ui.board;

import com.ajedrez.domain.model.Move;
import com.ajedrez.domain.model.Piece;
import com.ajedrez.domain.model.PieceColor;
import com.ajedrez.domain.model.PieceType;
import com.ajedrez.domain.model.Position;
import com.ajedrez.domain.model.Square;
import com.ajedrez.ui.theme.BoardTheme;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Componente reutilizable de tablero: dibuja una {@link Position} y permite mover con clic-clic
 * o arrastrando. No conoce las reglas: recibe un proveedor de jugadas legales y avisa la jugada
 * elegida por un callback. Se usa en la partida, las lecciones, los problemas y el historial.
 */
public final class BoardPanel extends JComponent {

    private static final int PREFERRED_SIZE = 560;
    private static final Color CHECK_COLOR = new Color(0xE0, 0x30, 0x30);
    private static final Color MOVE_DOT = new Color(0, 0, 0, 55);

    private final PieceRenderer renderer = new PieceRenderer();

    private Position position = Position.initial();
    private PieceColor bottomColor = PieceColor.WHITE;
    private BoardTheme theme = BoardTheme.GREEN;
    private boolean showCoordinates = true;
    private boolean interactive;
    private Move lastMove;
    private Move hintMove;
    private Square checkedKing;

    private Function<Square, List<Move>> legalMovesProvider = square -> List.of();
    private Consumer<Move> moveListener = move -> { };

    private Square selected;
    private List<Move> selectedMoves = List.of();
    private Point dragPoint;

    public BoardPanel() {
        setPreferredSize(new Dimension(PREFERRED_SIZE, PREFERRED_SIZE));
        setMinimumSize(new Dimension(240, 240));
        setOpaque(false);
        MouseHandler handler = new MouseHandler();
        addMouseListener(handler);
        addMouseMotionListener(handler);
    }

    // ------------------------------------------------------------------ API pública

    /**
     * @param inCheck si el bando que mueve está en jaque (se resalta su rey)
     */
    public void showPosition(Position newPosition, Move last, boolean inCheck) {
        this.position = newPosition;
        this.lastMove = last;
        this.hintMove = null;
        this.checkedKing = inCheck ? newPosition.board().findKing(newPosition.sideToMove()).orElse(null) : null;
        clearSelection();
    }

    public void setLegalMovesProvider(Function<Square, List<Move>> provider) {
        this.legalMovesProvider = provider;
    }

    public void setMoveListener(Consumer<Move> listener) {
        this.moveListener = listener;
    }

    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
        if (!interactive) {
            clearSelection();
        }
        setCursor(Cursor.getPredefinedCursor(interactive ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    public void setBottomColor(PieceColor color) {
        this.bottomColor = color;
        repaint();
    }

    public PieceColor bottomColor() {
        return bottomColor;
    }

    public void flip() {
        setBottomColor(bottomColor.opposite());
    }

    public void setTheme(BoardTheme theme) {
        this.theme = theme;
        repaint();
    }

    public void setShowCoordinates(boolean show) {
        this.showCoordinates = show;
        repaint();
    }

    public void showHint(Move move) {
        this.hintMove = move;
        repaint();
    }

    public void clearSelection() {
        selected = null;
        selectedMoves = List.of();
        dragPoint = null;
        repaint();
    }

    // ------------------------------------------------------------------ geometría

    private double squareSize() {
        return Math.floor(Math.min(getWidth(), getHeight()) / 8.0);
    }

    private double originX() {
        return (getWidth() - squareSize() * 8) / 2;
    }

    private double originY() {
        return (getHeight() - squareSize() * 8) / 2;
    }

    private Rectangle2D rectOf(Square square) {
        double size = squareSize();
        int column = bottomColor == PieceColor.WHITE ? square.file() : 7 - square.file();
        int row = bottomColor == PieceColor.WHITE ? 7 - square.rank() : square.rank();
        return new Rectangle2D.Double(originX() + column * size, originY() + row * size, size, size);
    }

    private Optional<Square> squareAt(Point point) {
        double size = squareSize();
        int column = (int) Math.floor((point.x - originX()) / size);
        int row = (int) Math.floor((point.y - originY()) / size);
        if (column < 0 || column > 7 || row < 0 || row > 7) {
            return Optional.empty();
        }
        int file = bottomColor == PieceColor.WHITE ? column : 7 - column;
        int rank = bottomColor == PieceColor.WHITE ? 7 - row : row;
        return Optional.of(Square.of(file, rank));
    }

    // ------------------------------------------------------------------ dibujo

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            paintSquares(g);
            paintCheck(g);
            paintMoveTargets(g);
            if (showCoordinates) {
                paintCoordinates(g);
            }
            paintPieces(g);
            paintHintArrow(g);
            paintDraggedPiece(g);
        } finally {
            g.dispose();
        }
    }

    private void paintSquares(Graphics2D g) {
        for (Square square : Square.ALL) {
            Rectangle2D rect = rectOf(square);
            boolean highlighted = lastMove != null && (square.equals(lastMove.from()) || square.equals(lastMove.to()));
            Color color;
            if (highlighted) {
                color = square.isLight() ? theme.lastMoveLight() : theme.lastMoveDark();
            } else {
                color = square.isLight() ? theme.light() : theme.dark();
            }
            g.setColor(color);
            g.fill(rect);
            if (square.equals(selected)) {
                g.setColor(withAlpha(theme.selection(), 140));
                g.fill(rect);
            }
        }
    }

    private void paintCheck(Graphics2D g) {
        if (checkedKing == null) {
            return;
        }
        Rectangle2D rect = rectOf(checkedKing);
        Point2D center = new Point2D.Double(rect.getCenterX(), rect.getCenterY());
        float radius = (float) (rect.getWidth() * 0.7);
        g.setPaint(new RadialGradientPaint(center, radius, new float[]{0f, 0.45f, 1f},
                new Color[]{CHECK_COLOR, withAlpha(CHECK_COLOR, 170), withAlpha(CHECK_COLOR, 0)}));
        g.fill(rect);
    }

    private void paintMoveTargets(Graphics2D g) {
        double size = squareSize();
        for (Move move : selectedMoves) {
            Rectangle2D rect = rectOf(move.to());
            g.setColor(MOVE_DOT);
            if (position.isCapture(move)) {
                g.setStroke(new BasicStroke((float) (size * 0.08)));
                double inset = size * 0.06;
                g.draw(new Ellipse2D.Double(rect.getX() + inset, rect.getY() + inset,
                        size - 2 * inset, size - 2 * inset));
            } else {
                double dot = size * 0.30;
                g.fill(new Ellipse2D.Double(rect.getCenterX() - dot / 2, rect.getCenterY() - dot / 2, dot, dot));
            }
        }
    }

    private void paintCoordinates(Graphics2D g) {
        double size = squareSize();
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, (int) Math.max(9, size * 0.16)));
        for (int i = 0; i < 8; i++) {
            Square fileSquare = bottomColor == PieceColor.WHITE ? Square.of(i, 0) : Square.of(7 - i, 7);
            Rectangle2D rect = rectOf(fileSquare);
            g.setColor(fileSquare.isLight() ? theme.dark() : theme.light());
            g.drawString(String.valueOf(fileSquare.fileChar()),
                    (float) (rect.getMaxX() - size * 0.17), (float) (rect.getMaxY() - size * 0.06));

            Square rankSquare = bottomColor == PieceColor.WHITE ? Square.of(0, i) : Square.of(7, 7 - i);
            rect = rectOf(rankSquare);
            g.setColor(rankSquare.isLight() ? theme.dark() : theme.light());
            g.drawString(String.valueOf(rankSquare.rankChar()),
                    (float) (rect.getX() + size * 0.05), (float) (rect.getY() + size * 0.2));
        }
    }

    private void paintPieces(Graphics2D g) {
        for (Square square : Square.ALL) {
            Piece piece = position.board().getOrNull(square);
            if (piece == null || (dragPoint != null && square.equals(selected))) {
                continue;
            }
            Rectangle2D rect = rectOf(square);
            renderer.paint(g, piece, rect.getX(), rect.getY(), rect.getWidth());
        }
    }

    private void paintHintArrow(Graphics2D g) {
        if (hintMove == null) {
            return;
        }
        Rectangle2D from = rectOf(hintMove.from());
        Rectangle2D to = rectOf(hintMove.to());
        double size = squareSize();
        double x1 = from.getCenterX();
        double y1 = from.getCenterY();
        double x2 = to.getCenterX();
        double y2 = to.getCenterY();
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double head = size * 0.38;
        double shaftEndX = x2 - Math.cos(angle) * head * 0.8;
        double shaftEndY = y2 - Math.sin(angle) * head * 0.8;

        g.setColor(withAlpha(theme.hint(), 200));
        g.setStroke(new BasicStroke((float) (size * 0.16), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(x1, y1, shaftEndX, shaftEndY));
        Path2D arrowHead = new Path2D.Double();
        arrowHead.moveTo(x2, y2);
        arrowHead.lineTo(x2 - head * Math.cos(angle - Math.PI / 6), y2 - head * Math.sin(angle - Math.PI / 6));
        arrowHead.lineTo(x2 - head * Math.cos(angle + Math.PI / 6), y2 - head * Math.sin(angle + Math.PI / 6));
        arrowHead.closePath();
        g.fill(arrowHead);
    }

    private void paintDraggedPiece(Graphics2D g) {
        if (dragPoint == null || selected == null) {
            return;
        }
        Piece piece = position.board().getOrNull(selected);
        if (piece == null) {
            return;
        }
        double size = squareSize() * 1.08;
        renderer.paint(g, piece, dragPoint.x - size / 2, dragPoint.y - size / 2, size);
    }

    private static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    // ------------------------------------------------------------------ interacción

    private void select(Square square) {
        selected = square;
        selectedMoves = legalMovesProvider.apply(square);
        repaint();
    }

    private boolean isTarget(Square square) {
        return selectedMoves.stream().anyMatch(m -> m.to().equals(square));
    }

    private boolean isOwnPiece(Square square) {
        return position.board().isOccupiedBy(square, position.sideToMove());
    }

    private void commitMoveTo(Square target) {
        List<Move> candidates = selectedMoves.stream().filter(m -> m.to().equals(target)).toList();
        clearSelection();
        if (candidates.isEmpty()) {
            return;
        }
        Move chosen = candidates.get(0);
        if (candidates.size() > 1) {
            Optional<PieceType> promotion = PromotionChooser.choose(this, position.sideToMove());
            if (promotion.isEmpty()) {
                return;
            }
            chosen = candidates.stream()
                    .filter(m -> m.promotionPiece() == promotion.get())
                    .findFirst()
                    .orElse(chosen);
        }
        moveListener.accept(chosen);
    }

    private final class MouseHandler extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            if (!interactive) {
                return;
            }
            Optional<Square> clicked = squareAt(e.getPoint());
            if (clicked.isEmpty()) {
                clearSelection();
                return;
            }
            Square square = clicked.get();
            if (selected != null && isTarget(square)) {
                commitMoveTo(square);
                return;
            }
            if (isOwnPiece(square)) {
                select(square);
                dragPoint = e.getPoint();
            } else {
                clearSelection();
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (dragPoint != null) {
                dragPoint = e.getPoint();
                repaint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (dragPoint == null) {
                return;
            }
            dragPoint = null;
            Optional<Square> target = squareAt(e.getPoint());
            if (target.isPresent() && !target.get().equals(selected) && isTarget(target.get())) {
                commitMoveTo(target.get());
            } else {
                repaint();
            }
        }
    }
}
