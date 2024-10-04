package it.polimi.ingsw.network.messages.servertoclient;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.MessageType;

/**
 * Message sent to the client when the lobby info provided for the creation of a new lobby are invalid.
 */
public class InvalidLobbyInfoForCreationMessage extends Message {
    public InvalidLobbyInfoForCreationMessage() {
        super(MessageType.INVALID_LOBBY_INFO_FOR_CREATION);
    }
}
