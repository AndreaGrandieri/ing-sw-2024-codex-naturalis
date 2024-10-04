package it.polimi.ingsw.controller;

import it.polimi.ingsw.client.HandlerController;
import it.polimi.ingsw.controller.event.lobby.LobbyEvents;
import it.polimi.ingsw.controller.event.lobby.LobbyStartEvent;
import it.polimi.ingsw.model.game.gamelobby.GameLobbyException;
import it.polimi.ingsw.model.game.gamelobby.LobbyInfo;

import java.util.*;

/**
 * This class is the main controller for the lobbies and matches. It only contains the logic implementation.
 */
public class LogicMatchController extends HandlerController {
    private final Map<LobbyInfo, GameController> matchesMap;
    private final Map<LobbyInfo, ChatController> chatMap;
    private final Map<LobbyInfo, List<String>> chronoMap;
    private Integer maxLobbies;
    private Integer maxMatches;

    /**
     * Constructor for the LogicMatchController class.
     * @param maxLobbies The maximum number of lobbies that can be created; if invalid, defaults to 256.
     * @param maxMatches The maximum number of matches that can be created; if invalid, defaults to 256.
     */
    public LogicMatchController(Integer maxLobbies, Integer maxMatches) {
        try {
            setMaxLobbies(maxLobbies);
            setMaxMatches(maxMatches);
        } catch (MatchControllerException e) {
            // Invalid maxLobbies or/and maxMatches detected. Falling back to default values
            try {
                setMaxLobbies(256);
                setMaxMatches(256);
            } catch (MatchControllerException ignored) {
                // setMaxLobbies and setMaxMatches are given safe values. This catch will never
                // execute
            }
        }

        matchesMap = new LinkedHashMap<>();
        chatMap = new HashMap<>();
        chronoMap = new HashMap<>();
    }

    /**
     * Creates a new lobby UUID. The UUID is a 4-character string and guaranteed to be unique.
     * @return The new lobby UUID.
     */
        private String createNewLobbyUUID() {
        List<String> UUIDs = matchesMap.keySet().stream()
                .map(LobbyInfo::getUuid)
                .toList();

        String UUID;
        do {
            UUID = java.util.UUID.randomUUID().toString().substring(0, 4);
        } while (UUIDs.contains(UUID));
        return UUID;
    }

    /**
     * Sets the maximum number of lobbies that can be created.
     * @param maxLobbies The maximum number of lobbies that can be created.
     * @throws MatchControllerException If the maximum number of lobbies is invalid.
     */
    public void setMaxLobbies(Integer maxLobbies) throws MatchControllerException {
        if (maxLobbies < 1 || maxLobbies > 256) {
            throw new MatchControllerException("Invalid number of lobbies. Valid range from 1 to 256.",
                    MatchControllerException.Reason.INVALID_PARAMETER);
        }

        this.maxLobbies = maxLobbies;
    }

    /**
     * Sets the maximum number of matches that can be created.
     * @param maxMatches The maximum number of matches that can be created.
     * @throws MatchControllerException If the maximum number of matches is invalid.
     */
    public void setMaxMatches(Integer maxMatches) throws MatchControllerException {
        if (maxMatches < 1 || maxMatches > 256) {
            throw new MatchControllerException("Invalid number of matches. Valid range from 1 to 256.",
                    MatchControllerException.Reason.INVALID_PARAMETER);
        }

        this.maxMatches = maxMatches;
    }

    /**
     * Get a list of all LobbyInfo stored in the controller.
     * @return A list of all LobbyInfo stored in the controller.
     */
        public ArrayList<LobbyInfo> getInfoLobbies() {
        ArrayList<LobbyInfo> infos = new ArrayList<>((matchesMap.keySet()).stream().map(LobbyInfo::new).toList());
        Collections.reverse(infos);
        return infos;
    }

    /**
     * Get a list of all LobbyInfo about lobby that can be joined by the user.
     * @param username The username of the user.
     * @return A list of all LobbyInfo about lobby that can be joined by the user.
     */
        public List<LobbyInfo> getLobbableBiasedLobbyInfo(String username) {
        return matchesMap.keySet().stream()
                .filter(lobby -> isLobbable(lobby) || chronoMap.get(lobby).contains(username))
                .toList();
    }

    /**
     * Returns whether a lobby is lobbable or not.
     * A lobby is lobbable if there is no GameController associated to it, ie: no match is ongoing and the lobby
     * can still be joined by new players.
     * @param info The LobbyInfo to check.
     * @return True if the lobby is lobbable, false otherwise.
     */
    private boolean isLobbable(LobbyInfo info) {
        // No GameController associated to the lobby
        return matchesMap.get(info) == null;
    }

    /**
     * Get the LobbyInfo of a lobby by its UUID.
     * @param lobbyUUID The UUID of the lobby.
     * @return The LobbyInfo of the lobby.
     * @throws MatchControllerException If the lobby with the given UUID is not found.
     */
        public LobbyInfo getInfoLobbyByUUID(String lobbyUUID) throws MatchControllerException {
        return matchesMap.keySet().stream()
                .filter(lobby -> lobby.getUuid().equals(lobbyUUID))
                .findAny()
                .orElseThrow(() -> new MatchControllerException("No lobby with the given lobbyUUID found.",
                        MatchControllerException.Reason.NO_ENTRY_FOUND));
    }

    public GameController getGameController(String username) throws MatchControllerException {
        return getMatchesMap().get(getLobbyInfoByPlayerUsername(username));
    }

    public ChatController getChatController(String username) throws MatchControllerException {
        LobbyInfo lobbyInfo = getLobbyInfoByPlayerUsername(username);
        return chatMap.get(lobbyInfo);
    }

    /**
     * Create a new lobby.
     * @param name The name of the lobby.
     * @param maxPlayers The maximum number of players that can join the lobby.
     * @param username The username of the user that creates the lobby (master).
     * @throws MatchControllerException If joinLobby fails.
     * @throws GameLobbyException If there is no more space for new lobbies on the server.
     */
    public void createLobby(String name, Integer maxPlayers, String username) throws
            MatchControllerException,
            GameLobbyException {
        // Checking that there is space for a new lobby
        if (matchesMap.size() == this.maxLobbies) {
            throw new GameLobbyException("No more space for new lobbies on the server.",
                    GameLobbyException.Reason.NO_MORE_SPACE_FOR_NEW_LOBBIES);
        }

        String uuid = createNewLobbyUUID();

        LobbyInfo lobbyInfo = new LobbyInfo(uuid, name, maxPlayers, username, false);
        matchesMap.put(lobbyInfo, null);
        chronoMap.put(lobbyInfo, new ArrayList<>());

        // We create also the chat controller, and add it in the chatMap, in the same way of GameController
        ChatController chatController = new ChatController(lobbyInfo);
        chatMap.put(lobbyInfo, chatController);

        // Return value of joinLobby can be ignored since logic guarantees it will always be false in this context
        joinLobby(lobbyInfo.getUuid(), username);
    }

    /**
     * Join a lobby.
     * @param lobbyUUID The UUID of the lobby to join.
     * @param username The username of the user that wants to join the lobby.
     * @return True if the user needs a GameController to interact in the match, false otherwise.
     * @throws MatchControllerException If joining process fails; If the lobby with the given UUID is not found.
     * @throws GameLobbyException If the lobby addPlayer procedure fails.
     */
        public Boolean joinLobby(String lobbyUUID, String username) throws MatchControllerException, GameLobbyException {
        LobbyInfo infoLobby = this.getInfoLobbyByUUID(lobbyUUID);

        // Before adding the user to the GameLobby, we are gonna check that doing this add is legal
        // A lobby is joinable iff it is lobbable
        if (isLobbable(infoLobby)) {
            // Adding the user to a lobby that is NOT a match ongoing: this means that there is no
            // GameController associated with this lobby. Thus, no GameController has to be specified to the
            // user.
            infoLobby.addPlayer(username);

            // Signaling that no GameController association procedure has to be run
            return false;
        } else if (chronoMap.get(infoLobby).contains(username)) {
            // If isLobbable failed, the GameLobby may still be joined by the chrono users...

            // User has the rights to join the lobby even if it is not lobbable
            // Keep in mind that the lobby is still not lobbable... with this we mean that a match is
            // ongoing: we so have to add the User to the lobby and the associated match
            infoLobby.addPlayer(username);

            // Now... since the lobby is a match ongoing there is a GameController associated with the lobby.
            // The user NEEDS this GameController to be able to correctly interact in the match.
            // Signaling that a GameController association procedure has to be run
            return true;
        }

        // Join failed. Thus, no GameController association procedure has to be run.
        throw new MatchControllerException("Could not join.", MatchControllerException.Reason.BAD_STATE_FOR_JOIN);
    }

    /**
     * Get the LobbyInfo of a lobby by the username of a player in the lobby.
     * @param username The username of the player in the lobby.
     * @return The LobbyInfo of the lobby.
     * @throws MatchControllerException If the lobby with the given player is not found.
     */
        public LobbyInfo getLobbyInfoByPlayerUsername(String username) throws MatchControllerException {
        return matchesMap.keySet().stream()
                .filter(lobby -> lobby.getPlayerUsernames().contains(username))
                .findAny()
                .orElseThrow(() -> new MatchControllerException("No lobby with the given player found.",
                        MatchControllerException.Reason.NO_ENTRY_FOUND));

    }

    /**
     * Start a lobby.
     * @param startIssuerUsername The username of the user that wants to start the lobby.
     * @throws MatchControllerException If the lobby is not in a valid state to be started; If the user is not the master
     */
    public void startLobby(String startIssuerUsername) throws MatchControllerException {
        LobbyInfo lobby = getLobbyInfoByPlayerUsername(startIssuerUsername);

        // The lobby can be started only by the master
        if (!lobby.getMasterUsername().equals(startIssuerUsername)) {
            throw new MatchControllerException("Only the master can start the lobby and a start has been attempted" +
                    " by a non master.", MatchControllerException.Reason.START_ATTEMPT_FROM_NON_MASTER);
        }

        // The lobby can be started only if there are at least 2 players
        if (lobby.getPlayerUsernames().size() < 2) {
            throw new MatchControllerException("The lobby must have at least 2 players to start.",
                    MatchControllerException.Reason.BAD_STATE_FOR_START);
        }

        // To start a lobby means basically to convert a GameLobby into a GameController
        // Here, we are creating a GameController from the GameLobby
        GameController gameController = new GameController(lobby.getPlayerUsernames());

        // Lobby creation is now ok. We are gonna associate its GameController to the GameLobby so that the system
        // knows that this lobby is "not lobbable"
        matchesMap.put(lobby, gameController);

        // Finally, we take a snapshot of all the users' usernames that are present when startLobby is called: we
        // are saving these usernames in chronoMap so that ONLY these users can rejoin the ongoing match in case
        // they disconnects while playing.
        chronoMap.get(lobby).addAll(lobby.getPlayerUsernames());

        lobby.setGameOngoing(true);

        executeHandlers(LobbyEvents.LOBBY_START, new LobbyStartEvent());
    }

    /**
     * Exit a lobby.
     * @param username The username of the user that wants to exit the lobby.
     * @throws MatchControllerException If the user is not in the lobby.
     */
    public void exitLobby(String username) throws MatchControllerException {
        // Notice that lobby.removeUser already performs master recalculation if needed

        // This method removes the specified User from the lobby if the User exists in that lobby
        // Please notice that the lobby needs to be "actually a lobby", ie: the associated match must not have
        // started yet (exists yet)

        LobbyInfo lobby = this.getLobbyInfoByPlayerUsername(username);

        // Check that the lobby is actually a lobby
        // Now, check that the User is in the lobby
        if (isLobbable(lobby) && lobby.getPlayerUsernames().contains(username)) {
            // Telling the ChatController to disconnect the user with the given username
            ChatController associatedChatController = chatMap.get(lobby);
            associatedChatController.disconnectPlayer(username);

            Boolean lobbyToPrune = lobby.removePlayer(username);

            if (lobbyToPrune) {
                matchesMap.remove(lobby);
            }
        }
    }

    /**
     * Exit a match.
     * @param username The username of the user that wants to exit the match.
     * @throws MatchControllerException If the user is not in the match.
     */
    public void exitMatch(String username) throws MatchControllerException {
        // This method removes the specified User from the match if the User exists in that Match
        // Being removed from the Match, the User is also removed from the corresponding lobby
        LobbyInfo lobby = this.getLobbyInfoByPlayerUsername(username);

        if (!isLobbable(lobby) && lobby.getPlayerUsernames().contains(username)) {
            // Now, we get the associatedGameController
            GameController associatedGameController = matchesMap.get(lobby);

            // Telling the GameController to disconnect the user with the given username
            // THIS IS THE WAY TO REMOVE A PLAYER FROM THE MATCH
            associatedGameController.disconnectPlayer(username);

            // Telling the ChatController to disconnect the user with the given username
            ChatController associatedChatController = chatMap.get(lobby);
            associatedChatController.disconnectPlayer(username);

            // Removing the player from the lobby. Yes... we are in the exitMatch function but anyway the information
            // about the user is saved in the LobbyInfo object. This LobbyInfo object is associated to a non-null
            // GameController since a match is ongoing.
            // THIS IS THE WAY TO REMOVE A PLAYER FROM THE LOBBY THAT IS AN ONGOING MATCH
            Boolean matchToPrune = lobby.removePlayer(username);

            if (matchToPrune) {
                // If the ongoing match gets empty... well just act like with the lobbies and prune it
                matchesMap.remove(lobby);
            }
        }
    }

        public Map<LobbyInfo, List<String>> getChronoMap() {
        return chronoMap;
    }

        public Map<LobbyInfo, GameController> getMatchesMap() {
        return matchesMap;
    }

        public Map<LobbyInfo, ChatController> getChatMap() {
        return chatMap;
    }
}
