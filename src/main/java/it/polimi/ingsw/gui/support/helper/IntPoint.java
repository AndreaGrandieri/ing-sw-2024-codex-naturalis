package it.polimi.ingsw.gui.support.helper;

import it.polimi.ingsw.model.player.ManuscriptPosition;
import javafx.geometry.Point2D;

// cartesian point (not in manuscript position system)
public record IntPoint(int x, int y) {
    public IntPoint(ManuscriptPosition pos) {
        this(
                pos.x() + pos.y(),
                pos.x() - pos.y()
        );
    }

    public IntPoint(Point2D pos) {
        this(
                (int) pos.getX(),
                (int) pos.getX()
        );
    }

    public double distance(IntPoint p2) {
        return toPoint2d().distance(p2.toPoint2d());
    }

    public Point2D toPoint2d() {
        return new Point2D(x, y);
    }

    public ManuscriptPosition toManuscriptPosition() {
        return new ManuscriptPosition((x + y) / 2, (x - y) / 2);
    }
}
