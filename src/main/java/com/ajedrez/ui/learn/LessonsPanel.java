package com.ajedrez.ui.learn;

import com.ajedrez.application.LearningService;
import com.ajedrez.domain.learning.Lesson;
import com.ajedrez.domain.learning.LessonCategory;
import com.ajedrez.ui.Refreshable;
import com.ajedrez.ui.board.BoardPreferences;
import com.ajedrez.ui.common.Icons;
import com.ajedrez.ui.common.ListCell;
import com.ajedrez.ui.theme.Theme;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import java.util.Set;

/** Pantalla "Aprender": lista de lecciones agrupadas por categoría + ejercicio interactivo. */
public final class LessonsPanel extends JPanel implements Refreshable {

    private final LearningService learning;
    private final ExercisePanel exercisePanel;
    private final DefaultListModel<Object> listModel = new DefaultListModel<>();
    private final JList<Object> list = new JList<>(listModel);
    private Set<String> completed = Set.of();

    public LessonsPanel(LearningService learning, BoardPreferences boardPreferences) {
        super(new BorderLayout(16, 0));
        this.learning = learning;
        this.exercisePanel = new ExercisePanel(learning, boardPreferences);
        setBorder(Theme.padding(16, 16));
        setBackground(Theme.BACKGROUND);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new LessonRenderer());
        list.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            if (list.getSelectedValue() instanceof Lesson lesson) {
                open(lesson);
            } else if (list.getSelectedValue() instanceof LessonCategory && list.getSelectedIndex() + 1 < listModel.size()) {
                list.setSelectedIndex(list.getSelectedIndex() + 1);
            }
        });
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(250, 10));
        add(scroll, BorderLayout.WEST);
        add(exercisePanel, BorderLayout.CENTER);

        rebuildList();
        selectFirstPending();
    }

    @Override
    public void refresh() {
        Object selected = list.getSelectedValue();
        rebuildList();
        if (selected != null) {
            list.setSelectedValue(selected, true);
            keepFirstHeaderVisible();
        }
    }

    /** Si se selecciona la primera lección, mostrar también el encabezado de su categoría. */
    private void keepFirstHeaderVisible() {
        if (list.getSelectedIndex() <= 1) {
            SwingUtilities.invokeLater(() -> list.ensureIndexIsVisible(0));
        }
    }

    private void rebuildList() {
        completed = learning.completedLessons();
        listModel.clear();
        LessonCategory current = null;
        for (Lesson lesson : learning.lessons()) {
            if (lesson.category() != current) {
                current = lesson.category();
                listModel.addElement(current);
            }
            listModel.addElement(lesson);
        }
    }

    private void selectFirstPending() {
        List<Lesson> lessons = learning.lessons();
        Lesson target = lessons.stream().filter(l -> !completed.contains(l.id())).findFirst()
                .orElse(lessons.get(0));
        list.setSelectedValue(target, true);
        keepFirstHeaderVisible();
    }

    private void open(Lesson lesson) {
        exercisePanel.load(lesson.title(), lesson.category().displayName(), lesson.theory(), lesson.goal(),
                null, lesson.exercise(), new ExercisePanel.Listener() {
                    @Override
                    public void onSolved(int mistakes) {
                        learning.completeLesson(lesson);
                        completed = learning.completedLessons();
                        list.repaint();
                    }

                    @Override
                    public void onNext() {
                        selectNext(lesson);
                    }
                });
    }

    private void selectNext(Lesson lesson) {
        List<Lesson> lessons = learning.lessons();
        int index = lessons.indexOf(lesson);
        if (index >= 0 && index + 1 < lessons.size()) {
            list.setSelectedValue(lessons.get(index + 1), true);
        }
    }

    /** Dibuja categorías como encabezados y lecciones con su estado (completada o pendiente). */
    private final class LessonRenderer implements ListCellRenderer<Object> {
        private final ListCell cell = new ListCell();
        private final JLabel header = new JLabel();

        public LessonRenderer() {
            header.setFont(Theme.font(Font.BOLD, 11));
            header.setForeground(Theme.TEXT_MUTED);
            header.setBorder(Theme.padding(12, 10));
        }

        @Override
        public Component getListCellRendererComponent(JList<?> jList, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            if (value instanceof LessonCategory category) {
                header.setText(category.displayName().toUpperCase());
                return header;
            }
            Lesson lesson = (Lesson) value;
            boolean done = completed.contains(lesson.id());
            return cell.render(done ? Icons.done() : Icons.pending(), lesson.title(), null, isSelected,
                    done ? Theme.SUCCESS : Theme.TEXT);
        }
    }
}
