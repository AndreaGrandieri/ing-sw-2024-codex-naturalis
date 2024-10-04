package it.polimi.ingsw.model.card.bonus;

import it.polimi.ingsw.model.card.properties.CardObject;
import it.polimi.ingsw.model.player.ManuscriptPosition;
import it.polimi.ingsw.model.player.PlayerManuscript;

import java.io.Serializable;

public class BonusObjects extends CardBonus implements Serializable {
    private final CardObject object;

    public BonusObjects(int points, CardObject object) {
        super(points);
        this.object = object;
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
        return (manuscript.getCardAt(position) == null) ?
                points * (1 + manuscript.getNumberOfItem(object)) :
                points * manuscript.getNumberOfItem(object);
    }

    public CardObject getObject() {
        return object;
    }

    @Override
    public String toString() {
        return "Points: " + points + " for each " + object + ", ";
    }
}
