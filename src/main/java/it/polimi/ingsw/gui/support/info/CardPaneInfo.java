package it.polimi.ingsw.gui.support.info;

import it.polimi.ingsw.gui.support.helper.Card2D;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;

public record CardPaneInfo(
        Point2D scenePosition,
        Card2D size,
        ObservableValue<Point2D> posProp
) {
    public CardPaneInfo(Point2D scenePosition, Card2D size) {
        this(scenePosition, size, null);
    }

}
