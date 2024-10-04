package it.polimi.ingsw.gui.support.helper;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Dimension2D;

import static java.lang.Math.max;
import static javafx.beans.binding.Bindings.createObjectBinding;

public class BackgroundHelper {
    public static int PANEL_CHANGED = 0;
    public static int MANUSCRIPT_CHANGED = 1;
    private final ObjectProperty<Card2D> cardSize;
    private final ObjectProperty<Dimension2D> panelSize;
    private final ObjectProperty<TableSummary> panelSum;

    private final ObjectProperty<PC<Dimension2D>> contentSize;
    private final ObjectProperty<PC<TableBounds>> backgroundBounds;
    private final ObjectProperty<TableBounds> manuscriptBounds;
    private int id;
    private TableBoundsDelta backgroundDelta;

    public BackgroundHelper(int id, ObservableValue<Card2D> cardObv, ObservableValue<Dimension2D> panelObv) {
        this.id = id;

        cardSize = new SimpleObjectProperty<>(new Card2D(0));
        cardSize.bind(cardObv);

        panelSize = new SimpleObjectProperty<>(new Dimension2D(0, 0));
        panelSize.bind(panelObv);

        contentSize = new SimpleObjectProperty<>(new PC<>(new Dimension2D(0, 0), 0));

        backgroundBounds = new SimpleObjectProperty<>(new PC<>(new TableBounds(0, 0, 0, 0), 0));

        panelSum = new SimpleObjectProperty<>(new TableSummary(0, 0));

        manuscriptBounds = new SimpleObjectProperty<>(new TableBounds(0, 0, 0, 0));


        panelSum.bind(
                createObjectBinding(() -> {
                    TableSummary manuscriptSum = manuscriptBounds.get().getTableSummary();

                    contentSize.set(new PC<>(
                            mergeSizes(panelSize.get(), sizeFromTableSummary(manuscriptSum)),
                            PANEL_CHANGED)
                    );
                    return new TableSummary(panelSize.get(), cardSize.get());
                }, panelSize, cardSize)
        );

        panelSum.addListener((observableValue, oldTabSum, newTabSum) -> onPanelSumChange(newTabSum));

        manuscriptBounds.addListener((observableValue, tableBounds, newManBounds) -> onManuscriptBoundsChange(newManBounds));
    }

    private static TableBounds boundsFromBoundsAndSummary(TableBounds manBounds, TableSummary panelSum) {
        TableSummary manSum = manBounds.getTableSummary();

        // background bound as if manuscript is bigger than contentSize, with margin
        int margin = 1;
        TableBounds bgBounds = new TableBounds(
                manBounds.getMinCol() - margin,
                manBounds.getMaxCol() + margin,
                manBounds.getMinRow() - margin,
                manBounds.getMaxRow() + margin
        );

        // manuscript has at least a dimension smaller than panelSize
        if (!manSum.contains(panelSum)) {

            //Cols
            int manCols = manSum.getNumCols();
            int panelCols = panelSum.getNumCols();

            // padding
            if (manCols < panelCols) {

                int paddingCols = panelCols - manCols;
                if (paddingCols % 2 != 0) paddingCols++;

                bgBounds.setMinCol(
                        bgBounds.getMinCol() - paddingCols / 2
                );

                bgBounds.setMaxCol(
                        bgBounds.getMaxCol() + paddingCols / 2
                );

            }

            //Rows
            int manRows = manSum.getNumRows();
            int panelRows = panelSum.getNumRows();

            // padding
            if (manRows < panelRows) {
                int paddingRows = panelRows - manRows;
                if (paddingRows % 2 != 0) paddingRows++;

                bgBounds.setMinRow(
                        bgBounds.getMinRow() - paddingRows / 2);

                bgBounds.setMaxRow(
                        bgBounds.getMaxRow() + paddingRows / 2
                );

            }
        }
        return bgBounds;
    }

    // Dumb
    private static Dimension2D mergeSizes(Dimension2D d1, Dimension2D d2) {
        return new Dimension2D(
                max(d1.getWidth(), d2.getWidth()),
                max(d1.getHeight(), d2.getHeight())
        );
    }

    public void setManuscriptBounds(TableBounds manuscriptBounds) {
        this.manuscriptBounds.set(manuscriptBounds);
    }

    private void onPanelSumChange(TableSummary newTabSum) {
        TableSummary manuscriptSum = manuscriptBounds.get().getTableSummary();

        TableBounds bgBounds = boundsFromBoundsAndSummary(manuscriptBounds.get(), newTabSum);

        backgroundBounds.set(new PC<>(bgBounds, PANEL_CHANGED));
    }


    private void onManuscriptBoundsChange(TableBounds newManBounds) {
        TableSummary manuscriptSum = manuscriptBounds.get().getTableSummary();
        TableSummary newManuscriptSum = newManBounds.getTableSummary();

        // new manuscriptBounds has different rows or cols count from old manuscriptBounds
        // manuscript has a dimension bigger than panelSum
        // recalculate content size
        if (!panelSum.get().contains(manuscriptSum)) {

            contentSize.set(new PC<>(
                    mergeSizes(panelSize.get(), sizeFromTableSummary(newManuscriptSum)),
                    MANUSCRIPT_CHANGED)
            );

        }

        TableBounds bgBounds = boundsFromBoundsAndSummary(newManBounds, panelSum.get());

        backgroundBounds.set(new PC<>(bgBounds, MANUSCRIPT_CHANGED));
    }

    public ObservableValue<PC<Dimension2D>> contentSizeProperty() {
        return contentSize;
    }


    public ObjectProperty<PC<TableBounds>> backgroundBoundsProperty() {
        return backgroundBounds;
    }

    public void setId(int id) {
        this.id = id;
        //        if (id == 0) System.out.println("BH " + cardSize);

    }

    private Dimension2D sizeFromTableSummary(TableSummary ts) {
        double cardWidth = cardSize.get().getWidth();
        double cardHeight = cardSize.get().getHeight();

        double overlapFreeW = cardSize.get().getFreeWidthFromOverlap();
        double overlapFreeH = cardSize.get().getFreeHeightFromOverlap();

        double manuscriptCols = ts.getNumCols();
        double manuscriptRows = ts.getNumRows();

        // Manuscript minimum size
        double manuscriptWidth = (manuscriptCols > 0 ? cardWidth : 0) + overlapFreeW * max(0, manuscriptCols - 1);
        double manuscriptHeight = (manuscriptRows > 0 ? cardHeight : 0) + overlapFreeH * max(0, manuscriptRows - 1);

        // Add half card to the border of the manuscript
        double contentWidth = manuscriptWidth + overlapFreeW * 0.5;
        double contentHeight = manuscriptHeight + overlapFreeH * 0.5;

        return new Dimension2D(contentWidth, contentHeight);
    }

    private boolean isFirstSizeBigger(Dimension2D d1, Dimension2D d2) {
        return (d1.getHeight() > d2.getHeight()) || (d1.getWidth() > d2.getWidth());
    }


    // Getters


    public TableBoundsDelta getBackgroundDelta() {
        return backgroundDelta;
    }
    // Calculate background rows and cols by dividing contentSize by cardOverlapFree
}

