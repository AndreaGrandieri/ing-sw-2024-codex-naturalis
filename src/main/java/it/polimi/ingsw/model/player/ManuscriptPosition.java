package it.polimi.ingsw.model.player;

import it.polimi.ingsw.model.card.properties.CornerPosition;

import java.io.Serializable;

/**
 * This record represents a position into the {@link PlayerManuscript}, and provides methods to work with it. <br>
 * By convention, the (0,0) position is where the {@code StarterCard} is placed,
 * the x coordinate is positive towards the upper right direction of the board,
 * the y coordinate is positive towards the lower right direction of the board.
 *
 * @param x first coordinate
 * @param y second coordinate
 */
public record ManuscriptPosition(int x, int y) implements Serializable {

    @Override
    public String toString() {
        return "x: " + x() + ", y: " + y();
    }

    /**
     * Returns a new {@code ManuscriptPosition} that represents the position obtained by starting from {@code this} position
     * and going towards the given {@link CornerPosition}
     * @param position {@link CornerPosition} to go to
     * @return new instance of {@code ManuscriptPosition} that represents the position obtained by starting from {@code this} position
     *      * and going towards the given {@link CornerPosition}
     */
    public ManuscriptPosition toPosition(CornerPosition position) {
        return switch (position) {
            case TOP_RIGHT -> topRight();
            case TOP_LEFT -> topLeft();
            case BOTTOM_RIGHT -> bottomRight();
            case BOTTOM_LEFT -> bottomLeft();
        };
    }

    public ManuscriptPosition topRight() {
        return new ManuscriptPosition(x + 1, y);
    }

    public ManuscriptPosition topLeft() {
        return new ManuscriptPosition(x, y - 1);
    }

    public ManuscriptPosition bottomRight() {
        return new ManuscriptPosition(x, y + 1);
    }

    public ManuscriptPosition bottomLeft() {
        return new ManuscriptPosition(x - 1, y);
    }

    /**
     * Sums {@code this} position's x coordinate to the given position x coordinate; <br>
     * does the same with the y coordinate; <br>
     * Returns a new {@code ManuscriptPosition} that has the calculated (x, y) as its own coordinates
     * @param position {@code ManuscriptPosition} to use for adding
     * @return new {@code ManuscriptPosition} that has the calculated (x, y) as its own coordinates
     */
    public ManuscriptPosition addPosition(ManuscriptPosition position) {
        return new ManuscriptPosition(x + position.x, y + position.y);
    }
}