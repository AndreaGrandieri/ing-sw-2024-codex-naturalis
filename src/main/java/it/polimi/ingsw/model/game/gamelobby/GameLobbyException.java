package it.polimi.ingsw.model.game.gamelobby;

/**
 * Exception thrown when something in the LobbyInfo class is wrong.
 */
public class GameLobbyException extends Exception {
    public enum Reason {
        INVALID_LOBBY_INFO_FOR_CREATION,
        LOBBY_ALREADY_FULL_EXCEPTION,
        NO_MORE_SPACE_FOR_NEW_LOBBIES,
        INVALID_JOIN_ATTEMPT,
    }

    private final Reason reason;

    public GameLobbyException(String message, Reason reason) {
        super(message);
        this.reason = reason;
    }

        public Reason getReason() {
        return reason;
    }
}
