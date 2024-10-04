package it.polimi.ingsw.gui.support.helper;

import it.polimi.ingsw.model.player.ManuscriptPosition;
import it.polimi.ingsw.model.player.PlayerManuscript;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.*;

public class TableBounds {
    private int minCol;
    private int maxCol;
    private int minRow;
    private int maxRow;

    public TableBounds() {
        this.minCol = 0;
        this.maxCol = 0;
        this.minRow = 0;
        this.maxRow = 0;

    }

    public TableBounds(int minCol, int maxCol, int minRow, int maxRow) {
        this.minCol = minCol;
        this.maxCol = maxCol;
        this.minRow = minRow;
        this.maxRow = maxRow;
    }

    public static TableBounds mergeBounds(TableBounds tb1, TableBounds tb2) {
        return new TableBounds(
                min(tb1.minCol, tb2.minCol),
                max(tb1.maxCol, tb2.maxCol),
                min(tb1.minRow, tb2.minRow),
                max(tb1.maxRow, tb2.maxRow)
        );
    }

    TableSummary getTableSummary() {
        return new TableSummary(
                getNumRows(),
                getNumCols()
        );
    }


    public TableBounds(PlayerManuscript manuscript) {

        //calculate rows and cols in cartesian system

        //Manuscript is always centered in position 0,0; minimum cols and rows are always 0 at the beginning
        this.minCol = 0;
        this.maxCol = 0;
        this.minRow = 0;
        this.maxRow = 0;

        //First calculate manuscript cols and rows, after calculate max and min x,y
        for (ManuscriptPosition mp : manuscript.getAllOccupiedPositions()) {
            int x = mp.x();
            int y = mp.y();
            int col = x + y;
            int row = x - y;

            this.minCol = min(this.minCol, col);
            this.maxCol = max(this.maxCol, col);
            this.minRow = min(this.minRow, row);
            this.maxRow = max(this.maxRow, row);
        }

        // Add margin to minimum background
        int margin = 1;
        this.minCol = this.minCol - margin;
        this.maxCol = this.maxCol + margin;
        this.minRow = this.minRow - margin;
        this.maxRow = this.maxRow + margin;

    }

    public static void main(String[] args) {
        TableBounds tb1 = new TableBounds(-5, 3, -3, 2);
        TableBounds tb2 = new TableBounds(-1, 6, -2, 4);
        tb1.newColsTo(tb2);
        System.out.println(tb1.newRowsTo(tb2));
        System.out.println(tb1.removedRowsTo(tb2));

    }

    public List<Integer> newColsTo(TableBounds other) {
        return Stream.concat(
                IntStream.range(other.minCol, minCol).boxed(),
                IntStream.range(maxCol + 1, other.maxCol + 1).boxed()
        ).toList();
    }

    public List<Integer> removedColsTo(TableBounds other) {
        return Stream.concat(
                IntStream.range(minCol, other.minCol).boxed(),
                IntStream.range(other.maxCol + 1, maxCol + 1).boxed()
        ).toList();
    }

    public List<Integer> newRowsTo(TableBounds other) {
        return Stream.concat(
                IntStream.range(other.minRow, minRow).boxed(),
                IntStream.range(maxRow + 1, other.maxRow + 1).boxed()
        ).toList();
    }

    public List<Integer> removedRowsTo(TableBounds other) {
        return Stream.concat(
                IntStream.range(minRow, other.minRow).boxed(),
                IntStream.range(other.maxRow + 1, maxRow + 1).boxed()
        ).toList();
    }

    public int getMinCol() {
        return minCol;
    }

    public void setMinCol(int minCol) {
        this.minCol = minCol;
    }

    public int getMaxCol() {
        return maxCol;
    }

    public void setMaxCol(int maxCol) {
        this.maxCol = maxCol;
    }

    public int getMinRow() {
        return minRow;
    }

    public void setMinRow(int minRow) {
        this.minRow = minRow;
    }

    public int getMaxRow() {
        return maxRow;
    }

    public void setMaxRow(int maxRow) {
        this.maxRow = maxRow;
    }

    public int getDeltaCols() {
        return maxCol + minCol;
    }

    public int getDeltaRows() {
        return maxRow + minRow;
    }

    public int getNumCols() {
        return abs(maxCol - minCol) + 1;
    }

    public int getNumRows() {
        return abs(maxRow - minRow) + 1;
    }

    @Override
    public String toString() {
        return "TableBounds{" +
                "minCol=" + minCol +
                ", maxCol=" + maxCol +
                ", numCols=" + getNumCols() +
                ", deltaCols=" + getDeltaCols() +
                ", minRow=" + minRow +
                ", maxRow=" + maxRow +
                ", numRows=" + getNumRows() +
                ", deltaRows=" + getDeltaRows() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableBounds that = (TableBounds) o;
        return minCol == that.minCol && maxCol == that.maxCol && minRow == that.minRow && maxRow == that.maxRow;
    }

}

