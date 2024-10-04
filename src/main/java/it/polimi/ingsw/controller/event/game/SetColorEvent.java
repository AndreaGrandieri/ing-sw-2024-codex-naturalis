package it.polimi.ingsw.controller.event.game;

import it.polimi.ingsw.controller.event.Event;
import it.polimi.ingsw.model.player.PlayerColor;

import java.io.Serializable;

/**
 * Event sent when a player sets their color
 * @param username the player's username
 * @param color the player's color
 */
public record SetColorEvent(
        String username,
        PlayerColor color
) implements Event, Serializable {
    @Override
    public String toString() {
        return "SetColorEvent{" +
                "username='" + username + '\'' +
                ", color=" + color +
                '}';
    }
}
