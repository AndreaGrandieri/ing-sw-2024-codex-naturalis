package it.polimi.ingsw.controller.event.game;

import it.polimi.ingsw.controller.event.Event;

import java.io.Serializable;

/**
 * Event sent when a player chooses a goal
 * @param username the player who chose the goal
 */
public record ChooseGoalEvent(
        String username
) implements Event, Serializable {
    @Override
    public String toString() {
        return "ChooseGoalEvent{" +
                "username='" + username + '\'' +
                '}';
    }
}
