package com.ajedrez.ui.common;

import com.ajedrez.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

/** Fábrica de componentes con el estilo de la aplicación (evita repetir configuración en cada vista). */
public final class Ui {

    private Ui() {
    }

    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.font(Font.BOLD, 22));
        label.setForeground(Theme.TEXT);
        return label;
    }

    public static JLabel subtitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.font(Font.BOLD, 15));
        label.setForeground(Theme.TEXT);
        return label;
    }

    public static JLabel muted(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.font(Font.PLAIN, 12));
        label.setForeground(Theme.TEXT_MUTED);
        return label;
    }

    public static JButton button(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setFocusable(false);
        button.addActionListener(e -> action.run());
        return button;
    }

    public static JButton primaryButton(String text, Runnable action) {
        JButton button = button(text, action);
        button.setFont(Theme.font(Font.BOLD, 13));
        button.setBackground(Theme.ACCENT_DARK);
        return button;
    }

    /** Área de texto de solo lectura con ajuste de línea, para teoría y descripciones. */
    public static JTextArea paragraph(String text) {
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFocusable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(false);
        area.setFont(Theme.font(Font.PLAIN, 14));
        area.setForeground(Theme.TEXT);
        area.setBorder(BorderFactory.createEmptyBorder());
        return area;
    }

    public static JPanel card(JComponent content) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.SURFACE);
        card.setBorder(Theme.card());
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    public static JScrollPane scroll(Component content) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        scroll.getViewport().setBackground(Theme.SURFACE);
        return scroll;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(26);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.setBackground(Theme.SURFACE);
        table.setForeground(Theme.TEXT);
    }

    public static DefaultTableCellRenderer centered() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        return renderer;
    }

    public static void copyToClipboard(String text) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static Color feedbackColor(boolean positive) {
        return positive ? Theme.SUCCESS : Theme.ERROR;
    }
}
