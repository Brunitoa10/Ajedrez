package com.ajedrez.ui.play;

import com.ajedrez.ai.Difficulty;
import com.ajedrez.application.GameMode;
import com.ajedrez.application.MatchSettings;
import com.ajedrez.domain.model.PieceColor;
import com.ajedrez.ui.common.Ui;
import com.ajedrez.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/** Diálogo modal para configurar una partida nueva. */
public final class NewGameDialog extends JDialog {

    private static final String RANDOM_COLOR = "Al azar";

    private final JRadioButton vsComputer = new JRadioButton(GameMode.VS_COMPUTER.displayName(), true);
    private final JRadioButton twoPlayers = new JRadioButton(GameMode.LOCAL_TWO_PLAYERS.displayName());
    private final JComboBox<Object> colorBox = new JComboBox<>(new Object[]{
            PieceColor.WHITE.displayName(), PieceColor.BLACK.displayName(), RANDOM_COLOR});
    private final JComboBox<Difficulty> difficultyBox = new JComboBox<>(Difficulty.values());
    private final JLabel difficultyDescription = Ui.muted(" ");
    private final JTextField nameField = new JTextField("Jugador", 16);
    private MatchSettings result;

    private NewGameDialog(Window owner, MatchSettings previous) {
        super(owner, "Nueva partida", ModalityType.APPLICATION_MODAL);
        ButtonGroup group = new ButtonGroup();
        group.add(vsComputer);
        group.add(twoPlayers);
        if (previous != null) {
            twoPlayers.setSelected(previous.mode() == GameMode.LOCAL_TWO_PLAYERS);
            colorBox.setSelectedIndex(previous.humanColor() == PieceColor.WHITE ? 0 : 1);
            difficultyBox.setSelectedItem(previous.difficulty());
            nameField.setText(previous.playerName());
        } else {
            difficultyBox.setSelectedItem(Difficulty.EASY);
        }
        vsComputer.addActionListener(e -> updateEnabled());
        twoPlayers.addActionListener(e -> updateEnabled());
        difficultyBox.addActionListener(e -> updateDescription());

        setContentPane(buildContent());
        updateEnabled();
        updateDescription();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    /** Muestra el diálogo y devuelve la configuración elegida, o vacío si se canceló. */
    public static Optional<MatchSettings> show(Window owner, MatchSettings previous) {
        NewGameDialog dialog = new NewGameDialog(owner, previous);
        dialog.setVisible(true);
        return Optional.ofNullable(dialog.result);
    }

    private JComponent buildContent() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(Theme.padding(18, 22));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 4, 5, 4);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        form.add(Ui.subtitle("Modo de juego"), c);
        c.gridy++;
        form.add(vsComputer, c);
        c.gridy++;
        form.add(twoPlayers, c);

        c.gridwidth = 1;
        c.gridy++;
        form.add(new JLabel("Tu nombre"), c);
        c.gridx = 1;
        form.add(nameField, c);

        c.gridx = 0;
        c.gridy++;
        form.add(new JLabel("Jugás con"), c);
        c.gridx = 1;
        form.add(colorBox, c);

        c.gridx = 0;
        c.gridy++;
        form.add(new JLabel("Dificultad"), c);
        c.gridx = 1;
        form.add(difficultyBox, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        form.add(difficultyDescription, c);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 16, 12, 16));
        buttons.add(Ui.button("Cancelar", this::dispose));
        JButton play = Ui.primaryButton("Jugar", this::accept);
        buttons.add(play);
        getRootPane().setDefaultButton(play);

        JPanel root = new JPanel(new BorderLayout());
        root.add(form, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        return root;
    }

    private void updateEnabled() {
        boolean computer = vsComputer.isSelected();
        colorBox.setEnabled(computer);
        difficultyBox.setEnabled(computer);
        nameField.setEnabled(computer);
        difficultyDescription.setVisible(computer);
    }

    private void updateDescription() {
        Difficulty selected = (Difficulty) difficultyBox.getSelectedItem();
        difficultyDescription.setText(selected == null ? " " : selected.description());
    }

    private void accept() {
        if (twoPlayers.isSelected()) {
            result = MatchSettings.twoPlayers();
        } else {
            result = MatchSettings.vsComputer(selectedColor(), (Difficulty) difficultyBox.getSelectedItem(),
                    nameField.getText());
        }
        dispose();
    }

    private PieceColor selectedColor() {
        return switch (colorBox.getSelectedIndex()) {
            case 0 -> PieceColor.WHITE;
            case 1 -> PieceColor.BLACK;
            default -> ThreadLocalRandom.current().nextBoolean() ? PieceColor.WHITE : PieceColor.BLACK;
        };
    }
}
