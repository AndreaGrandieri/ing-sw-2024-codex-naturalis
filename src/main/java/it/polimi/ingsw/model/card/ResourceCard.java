package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.card.bonus.CardBonus;
import it.polimi.ingsw.model.card.factory.ResourceCardFactory;
import it.polimi.ingsw.model.card.properties.CardKingdom;
import it.polimi.ingsw.model.card.properties.CornerItem;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents cards that can always be placed and may score points
 */
public class ResourceCard extends TypedCard implements Serializable {

    public ResourceCard(int id, List<CornerItem> frontCorners, CardKingdom kingdom, CardBonus bonus) {
        super(id, frontCorners, kingdom, bonus);
    }

    public ResourceCard(ResourceCard other) {
        super(other);
    }

    @Override
    public TypedCard cloneCard() {
        return new ResourceCard(this);
    }


    /**
     * Resource cards do not have any cost to be placed: this method returns an empty {@code Map}
     * @return Empty {@code Map}
     */
    @Override
    public Map<CardKingdom, Integer> getCost() {
        // resource cards have no cost requirements to be placed
        return new HashMap<>();
    }

    /**
     * {@inheritDoc}
     *
     * @return Clean version of {@code this} card
     */
    @Override
    public TypedCard cleanCard() {
        return ResourceCardFactory.generateCleanCard(this);
    }

    @Override
    public String toString() {
        return "{Resource, " + super.toString() + "}";
    }
}
