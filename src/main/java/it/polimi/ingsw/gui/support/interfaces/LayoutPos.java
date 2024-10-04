package it.polimi.ingsw.gui.support.interfaces;

import javafx.geometry.Point2D;

public interface LayoutPos {

    double getLayoutX();

    double getLayoutY();

    void setLayoutX(double x);

    void setLayoutY(double y);

    default Point2D getLayoutPos() {
        return new Point2D(getLayoutX(), getLayoutY());
    }

    default void setLayoutPos(Point2D pos) {
        setLayoutX(pos.getX());
        setLayoutY(pos.getY());
    }

}
