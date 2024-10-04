package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.card.bonus.CardBonus;
import it.polimi.ingsw.model.card.properties.CardFace;
import it.polimi.ingsw.model.card.properties.CardItem;
import it.polimi.ingsw.model.card.properties.CardKingdom;
import it.polimi.ingsw.model.card.properties.CornerItem;

import java.io.Serializable;
import java.util.List;

/**
 * This class represents cards that can be placed on the board during the middle game. <br>
 * It is the direct father of the following classes:
 * <ul>
 *     <li>{@link ResourceCard}: cards that can always be placed and may score points</li>
 *     <li>{@link GoldCard}: cards that always score points but can be placed only if the player has a certain amount of resources</li>
 * </ul>
 */
public abstract class TypedCard extends PlayableCard implements Serializable {

    private final CardKingdom kingdom;
    private final CardBonus bonus;

    public TypedCard(int id, List<CornerItem> frontCorners, CardKingdom kingdom, CardBonus bonus) {
        super(id, frontCorners);
        this.kingdom = kingdom;
        this.bonus = bonus;
    }

    public TypedCard(TypedCard other) {
        super(other);
        this.kingdom = other.getKingdom();
        this.bonus = other.getBonus();
    }

    public abstract TypedCard cloneCard();
    

    @Override
    public List<? extends CardItem> getVisibleItems() {
        return (getFace() == CardFace.FRONT) ? super.getVisibleItems() :
                List.of(kingdom);
    }

    public CardKingdom getKingdom() {
        return kingdom;
    }

    public CardBonus getBonus() {
        return bonus;
    }

    @Override
    public boolean matchKingdom(CardKingdom kingdom) {
        return kingdom.equals(this.kingdom);
    }

    /**
     * Returns a clean instance of {@code this} card. <br>
     * A clean card is a card that has:
     * <ul>
     *     <li>Different id, which is the same for cards that have same type (Gold/Resource) and kingdom</li>
     *     <li>All blank front corners (available for placing but with no items in them)</li>
     *     <li>Same kingdom</li>
     *     <li>{@code null} bonus</li>
     *     <li>Empty cost Map</li>
     * </ul>
     *
     * @return Clean version of {@code this} card
     */
    public abstract TypedCard cleanCard();

    public boolean isClean() {
        return getId() > 102;
    }

    @Override
    public String toString() {
        return kingdom + ", " + bonus + super.toString();
    }
}
