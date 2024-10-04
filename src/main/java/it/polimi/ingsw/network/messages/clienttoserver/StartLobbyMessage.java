package it.polimi.ingsw.network.messages.clienttoserver;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.MessageType;

/**
 * Message sent by the client to the server to start the lobby.
 */
public class StartLobbyMessage extends Message {
    public StartLobbyMessage() {
        super(MessageType.START_LOBBY);
    }
}
