package com.ajedrez.ui.board;

import com.ajedrez.domain.model.Piece;
import com.ajedrez.domain.model.PieceColor;
import com.ajedrez.domain.model.PieceType;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.util.Arrays;
import java.util.Optional;

/** Diálogo para elegir la pieza de coronación. */
final class PromotionChooser {

    private static final int ICON_SIZE = 56;

    private PromotionChooser() {
    }

    public static Optional<PieceType> choose(Component parent, PieceColor color) {
        PieceType[] choices = PieceType.PROMOTION_CHOICES;
        Icon[] icons = Arrays.stream(choices)
                .map(type -> new PieceIcon(Piece.of(color, type), ICON_SIZE))
                .toArray(Icon[]::new);
        int selected = JOptionPane.showOptionDialog(parent, "¿A qué pieza querés coronar?", "Coronación",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, icons, icons[0]);
        return selected < 0 ? Optional.empty() : Optional.of(choices[selected]);
    }
}
