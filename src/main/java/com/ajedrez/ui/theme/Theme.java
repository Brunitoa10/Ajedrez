package com.ajedrez.ui.theme;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tema oscuro de la aplicación sobre el Look&Feel Nimbus (incluido en Java, sin dependencias).
 * Centraliza colores y tipografías para no repetir valores mágicos en las vistas.
 */
public final class Theme {

    public static final Color BACKGROUND = new Color(0x1E, 0x1F, 0x24);
    public static final Color SURFACE = new Color(0x27, 0x29, 0x30);
    public static final Color SURFACE_ALT = new Color(0x31, 0x34, 0x3C);
    public static final Color BORDER = new Color(0x3C, 0x40, 0x4A);
    public static final Color TEXT = new Color(0xE8, 0xE9, 0xED);
    public static final Color TEXT_MUTED = new Color(0x9A, 0xA0, 0xAC);
    public static final Color ACCENT = new Color(0x81, 0xB6, 0x4C);
    public static final Color ACCENT_DARK = new Color(0x5D, 0x8A, 0x33);
    public static final Color SUCCESS = new Color(0x7F, 0xC9, 0x6B);
    public static final Color ERROR = new Color(0xE5, 0x6B, 0x6B);
    public static final Color WARNING = new Color(0xE8, 0xB9, 0x4F);

    private static final List<String> UI_FONT_CANDIDATES = List.of("Segoe UI", "Inter", "Noto Sans", "DejaVu Sans");
    private static String uiFontFamily = Font.SANS_SERIF;

    private Theme() {
    }

    public static void install() {
        uiFontFamily = firstAvailable(UI_FONT_CANDIDATES);
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    applyNimbusPalette();
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException e) {
            // Si Nimbus no está disponible se usa el Look&Feel por defecto.
        }
        Font base = font(Font.PLAIN, 13);
        for (Object key : UIManager.getLookAndFeelDefaults().keySet()) {
            if (key.toString().endsWith(".font")) {
                UIManager.put(key, base);
            }
        }
    }

    private static void applyNimbusPalette() {
        UIManager.put("control", SURFACE);
        UIManager.put("info", SURFACE_ALT);
        UIManager.put("nimbusBase", new Color(0x2B, 0x2E, 0x36));
        UIManager.put("nimbusAlertYellow", WARNING);
        UIManager.put("nimbusDisabledText", TEXT_MUTED);
        UIManager.put("nimbusFocus", ACCENT_DARK);
        UIManager.put("nimbusGreen", ACCENT);
        UIManager.put("nimbusInfoBlue", new Color(0x4A, 0x7E, 0xC0));
        UIManager.put("nimbusLightBackground", SURFACE);
        UIManager.put("nimbusOrange", ACCENT);
        UIManager.put("nimbusRed", ERROR);
        UIManager.put("nimbusSelectedText", Color.WHITE);
        UIManager.put("nimbusSelectionBackground", ACCENT_DARK);
        UIManager.put("nimbusBlueGrey", new Color(0x4A, 0x4F, 0x5A));
        UIManager.put("text", TEXT);
        UIManager.put("Table.alternateRowColor", SURFACE_ALT);
        UIManager.put("Table.gridColor", BORDER);
    }

    public static Font font(int style, float size) {
        return new Font(uiFontFamily, style, Math.round(size));
    }

    public static Border padding(int vertical, int horizontal) {
        return BorderFactory.createEmptyBorder(vertical, horizontal, vertical, horizontal);
    }

    public static Border padding(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }

    public static Border card() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                padding(12, 14));
    }

    public static String firstAvailable(List<String> candidates) {
        Set<String> installed = Arrays.stream(GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames()).collect(Collectors.toSet());
        return candidates.stream().filter(installed::contains).findFirst().orElse(Font.SANS_SERIF);
    }
}
