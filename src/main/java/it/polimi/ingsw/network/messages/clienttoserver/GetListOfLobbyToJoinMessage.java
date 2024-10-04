package it.polimi.ingsw.network.messages.clienttoserver;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.MessageType;

/**
 * Message sent by the client to the server to request the list of lobbies that the client can join.
 */
public class GetListOfLobbyToJoinMessage extends Message {
    public GetListOfLobbyToJoinMessage() {
        super(MessageType.GET_LIST_OF_LOBBY_TO_JOIN);
    }
}
