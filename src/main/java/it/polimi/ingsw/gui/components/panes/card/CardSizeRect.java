package it.polimi.ingsw.gui.components.panes.card;

import it.polimi.ingsw.gui.support.helper.Card2D;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.shape.Rectangle;

import static javafx.beans.binding.Bindings.createDoubleBinding;

public class CardSizeRect extends Rectangle {
    private final ObjectProperty<Card2D> size;
    private final ObjectProperty<Point2D> layoutPos;
    private final DoubleProperty scaleWidth;
    private final DoubleProperty scaleHeight;

    public CardSizeRect() {
        scaleWidth = new SimpleDoubleProperty(1);
        scaleHeight = new SimpleDoubleProperty(1);

        size = new SimpleObjectProperty<>(new Card2D(40));

        widthProperty().bind(createDoubleBinding(
                () -> size.get().getWidth() * 0.92 * scaleWidth.get(),
                size, scaleWidth
        ));
        heightProperty().bind(createDoubleBinding(
                () -> size.get().getHeight() * 0.92 * scaleHeight.get(),
                size, scaleHeight
        ));


        arcHeightProperty().bind(createDoubleBinding(
                () -> size.get().getRoundLength(),
                sizeProperty()
        ));
        arcWidthProperty().bind(createDoubleBinding(
                () -> size.get().getRoundLength(),
                sizeProperty()
        ));

        layoutPos = new SimpleObjectProperty<>(new Point2D(0, 0));
    }


    public DoubleProperty scaleWidthProperty() {
        return scaleWidth;
    }

    public DoubleProperty scaleHeightProperty() {
        return scaleHeight;
    }

    public void setSize(Card2D size) {
        this.size.set(size);
    }

    public ObjectProperty<Card2D> sizeProperty() {
        return size;
    }
}
