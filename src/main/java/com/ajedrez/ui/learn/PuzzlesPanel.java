package com.ajedrez.ui.learn;

import com.ajedrez.application.LearningService;
import com.ajedrez.application.PuzzleProgress;
import com.ajedrez.domain.learning.Puzzle;
import com.ajedrez.ui.Refreshable;
import com.ajedrez.ui.board.BoardPreferences;
import com.ajedrez.ui.common.Icons;
import com.ajedrez.ui.common.ListCell;
import com.ajedrez.ui.theme.Theme;

import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import java.util.Map;

/** Pantalla "Problemas": táctica ordenada por dificultad, con intentos y resueltos guardados. */
public final class PuzzlesPanel extends JPanel implements Refreshable {

    private final LearningService learning;
    private final ExercisePanel exercisePanel;
    private final JList<Puzzle> list;
    private Map<String, PuzzleProgress> progress = Map.of();

    public PuzzlesPanel(LearningService learning, BoardPreferences boardPreferences) {
        super(new BorderLayout(16, 0));
        this.learning = learning;
        this.exercisePanel = new ExercisePanel(learning, boardPreferences);
        this.list = new JList<>(learning.puzzles().toArray(Puzzle[]::new));
        setBorder(Theme.padding(16, 16));
        setBackground(Theme.BACKGROUND);

        progress = learning.puzzleProgress();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new PuzzleRenderer());
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && list.getSelectedValue() != null) {
                open(list.getSelectedValue());
            }
        });
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(270, 10));
        add(scroll, BorderLayout.WEST);
        add(exercisePanel, BorderLayout.CENTER);
        list.setSelectedIndex(firstUnsolvedIndex());
    }

    @Override
    public void refresh() {
        progress = learning.puzzleProgress();
        list.repaint();
    }

    private int firstUnsolvedIndex() {
        List<Puzzle> puzzles = learning.puzzles();
        for (int i = 0; i < puzzles.size(); i++) {
            if (!progressOf(puzzles.get(i)).solved()) {
                return i;
            }
        }
        return 0;
    }

    private PuzzleProgress progressOf(Puzzle puzzle) {
        return progress.getOrDefault(puzzle.id(), PuzzleProgress.none(puzzle.id()));
    }

    private void open(Puzzle puzzle) {
        String subtitle = puzzle.theme() + "  ·  Dificultad " + puzzle.rating();
        String theory = puzzle.goal()
                + "\n\nBuscá jugadas forzantes: jaques, capturas y amenazas."
                + "\nSi te trabás, el botón «Pista» primero te da una idea y después muestra la jugada.";
        exercisePanel.load(puzzle.title(), subtitle, theory, puzzle.goal(), puzzle.hint(), puzzle.exercise(),
                new ExercisePanel.Listener() {
                    private boolean failureRecorded;

                    @Override
                    public void onSolved(int mistakes) {
                        learning.recordPuzzleAttempt(puzzle, true);
                        refresh();
                    }

                    @Override
                    public void onWrongMove() {
                        if (!failureRecorded) {
                            failureRecorded = true;
                            learning.recordPuzzleAttempt(puzzle, false);
                            refresh();
                        }
                    }

                    @Override
                    public void onNext() {
                        int next = list.getSelectedIndex() + 1;
                        if (next < list.getModel().getSize()) {
                            list.setSelectedIndex(next);
                        }
                    }
                });
    }

    private final class PuzzleRenderer implements ListCellRenderer<Puzzle> {
        private final ListCell cell = new ListCell();

        @Override
        public Component getListCellRendererComponent(JList<? extends Puzzle> jList, Puzzle puzzle, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            PuzzleProgress p = progressOf(puzzle);
            Icon status = p.solved() ? Icons.done() : (p.attempts() > 0 ? Icons.failed() : Icons.pending());
            String details = puzzle.theme() + " · " + puzzle.rating()
                    + (p.attempts() > 0 ? " · intentos: " + p.attempts() : "");
            return cell.render(status, puzzle.title(), details, isSelected, p.solved() ? Theme.SUCCESS : Theme.TEXT);
        }
    }
}
