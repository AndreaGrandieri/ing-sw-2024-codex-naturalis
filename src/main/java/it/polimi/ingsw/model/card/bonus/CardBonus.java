package it.polimi.ingsw.model.card.bonus;

import it.polimi.ingsw.model.player.ManuscriptPosition;
import it.polimi.ingsw.model.player.PlayerManuscript;

import java.io.Serializable;


public abstract class CardBonus implements Serializable {
    protected int points;

    public CardBonus(int points) {
        this.points = points;
    }

    /**
     * This method calculates the amount of points scored when placing a card with such a bonus. <br>
     * Every subclass of {@link CardBonus} overrides this method, calculating the right amount of points based on the rulebook:
     * <ul>
     *     <li>{@link BonusCorners}: 2 points for each corner covered by the card; </li>
     *     <li>{@link BonusObjects}: 1 point for each of the specified object on the board;  </li>
     *     <li>{@link BonusFreePoints}: No condition needed, placing this card grants the specified amount of points </li>
     * </ul>
     *
     * @param manuscript {@link PlayerManuscript} on which the card has been placed
     * @param position   {@link ManuscriptPosition} where to place the card in the given manuscript
     * @return The amount of points scored by placing the card
     */
    public abstract int calculatePoints(PlayerManuscript manuscript, ManuscriptPosition position);

    /**
     * Returns the minimum amount of points granted by placing a card with this bonus
     *
     * @return The minimum amount of points granted by placing a card with this bonus
     */
    public int getPoints() {
        return points;
    }
}
