package it.polimi.ingsw.controller.event.game;

import it.polimi.ingsw.controller.GameState;
import it.polimi.ingsw.controller.event.Event;

import java.io.Serializable;

/**
 * Event sent when the state of the game changes
 * @param newState the new state of the game
 */
public record StateChangeEvent(
        GameState newState
) implements Event, Serializable {
    @Override
    public String toString() {
        return "StateChangeEvent{" +
                "newState=" + newState +
                '}';
    }
}
