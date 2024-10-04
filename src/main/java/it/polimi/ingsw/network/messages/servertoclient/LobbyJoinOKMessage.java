package it.polimi.ingsw.network.messages.servertoclient;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.MessageType;

/**
 * Message sent to the client when the client joins a lobby successfully.
 */
public class LobbyJoinOKMessage extends Message {
    public LobbyJoinOKMessage() {
        super(MessageType.LOBBY_JOIN_OK);
    }
}
