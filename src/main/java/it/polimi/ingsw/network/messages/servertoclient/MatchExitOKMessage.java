package it.polimi.ingsw.network.messages.servertoclient;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.MessageType;

/**
 * Message sent to the client when the client exits a match successfully.
 */
public class MatchExitOKMessage extends Message {
    public MatchExitOKMessage() {
        super(MessageType.MATCH_EXIT_OK);
    }
}
