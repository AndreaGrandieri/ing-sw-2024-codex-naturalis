package it.polimi.ingsw.controller.event.game;

import it.polimi.ingsw.controller.event.Event;

import java.io.Serializable;

/**
 * Event sent when the turn changes
 * @param currentUsername is the username of the player that has the turn
 */
public record TurnChangeEvent(
        String currentUsername
) implements Event, Serializable {
    @Override
    public String toString() {
        return "TurnChangeEvent{" +
                "currentUsername='" + currentUsername + '\'' +
                '}';
    }
}
