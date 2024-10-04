package it.polimi.ingsw.network.messages.clienttoserver;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.MessageType;

/**
 * Message sent by the client to the server to exit the match.
 */
public class MatchExitMessage extends Message {
    public MatchExitMessage() {
        super(MessageType.MATCH_EXIT);
    }
}
