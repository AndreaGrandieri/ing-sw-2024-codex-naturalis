package it.polimi.ingsw.network.messages.clienttoserver;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.MessageType;

/**
 * Message sent by the client to the server to ask which lobby to join.
 */
public class WhatLobbyToJoinMessage extends Message {
    private final String lobbyUUID;

    public WhatLobbyToJoinMessage(String lobbyUUID) {
        super(MessageType.WHAT_LOBBY_TO_JOIN);

        this.lobbyUUID = lobbyUUID;
    }

        public String getLobbyUUID() {
        return this.lobbyUUID;
    }
}
