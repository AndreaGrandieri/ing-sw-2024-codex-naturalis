package it.polimi.ingsw.network.messages.servertoclient;

import it.polimi.ingsw.model.game.gamelobby.GameLobbyException;
import it.polimi.ingsw.model.game.gamelobby.LobbyInfo;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.MessageType;

/**
 * Message sent to the client containing the lobby info of the lobby the client is in.
 */
public class LobbyInfoMessage extends Message {
    private final LobbyInfo lobbyInfo;

    public LobbyInfoMessage(LobbyInfo lobbyInfo, String username) {
        super(MessageType.LOBBY_INFO);

        LobbyInfo lobbyInfo1;
        try {
            // Rebuild lobbyInfo excluding the masterUsername info
            lobbyInfo1 = new LobbyInfo(
                    lobbyInfo.getUuid(),
                    lobbyInfo.getName(),
                    lobbyInfo.getMaxPlayers(),
                    lobbyInfo.getMasterUsername().equals(username) ? lobbyInfo.getMasterUsername() : null,
                    lobbyInfo.getPlayerUsernames(),
                    lobbyInfo.getGameOngoing()
            );
        } catch (GameLobbyException e) {
            // Situation here is pretty akward and should never happen. Basically it means
            // that we are trying to build a lobby with invalid data regarding the getName() and/or
            // the getMaxPlayers() information. But... how can we be doing this since gameLobby is a valid
            // constructed lobby that has to run against the creationChecks function upon its construction?
            // Basically we can never reach this state, so we can be pretty confident to just act dumb.
            // cause.
            lobbyInfo1 = null;
        }
        this.lobbyInfo = lobbyInfo1;
    }

        public LobbyInfo getLobbyInfo() {
        return lobbyInfo;
    }
}
