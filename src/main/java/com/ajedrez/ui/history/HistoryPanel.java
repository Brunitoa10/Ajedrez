package com.ajedrez.ui.history;

import com.ajedrez.application.GameHistoryService;
import com.ajedrez.application.GameHistoryService.Replay;
import com.ajedrez.application.GameRecord;
import com.ajedrez.domain.model.PieceColor;
import com.ajedrez.domain.model.Position;
import com.ajedrez.ui.Refreshable;
import com.ajedrez.ui.board.BoardPanel;
import com.ajedrez.ui.board.BoardPreferences;
import com.ajedrez.ui.common.Icons;
import com.ajedrez.ui.common.MoveListModel;
import com.ajedrez.ui.common.Ui;
import com.ajedrez.ui.theme.Theme;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/** Pantalla "Historial": lista de partidas guardadas con reproductor jugada a jugada. */
public final class HistoryPanel extends JPanel implements Refreshable {

    private final GameHistoryService history;
    private final GamesTableModel gamesModel = new GamesTableModel();
    private final JTable gamesTable = new JTable(gamesModel);
    private final BoardPanel board;
    private final MoveListModel movesModel = new MoveListModel();
    private final JTable movesTable = new JTable(movesModel);
    private final JLabel plyLabel = Ui.muted(" ");

    private Replay replay;
    private int ply;

    public HistoryPanel(GameHistoryService history, BoardPreferences boardPreferences) {
        super(new BorderLayout());
        this.history = history;
        this.board = boardPreferences.newBoard();
        setBorder(Theme.padding(16, 16));
        setBackground(Theme.BACKGROUND);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildGamesList(), buildReplay());
        split.setResizeWeight(0.0);
        split.setDividerSize(8);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);
        bindArrowKeys();
    }

    @Override
    public void refresh() {
        gamesModel.setGames(history.allGames());
        if (gamesModel.getRowCount() > 0) {
            gamesTable.setRowSelectionInterval(0, 0);
        } else {
            replay = null;
            movesModel.setMoves(List.of());
            board.showPosition(Position.initial(), null, false);
            plyLabel.setText("Todavía no hay partidas guardadas. ¡Jugá una!");
        }
    }

    private JComponent buildGamesList() {
        Ui.styleTable(gamesTable);
        gamesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gamesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedGame().ifPresent(this::openReplay);
            }
        });
        int[] widths = {120, 200, 200, 90, 180, 80};
        for (int i = 0; i < widths.length; i++) {
            gamesTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        gamesTable.getColumnModel().getColumn(3).setCellRenderer(Ui.centered());
        gamesTable.getColumnModel().getColumn(5).setCellRenderer(Ui.centered());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttons.setOpaque(false);
        JPanel grid = new JPanel(new GridLayout(1, 3, 8, 0));
        grid.setOpaque(false);
        grid.add(Ui.button("Copiar PGN", this::copyPgn));
        grid.add(Ui.button("Exportar PGN...", this::exportPgn));
        grid.add(Ui.button("Eliminar", this::deleteSelected));
        buttons.add(grid);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.add(Ui.title("Historial de partidas"), BorderLayout.NORTH);
        panel.add(Ui.scroll(gamesTable), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        panel.setBorder(Theme.padding(0, 0, 10, 0));
        panel.setPreferredSize(new Dimension(10, 250));
        panel.setMinimumSize(new Dimension(10, 140));
        return panel;
    }

    private JComponent buildReplay() {
        Ui.styleTable(movesTable);
        movesTable.getColumnModel().getColumn(0).setMaxWidth(44);
        movesTable.getColumnModel().getColumn(0).setCellRenderer(Ui.centered());
        movesTable.setCellSelectionEnabled(true);
        movesTable.getSelectionModel().addListSelectionListener(e -> jumpToSelectedCell());
        movesTable.getColumnModel().getSelectionModel().addListSelectionListener(e -> jumpToSelectedCell());

        JPanel nav = new JPanel(new GridLayout(1, 4, 6, 0));
        nav.setOpaque(false);
        nav.add(navButton(Icons.navigation(-1, true), "Inicio", () -> goTo(0)));
        nav.add(navButton(Icons.navigation(-1, false), "Anterior", () -> goTo(ply - 1)));
        nav.add(navButton(Icons.navigation(1, false), "Siguiente", () -> goTo(ply + 1)));
        nav.add(navButton(Icons.navigation(1, true), "Final", () -> goTo(Integer.MAX_VALUE)));

        JPanel controls = new JPanel(new BorderLayout(0, 6));
        controls.setOpaque(false);
        controls.add(nav, BorderLayout.NORTH);
        controls.add(plyLabel, BorderLayout.SOUTH);

        JPanel right = new JPanel(new BorderLayout(0, 8));
        right.setOpaque(false);
        movesTable.setPreferredScrollableViewportSize(new Dimension(220, 200));
        right.add(Ui.scroll(movesTable), BorderLayout.CENTER);
        right.add(controls, BorderLayout.SOUTH);
        right.setPreferredSize(new Dimension(230, 10));

        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setOpaque(false);
        panel.setBorder(Theme.padding(10, 0, 0, 0));
        panel.add(board, BorderLayout.CENTER);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    private JButton navButton(Icon icon, String tooltip, Runnable action) {
        JButton button = Ui.button("", action);
        button.setIcon(icon);
        button.setToolTipText(tooltip + " (también con las flechas del teclado)");
        return button;
    }

    private void bindArrowKeys() {
        bindKey("LEFT", () -> goTo(ply - 1));
        bindKey("RIGHT", () -> goTo(ply + 1));
        bindKey("HOME", () -> goTo(0));
        bindKey("END", () -> goTo(Integer.MAX_VALUE));
    }

    private void bindKey(String key, Runnable action) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), key);
        getActionMap().put(key, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isShowing()) {
                    action.run();
                }
            }
        });
    }

    private Optional<GameRecord> selectedGame() {
        int row = gamesTable.getSelectedRow();
        return row < 0 ? Optional.empty() : Optional.of(gamesModel.gameAt(gamesTable.convertRowIndexToModel(row)));
    }

    private void openReplay(GameRecord record) {
        replay = history.replay(record);
        movesModel.setMoves(replay.sanMoves());
        board.setBottomColor(record.humanColor() == null ? PieceColor.WHITE : record.humanColor());
        goTo(Integer.MAX_VALUE);
    }

    private void goTo(int targetPly) {
        if (replay == null) {
            return;
        }
        int maxPly = replay.moves().size();
        ply = Math.max(0, Math.min(targetPly, maxPly));
        Position position = replay.positions().get(ply);
        board.showPosition(position, ply == 0 ? null : replay.moves().get(ply - 1), false);
        plyLabel.setText(ply == 0 ? "Posición inicial" : "Jugada " + ((ply + 1) / 2) + " de " + ((maxPly + 1) / 2)
                + "  ·  " + replay.sanMoves().get(ply - 1));
        if (ply > 0) {
            movesTable.scrollRectToVisible(movesTable.getCellRect(MoveListModel.rowOfPly(ply - 1), 0, true));
        }
    }

    private void jumpToSelectedCell() {
        int row = movesTable.getSelectedRow();
        int column = movesTable.getSelectedColumn();
        if (replay == null || row < 0 || column < 1) {
            return;
        }
        int targetPly = row * 2 + column;
        if (targetPly <= replay.moves().size() && targetPly != ply) {
            goTo(targetPly);
        }
    }

    private void copyPgn() {
        selectedGame().ifPresent(game -> {
            Ui.copyToClipboard(game.pgn());
            plyLabel.setText("PGN copiado al portapapeles");
        });
    }

    private void exportPgn() {
        selectedGame().ifPresent(game -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Partidas PGN", "pgn"));
            chooser.setSelectedFile(new File("partida-" + game.id() + ".pgn"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    Files.writeString(chooser.getSelectedFile().toPath(), game.pgn(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    Ui.error(this, "No se pudo guardar el archivo: " + e.getMessage());
                }
            }
        });
    }

    private void deleteSelected() {
        selectedGame().ifPresent(game -> {
            int answer = JOptionPane.showConfirmDialog(this, "¿Eliminar la partida seleccionada?", "Eliminar",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (answer == JOptionPane.YES_OPTION) {
                history.delete(game.id());
                refresh();
            }
        });
    }

    /** Modelo de la tabla de partidas. */
    private static final class GamesTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {"Fecha", "Blancas", "Negras", "Resultado", "Motivo", "Jugadas"};
        private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

        private List<GameRecord> games = List.of();

        void setGames(List<GameRecord> newGames) {
            games = List.copyOf(newGames);
            fireTableDataChanged();
        }

        GameRecord gameAt(int row) {
            return games.get(row);
        }

        @Override
        public int getRowCount() {
            return games.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            GameRecord game = games.get(row);
            return switch (column) {
                case 0 -> game.playedAt().format(DATE);
                case 1 -> game.whiteName();
                case 2 -> game.blackName();
                case 3 -> game.result().pgn();
                case 4 -> game.termination().description();
                case 5 -> game.fullMoveCount();
                default -> "";
            };
        }
    }
}
