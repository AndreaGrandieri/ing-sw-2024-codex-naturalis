package it.polimi.ingsw.network.messages.servertoclient;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.MessageType;

/**
 * Message sent to the client when the username it chose is confirmed.
 */
public class UsernameConfirmedMessage extends Message {
    public UsernameConfirmedMessage() {
        super(MessageType.USERNAME_CONFIRMED);
    }
}
