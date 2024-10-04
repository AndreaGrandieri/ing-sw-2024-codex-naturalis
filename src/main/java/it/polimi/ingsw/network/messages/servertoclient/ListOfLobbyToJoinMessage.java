package it.polimi.ingsw.network.messages.servertoclient;

import it.polimi.ingsw.controller.MatchController;
import it.polimi.ingsw.controller.MatchControllerException;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.MessageType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Message sent to the client containing the list of lobbies that the client can join.
 */
public class ListOfLobbyToJoinMessage extends Message {
    public static class LobbyInfo implements Serializable {
        public String uuid;
        public String name;
        public Integer maxPlayers;
        public ArrayList<String> playerUsernames;
        public Boolean gameOngoing;

        @Override
        public String toString() {
            return "LobbyInfo{" +
                    "uuid='" + uuid + '\'' +
                    ", name='" + name + '\'' +
                    ", maxPlayers=" + maxPlayers +
                    ", playerUsernames=" + playerUsernames +
                    '}';
        }
    }

    private final List<LobbyInfo> lobbies;

    public ListOfLobbyToJoinMessage(String username) throws MatchControllerException {
        super(MessageType.LIST_OF_LOBBY_TO_JOIN);

        this.lobbies = new ArrayList<>();
        MatchController.getInstance().getLobbableBiasedLobbyInfo(username).forEach(lobby -> {
            LobbyInfo info = new LobbyInfo();

            info.uuid = lobby.getUuid();
            info.maxPlayers = lobby.getMaxPlayers();
            info.playerUsernames = new ArrayList<>();
            info.playerUsernames.addAll(lobby.getPlayerUsernames());
            info.name = lobby.getName();
            info.gameOngoing = lobby.getGameOngoing();

            this.lobbies.add(info);
        });
    }

        public List<LobbyInfo> getLobbies() {
        return lobbies;
    }
}
