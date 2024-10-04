package it.polimi.ingsw.controller.event.game;

import it.polimi.ingsw.controller.event.Event;
import it.polimi.ingsw.model.card.TypedCard;
import it.polimi.ingsw.model.game.CardType;

import java.io.Serializable;

/**
 * Event sent when a player draws a visible card from the table
 */
public record DrawVisibleEvent(
        String username,
        int visibleCardIndex,
        CardType visibleCardType,
        TypedCard visibleCard,
        TypedCard newDeckCard
) implements Event, Serializable {

    @Override
    public String toString() {
        return "DrawVisibileEvent{" +
                "username='" + username + '\'' +
                ", visibleCardIndex=" + visibleCardIndex +
                ", visibleCardType=" + visibleCardType +
                ", newDeckCard=" + newDeckCard +
                '}';
    }
}
