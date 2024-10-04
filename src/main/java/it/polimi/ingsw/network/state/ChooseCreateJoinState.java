package it.polimi.ingsw.network.state;

import it.polimi.ingsw.controller.MatchController;
import it.polimi.ingsw.controller.MatchControllerException;
import it.polimi.ingsw.logger.Logger;
import it.polimi.ingsw.model.game.gamelobby.GameLobbyException;
import it.polimi.ingsw.network.liveloop.User;
import it.polimi.ingsw.network.messages.clienttoserver.GetListOfLobbyToJoinMessage;
import it.polimi.ingsw.network.messages.clienttoserver.LobbyInfoForCreationMessage;
import it.polimi.ingsw.network.messages.clienttoserver.WhatLobbyToJoinMessage;
import it.polimi.ingsw.network.messages.servertoclient.*;

/**
 * This class represents the ChooseCreateJoinState of the User in the Server.
 * The User is in this State when it is choosing whether to create or join a Lobby.
 */
public class ChooseCreateJoinState extends State {
    public ChooseCreateJoinState(User user) {
        super(user, StateType.CHOOSECREATEJOIN);

        Logger.logInfo("User " + user.getConnectionUUID().substring(0, 3) + " is in the ChooseCreateJoinState.");
    }

    /**
     * Handle the LobbyInfoForCreationMessage from the Client.
     * <ul>
     *     <li>
     *         If the LobbyInfoForCreationMessage is valid, a LobbyJoinOKMessage is sent to the Client and the User is moved to the InLobbyState.
     *         The Lobby is created and the User is added to it.
     *     </li>
     *
     *     <li>
     *         If the LobbyInfoForCreationMessage is invalid, an InvalidLobbyInfoForCreationMessage is sent to the Client.
     *     </li>
     *
     *     <li>
     *         If there is no more space for new Lobbies, a NoMoreSpaceForNewLobbiesMessage is sent to the Client.
     *     </li>
     *
     *     <li>
     *         If the Lobby is already full, a LobbyAlreadyFullMessage is sent to the Client.
     *     </li>
     *
     *     <li>
     *         If an unexpected error occurs, an UnknownErrorMessage is sent to the Client.
     *     </li>
     * </ul>
     * @param message the LobbyInfoForCreationMessage received from the Client.
     */
    @Override
    public void onLobbyInfoForCreationMessage(LobbyInfoForCreationMessage message) {
        try {
            MatchController.getInstance().createLobby(message.getLobbyName(), message.getMaxPlayers(), this.user);

            this.user.send(new LobbyJoinOKMessage());
            this.user.setState(new InLobbyState(this.user));
        } catch (GameLobbyException e) {
            switch (e.getReason()) {
                case LOBBY_ALREADY_FULL_EXCEPTION -> this.user.send(new LobbyAlreadyFullMessage());
                case INVALID_LOBBY_INFO_FOR_CREATION -> this.user.send(new InvalidLobbyInfoForCreationMessage());
                case NO_MORE_SPACE_FOR_NEW_LOBBIES -> this.user.send(new NoMoreSpaceForNewLobbiesMessage());
            }
        } catch (MatchControllerException e) {
            // Specific MatchControllerException cases should not happen here and we can approximate
            // that getting this exception here is unexpected
            this.user.send(new UnknownErrorMessage());
        }
    }

    /**
     * Handle the WhatLobbyToJoinMessage from the Client.
     * <ul>
     *     <li>
     *         If the User successfully joins the Lobby, a LobbyJoinOKMessage is sent to the Client.
     *         The User is added to the Lobby and moved to the InLobbyState.
     *     </li>
     *
     *     <li>
     *         If the User successfully joins the Lobby and the Match is ongoing, a LobbyJoinOKMessage is sent to the Client,
     *         followed by a LobbyStartMessage.
     *         The User is added to the Lobby, to the Match and finally moved to the InGameState.
     *     </li>
     *
     *     <li>
     *         If the Lobby is already full, a LobbyAlreadyFullMessage is sent to the Client.
     *     </li>
     *
     *     <li>
     *         If the User attempts to join a lobby in a State not eligible for joining, an UnknownErrorMessage is sent to the Client.
     *     </li>
     *
     *     <li>
     *         If an unexpected error occurs, an UnknownErrorMessage is sent to the Client.
     *     </li>
     * </ul>
     * @param message the WhatLobbyToJoinMessage received from the Client.
     */
    @Override
    public void onWhatLobbyToJoinMessage(WhatLobbyToJoinMessage message) {
        try {
            Boolean isOngoingMatch = MatchController.getInstance().joinLobby(message.getLobbyUUID(), this.user);

            if (isOngoingMatch) {
                System.out.println("HERE");

                this.user.send(new LobbyJoinOKMessage());
                this.user.send(new LobbyStartMessage());
                this.user.setState(new InGameState(this.user));
                user.getGameController().reconnectPlayer(this.user.getUsername());
            } else {
                this.user.send(new LobbyJoinOKMessage());
                this.user.setState(new InLobbyState(this.user));
            }
        } catch (GameLobbyException e) {
            if (e.getReason() == GameLobbyException.Reason.LOBBY_ALREADY_FULL_EXCEPTION) {
                this.user.send(new LobbyAlreadyFullMessage());
            } else if (e.getReason() == GameLobbyException.Reason.INVALID_JOIN_ATTEMPT) {
                // This specific GameLobbyException case is pretty strange and should not happen. We can approximate
                // that getting this exception here is unexpected and the client may be doing something shady.
                // Just reply with an UnknownErrorMessage.
                this.user.send(new UnknownErrorMessage());
            }

            // Other cases CANNOT happen, so no handling
        } catch (MatchControllerException e) {
            // Specific MatchControllerException cases should not happen here and we can approximate
            // that getting this exception here is unexpected
            this.user.send(new UnknownErrorMessage());
        }
    }

    /**
     * Handle the GetListOfLobbyToJoinMessage from the Client.
     * <ul>
     *     <li>
     *         If the operation succeeds, a ListOfLobbyToJoinMessage is sent to the Client.
     *         The message contains the list of Lobbies that the User can join.
     *     </li>
     *
     *     <li>
     *         If an unexpected error occurs, an UnknownErrorMessage is sent to the Client.
     *     </li>
     * </ul>
     * @param message the GetListOfLobbyToJoinMessage received from the Client.
     */
    @Override
    public void onGetListOfLobbyToJoinMessage(GetListOfLobbyToJoinMessage message) {
        try {
            this.user.send(new ListOfLobbyToJoinMessage(this.user.getUsername()));
        } catch (MatchControllerException e) {
            // Specific MatchControllerException cases should not happen here and we can approximate
            // that getting this exception here is unexpected
            this.user.send(new UnknownErrorMessage());
        }
    }
}
