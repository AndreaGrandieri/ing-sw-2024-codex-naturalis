package it.polimi.ingsw.network.liveloop;

import it.polimi.ingsw.controller.ChatController;
import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.logger.Logger;
import it.polimi.ingsw.network.Profiles;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.clienttoserver.*;
import it.polimi.ingsw.network.rmi.UserStub;
import it.polimi.ingsw.network.state.SettingUsernameState;
import it.polimi.ingsw.network.state.State;
import it.polimi.ingsw.network.tcpip.InvalidTCPConnectionException;
import it.polimi.ingsw.network.tcpip.Server;
import it.polimi.ingsw.network.tcpip.ServerException;
import it.polimi.ingsw.util.TextValidator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static it.polimi.ingsw.network.messages.util.Casting.*;

/**
 * Represents a User in the Server.
 */
@SuppressWarnings("unchecked")
public class User {
    private final String connectionUUID;
    private final UserStub userStub;
    private State state;
    private final ListenLoop listenLoop;
    private String username;
    private GameController gc;
    private ChatController cc;

    /**
     * Creates a User.
     * @param connectionUUID The connection UUID of the User.
     * @param RMI Whether the User is using RMI or not.
     * @param userStub The UserStub of the User, if the User is using RMI, null otherwise.
     */
    public User(String connectionUUID, Boolean RMI, UserStub userStub) {
        this.connectionUUID = connectionUUID;

        if (!RMI) {
            this.listenLoop = new ListenLoop(connectionUUID, 0);
            this.listenLoop.start();
            this.userStub = null;
        } else {
            this.userStub = userStub;
            this.listenLoop = null;
        }

        this.username = null;
        this.state = new SettingUsernameState(this);

        this.cc = null;
        this.gc = null;
    }

        public String getConnectionUUID() {
        return this.connectionUUID;
    }

        public UserStub getUserStub() {
        return userStub;
    }

    public void setState(State state) {
        this.state = state;
    }

        public State getState() {
        return state;
    }

    /**
     * Sets the username of the User.
     * @param username The username to set.
     * @throws UserException If the username is invalid.
     */
    public void setUsername(String username) throws UserException {
        // Checks for the validity of the username
        Pattern pattern = Pattern.compile(TextValidator.usernameValidator);
        Matcher matcher = pattern.matcher(username);
        if (!matcher.matches()) {
            throw new UserException("Username is invalid.", UserException.Reason.INVALID_USERNAME);
        }

        this.username = username;
    }

        public GameController getGameController() {
        return this.gc;
    }

    public void setGameController(GameController gc) {
        this.gc = gc;
    }

        public ChatController getChatController() {
        return this.cc;
    }

    public void setChatController(ChatController cc) {
        this.cc = cc;
    }

        public String getUsername() {
        return this.username;
    }

    /**
     * Sends a message to the User using the network.
     * @param message The message to send.
     */
    public void send(Message message) {
        try {
            Server.getInstance().sendSerializableObject(this.connectionUUID, message);
        } catch (ServerException | InvalidTCPConnectionException e) {
            // What does it mean if the server throws an exception here?
            // Basically it means that the server is not able to send the message to the client.
            // We can infer that the client is not reachable anymore, so the procedures to safely deallocate
            // the resources associated with this user should be started.

            // ServerException | InvalidTCPConnectionException is thrown when the connection is closed (gracefully or not). This is a
            // critical error. The User is not reachable anymore. Safely deallocate User resources and stop the loop.

            Profiles.getInstance().silentPruneUser(this.connectionUUID, false);

            // Any other resource that may refer to the User (somehow) and use it will encounter exceptions and some exception
            // handling just like this one will prune the (this) User (prune chaining phylosophy).
        }

        Logger.logInfo("Replied to user " + this.connectionUUID.substring(0, 3) + " with message " + message.getType());
    }

    /**
     * Reacts to messages received from the Client (User).
     * The reaction is dynamic and depends on the current state of the User.
     * @param message The message to react to.
     */
    public void react(Message message) {
        try {
            switch (message.getType()) {
                case USERNAME -> this.state.onUsernameMessage((UsernameMessage) message);
                case LOBBY_INFO_FOR_CREATION -> this.state.onLobbyInfoForCreationMessage((LobbyInfoForCreationMessage) message);
                case WHAT_LOBBY_TO_JOIN -> this.state.onWhatLobbyToJoinMessage((WhatLobbyToJoinMessage) message);
                case START_LOBBY -> this.state.onStartLobbyMessage((StartLobbyMessage) message);
                case GET_LIST_OF_LOBBY_TO_JOIN -> this.state.onGetListOfLobbyToJoinMessage((GetListOfLobbyToJoinMessage) message);
                case GET_LOBBY_INFO_MESSAGE -> this.state.onGetLobbyInfoMessage((GetLobbyInfoMessage) message);
                case LOBBY_EXIT -> this.state.onLobbyExitMessage((LobbyExitMessage) message);
                case MATCH_EXIT -> this.state.onMatchExitMessage((MatchExitMessage) message);

                /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                case GET_STARTER_CARD -> this.state.onGetStarterCardMessage(singleArgCast(message));
                case SET_STARTER_CARD -> this.state.onSetStarterCardMessage(doubleArgCast(message));
                case GET_AVAILABLE_COLORS -> this.state.onGetAvailableColorsMessage(zeroArgCast(message));
                case SET_PLAYER_COLOR -> this.state.onSetPlayerColorMessage(doubleArgCast(message));
                case GET_PROPOSED_PRIVATE_GOALS -> this.state.onGetProposedPrivateGoalsMessage(singleArgCast(message));
                case CHOOSE_PRIVATE_GOAL -> this.state.onChoosePrivateGoalMessage(doubleArgCast(message));
                case GET_PLAYER_DATA -> this.state.onGetPlayerDataMessage(singleArgCast(message));
                case GET_ALL_PLAYER_DATA -> this.state.onGetAllPlayerDataMessage(singleArgCast(message));
                case GET_MANUSCRIPT -> this.state.onGetManuscriptMessage(singleArgCast(message));
                case GET_GAMEFLOW -> this.state.onGetGameFlowMessage(zeroArgCast(message));
                case GET_GAMEBOARD -> this.state.onGetGameBoardMessage(zeroArgCast(message));
                case PLACE_CARD -> this.state.onPlaceCardMessage(tripleArgCast(message));
                case DRAW_VISIBLE_CARD -> this.state.onDrawVisibleCardMessage(tripleArgCast(message));
                case DRAW_COVERED_CARD -> this.state.onDrawCoveredCardMessage(doubleArgCast(message));
                case GET_GAME_ENDED -> this.state.onGetGameEndedMessage(zeroArgCast(message));
                case GET_WINNER -> this.state.onGetWinnerMessage(zeroArgCast(message));

                /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                case SEND_BROADCAST_MESSAGE -> this.state.onSendBroadcastMessage(singleArgCast(message));
                case SEND_PRIVATE_MESSAGE -> this.state.onSendPrivateMessage(doubleArgCast(message));
                case GET_RECIPIENTS -> this.state.onGetRecipientsMessage(zeroArgCast(message));

                /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                case LOBBY_INFO,
                        LOBBY_JOIN_OK,
                        LIST_OF_LOBBY_TO_JOIN,
                        LOBBY_START_OK,
                        USERNAME_ALREADY_TAKEN,
                        USERNAME_CONFIRMED,
                        INVALID_LOBBY_INFO_FOR_CREATION,
                        LOBBY_ALREADY_FULL,
                        USERNAME_NOT_VALID,
                        NO_MORE_SPACE_FOR_NEW_LOBBIES,
                        LOBBY_START_KO,
                        LOBBY_EXIT_OK,
                        MATCH_EXIT_OK,
                        UNKNOWN_ERROR,
                        LOBBY_START -> {
                    // All these messages are sent by the server to the client, so they should not be handled.
                    // They should be handled by the client.

                    // Handle: reply with UknownErrorMessage
                    this.state.defaultSendUnkownError();
                }
            }
        } catch (RuntimeException e) {
            // This handles any RuntimeException that may have not been catched (due to the unchecked nature
            // of the exception...)
            Logger.logInfo("Caught a RuntimeException in react, probably from GameController (" + e.getMessage() + ")");
            this.state.defaultSendUnkownError();
        }
    }
}
