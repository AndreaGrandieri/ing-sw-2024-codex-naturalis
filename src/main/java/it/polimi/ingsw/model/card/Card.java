package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.card.properties.CardFace;

import java.io.Serializable;

/**
 * This class represents the most abstract concept of card in this game. <br>
 * It is the direct father of the following classes:
 * <ul>
 *     <li>{@link GoalCard}: cards containing additional goals of the game</li>
 *     <li>{@link PlayableCard}: cards that can be placed on the gaming board</li>
 * </ul>
 */
public abstract class Card implements Serializable {

    private final int id;
    private CardFace face;

    public Card(int id) {
        this.id = id;
        face = CardFace.FRONT;
    }

    public Card(Card other) {
        this.id = other.id;
        this.face = other.face;
    }

    public abstract Card cloneCard();

    public int getId() {
        return id;
    }

    public void flip() {
        if(face == CardFace.FRONT)
            face = CardFace.BACK;
        else if(face == CardFace.BACK)
            face = CardFace.FRONT;
    }

    /**
     * Unlike flip(), this method sets the face of this card to the given one
     *
     * @param face {@link CardFace} to set on this card
     */
    public void setFace(CardFace face) {
        this.face = face;
    }

    public CardFace getFace() {
        return face;
    }

    @Override
    public String toString() {
        return face + ", ";
    }

    /**
     * Override of the default equals() method, that compares the cards by id
     *
     * @param o other card to compare to {@code this}
     * @return {@code true} if {@code this} and {@code other} have the same {@code id}; {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Card other = (Card) o;
        return this.id == other.id;
    }

}
