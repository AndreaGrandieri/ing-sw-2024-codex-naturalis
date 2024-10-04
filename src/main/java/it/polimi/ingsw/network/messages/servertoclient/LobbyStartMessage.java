package it.polimi.ingsw.network.messages.servertoclient;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.MessageType;

/**
 * Message sent to the client when the lobby is started.
 */
public class LobbyStartMessage extends Message {
    public LobbyStartMessage() {
        super(MessageType.LOBBY_START);
    }
}
