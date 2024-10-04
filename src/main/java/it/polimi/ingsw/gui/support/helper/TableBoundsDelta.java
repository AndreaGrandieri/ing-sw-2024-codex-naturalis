package it.polimi.ingsw.gui.support.helper;

import java.util.List;

public class TableBoundsDelta {
    private final List<Integer> addedCols;
    private final List<Integer> addedRows;
    private final List<Integer> removedCols;
    private final List<Integer> removedRows;

    @Override
    public String toString() {
        return "TableBoundsDelta{" +
                "addedCols=" + addedCols +
                ", addedRows=" + addedRows +
                ", removedCols=" + removedCols +
                ", removedRows=" + removedRows +
                ", firstBounds=" + firstBounds +
                ", secondBounds=" + secondBounds +
                '}';
    }

    private final TableBounds firstBounds;
    private final TableBounds secondBounds;

    public TableBoundsDelta(TableBounds tb1, TableBounds tb2) {
        addedCols = tb1.newColsTo(tb2);
        addedRows = tb1.newRowsTo(tb2);
        removedCols = tb1.removedColsTo(tb2);
        removedRows = tb1.removedRowsTo(tb2);
        firstBounds = tb1;
        secondBounds = tb2;
    }

    public TableBounds getFirstBounds() {
        return firstBounds;
    }

    public TableBounds getSecondBounds() {
        return secondBounds;
    }

    public List<Integer> addedCols() {
        return addedCols;
    }

    public List<Integer> addedRows() {
        return addedRows;
    }

    public List<Integer> removedCols() {
        return removedCols;
    }

    public List<Integer> removedRows() {
        return removedRows;
    }
}
