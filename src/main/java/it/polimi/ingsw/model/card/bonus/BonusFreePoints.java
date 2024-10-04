package it.polimi.ingsw.model.card.bonus;

import it.polimi.ingsw.model.player.ManuscriptPosition;
import it.polimi.ingsw.model.player.PlayerManuscript;

import java.io.Serializable;

public class BonusFreePoints extends CardBonus implements Serializable {

    public BonusFreePoints(int points) {
        super(points);
    }


    /**
     * {@inheritDoc}
     *
     * @param manuscript {@link PlayerManuscript} on which the card has been placed
     * @param position   {@link ManuscriptPosition} where to place the card in the given manuscript
     * @return The amount of points scored by placing the card
     */
    @Override
    public int calculatePoints(PlayerManuscript manuscript, ManuscriptPosition position) {
        return points;
    }

    @Override
    public String toString() {
        return "Points: " + points + " Free points, ";
    }
}
