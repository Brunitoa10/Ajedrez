package com.ajedrez.ui;

import com.ajedrez.bootstrap.AppContext;
import com.ajedrez.domain.model.Piece;
import com.ajedrez.ui.board.BoardPreferences;
import com.ajedrez.ui.board.PieceIcon;
import com.ajedrez.ui.common.NavButton;
import com.ajedrez.ui.common.Ui;
import com.ajedrez.ui.history.HistoryPanel;
import com.ajedrez.ui.learn.LessonsPanel;
import com.ajedrez.ui.learn.PuzzlesPanel;
import com.ajedrez.ui.play.PlayPanel;
import com.ajedrez.ui.stats.StatsPanel;
import com.ajedrez.ui.theme.BoardTheme;
import com.ajedrez.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ventana principal: barra lateral de navegación + pantallas intercambiables (CardLayout).
 */
public final class MainFrame extends JFrame {

    private static final String PLAY = "Jugar";
    private static final String LESSONS = "Aprender";
    private static final String PUZZLES = "Problemas";
    private static final String HISTORY = "Historial";
    private static final String STATS = "Estadísticas";

    private static final String WELCOME = "portada";
    private static final String WORKSPACE = "aplicacion";

    private final CardLayout rootCards = new CardLayout();
    private final JPanel root = new JPanel(rootCards);
    private final JMenuBar menuBar;
    private final FadeOverlay fadeOverlay;
    private boolean inWorkspace;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, Component> screens = new LinkedHashMap<>();
    private final Map<String, JToggleButton> navButtons = new LinkedHashMap<>();
    private final BoardPreferences boardPreferences = new BoardPreferences();
    private final PlayPanel playPanel;
    private final AppContext context;

    public MainFrame(AppContext context) {
        super("Ajedrez");
        this.context = context;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 680));
        setPreferredSize(new Dimension(1280, 800));
        setIconImages(List.of(appIcon(32), appIcon(64)));

        playPanel = new PlayPanel(context.gameService(), boardPreferences);
        screens.put(PLAY, playPanel);
        screens.put(LESSONS, new LessonsPanel(context.learningService(), boardPreferences));
        screens.put(PUZZLES, new PuzzlesPanel(context.learningService(), boardPreferences));
        screens.put(HISTORY, new HistoryPanel(context.historyService(), boardPreferences));
        screens.put(STATS, new StatsPanel(context.statisticsService()));
        screens.forEach((name, screen) -> content.add(screen, name));
        content.setBackground(Theme.BACKGROUND);

        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setBackground(Theme.BACKGROUND);
        workspace.add(buildNavigation(), BorderLayout.WEST);
        workspace.add(content, BorderLayout.CENTER);
        workspace.add(buildStatusBar(), BorderLayout.SOUTH);

        root.add(new WelcomePanel(this::enterApplication), WELCOME);
        root.add(workspace, WORKSPACE);
        setContentPane(root);
        menuBar = buildMenu();
        fadeOverlay = new FadeOverlay();
        setGlassPane(fadeOverlay);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                playPanel.dispose();
                context.close();
            }
        });
        pack();
        setLocationRelativeTo(null);
        rootCards.show(root, WELCOME);
    }

    /** Se llama cuando la ventana ya está visible. */
    public void onStartup() {
        context.startupWarning().ifPresent(warning -> Ui.info(this, "Base de datos", warning));
    }

    /** Botón "Jugar" de la portada: despliega la aplicación con un fundido y propone la primera partida. */
    private void enterApplication() {
        if (inWorkspace) {
            return;
        }
        inWorkspace = true;
        setJMenuBar(menuBar);
        show(PLAY);
        rootCards.show(root, WORKSPACE);
        revalidate();
        fadeOverlay.fadeIn(() -> SwingUtilities.invokeLater(playPanel::newGame));
    }

    private void show(String name) {
        Component screen = screens.get(name);
        if (screen instanceof Refreshable refreshable) {
            refreshable.refresh();
        }
        cards.show(content, name);
        JToggleButton button = navButtons.get(name);
        if (button != null) {
            button.setSelected(true);
        }
    }

    private JComponent buildNavigation() {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(Theme.SURFACE);
        nav.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER), Theme.padding(18, 14)));
        nav.setPreferredSize(new Dimension(190, 10));

        JLabel brand = new JLabel("Ajedrez", new PieceIcon(Piece.WHITE_KNIGHT, 34), JLabel.LEFT);
        brand.setFont(Theme.font(Font.BOLD, 20));
        brand.setForeground(Theme.TEXT);
        brand.setAlignmentX(LEFT_ALIGNMENT);
        nav.add(brand);
        nav.add(Box.createVerticalStrut(24));

        ButtonGroup group = new ButtonGroup();
        for (String name : screens.keySet()) {
            JToggleButton button = new NavButton(name);
            button.setAlignmentX(LEFT_ALIGNMENT);
            button.addActionListener(e -> show(name));
            group.add(button);
            navButtons.put(name, button);
            nav.add(button);
            nav.add(Box.createVerticalStrut(6));
        }
        nav.add(Box.createVerticalGlue());
        JLabel version = Ui.muted("v1.0 · portable");
        version.setAlignmentX(LEFT_ALIGNMENT);
        nav.add(version);
        return nav;
    }

    private JComponent buildStatusBar() {
        JLabel label = Ui.muted("Base de datos: " + context.databaseConfig().describe());
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.SURFACE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER), Theme.padding(4, 12)));
        bar.add(label, BorderLayout.WEST);
        return bar;
    }

    private JMenuBar buildMenu() {
        JMenu game = new JMenu("Partida");
        game.add(menuItem("Nueva partida...", () -> {
            show(PLAY);
            playPanel.newGame();
        }));
        game.addSeparator();
        game.add(menuItem("Salir", this::dispose));

        JMenu boardMenu = new JMenu("Tablero");
        ButtonGroup themes = new ButtonGroup();
        for (BoardTheme theme : BoardTheme.ALL) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(theme.name(), theme == boardPreferences.theme());
            item.addActionListener(e -> boardPreferences.setTheme(theme));
            themes.add(item);
            boardMenu.add(item);
        }
        boardMenu.addSeparator();
        JCheckBoxMenuItem coordinates = new JCheckBoxMenuItem("Mostrar coordenadas", boardPreferences.showCoordinates());
        coordinates.addActionListener(e -> boardPreferences.setShowCoordinates(coordinates.isSelected()));
        boardMenu.add(coordinates);

        JMenu help = new JMenu("Ayuda");
        help.add(menuItem("Acerca de...", this::showAbout));

        JMenuBar bar = new JMenuBar();
        bar.add(game);
        bar.add(boardMenu);
        bar.add(help);
        return bar;
    }

    private static JMenuItem menuItem(String text, Runnable action) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(e -> action.run());
        return item;
    }

    private void showAbout() {
        Ui.info(this, "Acerca de", """
                Ajedrez 1.0
                Motor propio alfa-beta con 5 niveles, lecciones y problemas.

                Datos: %s
                Carpeta de la aplicación: %s""".formatted(
                context.databaseConfig().describe(), context.paths().home()));
    }

    /**
     * Capa transparente sobre la ventana que se desvanece del color de la portada al contenido.
     */
    private static final class FadeOverlay extends JComponent {
        private static final int DURATION_MS = 380;
        private static final int FRAME_MS = 16;
        private static final Color COLOR = new Color(0x0B, 0x0C, 0x10);
        private float alpha;

        public void fadeIn(Runnable onFinished) {
            alpha = 1f;
            setVisible(true);
            long start = System.currentTimeMillis();
            Timer timer = new Timer(FRAME_MS, null);
            timer.addActionListener(e -> {
                float progress = Math.min(1f, (System.currentTimeMillis() - start) / (float) DURATION_MS);
                alpha = 1f - progress * progress * (3 - 2 * progress);
                repaint();
                if (progress >= 1f) {
                    timer.stop();
                    setVisible(false);
                    onFinished.run();
                }
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, alpha)));
            g2.setColor(COLOR);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    private static Image appIcon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Theme.ACCENT_DARK);
        g.fillRoundRect(0, 0, size, size, size / 4, size / 4);
        new PieceIcon(Piece.WHITE_KNIGHT, size).paintIcon(null, g, 0, 0);
        g.dispose();
        return image;
    }
}
