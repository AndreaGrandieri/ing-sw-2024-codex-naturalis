package it.polimi.ingsw.network.messages.clienttoserver;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.MessageType;

/**
 * Message sent by the client to the server to request the information of the lobby that the client is in.
 */
public class GetLobbyInfoMessage extends Message {
    public GetLobbyInfoMessage() {
        super(MessageType.GET_LOBBY_INFO_MESSAGE);
    }
}
