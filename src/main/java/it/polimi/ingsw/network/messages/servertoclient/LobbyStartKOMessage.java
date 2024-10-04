package it.polimi.ingsw.network.messages.servertoclient;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.MessageType;

/**
 * Message sent to the client when the client tries to start a lobby but the operation fails.
 */
public class LobbyStartKOMessage extends Message {
    public LobbyStartKOMessage() {
        super(MessageType.LOBBY_START_KO);
    }
}
