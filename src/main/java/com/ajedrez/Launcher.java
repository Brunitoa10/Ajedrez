package com.ajedrez;

import com.ajedrez.bootstrap.AppContext;
import com.ajedrez.infrastructure.config.AppPaths;
import com.ajedrez.ui.MainFrame;
import com.ajedrez.ui.common.Ui;
import com.ajedrez.ui.theme.Theme;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Punto de entrada de la aplicación. */
public final class Launcher {

    private static final Logger LOG = Logger.getLogger(Launcher.class.getName());
    /** Diálogos estándar de Swing (Sí/No, selector de archivos) en castellano. */
    private static final Locale SPANISH = Locale.forLanguageTag("es-AR");

    private Launcher() {
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        Locale.setDefault(SPANISH);
        JComponent.setDefaultLocale(SPANISH);
        SwingUtilities.invokeLater(Launcher::start);
    }

    private static void start() {
        Theme.install();
        try {
            AppPaths paths = AppPaths.detect();
            AppContext context = AppContext.start(paths);
            MainFrame frame = new MainFrame(context);
            frame.setVisible(true);
            frame.onStartup();
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "No se pudo iniciar la aplicación", e);
            Ui.error(null, "No se pudo iniciar la aplicación:\n" + e.getMessage());
            System.exit(1);
        }
    }
}
