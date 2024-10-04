package it.polimi.ingsw.network.messages;

import java.io.Serializable;

/**
 * Abstract class that represents a message sent between the server and the client.
 */
abstract public class Message implements Serializable {
    private final MessageType messageType;

    public Message(MessageType messageType) {
        this.messageType = messageType;
    }

        public MessageType getType() {
        return this.messageType;
    }
}
