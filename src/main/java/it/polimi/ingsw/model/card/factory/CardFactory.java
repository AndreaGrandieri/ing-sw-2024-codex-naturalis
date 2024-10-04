package it.polimi.ingsw.model.card.factory;

import it.polimi.ingsw.model.card.Card;

public interface CardFactory {
    /**
     * Returns the id of the first card that this {@code CardFactory} generates
     *
     * @return The id of the first card that this {@code CardFactory} generates
     */
    int getFirstId();

    /**
     * Returns the total number of cards that this {@code CardFactory} generates
     *
     * @return The total number of cards that this {@code CardFactory} generates
     */
    int getSize();

    /**
     * Generates and returns the card with the given id
     * @param id {@code id} of card to generate
     * @throws WrongIdException if the given id does not match a card that this factory can generate
     * @return {@code Card} generated
     */
    Card generateCard(int id);
}
