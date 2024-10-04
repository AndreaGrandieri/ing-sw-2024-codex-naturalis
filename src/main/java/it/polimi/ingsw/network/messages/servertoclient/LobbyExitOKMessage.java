package it.polimi.ingsw.network.messages.servertoclient;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.MessageType;

/**
 * Message sent to the client when the client exits a lobby successfully.
 */
public class LobbyExitOKMessage extends Message {
    public LobbyExitOKMessage() {
        super(MessageType.LOBBY_EXIT_OK);
    }
}
