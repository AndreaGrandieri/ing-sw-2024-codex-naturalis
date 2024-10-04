package it.polimi.ingsw.controller.event.game;

import it.polimi.ingsw.controller.event.Event;
import it.polimi.ingsw.model.card.TypedCard;
import it.polimi.ingsw.model.player.ManuscriptPosition;

import java.io.Serializable;

/**
 * Event sent when a player places a card on the manuscript
 * @param username the player who placed the card
 * @param card the card placed
 * @param handIndex the index of the card in the player's hand
 * @param position the position where the card was placed
 */
public record PlaceCardEvent(
        String username,
        TypedCard card,
        int handIndex,
        ManuscriptPosition position
) implements Event, Serializable {

    @Override
    public String toString() {
        return "PlaceCardEvent{" +
                "username='" + username + '\'' +
                ", drawnDeckCard=" + card +
                ", position=" + position +
                '}';
    }
}
