package it.polimi.ingsw.network.messages.clienttoserver;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.MessageType;

/**
 * Message sent by the client to the server to provide the username of the player.
 */
public class UsernameMessage extends Message {
    private final String username;

    public UsernameMessage(String username) {
        super(MessageType.USERNAME);
        this.username = username;
    }

        public String getUsername() {
        return this.username;
    }
}
