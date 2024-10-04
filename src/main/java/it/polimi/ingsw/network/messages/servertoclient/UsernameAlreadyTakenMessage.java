package it.polimi.ingsw.network.messages.servertoclient;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.MessageType;

/**
 * Message sent to the client when the username it chose is already taken.
 */
public class UsernameAlreadyTakenMessage extends Message {
    public UsernameAlreadyTakenMessage() {
        super(MessageType.USERNAME_ALREADY_TAKEN);
    }
}
