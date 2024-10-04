package it.polimi.ingsw.gui.components.panes.board;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class PionCircle extends Region {
    private final Region r = new Region();
    private final Circle inner;
    private final Circle outer;

    private final ObjectProperty<PionPos> pos;

    private static final double PREF_RADIUS = 20;
    private static final double PREF_DELTA_X = 4;
    private static final double PREF_DELTA_Y = -4;

    public DoubleProperty radiusProperty() {
        return inner.radiusProperty();
    }

    public PionCircle(double rad, Color color) {
        pos = new SimpleObjectProperty<>(new PionPos(0, 0));

        inner = new Circle(rad, Color.BLACK);

        outer = new Circle(rad, color);

        outer.radiusProperty().bind(inner.radiusProperty());

        outer.centerXProperty().bind(Bindings.createDoubleBinding(() -> {
            double radius = inner.radiusProperty().get();
            double ratio = radius / PREF_RADIUS;
            return inner.centerXProperty().get() + PREF_DELTA_X * ratio;
        }, inner.centerXProperty(), inner.radiusProperty()));

        outer.centerYProperty().bind(outer.centerXProperty().multiply(-1));

        getChildren().addAll(inner, outer);
    }

    public PionPos getPos() {
        return pos.get();
    }

    public void setPos(PionPos pos) {
        this.pos.set(pos);
    }

    public ObjectProperty<PionPos> posProperty() {
        return pos;
    }
}
