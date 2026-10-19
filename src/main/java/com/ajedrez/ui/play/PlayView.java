package com.ajedrez.ui.play;

import com.ajedrez.domain.model.Move;
import com.ajedrez.domain.model.PieceColor;
import com.ajedrez.domain.model.Position;

import java.util.List;

/**
 * Vista pasiva de la partida (patrón Model-View-Presenter). El presentador decide QUÉ mostrar;
 * la vista solo sabe CÓMO. Gracias a esta interfaz el presentador se puede probar sin Swing.
 */
public interface PlayView {

    public void setOrientation(PieceColor bottomColor);

    public void showPlayers(String whiteName, String blackName);

    public void showPosition(Position position, Move lastMove, boolean inCheck);

    public void showMoves(List<String> sanMoves);

    public void showStatus(String status);

    public void setThinking(boolean thinking);

    public void setInteractive(boolean interactive);

    public void setControlsState(boolean canUndo, boolean canResign, boolean canOfferDraw);

    public void showHint(Move move);

    public void showGameOver(String message);

    public void showError(String message);
}
