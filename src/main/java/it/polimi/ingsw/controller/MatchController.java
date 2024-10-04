package it.polimi.ingsw.controller;

import it.polimi.ingsw.controller.event.Event;
import it.polimi.ingsw.controller.event.chat.ChatEvents;
import it.polimi.ingsw.controller.event.game.GameEvents;
import it.polimi.ingsw.controller.event.game.MatchCompositionChangeEvent;
import it.polimi.ingsw.controller.event.lobby.LobbyStartEvent;
import it.polimi.ingsw.model.game.gamelobby.GameLobbyException;
import it.polimi.ingsw.model.game.gamelobby.LobbyInfo;
import it.polimi.ingsw.network.Profiles;
import it.polimi.ingsw.network.ProfilesException;
import it.polimi.ingsw.network.liveloop.User;
import it.polimi.ingsw.network.messages.MessageType;
import it.polimi.ingsw.network.messages.SingleArgMessage;
import it.polimi.ingsw.network.messages.servertoclient.ListOfLobbyToJoinMessage;
import it.polimi.ingsw.network.messages.servertoclient.LobbyInfoMessage;
import it.polimi.ingsw.network.messages.servertoclient.LobbyStartMessage;
import it.polimi.ingsw.network.rmi.UserStub;
import it.polimi.ingsw.network.state.ChooseCreateJoinState;
import it.polimi.ingsw.network.state.InGameState;
import it.polimi.ingsw.network.state.InLobbyState;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the main controller for the lobbies and matches. It extends the LogicMatchController class.
 */
public class MatchController extends LogicMatchController {
    private static MatchController instance;

    public MatchController(Integer maxLobbies, Integer maxMatches) {
        super(maxLobbies, maxMatches);
    }

        public static MatchController getInstance(Integer maxLobbies, Integer maxMatches) {
        if (instance == null) {
            instance = new MatchController(maxLobbies, maxMatches);
        }
        return instance;
    }

        public static MatchController getInstance() {
        if (instance == null) {
            instance = new MatchController(256, 256);
        }
        return instance;
    }

    /**
     * This method returns a list of User objects from a LobbyInfo object.
     * @param info The LobbyInfo object.
     * @return A list of User objects.
     * @throws MatchControllerException If the User object cannot be retrieved.
     */
        private List<User> usersFromLobbyInfo(LobbyInfo info) throws MatchControllerException {
        List<User> users = new ArrayList<>();
        for (String username : info.getPlayerUsernames()) {
            users.add(this.getUser(username));
        }
        return users;
    }

    /**
     * This method returns a User object from a username.
     * @param username The username.
     * @return A User object.
     * @throws MatchControllerException If the User object cannot be retrieved.
     */
        private User getUser(String username) throws MatchControllerException {
        try {
            return Profiles.getInstance().getUserByUsername(username);
        } catch (ProfilesException e) {
            throw new MatchControllerException("Cannot get username: " + e, MatchControllerException.Reason.NO_ENTRY_FOUND);
        }
    }

    /**
     * Creates a lobby.
     * @param name The name of the lobby.
     * @param maxPlayers The maximum number of players in the lobby.
     * @param user The user that creates the lobby.
     * @throws MatchControllerException If thrown by {@link LogicMatchController#createLobby(String, Integer, String)}
     * @throws GameLobbyException If thrown by {@link LogicMatchController#createLobby(String, Integer, String)}
     */
    public void createLobby(String name, Integer maxPlayers, User user) throws
            MatchControllerException,
            GameLobbyException {
        super.createLobby(name, maxPlayers, user.getUsername());

        LobbyInfo info = getLobbyInfoByPlayerUsername(user.getUsername());

        // Setting also the ChatController for each user
        ChatController chatController = getChatMap().get(info);
        user.setChatController(chatController);

        // Chat controller adds (1)
        chatController.addEventHandler(user.getUsername(), ChatEvents.BROADCAST_MESSAGE, (e) -> {
            List<User> userList = Profiles.getInstance().getUsersByUsernameList(chatController.getChatDistributionList());
            if (userList.contains(user)) {
                user.send(new SingleArgMessage<>(MessageType.BROADCAST_MESSAGE_EVENT, e));
            }
        });

        // Chat controller adds (2)
        chatController.addEventHandler(user.getUsername(), ChatEvents.PRIVATE_MESSAGE, (e) -> {
            List<User> userList = Profiles.getInstance().getUsersByUsernameList(chatController.getChatDistributionList());
            if (userList.contains(user)) {
                user.send(new SingleArgMessage<>(MessageType.PRIVATE_MESSAGE_EVENT, e));
            }
        });
    }

    /**
     * Creates a lobby in an RMI communication.
     * @param name The name of the lobby.
     * @param maxPlayers The maximum number of players in the lobby.
     * @param user The user that creates the lobby.
     * @throws GameLobbyException If thrown by {@link LogicMatchController#createLobby(String, Integer, String)}
     * @throws MatchControllerException If thrown by {@link LogicMatchController#createLobby(String, Integer, String)}
     */
    public void createLobbyRMI(String name, Integer maxPlayers, User user) throws
            GameLobbyException,
            MatchControllerException {
        super.createLobby(name, maxPlayers, user.getUsername());

        LobbyInfo info = getLobbyInfoByPlayerUsername(user.getUsername());

        // Setting also the ChatController for each user
        ChatController chatController = getChatMap().get(info);
        user.setChatController(chatController);

        // Chat controller adds (1)
        chatController.addEventHandler(user.getUsername(), ChatEvents.BROADCAST_MESSAGE, (e) -> {
            List<User> userList = Profiles.getInstance().getUsersByUsernameList(chatController.getChatDistributionList());
            if (userList.contains(user)) {
                try {
                    if (userList.contains(user)) {
                        user.getUserStub().pushEvent(new SingleArgMessage<>(MessageType.BROADCAST_MESSAGE_EVENT, e));
                    }
                } catch (RemoteException ignored) {
                    // Handler failed due to RMI error. Nothing to do here. Ping service will prune
                    // the necessary.
                }
            }
        });

        // Chat controller adds (2)
        chatController.addEventHandler(user.getUsername(), ChatEvents.PRIVATE_MESSAGE, (e) -> {
            List<User> userList = Profiles.getInstance().getUsersByUsernameList(chatController.getChatDistributionList());
            try {
                if (userList.contains(user)) {
                    user.getUserStub().pushEvent(new SingleArgMessage<>(MessageType.PRIVATE_MESSAGE_EVENT, e));
                }
            } catch (RemoteException ignored) {
                // Handler failed due to RMI error. Nothing to do here. Ping service will prune
                // the necessary.
            }
        });

        // State has to be here since the handling is out of the State override
        user.setState(new InLobbyState(user));
    }

    /**
     * Joins a lobby.
     * @param lobbyUUID The UUID of the lobby.
     * @param user The user that joins the lobby.
     * @return True if the lobby is an ongoing match, false otherwise.
     * @throws GameLobbyException If thrown by {@link LogicMatchController#joinLobby(String, String)}
     * @throws MatchControllerException If thrown by {@link LogicMatchController#joinLobby(String, String)}
     */
        public Boolean joinLobby(String lobbyUUID, User user) throws GameLobbyException,
            MatchControllerException {
        Boolean runGameControllerAssociationProcedure = super.joinLobby(lobbyUUID, user.getUsername());

        LobbyInfo lobbyInfo = MatchController.getInstance().getInfoLobbyByUUID(lobbyUUID);

        // Setting also the ChatController for each user
        ChatController chatController = getChatMap().get(lobbyInfo);
        user.setChatController(chatController);

        // Chat controller adds (1)
        chatController.addEventHandler(user.getUsername(), ChatEvents.BROADCAST_MESSAGE, (e) -> {
            List<User> userList = Profiles.getInstance().getUsersByUsernameList(chatController.getChatDistributionList());
            if (userList.contains(user)) {
                user.send(new SingleArgMessage<>(MessageType.BROADCAST_MESSAGE_EVENT, e));
            }
        });

        // Chat controller adds (2)
        chatController.addEventHandler(user.getUsername(), ChatEvents.PRIVATE_MESSAGE, (e) -> {
            List<User> userList = Profiles.getInstance().getUsersByUsernameList(chatController.getChatDistributionList());
            if (userList.contains(user)) {
                user.send(new SingleArgMessage<>(MessageType.PRIVATE_MESSAGE_EVENT, e));
            }
        });

        // If true, it means that the lobby IS an ongoing match!
        if (runGameControllerAssociationProcedure) {
            List<User> users = List.of(user);

            GameController gameController = MatchController.getInstance().getMatchesMap().get(lobbyInfo);
            user.setGameController(gameController);

            // I need to add handlers to notify the new client about events
            gameController.addEventHandler(user.getUsername(), GameEvents.PLACE_EVENT, (e) ->
                    sendEachUser(e, users, MessageType.PLACE_CARD_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.CHOOSE_GOAL, (e) ->
                    sendEachUser(e, users, MessageType.CHOOSE_GOAL_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.SET_COLOR, (e) ->
                    sendEachUser(e, users, MessageType.SET_COLOR_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.SET_STARTER, (e) ->
                    sendEachUser(e, users, MessageType.SET_STARTER_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.TURN_CHANGE, (e) ->
                    sendEachUser(e, users, MessageType.TURN_CHANGE_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.DRAW_COVERED, (e) ->
                    sendEachUser(e, users, MessageType.DRAW_COVERED_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.DRAW_VISIBLE, (e) ->
                    sendEachUser(e, users, MessageType.DRAW_VISIBLE_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.STATE_CHANGE, (e) ->
                    sendEachUser(e, users, MessageType.STATE_CHANGE_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.MATCH_COMPOSITION_CHANGE, (e) ->
                    sendEachUser(e, users, MessageType.MATCH_COMPOSITION_CHANGE_EVENT));

            return true;
        } else {
            // Get the list of users already in the lobby that is being joined
            List<User> users = this.usersFromLobbyInfo(lobbyInfo);

            users.stream()
                    // Removing myself
                    .filter(_user -> !_user.equals(user))
                    .forEach(_user -> sendEachUser(new MatchCompositionChangeEvent(), List.of(_user), MessageType.MATCH_COMPOSITION_CHANGE_EVENT));

            return false;
        }
    }

    /**
     * Joins a lobby in an RMI communication.
     * @param lobbyUUID The UUID of the lobby.
     * @param user The user that joins the lobby.
     * @throws GameLobbyException If thrown by {@link LogicMatchController#joinLobby(String, String)}
     * @throws MatchControllerException If thrown by {@link LogicMatchController#joinLobby(String, String)}
     * @throws RemoteException If something goes wrong with the RMI communication.
     */
    public void joinLobbyRMI(String lobbyUUID, User user) throws GameLobbyException, MatchControllerException, RemoteException {
        Boolean runGameControllerAssociationProcedure = super.joinLobby(lobbyUUID, user.getUsername());

        LobbyInfo lobbyInfo = MatchController.getInstance().getInfoLobbyByUUID(lobbyUUID);

        // Setting also the ChatController for each user
        ChatController chatController = getChatMap().get(lobbyInfo);
        user.setChatController(chatController);

        // Chat controller adds (1)
        chatController.addEventHandler(user.getUsername(), ChatEvents.BROADCAST_MESSAGE, (e) -> {
            List<User> userList = Profiles.getInstance().getUsersByUsernameList(chatController.getChatDistributionList());
            if (userList.contains(user)) {
                try {
                    if (userList.contains(user)) {
                        user.getUserStub().pushEvent(new SingleArgMessage<>(MessageType.BROADCAST_MESSAGE_EVENT, e));
                    }
                } catch (RemoteException ignored) {
                    // Handler failed due to RMI error. Nothing to do here. Ping service will prune
                    // the necessary.
                }
            }
        });


        // Chat controller adds (2)
        chatController.addEventHandler(user.getUsername(), ChatEvents.PRIVATE_MESSAGE, (e) -> {
            List<User> userList = Profiles.getInstance().getUsersByUsernameList(chatController.getChatDistributionList());
            try {
                if (userList.contains(user)) {
                    user.getUserStub().pushEvent(new SingleArgMessage<>(MessageType.PRIVATE_MESSAGE_EVENT, e));
                }
            } catch (RemoteException ignored) {
                // Handler failed due to RMI error. Nothing to do here. Ping service will prune
                // the necessary.
            }
        });

        // If true, it means that the lobby IS an ongoing match!
        if (runGameControllerAssociationProcedure) {
            List<User> users = List.of(user);

            GameController gameController = MatchController.getInstance().getMatchesMap().get(lobbyInfo);
            user.setGameController(gameController);

            // I need to add handlers to notify the new client about events
            gameController.addEventHandler(user.getUsername(), GameEvents.PLACE_EVENT, (e) ->
                    pushAtEachUser(e, users, MessageType.PLACE_CARD_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.CHOOSE_GOAL, (e) ->
                    pushAtEachUser(e, users, MessageType.CHOOSE_GOAL_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.SET_COLOR, (e) ->
                    pushAtEachUser(e, users, MessageType.SET_COLOR_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.SET_STARTER, (e) ->
                    pushAtEachUser(e, users, MessageType.SET_STARTER_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.TURN_CHANGE, (e) ->
                    pushAtEachUser(e, users, MessageType.TURN_CHANGE_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.DRAW_COVERED, (e) ->
                    pushAtEachUser(e, users, MessageType.DRAW_COVERED_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.DRAW_VISIBLE, (e) ->
                    pushAtEachUser(e, users, MessageType.DRAW_VISIBLE_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.STATE_CHANGE, (e) ->
                    pushAtEachUser(e, users, MessageType.STATE_CHANGE_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.MATCH_COMPOSITION_CHANGE, (e) ->
                    pushAtEachUser(e, users, MessageType.MATCH_COMPOSITION_CHANGE_EVENT));

            user.getUserStub().pushEvent(new SingleArgMessage<>(MessageType.LOBBY_START_WRAPPED, new LobbyStartEvent()));
            user.setState(new InGameState(user));
            user.getGameController().reconnectPlayer(user.getUsername());
        } else {
            // Get the list of users already in the lobby that is being joined
            List<User> users = this.usersFromLobbyInfo(lobbyInfo);

            users.stream()
                    // Removing myself
                    .filter(_user -> !_user.equals(user))
                    .forEach(_user -> pushAtEachUser(new MatchCompositionChangeEvent(), List.of(_user), MessageType.MATCH_COMPOSITION_CHANGE_EVENT));

            user.setState(new InLobbyState(user));
        }
    }

        public List<ListOfLobbyToJoinMessage.LobbyInfo> getListOfLobbyToJoinRMI(User user) throws MatchControllerException {
        ListOfLobbyToJoinMessage list = new ListOfLobbyToJoinMessage(user.getUsername());
        return list.getLobbies();
    }

        public LobbyInfo getLobbyInfoRMI(User user) throws MatchControllerException {
        LobbyInfoMessage info = new LobbyInfoMessage(MatchController.getInstance().getLobbyInfoByPlayerUsername(user.getUsername()), user.getUsername());
        return info.getLobbyInfo();
    }

    /**
     * Starts a lobby.
     * @param startIssuerUsername The username of the user that wants to start the lobby.
     * @throws MatchControllerException If thrown by {@link LogicMatchController#startLobby(String)}
     */
    @Override
    public void startLobby(String startIssuerUsername) throws MatchControllerException {
        super.startLobby(startIssuerUsername);

        LobbyInfo info = getLobbyInfoByPlayerUsername(startIssuerUsername);
        GameController gameController = getMatchesMap().get(info);

        // Setting the same GameController of the match to all the users in the starting match
        List<User> users = usersFromLobbyInfo(info);
        users.forEach(u -> u.setGameController(gameController));

        // Add handlers to notify clients about events
        for (User user : users) {
            gameController.addEventHandler(user.getUsername(), GameEvents.PLACE_EVENT, (e) ->
                    sendEachUser(e, List.of(user), MessageType.PLACE_CARD_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.CHOOSE_GOAL, (e) ->
                    sendEachUser(e, List.of(user), MessageType.CHOOSE_GOAL_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.SET_COLOR, (e) ->
                    sendEachUser(e, List.of(user), MessageType.SET_COLOR_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.SET_STARTER, (e) ->
                    sendEachUser(e, List.of(user), MessageType.SET_STARTER_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.TURN_CHANGE, (e) ->
                    sendEachUser(e, List.of(user), MessageType.TURN_CHANGE_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.DRAW_COVERED, (e) ->
                    sendEachUser(e, List.of(user), MessageType.DRAW_COVERED_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.DRAW_VISIBLE, (e) ->
                    sendEachUser(e, List.of(user), MessageType.DRAW_VISIBLE_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.STATE_CHANGE, (e) ->
                    sendEachUser(e, List.of(user), MessageType.STATE_CHANGE_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.MATCH_COMPOSITION_CHANGE, (e) ->
                    sendEachUser(e, List.of(user), MessageType.MATCH_COMPOSITION_CHANGE_EVENT));
        }

        // Notifing all users in the lobby, except from the master that started the lobby, that the lobby
        // started.
        // Build a list of all Users in the lobby except the master
        List<User> toNotifyStart = users.stream()
                .filter(user -> !user.getUsername().equals(info.getMasterUsername()))
                .toList();

        for (User user : toNotifyStart) {
            user.send(new LobbyStartMessage());
            user.setState(new InGameState(user));
        }
    }

    /**
     * Starts a lobby in an RMI communication.
     * @param userStub The user that wants to start the lobby.
     * @throws MatchControllerException If thrown by {@link LogicMatchController#startLobby(String)}
     * @throws RemoteException If something goes wrong with the RMI communication.
     */
    public void startLobbyRMI(UserStub userStub) throws MatchControllerException, RemoteException {
        super.startLobby(userStub.getUser().getUsername());

        LobbyInfo info = getLobbyInfoByPlayerUsername(userStub.getUser().getUsername());
        GameController gameController = getMatchesMap().get(info);

        // Setting the same GameController of the match to all the users in the starting match
        List<User> users = usersFromLobbyInfo(info);
        users.forEach(u -> u.setGameController(gameController));

        // Add handlers to notify clients about events
        for (User user : users) {
            gameController.addEventHandler(user.getUsername(), GameEvents.PLACE_EVENT, (e) ->
                    pushAtEachUser(e, List.of(user), MessageType.PLACE_CARD_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.CHOOSE_GOAL, (e) ->
                    pushAtEachUser(e, List.of(user), MessageType.CHOOSE_GOAL_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.SET_COLOR, (e) ->
                    pushAtEachUser(e, List.of(user), MessageType.SET_COLOR_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.SET_STARTER, (e) ->
                    pushAtEachUser(e, List.of(user), MessageType.SET_STARTER_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.TURN_CHANGE, (e) ->
                    pushAtEachUser(e, List.of(user), MessageType.TURN_CHANGE_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.DRAW_COVERED, (e) ->
                    pushAtEachUser(e, List.of(user), MessageType.DRAW_COVERED_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.DRAW_VISIBLE, (e) ->
                    pushAtEachUser(e, List.of(user), MessageType.DRAW_VISIBLE_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.STATE_CHANGE, (e) ->
                    pushAtEachUser(e, List.of(user), MessageType.STATE_CHANGE_EVENT));
            gameController.addEventHandler(user.getUsername(), GameEvents.MATCH_COMPOSITION_CHANGE, (e) ->
                    pushAtEachUser(e, List.of(user), MessageType.MATCH_COMPOSITION_CHANGE_EVENT));
        }

        // Notifing all users in the lobby, except from the master that started the lobby, that the lobby
        // started.
        for (User user : users) {
            UserStub _userStub = user.getUserStub();
            _userStub.pushEvent(new SingleArgMessage<>(MessageType.LOBBY_START_WRAPPED, new LobbyStartEvent()));
            user.setState(new InGameState(user));
        }
    }

    /**
     * Exits a lobby.
     * @param user The user that exits the lobby.
     * @throws MatchControllerException If thrown by {@link LogicMatchController#exitLobby(String)}
     */
    public void exitLobby(User user) throws MatchControllerException {
        if (user.getUsername() != null) {
            LobbyInfo lobbyInfo = this.getLobbyInfoByPlayerUsername(user.getUsername());
            List<User> users = this.usersFromLobbyInfo(lobbyInfo);

            super.exitLobby(user.getUsername());

            // Get the list of users already in the lobby that is being joined
            users.stream()
                    // Removing myself (actually I think that I'm already missing from this list since I've left, but
                    // neverming let's filter it anyway :))
                    .filter(_user -> !_user.equals(user))
                    .forEach(_user -> sendEachUser(new MatchCompositionChangeEvent(), List.of(_user), MessageType.MATCH_COMPOSITION_CHANGE_EVENT));
        }
    }

    /**
     * Exits a lobby in an RMI communication.
     * @param user The user that exits the lobby.
     * @throws MatchControllerException If thrown by {@link LogicMatchController#exitLobby(String)}
     */
    public void exitLobbyRMI(User user) throws MatchControllerException {
        if (user.getUsername() != null) {
            // Get the list of users already in the lobby that is being joined
            LobbyInfo lobbyInfo = this.getLobbyInfoByPlayerUsername(user.getUsername());
            List<User> users = this.usersFromLobbyInfo(lobbyInfo);

            super.exitLobby(user.getUsername());

            users.stream()
                    // Removing myself
                    .filter(_user -> !_user.equals(user))
                    .forEach(_user -> pushAtEachUser(new MatchCompositionChangeEvent(), List.of(_user), MessageType.MATCH_COMPOSITION_CHANGE_EVENT));

            user.setChatController(null);
            user.setGameController(null);

            // State has to be here since the handling is out of the State override
            user.setState(new ChooseCreateJoinState(user));
        }
    }

    /**
     * Exits a match.
     * @param user The user that exits the match.
     * @throws MatchControllerException If thrown by {@link LogicMatchController#exitMatch(String)}
     */
    public void exitMatch(User user) throws MatchControllerException {
        if (user.getUsername() != null) {
            super.exitMatch(user.getUsername());
        }
    }

    /**
     * Exits a match in an RMI communication.
     * @param user The user that exits the match.
     * @throws MatchControllerException If thrown by {@link LogicMatchController#exitMatch(String)}
     */
    public void exitMatchRMI(User user) throws MatchControllerException {
        this.exitMatch(user);

        // State has to be here since the handling is out of the State override
        user.setState(new ChooseCreateJoinState(user));
    }

    /**
     * Sends a message to each user in a list.
     * @param e The event to send (encapsulated in a Message object)
     * @param users The list of users to send the message to.
     * @param type The type of the message.
     * @param <T> The type of the event.
     */
    public <T extends Event> void sendEachUser(T e, List<User> users, MessageType type) {
        for (User user : users) {
            user.send(new SingleArgMessage<>(type, e));
        }
    }

    /**
     * Pushes a message to each user in a list in an RMI communication.
     * @param e The event to push (encapsulated in a Message object)
     * @param users The list of users to send the message to.
     * @param type The type of the message.
     * @param <T> The type of the event.
     */
    public <T extends Event> void pushAtEachUser(T e, List<User> users, MessageType type) {
        try {
            for (User user : users) {
                user.getUserStub().pushEvent(new SingleArgMessage<>(type, e));
            }
        } catch (RemoteException ex) {
            // RMI communication failed. This is a problem, but should not prevent the Server from going
            // on with its activity. Since we have an RMI Ping Service that guards for the connection
            // health and prune the necessary, we are trustful that that service will handle everything.
            // So, here we won't do anything.
        }
    }
}
