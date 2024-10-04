package it.polimi.ingsw.controller.event.game;

import it.polimi.ingsw.controller.event.Event;

import java.io.Serializable;

/**
 * Event sent when the match composition (of players) changes. Also used for lobbies that are not ongoing matches.
 */
public record MatchCompositionChangeEvent() implements Event, Serializable {
}
