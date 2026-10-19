package com.ajedrez.ui.common;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/** Modelo de tabla "N° | Blancas | Negras" a partir de una lista de jugadas en SAN. */
public final class MoveListModel extends AbstractTableModel {

    private static final String[] COLUMNS = {"#", "Blancas", "Negras"};

    private List<String> moves = new ArrayList<>();

    public void setMoves(List<String> sanMoves) {
        this.moves = List.copyOf(sanMoves);
        fireTableDataChanged();
    }

    /** Fila de la tabla que contiene la media jugada indicada (0 = primera jugada de blancas). */
    public static int rowOfPly(int ply) {
        return ply / 2;
    }

    @Override
    public int getRowCount() {
        return (moves.size() + 1) / 2;
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (column == 0) {
            return (row + 1) + ".";
        }
        int index = row * 2 + (column - 1);
        return index < moves.size() ? moves.get(index) : "";
    }
}
