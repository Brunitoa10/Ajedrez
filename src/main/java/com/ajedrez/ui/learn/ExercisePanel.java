package com.ajedrez.ui.learn;

import com.ajedrez.application.LearningService;
import com.ajedrez.domain.learning.Exercise;
import com.ajedrez.domain.learning.ExerciseFeedback;
import com.ajedrez.domain.learning.ExerciseSession;
import com.ajedrez.domain.model.Move;
import com.ajedrez.domain.model.Position;
import com.ajedrez.ui.board.BoardPanel;
import com.ajedrez.ui.board.BoardPreferences;
import com.ajedrez.ui.common.Ui;
import com.ajedrez.ui.theme.Theme;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

/**
 * Componente reutilizable para cualquier ejercicio interactivo (lecciones y problemas):
 * tablero + teoría + objetivo + feedback + botones Reiniciar / Pista / Siguiente.
 * Composición sobre herencia: lecciones y problemas usan este panel, no lo extienden.
 */
public final class ExercisePanel extends JPanel {

    private static final int OPPONENT_DELAY_MS = 550;
    private static final int SIDEBAR_WIDTH = 360;
    /** El motor HTML de Swing escala los "px" de CSS (~x1,33), por eso es menor que la barra lateral. */
    private static final int HTML_WRAP_WIDTH = 250;

    /** Eventos del ejercicio para que la pantalla contenedora registre el progreso. */
    public interface Listener {
        public default void onSolved(int mistakes) {
        }

        public default void onWrongMove() {
        }

        public default void onNext() {
        }
    }

    private final LearningService learning;
    private final BoardPanel board;
    private final JLabel title = Ui.title(" ");
    private final JLabel subtitle = Ui.muted(" ");
    private final JTextArea theory = Ui.paragraph("");
    private final JLabel goal = new JLabel(" ");
    private final JLabel feedback = new JLabel(" ");
    private final JButton nextButton;

    private ExerciseSession session;
    private Listener listener = new Listener() { };
    private Timer pendingReply;
    private String textHint;
    private boolean textHintShown;

    public ExercisePanel(LearningService learning, BoardPreferences boardPreferences) {
        super(new BorderLayout(16, 0));
        this.learning = learning;
        this.board = boardPreferences.newBoard();
        board.setLegalMovesProvider(sq -> session == null ? List.<Move>of() : session.legalMovesFrom(sq));
        board.setMoveListener(this::onStudentMove);
        nextButton = Ui.primaryButton("Siguiente", () -> listener.onNext());

        setOpaque(false);
        add(board, BorderLayout.CENTER);
        add(buildSidebar(), BorderLayout.EAST);
    }

    /**
     * @param textHint pista escrita que se muestra al primer clic en "Pista" (puede ser nula);
     *                 el siguiente clic dibuja la flecha con la jugada.
     */
    public void load(String titleText, String subtitleText, String theoryText, String goalText,
                     String textHint, Exercise exercise, Listener exerciseListener) {
        stopPendingReply();
        this.textHint = textHint;
        this.textHintShown = false;
        title.setText(titleText);
        subtitle.setText(subtitleText);
        theory.setText(theoryText);
        goal.setText(wrapped("<b>Objetivo:</b> " + escape(goalText)));
        listener = exerciseListener;
        session = learning.startExercise(exercise);
        board.setBottomColor(session.studentColor());
        showCurrent(null, "Tu turno: juegan las " + session.studentColor().displayName() + ".", true);
    }

    private JComponent buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 12));
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 10));

        goal.setFont(Theme.font(Font.PLAIN, 14));
        goal.setForeground(Theme.ACCENT);
        feedback.setFont(Theme.font(Font.BOLD, 15));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(title);
        header.add(Box.createVerticalStrut(2));
        header.add(subtitle);
        for (Component c : header.getComponents()) {
            ((JComponent) c).setAlignmentX(LEFT_ALIGNMENT);
        }

        JPanel goalBox = new JPanel();
        goalBox.setLayout(new BoxLayout(goalBox, BoxLayout.Y_AXIS));
        goalBox.setOpaque(false);
        goalBox.add(goal);
        goalBox.add(Box.createVerticalStrut(8));
        goalBox.add(feedback);

        JPanel lessonText = new JPanel(new BorderLayout(0, 14));
        lessonText.setOpaque(false);
        lessonText.add(Ui.card(theory), BorderLayout.CENTER);
        lessonText.add(goalBox, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(lessonText, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(1, 3, 8, 0));
        buttons.setOpaque(false);
        buttons.add(Ui.button("Reiniciar", this::restart));
        buttons.add(Ui.button("Pista", this::hint));
        buttons.add(nextButton);

        sidebar.add(header, BorderLayout.NORTH);
        sidebar.add(center, BorderLayout.CENTER);
        sidebar.add(buttons, BorderLayout.SOUTH);
        return sidebar;
    }

    private void onStudentMove(Move move) {
        Position before = session.position();
        ExerciseFeedback result = session.attempt(move);
        switch (result.kind()) {
            case WRONG, ILLEGAL -> {
                showCurrent(null, result.message(), false);
                listener.onWrongMove();
            }
            case SOLVED -> {
                showCurrent(move, result.message(), true);
                board.setInteractive(false);
                listener.onSolved(session.mistakes());
            }
            case CORRECT -> playOpponentReply(before.apply(move), move, result);
        }
    }

    /** Muestra la jugada del alumno y, tras una pausa, la respuesta del rival. */
    private void playOpponentReply(Position afterStudent, Move studentMove, ExerciseFeedback result) {
        board.showPosition(afterStudent, studentMove, learning.isInCheck(afterStudent));
        board.setInteractive(false);
        setFeedback(result.message(), true);
        pendingReply = new Timer(OPPONENT_DELAY_MS, e -> {
            pendingReply = null;
            board.showPosition(session.position(), result.reply().orElse(null), session.isInCheck());
            board.setInteractive(true);
        });
        pendingReply.setRepeats(false);
        pendingReply.start();
    }

    private void showCurrent(Move lastMove, String message, boolean positive) {
        board.showPosition(session.position(), lastMove, session.isInCheck());
        board.setInteractive(!session.isSolved());
        setFeedback(message, positive);
    }

    private void setFeedback(String message, boolean positive) {
        feedback.setText(wrapped(escape(message)));
        feedback.setForeground(Ui.feedbackColor(positive));
    }

    /** HTML con ancho fijo para que los textos largos hagan salto de línea en la barra lateral. */
    private static String wrapped(String html) {
        return "<html><body style='width:" + HTML_WRAP_WIDTH + "px'>" + html + "</body></html>";
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void restart() {
        if (session == null) {
            return;
        }
        stopPendingReply();
        session.reset();
        showCurrent(null, "Ejercicio reiniciado.", true);
    }

    private void hint() {
        if (session == null || pendingReply != null) {
            return;
        }
        if (textHint != null && !textHintShown) {
            textHintShown = true;
            setFeedback("Pista: " + textHint, true);
            return;
        }
        session.hint().ifPresent(board::showHint);
    }

    private void stopPendingReply() {
        if (pendingReply != null) {
            pendingReply.stop();
            pendingReply = null;
        }
    }
}
