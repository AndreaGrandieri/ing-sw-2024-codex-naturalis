package it.polimi.ingsw.gui.support.helper;

import javafx.geometry.Dimension2D;

import static java.lang.Math.max;

public class TableSummary {
    private final int numRows;
    private final int numCols;

    public TableSummary(int numRows, int numCols) {
        this.numRows = numRows;
        this.numCols = numCols;
    }

    public TableSummary(Dimension2D panelSize, Card2D cardSize) {
        double sizeHeight = panelSize.getHeight();
        double sizeWidth = panelSize.getWidth();

        double cardWidth = cardSize.getWidth();
        double cardHeight = cardSize.getHeight();

        double overlapFreeW = cardSize.getFreeWidthFromOverlap();
        double overlapFreeH = cardSize.getFreeHeightFromOverlap();

        //Calculate number of rows and cols
        int numRows = (int) (Math.floor((sizeHeight - cardHeight) / overlapFreeH)) + 1;

        // Add margin rows
        //        numRows = numRows + 1;


        int numCols = (int) (Math.floor((sizeWidth - cardWidth) / overlapFreeW)) + 1;

        // Add margin cols
        //        numCols = numCols + 1;
        this.numCols = max(0, numCols);
        this.numRows = max(0, numRows);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableSummary tableSummary = (TableSummary) o;
        return numRows == tableSummary.numRows && numCols == tableSummary.numCols;
    }

    public boolean contains(TableSummary ts) {
        return this.numCols > ts.numCols && this.numRows > ts.numRows;
    }

    public int getNumRows() {
        return numRows;
    }

    @Override
    public String toString() {
        return "TableSummary{" +
                "numRows=" + numRows +
                ", numCols=" + numCols +
                '}';
    }

    public int getNumCols() {
        return numCols;
    }
}
