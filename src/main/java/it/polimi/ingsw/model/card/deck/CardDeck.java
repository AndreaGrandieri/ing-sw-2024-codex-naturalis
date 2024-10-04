package it.polimi.ingsw.model.card.deck;

import it.polimi.ingsw.model.card.Card;
import it.polimi.ingsw.model.card.factory.CardFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * This class offers the basic usage for a deck of a generic CardType.
 * Creating a new CardDeck shuffles it.
 *@param <T> Type of cards to put in the deck
 *
 */

public class CardDeck <T extends Card> implements Serializable {
    protected List<T> cards = new ArrayList<>();

    public CardDeck(CardFactory actualFactory) {
        for(int i = 0; i< actualFactory.getSize(); i++) {
            cards.add((T) actualFactory.generateCard(actualFactory.getFirstId()+i));
        }
        shuffle();
    }

    public CardDeck(CardDeck<T> other) {
        cards = List.copyOf(other.cards);
    }

    private void shuffle() {
        // https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle
        Random gen = new Random();
        int n = cards.size();
        for (int i = 0; i <  n - 1; i++) {
            int j = gen.nextInt(n - i);
            j += i;
            Collections.swap(cards, i, j);
        }
    }

    /**
     * Returns a List of all cards present in the deck
     * @return A List of all cards present in the deck
     */
    public List<T> getCards() {
        return new ArrayList<>(cards);
    }

    /**
     * Removes the first card of the deck and returns it
     * @throws EmptyDeckException If there are no cards in this deck
     * @return The first card of the deck
     */
    public T drawNextCard() {
        if(cards.isEmpty())
            throw new EmptyDeckException();
        T cardDrew = cards.get(0);

        cards.remove(0);
        return cardDrew;
    }

    /**
     * Removes the first n cards from the deck and returns them inside a List
     * @throws  NotEnoughCardsException if deck contains less than n cards
     * @param n number of cards to draw
     * @return List containing the first n cards from the deck
     */
    public List<T> drawNextNCards(int n) {
        if(cards.size() < n)
            throw new NotEnoughCardsException();

        List<T> cards = new ArrayList<>();

        for (int i = 0; i < n; i++)
            cards.add(drawNextCard());

        return cards;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(cards.size() + " cards in this deck:\n");

        for (int i = 0; i < cards.size(); i++) {
            s.append(i).append(": ").append(cards.get(i)).append("\n");
        }

        return s.toString();
    }
}
