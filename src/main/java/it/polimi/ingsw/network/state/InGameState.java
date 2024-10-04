package it.polimi.ingsw.network.state;

import it.polimi.ingsw.controller.GameFlow;
import it.polimi.ingsw.controller.MatchController;
import it.polimi.ingsw.controller.MatchControllerException;
import it.polimi.ingsw.logger.Logger;
import it.polimi.ingsw.model.card.GoalCard;
import it.polimi.ingsw.model.card.StarterCard;
import it.polimi.ingsw.model.card.TypedCard;
import it.polimi.ingsw.model.card.properties.CardFace;
import it.polimi.ingsw.model.game.CardType;
import it.polimi.ingsw.model.game.CommonBoard;
import it.polimi.ingsw.model.game.gamelobby.LobbyInfo;
import it.polimi.ingsw.model.player.ManuscriptPosition;
import it.polimi.ingsw.model.player.PlayerColor;
import it.polimi.ingsw.model.player.PlayerData;
import it.polimi.ingsw.model.player.PlayerManuscript;
import it.polimi.ingsw.network.liveloop.User;
import it.polimi.ingsw.network.messages.*;
import it.polimi.ingsw.network.messages.clienttoserver.GetLobbyInfoMessage;
import it.polimi.ingsw.network.messages.clienttoserver.MatchExitMessage;
import it.polimi.ingsw.network.messages.servertoclient.LobbyInfoMessage;
import it.polimi.ingsw.network.messages.servertoclient.MatchExitOKMessage;
import it.polimi.ingsw.network.messages.servertoclient.UnknownErrorMessage;

import java.util.List;

/**
 * This class represents the InGameState of the User in the Server.
 * The User is in this State when it is in a Match.
 * The Match is ongoing.
 */
public class InGameState extends State {
    public InGameState(User user) {
        super(user, StateType.INGAME);

        Logger.logInfo("Player " + user.getUsername() + " is in the InGameState");
    }

    /**
     * Handle the GetLobbyInfoMessage from the Client.
     * <ul>
     *     <li>
     *         If the operation succeeds, a LobbyInfoMessage is sent to the Client.
     *         The LobbyInfoMessage contains the information about the Lobby the User is in.
     *     </li>
     *
     *     <li>
     *         If an unexpected error occurs, an UnknownErrorMessage is sent to the Client.
     *     </li>
     * </ul>
     * @param message the GetLobbyInfoMessage received from the Client.
     */
    @Override
    public void onGetLobbyInfoMessage(GetLobbyInfoMessage message) {
        try {
            LobbyInfo lobbyInfo = MatchController.getInstance().getLobbyInfoByPlayerUsername(this.user.getUsername());
            this.user.send(new LobbyInfoMessage(lobbyInfo, this.user.getUsername()));
        } catch (MatchControllerException e) {
            this.user.send(new UnknownErrorMessage());
        }
    }

    /**
     * Handle the MatchExitMessage from the Client.
     * <ul>
     *     <li>
     *         If the operation succeeds, a MatchExitOKMessage is sent to the Client.
     *         The User is moved to the ChooseCreateJoinState.
     *         The User is no longer in the Match nor in the corresponding Lobby.
     *     </li>
     *
     *     <li>
     *         If an unexpected error occurs, an UnknownErrorMessage is sent to the Client.
     *     </li>
     * </ul>
     * @param message the MatchExitMessage received from the Client.
     */
    @Override
    public void onMatchExitMessage(MatchExitMessage message) {
        try {
            MatchController.getInstance().exitMatch(this.user);
            this.user.send(new MatchExitOKMessage());
            this.user.setState(new ChooseCreateJoinState(this.user));
        } catch (MatchControllerException e) {
            // Specific MatchControllerException cases should not happen here and we can approximate
            // that getting this exception here is unexpected
            this.user.send(new UnknownErrorMessage());
        }
    }

    /**
     * Handle the GetStarterCardMessage from the Client.
     * <ul>
     *     <li>
     *         An AnswerGetStarterCardMessage is always sent to the Client.
     *         The AnswerGetStarterCardMessage contains the StarterCard requested by the Client.
     *         The StarterCard may be null (see {@link it.polimi.ingsw.controller.GameController} specifications).
     *     </li>
     * </ul>
     * @param message the GetStarterCardMessage received from the Client.
     */
    @Override
    public void onGetStarterCardMessage(SingleArgMessage<String> message) {
        StarterCard card = user.getGameController().getStarterCard(message.get());
        user.send(new SingleArgMessage<>(MessageType.ANSWER_GET_STARTER_CARD, card));
    }

    /**
     * Handle the SetStarterCardMessage from the Client.
     * <ul>
     *     <li>
     *         An AnswerSetStarterCardMessage is always sent to the Client.
     *
     *         <ul>
     *             <li>
     *                 If the operation succeeds, the AnswerSetStarterCardMessage contains true.
     *             </li>
     *
     *             <li>
     *                 If the operation fails, the AnswerSetStarterCardMessage contains false.
     *             </li>
     *         </ul>
     *     </li>
     * </ul>
     * @param message the SetStarterCardMessage received from the Client.
     */
    @Override
    public void onSetStarterCardMessage(DoubleArgMessage<String, CardFace> message) {
        boolean result = user.getGameController().setStarterCard(message.get1(), message.get2());
        user.send(new SingleArgMessage<>(MessageType.ANSWER_SET_STARTER_CARD, result));
    }

    /**
     * Handle the GetAvailableColorsMessage from the Client.
     * <ul>
     *     <li>
     *         An AnswerGetAvailableColorsMessage is always sent to the Client.
     *         The AnswerGetAvailableColorsMessage contains the List of PlayerColor available for the User.
     *         The List may be empty (see {@link it.polimi.ingsw.controller.GameController} specifications).
     *     </li>
     * </ul>
     * @param message the GetAvailableColorsMessage received from the Client.
     */
    @Override
    public void onGetAvailableColorsMessage(ZeroArgMessage message) {
        List<PlayerColor> colors = user.getGameController().getAvailableColors();
        user.send(new SingleArgMessage<>(MessageType.ANSWER_GET_AVAILABLE_COLORS, colors));
    }

    /**
     * Handle the SetPlayerColorMessage from the Client.
     * <ul>
     *     <li>
     *         An AnswerSetPlayerColorMessage is always sent to the Client.
     *
     *         <ul>
     *             <li>
     *                 If the operation succeeds, the AnswerSetPlayerColorMessage contains true.
     *             </li>
     *
     *             <li>
     *                 If the operation fails, the AnswerSetPlayerColorMessage contains false.
     *             </li>
     *         </ul>
     *     </li>
     * </ul>
     * @param message the SetPlayerColorMessage received from the Client.
     */
    @Override
    public void onSetPlayerColorMessage(DoubleArgMessage<String, PlayerColor> message) {
        boolean result = user.getGameController().setPlayerColor(message.get1(), message.get2());
        user.send(new SingleArgMessage<>(MessageType.ANSWER_SET_PLAYER_COLOR, result));
    }

    /**
     * Handle the GetProposedPrivateGoalsMessage from the Client.
     * <ul>
     *     <li>
     *         An AnswerGetProposedPrivateGoalsMessage is always sent to the Client.
     *         The AnswerGetProposedPrivateGoalsMessage contains the List of GoalCard proposed to the User.
     *         The List may be empty (see {@link it.polimi.ingsw.controller.GameController} specifications).
     *     </li>
     * </ul>
     * @param message the GetProposedPrivateGoalsMessage received from the Client.
     */
    @Override
    public void onGetProposedPrivateGoalsMessage(SingleArgMessage<String> message) {
        List<GoalCard> goals = user.getGameController().getProposedPrivateGoals(message.get());
        user.send(new SingleArgMessage<>(MessageType.ANSWER_GET_PROPOSED_PRIVATE_GOALS, goals));
    }

    /**
     * Handle the ChoosePrivateGoalMessage from the Client.
     * <ul>
     *     <li>
     *         An AnswerChoosePrivateGoalMessage is always sent to the Client.
     *
     *         <ul>
     *             <li>
     *                 If the operation succeeds, the AnswerChoosePrivateGoalMessage contains true.
     *             </li>
     *
     *             <li>
     *                 If the operation fails, the AnswerChoosePrivateGoalMessage contains false.
     *             </li>
     *         </ul>
     *     </li>
     * </ul>
     * @param message the ChoosePrivateGoalMessage received from the Client.
     */
    @Override
    public void onChoosePrivateGoalMessage(DoubleArgMessage<String, GoalCard> message) {
        boolean result = user.getGameController().choosePrivateGoal(message.get1(), message.get2());
        user.send(new SingleArgMessage<>(MessageType.ANSWER_CHOOSE_PRIVATE_GOAL, result));
    }

    /**
     * Handle the GetPlayerDataMessage from the Client.
     * <ul>
     *     <li>
     *         An GetPlayerDataMessage is always sent to the Client.
     *         The GetPlayerDataMessage contains the PlayerData requested by the Client.
     *         The PlayerData may be null (see {@link it.polimi.ingsw.controller.GameController} specifications).
     *     </li>
     * </ul>
     * @param message the GetPlayerDataMessage received from the Client.
     */
    @Override
    public void onGetPlayerDataMessage(SingleArgMessage<String> message) {
        PlayerData playerData;
        if (message.get().equals(user.getUsername())) {
            playerData = user.getGameController().getPlayerData(message.get());
        } else {
            playerData = user.getGameController().getCleanPlayerData(message.get());
        }
        user.send(new SingleArgMessage<>(MessageType.ANSWER_GET_PLAYER_DATA, playerData));
    }

    /**
     * Handle the GetAllPlayerDataMessage from the Client.
     * <ul>
     *     <li>
     *         An AnswerGetAllPlayerDataMessage is always sent to the Client.
     *         The AnswerGetAllPlayerDataMessage contains the List of PlayerData of all players in the Match.
     *         The List may be empty (see {@link it.polimi.ingsw.controller.GameController} specifications).
     *     </li>
     * </ul>
     * @param message the GetAllPlayerDataMessage received from the Client.
     */
    @Override
    public void onGetAllPlayerDataMessage(SingleArgMessage<String> message) {
        List<PlayerData> players = user.getGameController().getAllPlayerData(message.get());
        user.send(new SingleArgMessage<>(MessageType.ANSWER_GET_ALL_PLAYER_DATA, players));
    }

    /**
     * Handle the GetManuscriptMessage from the Client.
     * <ul>
     *     <li>
     *         An AnswerGetManuscriptMessage is always sent to the Client.
     *         The AnswerGetManuscriptMessage contains the PlayerManuscript requested by the Client.
     *         The PlayerManuscript may be null (see {@link it.polimi.ingsw.controller.GameController} specifications).
     *    </li>
     * </ul>
     * @param message the GetManuscriptMessage received from the Client.
     */
    @Override
    public void onGetManuscriptMessage(SingleArgMessage<String> message) {
        PlayerManuscript manuscript = user.getGameController().getManuscript(message.get());
        user.send(new SingleArgMessage<>(MessageType.ANSWER_GET_MANUSCRIPT, manuscript));
    }

    /**
     * Handle the GetGameFlowMessage from the Client.
     * <ul>
     *     <li>
     *         An AnswerGetGameFlowMessage is always sent to the Client.
     *         The AnswerGetGameFlowMessage contains the GameFlow of the Match.
     *         The GameFlow may be null (see {@link it.polimi.ingsw.controller.GameController} specifications).
     *     </li>
     * </ul>
     * @param message the GetGameFlowMessage received from the Client.
     */
    @Override
    public void onGetGameFlowMessage(ZeroArgMessage message) {
        GameFlow flow = user.getGameController().getGameFlow();
        user.send(new SingleArgMessage<>(MessageType.ANSWER_GET_GAMEFLOW, flow));
    }

    /**
     * Handle the GetGameBoardMessage from the Client.
     * <ul>
     *     <li>
     *         An AnswerGetGameBoardMessage is always sent to the Client.
     *         The AnswerGetGameBoardMessage contains the CommonBoard of the Match.
     *         The CommonBoard may be null (see {@link it.polimi.ingsw.controller.GameController} specifications).
     *     </li>
     * </ul>
     * @param message the GetGameBoardMessage received from the Client.
     */
    @Override
    public void onGetGameBoardMessage(ZeroArgMessage message) {
        CommonBoard board = user.getGameController().getGameBoard();
        user.send(new SingleArgMessage<>(MessageType.ANSWER_GET_GAMEBOARD, board));
    }

    /**
     * Handle the PlaceCardMessage from the Client.
     * <ul>
     *     <li>
     *         An AnswerPlaceCardMessage is always sent to the Client.
     *
     *         <ul>
     *             <li>
     *                 If the operation succeeds, the AnswerPlaceCardMessage contains true.
     *             </li>
     *
     *             <li>
     *                 If the operation fails, the AnswerPlaceCardMessage contains false.
     *             </li>
     *         </ul>
     *     </li>
     * </ul>
     * @param message the PlaceCardMessage received from the Client.
     */
    @Override
    public void onPlaceCardMessage(TripleArgMessage<String, TypedCard, ManuscriptPosition> message) {
        boolean result = user.getGameController().placeCard(message.get1(), message.get2(), message.get3());
        user.send(new SingleArgMessage<>(MessageType.ANSWER_PLACE_CARD, result));
    }

    /**
     * Handle the DrawVisibleCardMessage from the Client.
     * <ul>
     *     <li>
     *         An AnswerDrawVisibleCardMessage is always sent to the Client.
     *         The AnswerDrawVisibleCardMessage contains the TypedCard drawn by the User.
     *         The TypedCard may be null (see {@link it.polimi.ingsw.controller.GameController} specifications).
     *     </li>
     * </ul>
     * @param message the DrawVisibleCardMessage received from the Client.
     */
    @Override
    public void onDrawVisibleCardMessage(TripleArgMessage<String, CardType, Integer> message) {
        TypedCard card = user.getGameController().drawVisibleCard(message.get1(), message.get2(), message.get3());
        user.send(new SingleArgMessage<>(MessageType.ANSWER_DRAW_VISIBLE_CARD, card));
    }

    /**
     * Handle the DrawCoveredCardMessage from the Client.
     * <ul>
     *     <li>
     *         An AnswerDrawCoveredCardMessage is always sent to the Client.
     *         The AnswerDrawCoveredCardMessage contains the TypedCard drawn by the User.
     *         The TypedCard may be null (see {@link it.polimi.ingsw.controller.GameController} specifications).
     *     </li>
     * </ul>
     * @param message the DrawCoveredCardMessage received from the Client.
     */
    @Override
    public void onDrawCoveredCardMessage(DoubleArgMessage<String, CardType> message) {
        TypedCard card = user.getGameController().drawCoveredCard(message.get1(), message.get2());
        user.send(new SingleArgMessage<>(MessageType.ANSWER_DRAW_COVERED_CARD, card));
    }

    /**
     * Handle the GetGameEndedMessage from the Client.
     * <ul>
     *     <li>
     *         An AnswerGetGameEndedMessage is always sent to the Client.
     *
     *         <ul>
     *             <li>
     *                 If the Game has ended, the AnswerGetGameEndedMessage contains true.
     *             </li>
     *
     *             <li>
     *                 If the Game has not ended, the AnswerGetGameEndedMessage contains false.
     *             </li>
     *         </ul>
     *     </li>
     * </ul>
     * @param message the GetGameEndedMessage received from the Client.
     */
    @Override
    public void onGetGameEndedMessage(ZeroArgMessage message) {
        boolean ended = user.getGameController().gameEnded();
        user.send(new SingleArgMessage<>(MessageType.ANSWER_GET_GAME_ENDED, ended));
    }

    /**
     * Handle the GetWinnerMessage from the Client.
     * <ul>
     *     <li>
     *         An AnswerGetWinnerMessage is always sent to the Client.
     *         The AnswerGetWinnerMessage contains the winner of the Match.
     *         The winner may be null (see {@link it.polimi.ingsw.controller.GameController} specifications).
     *     </li>
     * </ul>
     * @param message the GetWinnerMessage received from the Client.
     */
    @Override
    public void onGetWinnerMessage(ZeroArgMessage message) {
        String winner = user.getGameController().getWinner();
        user.send(new SingleArgMessage<>(MessageType.ANSWER_GET_WINNER, winner));
    }

    // Chat-related commands

    /**
     * Handle the SendBroadcastMessage from the Client.
     * <ul>
     *     <li>
     *         An AnswerSendBroadcastMessage is always sent to the Client.
     *
     *         <ul>
     *             <li>
     *                 If the operation succeeds, the outcome payload is true.
     *             </li>
     *
     *             <li>
     *                 If the operation fails, the outcome payload is false.
     *             </li>
     *         </ul>
     *     </li>
     * </ul>
     * @param message the SendBroadcastMessage received from the Client.
     */
    @Override
    public void onSendBroadcastMessage(SingleArgMessage<String> message) {
        boolean outcome = user.getChatController().sendBroadcastMessage(message.get(), user.getUsername());
        user.send(new SingleArgMessage<>(MessageType.ANSWER_SEND_BROADCAST_MESSAGE, outcome));
    }

    /**
     * Handle the SendPrivateMessage from the Client.
     * <ul>
     *     <li>
     *         An AnswerSendPrivateMessage is always sent to the Client.
     *
     *         <ul>
     *             <li>
     *                 If the operation succeeds, the outcome payload is true.
     *             </li>
     *
     *             <li>
     *                 If the operation fails, the outcome payload is false.
     *             </li>
     *         </ul>
     *     </li>
     * </ul>
     * @param message the SendPrivateMessage received from the Client.
     */
    @Override
    public void onSendPrivateMessage(DoubleArgMessage<String, String> message) {
        boolean outcome = user.getChatController().sendPrivateMessage(message.get1(), user.getUsername(), message.get2());
        user.send(new SingleArgMessage<>(MessageType.ANSWER_SEND_PRIVATE_MESSAGE, outcome));
    }

    /**
     * Handle the onGetRecipientsMessage from the Client.
     * <ul>
     *     <li>
     *         An AnswerGetRecipientsMessage is always sent to the Client.
     *         The message contains the list of Users that the User can send a private message to.
     *     </li>
     * </ul>
     * @param message the GetRecipientsMessage received from the Client.
     */
    @Override
    public void onGetRecipientsMessage(ZeroArgMessage message) {
        List<String> recipients = user
                .getChatController()
                .getChatDistributionList()
                .stream()
                .filter(s -> !s.equals(user.getUsername()))
                .toList();
        user.send(new SingleArgMessage<>(MessageType.ANSWER_GET_RECIPIENTS, recipients));
    }
}
