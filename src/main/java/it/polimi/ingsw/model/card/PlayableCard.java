package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.card.properties.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This class represents all cards that can be placed on the gaming board. <br>
 * It is the direct father of the following classes:
 * <ul>
 *     <li>{@link StarterCard}: cards that players can use to start their game</li>
 *     <li>{@link TypedCard}: cards that can be placed into the board during the game</li>
 * </ul>
 */
public abstract class PlayableCard extends Card implements Serializable {

    private final List<CornerItem> frontCorners;

    public PlayableCard(int id, List<CornerItem> frontCorners) {
        super(id);
        this.frontCorners = frontCorners;
    }

    public PlayableCard(PlayableCard other) {
        super(other);
        frontCorners = other.frontCorners == null ? null : new ArrayList<>(Arrays.asList(other.getCorner(CornerPosition.TOP_LEFT),
                other.getCorner(CornerPosition.TOP_RIGHT),
                other.getCorner(CornerPosition.BOTTOM_LEFT),
                other.getCorner(CornerPosition.BOTTOM_RIGHT)));
    }

    @Override
    public abstract PlayableCard cloneCard();

    /**
     * Returns the item this card contains at the given {@code CornerPosition}; <br>
     * returns {@code null} if this card has no corner in that position
     *
     * @param position {@link CornerPosition} to get the item from
     * @return {@link CornerItem} containing a {@code not null} {@code CardItem} if there is one;
     * {@link CornerItem} containing a {@code null} {@code CardItem} if that corner is empty;
     * {@code null} if that corner is not available for placing another card
     */
    public CornerItem getCorner(CornerPosition position) {
        if (getFace() == CardFace.BACK)
            return new CornerItem(null);
        return frontCorners.get(position.getIndex());
    }

    public List<? extends CardItem> getVisibleItems() {
        List<CardItem> items = new ArrayList<>();
        for (CornerItem corner : frontCorners) {
            if (corner != null && corner.item() != null)
                items.add(corner.item());
        }
        return items;
    }

    public boolean hasFreeCorner(CornerPosition corner) {
        return getCorner(corner) != null;
    }

    public abstract boolean matchKingdom(CardKingdom kingdom);

    /**
     * Returns the cost requirements that have to be satisfied to place this card on the board. <br>
     * Practically, this method returns an empty {@code Map} unless this card is a {@link GoldCard}
     *
     * @return {@code Map} containing, for each {@code CardItem}, the amount of them that have to be in the board
     * to be able to place this card
     */
    public abstract Map<CardKingdom, Integer> getCost();

    @Override
    public String toString() {
        StringBuilder corners = new StringBuilder();

        for (int i = 0; i < frontCorners.size(); i++) {
            if (frontCorners.get(i) != null) {
                corners.append(CornerPosition.getInstance(i).toString()).append(": ").append(frontCorners.get(i).toString());
                corners.append((i == frontCorners.size() - 1) ? "" : ", ");
            }
        }
        return super.toString() +
                "Items: {" +
                corners + "}";
    }
}
