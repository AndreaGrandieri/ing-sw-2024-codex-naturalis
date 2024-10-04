package it.polimi.ingsw.network.messages.servertoclient;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.MessageType;

/**
 * Message sent to the client when the lobby has been started successfully.
 */
public class LobbyStartOKMessage extends Message {
    public LobbyStartOKMessage() {
        super(MessageType.LOBBY_START_OK);
    }
}
