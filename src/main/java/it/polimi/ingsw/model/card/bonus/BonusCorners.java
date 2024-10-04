package it.polimi.ingsw.model.card.bonus;

import it.polimi.ingsw.model.player.ManuscriptPosition;
import it.polimi.ingsw.model.player.PlayerManuscript;

import java.io.Serializable;

public class BonusCorners extends CardBonus implements Serializable {

    public BonusCorners(int points) {
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
        int total = 0;
        int x = position.x();
        int y = position.y();
        ManuscriptPosition[] neighbors = new ManuscriptPosition[]{
                new ManuscriptPosition(x - 1, y),
                new ManuscriptPosition(x + 1, y),
                new ManuscriptPosition(x, y - 1),
                new ManuscriptPosition(x, y + 1)
        };

        for (ManuscriptPosition pos : neighbors)
            if (manuscript.getCardAt(pos) != null)
                total += points;

        return total;
    }

    @Override
    public String toString() {
        return "Points: " + points + " for each Corner, ";
    }
}
