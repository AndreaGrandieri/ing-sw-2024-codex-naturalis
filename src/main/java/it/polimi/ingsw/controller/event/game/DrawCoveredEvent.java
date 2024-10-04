package it.polimi.ingsw.controller.event.game;

import it.polimi.ingsw.controller.event.Event;
import it.polimi.ingsw.model.card.TypedCard;
import it.polimi.ingsw.model.game.CardType;

import java.io.Serializable;

/**
 * Event sent when a player draws a covered card from the deck
 * @param username the player that drew the card
 * @param drawnDeckCard the card drawn from the deck
 * @param drawnCardType the type of the card drawn
 */
public record DrawCoveredEvent(
        String username,
        TypedCard drawnDeckCard,
        CardType drawnCardType
) implements Event, Serializable {

    @Override
    public String toString() {
        return "DrawCoveredEvent{" +
                "username='" + username + '\'' +
                ", drawnDeckCard=" + drawnDeckCard +
                ", visibleCardType=" + drawnCardType +
                '}';
    }
}
