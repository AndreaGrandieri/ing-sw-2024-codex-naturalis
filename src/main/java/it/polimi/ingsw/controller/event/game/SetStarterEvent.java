package it.polimi.ingsw.controller.event.game;

import it.polimi.ingsw.controller.event.Event;
import it.polimi.ingsw.model.card.StarterCard;

import java.io.Serializable;

/**
 * Event sent when a player sets his starter card
 * @param username the player who set the card
 * @param card the card set
 */
public record SetStarterEvent(
        String username,
        StarterCard card
) implements Event, Serializable {
    @Override
    public String toString() {
        return "SetStarterEvent{" +
                "username='" + username + '\'' +
                ", drawnDeckCard=" + card +
                '}';
    }
}
