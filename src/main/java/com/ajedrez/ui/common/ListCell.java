package com.ajedrez.ui.common;

import com.ajedrez.ui.theme.Theme;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

/**
 * Celda de lista de una o dos líneas con ícono, pintada explícitamente (sin depender de cómo
 * cada Look&Feel resuelve los colores de selección).
 */
public final class ListCell extends JPanel {

    private final JLabel icon = new JLabel();
    private final JLabel title = new JLabel();
    private final JLabel subtitle = new JLabel();

    public ListCell() {
        super(new BorderLayout(10, 0));
        setBorder(Theme.padding(6, 12));
        title.setFont(Theme.font(Font.BOLD, 13));
        subtitle.setFont(Theme.font(Font.PLAIN, 12));
        JPanel texts = new JPanel();
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.setOpaque(false);
        texts.add(title);
        texts.add(subtitle);
        add(icon, BorderLayout.WEST);
        add(texts, BorderLayout.CENTER);
    }

    public ListCell render(Icon iconValue, String titleText, String subtitleText, boolean selected,
                           Color titleColor) {
        setOpaque(true);
        setBackground(selected ? Theme.ACCENT_DARK : Theme.SURFACE);
        icon.setIcon(iconValue);
        title.setText(titleText);
        title.setForeground(selected ? Color.WHITE : titleColor);
        subtitle.setText(subtitleText);
        subtitle.setVisible(subtitleText != null && !subtitleText.isEmpty());
        subtitle.setForeground(selected ? Color.WHITE : Theme.TEXT_MUTED);
        return this;
    }
}
