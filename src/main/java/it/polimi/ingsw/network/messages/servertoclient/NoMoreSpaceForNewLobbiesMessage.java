package it.polimi.ingsw.network.messages.servertoclient;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.MessageType;

/**
 * Message sent to the client when there is no more space for new lobbies.
 */
public class NoMoreSpaceForNewLobbiesMessage extends Message {
    public NoMoreSpaceForNewLobbiesMessage() {
        super(MessageType.NO_MORE_SPACE_FOR_NEW_LOBBIES);
    }
}
