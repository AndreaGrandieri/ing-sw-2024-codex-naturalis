package it.polimi.ingsw.network.state;

import it.polimi.ingsw.model.card.GoalCard;
import it.polimi.ingsw.model.card.TypedCard;
import it.polimi.ingsw.model.card.properties.CardFace;
import it.polimi.ingsw.model.game.CardType;
import it.polimi.ingsw.model.player.ManuscriptPosition;
import it.polimi.ingsw.model.player.PlayerColor;
import it.polimi.ingsw.network.liveloop.User;
import it.polimi.ingsw.network.messages.DoubleArgMessage;
import it.polimi.ingsw.network.messages.SingleArgMessage;
import it.polimi.ingsw.network.messages.TripleArgMessage;
import it.polimi.ingsw.network.messages.ZeroArgMessage;
import it.polimi.ingsw.network.messages.clienttoserver.*;
import it.polimi.ingsw.network.messages.servertoclient.UnknownErrorMessage;

/**
 * This class represents the state of the User in the Server.
 */
abstract public class State {
    protected final User user;
    protected final StateType stateType;

    public State(User user, StateType stateType) {
        this.user = user;
        this.stateType = stateType;
    }

        public StateType getStateType() {
        return stateType;
    }

    /**
     * Answer to Client with an UnknownErrorMessage when the received message is not recognized or is incompatible
     * with the current State.
     */
    public void defaultSendUnkownError() {
        this.user.send(new UnknownErrorMessage());
    }

    /**
     * Handle the UsernameMessage from the Client.
     * @param message the UsernameMessage received from the Client.
     */
    public void onUsernameMessage(UsernameMessage message) {
        this.defaultSendUnkownError();
    }

    /**
     * Handle the LobbyInfoForCreationMessage from the Client.
     * @param message the LobbyInfoForCreationMessage received from the Client.
     */
    public void onLobbyInfoForCreationMessage(LobbyInfoForCreationMessage message) {
        this.defaultSendUnkownError();
    }

    /**
     * Handle the WhatLobbyToJoinMessage from the Client.
     * @param message the WhatLobbyToJoinMessage received from the Client.
     */
    public void onWhatLobbyToJoinMessage(WhatLobbyToJoinMessage message) {
        this.defaultSendUnkownError();
    }

    /**
     * Handle the GetListOfLobbyToJoinMessage from the Client.
     * @param message the GetListOfLobbyToJoinMessage received from the Client.
     */
    public void onGetListOfLobbyToJoinMessage(GetListOfLobbyToJoinMessage message) {
        this.defaultSendUnkownError();
    }

    /**
     * Handle the GetLobbyInfoMessage from the Client.
     * @param message the GetLobbyInfoMessage received from the Client.
     */
    public void onGetLobbyInfoMessage(GetLobbyInfoMessage message) {
        this.defaultSendUnkownError();
    }

    /**
     * Handle the StartLobbyMessage from the Client.
     * @param message the StartLobbyMessage received from the Client.
     */
    public void onStartLobbyMessage(StartLobbyMessage message) {
        this.defaultSendUnkownError();
    }

    /**
     * Handle the JoinLobbyMessage from the Client.
     * @param message the JoinLobbyMessage received from the Client.
     */
    public void onLobbyExitMessage(LobbyExitMessage message) {
        this.defaultSendUnkownError();
    }

    /**
     * Handle the MatchExitMessage from the Client.
     * @param message the MatchExitMessage received from the Client.
     */
    public void onMatchExitMessage(MatchExitMessage message) {
        this.defaultSendUnkownError();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Handle the GetStarterCardMessage from the Client.
     * @param message the GetStarterCardMessage received from the Client.
     */
    public void onGetStarterCardMessage(SingleArgMessage<String> message) {
        this.defaultSendUnkownError();
    }

    /**
     * Handle the SetStarterCardMessage from the Client.
     * @param message the SetStarterCardMessage received from the Client.
     */
    public void onSetStarterCardMessage(DoubleArgMessage<String, CardFace> message) {
        this.defaultSendUnkownError();
    }

    /**
     * Handle the GetAvailableColorsMessage from the Client.
     * @param message the GetAvailableColorsMessage received from the Client.
     */
    public void onGetAvailableColorsMessage(ZeroArgMessage message) {
        this.defaultSendUnkownError();
    }

    /**
     * Handle the SetPlayerColorMessage from the Client.
     * @param message the SetPlayerColorMessage received from the Client.
     */
    public void onSetPlayerColorMessage(DoubleArgMessage<String, PlayerColor> message) {
        this.defaultSendUnkownError();
    }

    /**
     * Handle the GetProposedPrivateGoalsMessage from the Client.
     * @param message the GetProposedPrivateGoalsMessage received from the Client.
     */
    public void onGetProposedPrivateGoalsMessage(SingleArgMessage<String> message) {
        this.defaultSendUnkownError();
    }

    /**
     * Handle the ChoosePrivateGoalMessage from the Client.
     * @param message the ChoosePrivateGoalMessage received from the Client.
     */
    public void onChoosePrivateGoalMessage(DoubleArgMessage<String, GoalCard> message) {
        this.defaultSendUnkownError();
    }

    /**
     * Handle the GetProposedPublicGoalsMessage from the Client.
     * @param message the GetProposedPublicGoalsMessage received from the Client.
     */
    public void onGetPlayerDataMessage(SingleArgMessage<String> message) {
        this.defaultSendUnkownError();
    }

    /**
     * Handle the ChoosePublicGoalMessage from the Client.
     * @param message the ChoosePublicGoalMessage received from the Client.
     */
    public void onGetAllPlayerDataMessage(SingleArgMessage<String> message) {
        this.defaultSendUnkownError();
    }

    /**
     * Handle the GetManuscriptMessage from the Client.
     * @param message the GetManuscriptMessage received from the Client.
     */
    public void onGetManuscriptMessage(SingleArgMessage<String> message) {
        this.defaultSendUnkownError();
    }

    /**
     * Handle the GetManuscriptPositionMessage from the Client.
     * @param message the GetManuscriptPositionMessage received from the Client.
     */
    public void onGetGameFlowMessage(ZeroArgMessage message) {
        this.defaultSendUnkownError();
    }

    /**
     * Handle the GetGameBoardMessage from the Client.
     * @param message the GetGameBoardMessage received from the Client.
     */
    public void onGetGameBoardMessage(ZeroArgMessage message) {
        this.defaultSendUnkownError();
    }

    /**
     * Handle the GetGameBoardMessage from the Client.
     * @param message the GetGameBoardMessage received from the Client.
     */
    public void onPlaceCardMessage(TripleArgMessage<String, TypedCard, ManuscriptPosition> message) {
        this.defaultSendUnkownError();
    }

    /**
     * Handle the GetGameBoardMessage from the Client.
     * @param message the GetGameBoardMessage received from the Client.
     */
    public void onDrawVisibleCardMessage(TripleArgMessage<String, CardType, Integer> message) {
        this.defaultSendUnkownError();
    }

    /**
     * Handle the GetGameBoardMessage from the Client.
     * @param message the GetGameBoardMessage received from the Client.
     */
    public void onDrawCoveredCardMessage(DoubleArgMessage<String, CardType> message) {
        this.defaultSendUnkownError();
    }

    /**
     * Handle the GetGameBoardMessage from the Client.
     * @param message the GetGameBoardMessage received from the Client.
     */
    public void onGetGameEndedMessage(ZeroArgMessage message) {
        this.defaultSendUnkownError();
    }

    /**
     * Handle the GetGameBoardMessage from the Client.
     * @param message the GetGameBoardMessage received from the Client.
     */
    public void onGetWinnerMessage(ZeroArgMessage message) {
        this.defaultSendUnkownError();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Handle the GetGameBoardMessage from the Client.
     * @param message the GetGameBoardMessage received from the Client.
     */
    public void onSendBroadcastMessage(SingleArgMessage<String> message) {
        this.defaultSendUnkownError();
    }

    /**
     * Handle the GetGameBoardMessage from the Client.
     * @param message the GetGameBoardMessage received from the Client.
     */
    public void onSendPrivateMessage(DoubleArgMessage<String, String> message) {
        this.defaultSendUnkownError();
    }

    /**
     * Handle the GetGameBoardMessage from the Client.
     * @param message the GetGameBoardMessage received from the Client.
     */
    public void onGetRecipientsMessage(ZeroArgMessage message) {
        this.defaultSendUnkownError();
    }
}
