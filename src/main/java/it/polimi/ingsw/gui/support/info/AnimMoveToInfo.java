package it.polimi.ingsw.gui.support.info;

import it.polimi.ingsw.gui.components.panes.card.CardRect;
import it.polimi.ingsw.gui.support.helper.Card2D;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;

public record AnimMoveToInfo(
        Point2D startPos,
        Point2D endPos,
        Card2D endSize,
        CardRect animatedPane,
        ObservableValue<Point2D> endPosProp
) {
    public AnimMoveToInfo(Point2D startPos, Point2D endPos, Card2D endSize, CardRect animatedPane) {
        this(startPos, endPos, endSize, animatedPane, null);
    }

}
