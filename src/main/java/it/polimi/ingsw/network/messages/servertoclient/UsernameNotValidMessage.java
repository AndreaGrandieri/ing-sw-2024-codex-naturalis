package it.polimi.ingsw.network.messages.servertoclient;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.MessageType;

/**
 * Message sent to the client when the username it chose is not valid.
 */
public class UsernameNotValidMessage extends Message {
    private final String reason;

    public UsernameNotValidMessage(String reason) {
        super(MessageType.USERNAME_NOT_VALID);
        this.reason = reason;
    }

        public String getReason() {
        return reason;
    }
}
