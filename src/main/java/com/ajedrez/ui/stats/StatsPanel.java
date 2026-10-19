package com.ajedrez.ui.stats;

import com.ajedrez.application.StatisticsService;
import com.ajedrez.application.StatisticsService.DifficultyStats;
import com.ajedrez.application.StatisticsService.Summary;
import com.ajedrez.ui.Refreshable;
import com.ajedrez.ui.common.Ui;
import com.ajedrez.ui.theme.Theme;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.Locale;

/** Pantalla "Estadísticas": rendimiento por nivel contra la IA y progreso de aprendizaje. */
public final class StatsPanel extends JPanel implements Refreshable {

    private final StatisticsService statistics;
    private final StatsTableModel tableModel = new StatsTableModel();
    private final JLabel totalGames = bigNumber();
    private final JLabel overallScore = bigNumber();
    private final JLabel twoPlayerGames = bigNumber();
    private final JProgressBar lessonsBar = new JProgressBar();
    private final JProgressBar puzzlesBar = new JProgressBar();

    public StatsPanel(StatisticsService statistics) {
        super(new BorderLayout(0, 16));
        this.statistics = statistics;
        setBorder(Theme.padding(20, 24));
        setBackground(Theme.BACKGROUND);

        JPanel cards = new JPanel(new GridLayout(1, 3, 12, 0));
        cards.setOpaque(false);
        cards.add(statCard("Partidas contra la IA", totalGames));
        cards.add(statCard("Puntuación global", overallScore));
        cards.add(statCard("Partidas de 2 jugadores", twoPlayerGames));

        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);
        top.add(Ui.title("Estadísticas"), BorderLayout.NORTH);
        top.add(cards, BorderLayout.CENTER);

        JTable table = new JTable(tableModel);
        Ui.styleTable(table);
        for (int i = 1; i < tableModel.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(Ui.centered());
        }

        JPanel learning = new JPanel(new GridLayout(2, 2, 12, 10));
        learning.setOpaque(false);
        learning.add(new JLabel("Lecciones completadas"));
        learning.add(lessonsBar);
        learning.add(new JLabel("Problemas resueltos"));
        learning.add(puzzlesBar);
        lessonsBar.setStringPainted(true);
        puzzlesBar.setStringPainted(true);

        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setOpaque(false);
        bottom.add(Ui.subtitle("Aprendizaje"), BorderLayout.NORTH);
        bottom.add(Ui.card(learning), BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(Ui.subtitle("Resultados por nivel de dificultad"), BorderLayout.NORTH);
        center.add(Ui.scroll(table), BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    @Override
    public void refresh() {
        Summary summary = statistics.summary();
        tableModel.setRows(summary.perDifficulty());
        int games = summary.gamesVsComputer();
        totalGames.setText(String.valueOf(games));
        double points = summary.perDifficulty().stream().mapToDouble(s -> s.wins() + s.draws() * 0.5).sum();
        overallScore.setText(games == 0 ? "—" : String.format(Locale.ROOT, "%.0f%%", 100 * points / games));
        twoPlayerGames.setText(String.valueOf(summary.twoPlayerGames()));
        setProgress(lessonsBar, summary.lessonsCompleted(), summary.lessonsTotal());
        setProgress(puzzlesBar, summary.puzzlesSolved(), summary.puzzlesTotal());
    }

    private static void setProgress(JProgressBar bar, int done, int total) {
        bar.setMaximum(Math.max(1, total));
        bar.setValue(done);
        bar.setString(done + " de " + total);
    }

    private static JLabel bigNumber() {
        JLabel label = new JLabel("0");
        label.setFont(Theme.font(Font.BOLD, 30));
        label.setForeground(Theme.ACCENT);
        return label;
    }

    private static JComponent statCard(String caption, JLabel value) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        panel.add(Ui.muted(caption), BorderLayout.NORTH);
        panel.add(value, BorderLayout.CENTER);
        return Ui.card(panel);
    }

    private static final class StatsTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {"Nivel", "Jugadas", "Ganadas", "Tablas", "Perdidas", "Puntuación"};
        private List<DifficultyStats> rows = List.of();

        void setRows(List<DifficultyStats> newRows) {
            rows = List.copyOf(newRows);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return rows.size();
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
            DifficultyStats stats = rows.get(row);
            return switch (column) {
                case 0 -> stats.difficulty().displayName();
                case 1 -> stats.played();
                case 2 -> stats.wins();
                case 3 -> stats.draws();
                case 4 -> stats.losses();
                case 5 -> stats.played() == 0 ? "—" : String.format(Locale.ROOT, "%.0f%%", stats.scorePercent());
                default -> "";
            };
        }
    }
}
