package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.card.bonus.CardBonus;
import it.polimi.ingsw.model.card.factory.GoldCardFactory;
import it.polimi.ingsw.model.card.properties.CardKingdom;
import it.polimi.ingsw.model.card.properties.CornerItem;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents cards that always score points but can be placed only if the player has a certain amount of resources
 */
public class GoldCard extends TypedCard implements Serializable {
    private final Map<CardKingdom, Integer> cost;

    public GoldCard(int id, List<CornerItem> frontCorners, CardKingdom kingdom, CardBonus bonus, Map<CardKingdom, Integer> cost) {
        super(id, frontCorners, kingdom, bonus);
        this.cost = cost;
    }

    public GoldCard(GoldCard other) {
        super(other);
        this.cost = other.cost == null ? null : new HashMap<>(other.cost);
    }

    @Override
    public TypedCard cloneCard() {
        return new GoldCard(this);
    }


    /**
     * Returns the cost requirements that have to be satisfied to place this card on the board.
     *
     * @return {@code Map} containing, for each {@code CardItem}, the amount of them that have to be in the board
     * to be able to place this card
     */
    @Override
    public Map<CardKingdom, Integer> getCost() {
        // gold cards do have cost requirements to be placed
        return cost;
    }

    /**
     * {@inheritDoc}
     * @return Clean version of {@code this} card
     */
    @Override
    public TypedCard cleanCard() {
        return GoldCardFactory.generateCleanCard(this);
    }

    @Override
    public String toString() {

        StringBuilder costString = new StringBuilder();
        int i = 0;
        for (CardKingdom k : cost.keySet()) {
            costString.append(k).append(": ").append(cost.get(k).toString());
            costString.append((i == cost.keySet().size() - 1) ? "" : ", ");
            i++;
        }

        return "{Gold, " + super.toString() + ",  Cost: {" + costString + "}}";
    }
}
