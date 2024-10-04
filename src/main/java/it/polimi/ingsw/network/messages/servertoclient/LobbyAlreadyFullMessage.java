package it.polimi.ingsw.network.messages.servertoclient;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.MessageType;

/**
 * Message sent to the client when the client tries to join a lobby that is already full.
 */
public class LobbyAlreadyFullMessage extends Message {
    public LobbyAlreadyFullMessage() {
        super(MessageType.LOBBY_ALREADY_FULL);
    }
}
