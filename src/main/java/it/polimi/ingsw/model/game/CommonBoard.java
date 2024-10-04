package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.card.GoalCard;
import it.polimi.ingsw.model.card.GoldCard;
import it.polimi.ingsw.model.card.ResourceCard;
import it.polimi.ingsw.model.card.TypedCard;
import it.polimi.ingsw.model.card.deck.EmptyDeckException;
import it.polimi.ingsw.model.card.deck.PeekableCardDeck;
import it.polimi.ingsw.model.card.factory.GoldCardFactory;
import it.polimi.ingsw.model.card.factory.ResourceCardFactory;
import it.polimi.ingsw.model.card.properties.CardKingdom;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class represents that part of the gaming board which all players can access to. <br>
 * It includes:
 * <ul>
 *     <li>The two common {@link GoalCard}s</li>
 *     <li>The deck of {@link ResourceCard}s, along with its two {@code visibleCards}</li>
 *     <li>The deck of {@link GoldCard}s, along with its two {@code visibleCards}</li>
 * </ul>
 */

public class CommonBoard implements Serializable {
    private final List<GoalCard> commonGoals;

    private final Map<CardType, PeekableCardDeck<TypedCard>> coveredDecks;
    private final Map<CardType, List<TypedCard>> visibleCards;

    public CommonBoard(List<GoalCard> commonGoals) {
        // initialize gold deck and pick 2 cards from it to make them visible
        coveredDecks = new HashMap<>();
        coveredDecks.put(CardType.GOLD, new PeekableCardDeck<>(new GoldCardFactory()));
        visibleCards = new HashMap<>();
        visibleCards.put(CardType.GOLD, new ArrayList<>(coveredDecks.get(CardType.GOLD).drawNextNCards(2)));

        // initialize resource deck and pick 2 cards from it to make them visible
        coveredDecks.put(CardType.RESOURCE, new PeekableCardDeck<>(new ResourceCardFactory()));
        visibleCards.put(CardType.RESOURCE, new ArrayList<>(coveredDecks.get(CardType.RESOURCE).drawNextNCards(2)));

        // initialize commonGoals with value from GameController
        this.commonGoals = commonGoals;
    }

    public CommonBoard(CommonBoard other) {
        this.commonGoals = List.copyOf(other.commonGoals);

        coveredDecks = Stream.of(CardType.values()).collect(Collectors.toUnmodifiableMap(
                ct -> ct, ct -> new PeekableCardDeck<>(other.coveredDecks.get(ct))
        ));

        visibleCards = Stream.of(CardType.values()).collect(Collectors.toUnmodifiableMap(
                ct -> ct, ct -> new ArrayList<>(other.visibleCards.get(ct))
        ));

    }

    /**
     * Returns a new List containing all {@code commonGoals} of this board
     *
     * @return List containing all {@code commonGoals} of this board
     */
    public List<GoalCard> getCommonGoals() {
        return new ArrayList<>(commonGoals);
    }

    /**
     * Draws a card from the specified deck and returns it
     *
     * @param type type of deck to be drawn
     * @return the card drawn
     * @throws EmptyDeckException if the requested deck is empty
     */
    public TypedCard drawCovered(CardType type) throws EmptyDeckException {
        return coveredDecks.get(type).drawNextCard();
    }

    /**
     * Returns a new List containing all {@code visibleCards} of the specified type
     *
     * @param type type of {@code visibleCards} to get
     * @return List containing {@code visibleCards} of the specified {@code type}
     */
    public List<TypedCard> getVisibleCards(CardType type) {
        return new ArrayList<>(visibleCards.get(type));
    }

    /**
     * Given a {@link CardType}, peeks and returns the {@link CardKingdom} of that {@code type}'s deck
     *
     * @param type type of deck to peek
     * @return {@link CardKingdom} of requested deck's first card
     */
    public CardKingdom peekTopCard(CardType type) {
        try {
            return coveredDecks.get(type).peekKingdom();
        } catch (EmptyDeckException e) {
            return null;
        }
    }

    /**
     * Returns the {@code visible card} of the specified type, standing in the specified index;
     * replaces that card with another one from the relative covered deck if possible;
     * throws {@code RuntimeException} if index is not 0 or 1
     *
     * @param type  type of visible card to draw
     * @param index index of visible card to draw
     * @return the visible card drawn; {@code null} if there is no card in the specified spot
     */
    public TypedCard drawVisibleCard(CardType type, int index) {

        // only 2 visible cards are present
        if (index < 0 || index > 1)
            throw new RuntimeException("Invalid index");

        // save the chosen card and remove it from board
        TypedCard drawn = visibleCards.get(type).get(index);
        visibleCards.get(type).set(index, null);

        // try to replace drawn card
        try {
            TypedCard newVisible = coveredDecks.get(type).drawNextCard();
            visibleCards.get(type).set(index, newVisible);
        } catch (EmptyDeckException e) {
            // we anyway return what we drew:
            // visible card we drew before trying to replace it
            // or 'null' if there was no visible card in that spot
            return drawn;
        }

        // return drawn card if there was no problem
        return drawn;
    }

    /**
     * Utility method to see if there are still any cards in this board's {@code decks}
     *
     * @return {@code true} if all decks of this board contain no cards; {@code false} otherwise
     */
    public boolean allDecksEmpty() {
        // for every type of card, we check if there are cards left to draw
        for (CardType type : CardType.values()) {
            // if one of the covered decks has still cards in it, we return false
            if (!isDecksEmpty(type))
                return false;

            // if not all visible cards of the current type are null, we return false
            for (TypedCard card : visibleCards.get(type))
                if (card != null) return false;
        }

        // if none of our checks is verified, there are no cards left to draw
        return true;
    }

    public boolean isDecksEmpty(CardType type) {
       return coveredDecks.get(type).getCards().isEmpty();
    }

        public String coveredDecksToString() {
        return coveredDecks.get(CardType.RESOURCE).toString() +
                coveredDecks.get(CardType.GOLD).toString();
    }

    public String visibleCardsToString() {
        return "" + visibleCards.get(CardType.GOLD).get(0) +
                visibleCards.get(CardType.GOLD).get(1) +
                visibleCards.get(CardType.RESOURCE).get(0) +
                visibleCards.get(CardType.RESOURCE).get(1);
    }

}
