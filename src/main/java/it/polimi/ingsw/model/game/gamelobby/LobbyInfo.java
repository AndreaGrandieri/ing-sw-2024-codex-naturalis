package it.polimi.ingsw.model.game.gamelobby;

import it.polimi.ingsw.util.TextValidator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents the information of a lobby.
 */
public class LobbyInfo implements Serializable {
    private final String uuid;
    private final String name;
    private final Integer maxPlayers;
    private final List<String> playerUsernames;
    private String masterUsername;
    private Boolean gameOngoing;

    public LobbyInfo(String uuid, String name, Integer maxPlayers,
                     String masterUsername, List<String> playerUsernames,
                     Boolean gameOngoing) throws GameLobbyException {
        creationChecks(name, maxPlayers);

        this.uuid = uuid;
        this.name = name;
        this.maxPlayers = maxPlayers;
        this.masterUsername = masterUsername;

        // DO NOT EVER F****** FORGET TO NEW ALLOCATE! This resolves an important bug.
        this.playerUsernames = new ArrayList<>(playerUsernames);

        this.gameOngoing = gameOngoing;
    }

    public LobbyInfo(String uuid, String name, Integer maxPlayers, List<String> playerUsernames,
                     Boolean gameOngoing)
            throws GameLobbyException {
        this(uuid, name, maxPlayers, null, playerUsernames, gameOngoing);
    }

    public LobbyInfo(String uuid, String name, Integer maxPlayers, String masterUsername,
                     Boolean gameOngoing)
            throws GameLobbyException {
        this(uuid, name, maxPlayers, masterUsername, new ArrayList<>(), gameOngoing);
    }

    public LobbyInfo(LobbyInfo info) {
        this.uuid = info.uuid;
        this.name = info.name;
        this.maxPlayers = info.maxPlayers;
        this.masterUsername = info.masterUsername;
        this.playerUsernames = new ArrayList<>(info.playerUsernames);
        this.gameOngoing = info.gameOngoing;
    }

    /**
     * Checks if the information for the creation of a lobby is valid.
     * @param name The name of the lobby.
     * @param maxPlayers The maximum number of players in the lobby.
     * @throws GameLobbyException If the information is not valid.
     */
    private void creationChecks(String name, Integer maxPlayers) throws GameLobbyException {
        // Checks
        // Checking that the name of the lobby is valid
        Pattern pattern = Pattern.compile(TextValidator.lobbyNameValidator);
        Matcher matcher = pattern.matcher(name);
        if (!matcher.matches()) {
            throw new GameLobbyException("Invalid lobby name.",
                    GameLobbyException.Reason.INVALID_LOBBY_INFO_FOR_CREATION);
        }

        pattern = Pattern.compile(TextValidator.lobbyPlayersNumValidator);
        matcher = pattern.matcher(maxPlayers.toString());
        if (!matcher.matches()) {
            throw new GameLobbyException("Invalid number of players. Valid range from 2 to 4.",
                    GameLobbyException.Reason.INVALID_LOBBY_INFO_FOR_CREATION);
        }
    }

        public String getUuid() {
        return uuid;
    }

        public String getName() {
        return name;
    }

        public Integer getMaxPlayers() {
        return maxPlayers;
    }

        public String getMasterUsername() {
        return masterUsername;
    }

    /**
     * Returns the list of players usernames in the lobby.
     * @return The list of players usernames in the lobby.
     */
        public List<String> getPlayerUsernames() {
        return playerUsernames;
    }

    /**
     * Adds a player to the lobby.
     * The player is added only if the lobby is not full and the player is not already in the lobby.
     * @param username The username of the player to add.
     * @throws GameLobbyException If the lobby is full or the player is already in the lobby.
     */
    public void addPlayer(String username) throws GameLobbyException {
        if (this.playerUsernames.size() >= this.maxPlayers) {
            throw new GameLobbyException("The lobby is already full. Won't join.",
                    GameLobbyException.Reason.LOBBY_ALREADY_FULL_EXCEPTION);
        }

        if (this.playerUsernames.contains(username)) {
            throw new GameLobbyException("The user trying to join is already in the lobby. Won't join.",
                    GameLobbyException.Reason.INVALID_JOIN_ATTEMPT);
        }

        playerUsernames.add(username);
    }

    /**
     * Removes a player from the lobby.
     * @param username The username of the player to remove.
     * @return True if the lobby is empty after the removal, false otherwise.
     */
        public Boolean removePlayer(String username) {
        this.playerUsernames.remove(username);

        if (this.playerUsernames.isEmpty()) {
            // Signals that the lobby needs to be pruned since it is empty
            return true;
        }

        // If the player that left was the master, the master is changed to a random player in the lobby.
        if (username.equals(this.masterUsername)) {
            // Create a Random object
            Random random = new Random();

            // Get a random index from 0 to the size of the players list
            int randomIndex = random.nextInt(this.playerUsernames.size());

            // Get the player at the random index and set as the new master
            this.masterUsername = this.playerUsernames.get(randomIndex);
        }

        // Signals that the lobby is not empty so it does not need pruning
        return false;

        // Removal, with any type of return status won't remove the user username from chrono
    }

    @Override
    public String toString() {
        String masterUsernameString = (masterUsername != null) ? ", masterUsername='" + masterUsername + "'" : "";
        return "LobbyInfo{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", maxPlayers=" + maxPlayers +
                masterUsernameString +
                ", playerUsernames=" + playerUsernames +
                ", gameOngoing=" + gameOngoing +
                '}';
    }

        public Boolean getGameOngoing() {
        return gameOngoing;
    }

    public void setGameOngoing(Boolean gameOngoing) {
        this.gameOngoing = gameOngoing;
    }
}
