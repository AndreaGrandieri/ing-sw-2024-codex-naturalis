package it.polimi.ingsw.gui.components.panes.manuscript;

import javafx.scene.paint.Color;

import static java.lang.Math.max;
import static javafx.beans.binding.Bindings.createDoubleBinding;

public class ManBackgroundRect extends ManuscriptLayoutRectangle {
    public ManBackgroundRect() {
        setFill(Color.TRANSPARENT);
        setStroke(Color.web("#cdb95b"));

        arcHeightProperty().unbind();
        arcWidthProperty().unbind();
        setArcHeight(2);
        setArcWidth(2);
        strokeWidthProperty().bind(createDoubleBinding(() -> max(sizeProperty().get().getBorderWidth(), 2), sizeProperty()));
    }
}
