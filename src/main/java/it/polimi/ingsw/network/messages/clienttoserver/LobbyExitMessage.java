package it.polimi.ingsw.network.messages.clienttoserver;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.MessageType;

/**
 * Message sent by the client to the server to exit the lobby.
 */
public class LobbyExitMessage extends Message {
    public LobbyExitMessage() {
        super(MessageType.LOBBY_EXIT);
    }
}
