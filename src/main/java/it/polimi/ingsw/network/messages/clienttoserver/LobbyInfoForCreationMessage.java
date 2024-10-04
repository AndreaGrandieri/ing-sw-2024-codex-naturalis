package it.polimi.ingsw.network.messages.clienttoserver;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.MessageType;

/**
 * Message sent by the client to the server to provide the information of the lobby that the client wants to create.
 */
public class LobbyInfoForCreationMessage extends Message {
    private final String lobbyName;
    private final Integer maxPlayers;

    public LobbyInfoForCreationMessage(String lobbyName, Integer maxPlayers) {
        super(MessageType.LOBBY_INFO_FOR_CREATION);

        this.lobbyName = lobbyName;
        this.maxPlayers = maxPlayers;
    }

        public String getLobbyName() {
        return this.lobbyName;
    }

        public Integer getMaxPlayers() {
        return this.maxPlayers;
    }
}
