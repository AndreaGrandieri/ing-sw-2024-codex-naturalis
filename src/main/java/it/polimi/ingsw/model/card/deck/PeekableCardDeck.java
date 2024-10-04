package it.polimi.ingsw.model.card.deck;

import it.polimi.ingsw.model.card.TypedCard;
import it.polimi.ingsw.model.card.factory.CardFactory;
import it.polimi.ingsw.model.card.properties.CardKingdom;

import java.io.Serializable;

/**
 * Evolution of the basic {@link CardDeck}. This class offers a deck that can be peeked;
 * peeking is knowing the first card's {@link CardKingdom}
 * @param <T> Type of cards to put in the deck
 */

public class PeekableCardDeck<T extends TypedCard> extends CardDeck<T> implements Serializable {

    public PeekableCardDeck(CardFactory actualFactory) {
        super(actualFactory);
    }

    public PeekableCardDeck(PeekableCardDeck<T> other) {
        super(other);
    }

    /**
     * Returns the first card's {@link CardKingdom}
     * @return The first card's {@link CardKingdom}
     * @throws EmptyDeckException if there are no cards in this deck
     */
    public CardKingdom peekKingdom() {
        if(getCards().isEmpty())
            throw new EmptyDeckException();

        return getCards().get(0).getKingdom();
    }
}
