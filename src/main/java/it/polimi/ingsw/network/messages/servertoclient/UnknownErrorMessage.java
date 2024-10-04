package it.polimi.ingsw.network.messages.servertoclient;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.MessageType;

/**
 * Message sent to the client when an unknown error occurs.
 */
public class UnknownErrorMessage extends Message {
    public UnknownErrorMessage() {
        super(MessageType.UNKNOWN_ERROR);
    }
}
