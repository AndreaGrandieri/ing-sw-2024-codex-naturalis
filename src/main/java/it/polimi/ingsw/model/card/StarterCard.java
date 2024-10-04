package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.card.properties.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents cards that players can use to start their game
 */
public class StarterCard extends PlayableCard implements Serializable {
    private final List<CardKingdom> centerResources;
    private final List<CardKingdom> backCorners;

    public StarterCard(
            int id,
            List<CornerItem> frontCorners,
            List<CardKingdom> centerResources,
            List<CardKingdom> backCorners
    ) {
        super(id, frontCorners);
        this.centerResources = centerResources;
        this.backCorners = backCorners;
    }

    public StarterCard(StarterCard other) {
        super(other);
        this.centerResources = new ArrayList<>(other.getCenterResources());
        this.backCorners = new ArrayList<>(other.getBackCorners());
    }

    public StarterCard cloneStarter() {
        return new StarterCard(this);
    }

    @Override
    public PlayableCard cloneCard() {
        return cloneStarter();
    }

    @Override
    public List<? extends CardItem> getVisibleItems() {
        return (getFace() == CardFace.FRONT) ? super.getVisibleItems() :
                backCorners;
    }

    @Override
    public CornerItem getCorner(CornerPosition position) {
        if (getFace() == CardFace.BACK)
            return new CornerItem(backCorners.get(position.getIndex()));
        return super.getCorner(position);
    }

    @Override
    public boolean matchKingdom(CardKingdom kingdom) {
        return false;
    }

    /**
     * Starter cards do not have any cost to be placed: this method returns an empty {@code Map}
     *
     * @return Empty {@code Map}
     */
    @Override
    public Map<CardKingdom, Integer> getCost() {
        // starter cards have no cost requirements to be placed
        return new HashMap<>();
    }

    public List<CardKingdom> getCenterResources() {
        return new ArrayList<>(centerResources);
    }

    public List<CardKingdom> getBackCorners() {
        return backCorners;
    }

    @Override
    public String toString() {
        String corners = "";

        for (int i = 0; i < backCorners.size(); i++) {
            if (backCorners.get(i) != null) {
                corners += CornerPosition.getInstance(i).toString() + ": " + backCorners.get(i).toString();
                corners += (i == backCorners.size() - 1) ? "" : ", ";
            }
        }
        return "{Starter, " + getFace() + ", Center Resources: " + centerResources + ", Back Corners: {" + corners + "}}";
    }
}
