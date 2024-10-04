package it.polimi.ingsw.controller.event.lobby;

import it.polimi.ingsw.controller.event.EventType;

/**
 * Class that contains all the events related to the lobby
 */
public class LobbyEvents {
    public static EventType<LobbyStartEvent> LOBBY_START = new EventType<>();
}
