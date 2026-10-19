package com.ajedrez.ui.play;

import com.ajedrez.application.GameService;
import com.ajedrez.application.MatchSettings;
import com.ajedrez.domain.model.Move;
import com.ajedrez.domain.model.Piece;
import com.ajedrez.domain.model.PieceColor;
import com.ajedrez.domain.model.PieceType;
import com.ajedrez.domain.model.Position;
import com.ajedrez.ui.board.BoardPanel;
import com.ajedrez.ui.board.BoardPreferences;
import com.ajedrez.ui.board.PieceIcon;
import com.ajedrez.ui.common.MoveListModel;
import com.ajedrez.ui.common.Ui;
import com.ajedrez.ui.theme.Theme;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Pantalla de juego: tablero + panel lateral con jugadores, estado, jugadas y acciones. */
public final class PlayPanel extends JPanel implements PlayView {

    private static final int SIDEBAR_WIDTH = 300;

    private final PlayPresenter presenter;
    private final BoardPanel board;
    private final JLabel topPlayer = new JLabel();
    private final JLabel bottomPlayer = new JLabel();
    private final JLabel status = new JLabel(" ");
    private final JProgressBar thinkingBar = new JProgressBar();
    private final MoveListModel movesModel = new MoveListModel();
    private final JTable movesTable = new JTable(movesModel);
    private final JButton undoButton;
    private final JButton resignButton;
    private final JButton drawButton;
    private final JButton hintButton;

    private String whiteName = "Blancas";
    private String blackName = "Negras";
    private MatchSettings lastSettings;

    public PlayPanel(GameService service, BoardPreferences boardPreferences) {
        super(new BorderLayout(16, 0));
        this.presenter = new PlayPresenter(service, SwingUtilities::invokeLater, this);
        this.board = boardPreferences.newBoard();
        board.setLegalMovesProvider(presenter::legalMovesFrom);
        board.setMoveListener(presenter::onHumanMove);

        undoButton = Ui.button("Deshacer", presenter::undo);
        resignButton = Ui.button("Abandonar", this::confirmResign);
        drawButton = Ui.button("Tablas", presenter::agreeDraw);
        hintButton = Ui.button("Pista", presenter::requestHint);

        setBorder(Theme.padding(16, 16));
        setBackground(Theme.BACKGROUND);
        add(board, BorderLayout.CENTER);
        add(buildSidebar(), BorderLayout.EAST);
    }

    /** Abre el diálogo de nueva partida y la inicia si el usuario acepta. */
    public void newGame() {
        NewGameDialog.show(SwingUtilities.getWindowAncestor(this), lastSettings).ifPresent(settings -> {
            lastSettings = settings;
            presenter.start(settings);
        });
    }

    public boolean hasGame() {
        return presenter.hasGame();
    }

    public void dispose() {
        presenter.dispose();
    }

    private JComponent buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 10));
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 10));

        stylePlayerLabel(topPlayer);
        stylePlayerLabel(bottomPlayer);
        status.setFont(Theme.font(Font.BOLD, 15));
        status.setForeground(Theme.ACCENT);
        thinkingBar.setIndeterminate(true);
        thinkingBar.setVisible(false);
        thinkingBar.setPreferredSize(new Dimension(10, 6));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(Ui.card(topPlayer));
        header.add(Box.createVerticalStrut(10));
        header.add(status);
        header.add(Box.createVerticalStrut(6));
        header.add(thinkingBar);
        for (Component c : header.getComponents()) {
            ((JComponent) c).setAlignmentX(LEFT_ALIGNMENT);
        }

        Ui.styleTable(movesTable);
        movesTable.getColumnModel().getColumn(0).setMaxWidth(44);
        movesTable.getColumnModel().getColumn(0).setCellRenderer(Ui.centered());
        movesTable.setRowSelectionAllowed(false);

        JPanel actions = new JPanel(new GridLayout(0, 2, 8, 8));
        actions.setOpaque(false);
        actions.add(Ui.primaryButton("Nueva partida", this::newGame));
        actions.add(undoButton);
        actions.add(hintButton);
        actions.add(Ui.button("Girar tablero", this::flipBoard));
        actions.add(drawButton);
        actions.add(resignButton);
        actions.add(Ui.button("Copiar PGN", this::copyPgn));
        actions.add(Ui.button("Guardar PGN...", this::savePgn));

        JPanel footer = new JPanel(new BorderLayout(0, 10));
        footer.setOpaque(false);
        footer.add(Ui.card(bottomPlayer), BorderLayout.NORTH);
        footer.add(actions, BorderLayout.CENTER);

        sidebar.add(header, BorderLayout.NORTH);
        sidebar.add(Ui.scroll(movesTable), BorderLayout.CENTER);
        sidebar.add(footer, BorderLayout.SOUTH);
        setControlsState(false, false, false);
        hintButton.setEnabled(false);
        return sidebar;
    }

    private static void stylePlayerLabel(JLabel label) {
        label.setFont(Theme.font(Font.BOLD, 14));
        label.setForeground(Theme.TEXT);
        label.setIconTextGap(10);
    }

    private void flipBoard() {
        board.flip();
        updatePlayerLabels();
    }

    private void confirmResign() {
        int answer = JOptionPane.showConfirmDialog(this, "¿Seguro que querés abandonar?", "Abandonar",
                JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            presenter.resign();
        }
    }

    private void copyPgn() {
        presenter.pgn().ifPresent(pgn -> {
            Ui.copyToClipboard(pgn);
            showStatus("PGN copiado al portapapeles");
        });
    }

    private void savePgn() {
        presenter.pgn().ifPresent(pgn -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Partidas PGN", "pgn"));
            chooser.setSelectedFile(new File("partida.pgn"));
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            Path target = chooser.getSelectedFile().toPath();
            if (!target.toString().toLowerCase().endsWith(".pgn")) {
                target = target.resolveSibling(target.getFileName() + ".pgn");
            }
            try {
                Files.writeString(target, pgn, StandardCharsets.UTF_8);
                showStatus("PGN guardado en " + target.getFileName());
            } catch (IOException e) {
                showError("No se pudo guardar el archivo: " + e.getMessage());
            }
        });
    }

    private void updatePlayerLabels() {
        PieceColor bottom = board.bottomColor();
        setPlayer(bottomPlayer, bottom);
        setPlayer(topPlayer, bottom.opposite());
    }

    private void setPlayer(JLabel label, PieceColor color) {
        label.setText(color == PieceColor.WHITE ? whiteName : blackName);
        label.setIcon(new PieceIcon(Piece.of(color, PieceType.KING), 26));
    }

    // ------------------------------------------------------------------ PlayView

    @Override
    public void setOrientation(PieceColor bottomColor) {
        board.setBottomColor(bottomColor);
        updatePlayerLabels();
    }

    @Override
    public void showPlayers(String white, String black) {
        whiteName = white;
        blackName = black;
        updatePlayerLabels();
    }

    @Override
    public void showPosition(Position position, Move lastMove, boolean inCheck) {
        board.showPosition(position, lastMove, inCheck);
    }

    @Override
    public void showMoves(List<String> sanMoves) {
        movesModel.setMoves(sanMoves);
        int lastRow = movesModel.getRowCount() - 1;
        if (lastRow >= 0) {
            movesTable.scrollRectToVisible(movesTable.getCellRect(lastRow, 0, true));
        }
    }

    @Override
    public void showStatus(String text) {
        status.setText(text);
    }

    @Override
    public void setThinking(boolean thinking) {
        thinkingBar.setVisible(thinking);
        revalidate();
    }

    @Override
    public void setInteractive(boolean interactive) {
        board.setInteractive(interactive);
        hintButton.setEnabled(interactive);
    }

    @Override
    public void setControlsState(boolean canUndo, boolean canResign, boolean canOfferDraw) {
        undoButton.setEnabled(canUndo);
        resignButton.setEnabled(canResign);
        drawButton.setEnabled(canOfferDraw);
    }

    @Override
    public void showHint(Move move) {
        board.showHint(move);
        showStatus("Pista: mirá la flecha");
    }

    @Override
    public void showGameOver(String message) {
        SwingUtilities.invokeLater(() -> Ui.info(this, "Fin de la partida",
                message + "\n\nLa partida quedó guardada en el historial."));
    }

    @Override
    public void showError(String message) {
        Ui.error(this, message);
    }
}
