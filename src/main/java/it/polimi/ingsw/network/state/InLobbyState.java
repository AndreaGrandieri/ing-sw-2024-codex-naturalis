package it.polimi.ingsw.network.state;

import it.polimi.ingsw.controller.MatchController;
import it.polimi.ingsw.controller.MatchControllerException;
import it.polimi.ingsw.logger.Logger;
import it.polimi.ingsw.model.game.gamelobby.LobbyInfo;
import it.polimi.ingsw.network.liveloop.User;
import it.polimi.ingsw.network.messages.DoubleArgMessage;
import it.polimi.ingsw.network.messages.MessageType;
import it.polimi.ingsw.network.messages.SingleArgMessage;
import it.polimi.ingsw.network.messages.ZeroArgMessage;
import it.polimi.ingsw.network.messages.clienttoserver.GetLobbyInfoMessage;
import it.polimi.ingsw.network.messages.clienttoserver.LobbyExitMessage;
import it.polimi.ingsw.network.messages.clienttoserver.StartLobbyMessage;
import it.polimi.ingsw.network.messages.servertoclient.*;

import java.util.List;

/**
 * This class represents the InLobbyState of the User in the Server.
 * The User is in this State when it is in a Lobby.
 * The Match has not started yet.
 */
public class InLobbyState extends State {
    public InLobbyState(User user) {
        super(user, StateType.INLOBBY);

        Logger.logInfo("Player " + user.getUsername() + " is in the InLobbyState");
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
     * Handle the StartLobbyMessage from the Client.
     * <ul>
     *     <li>
     *         If the operation succeeds, a LobbyStartOKMessage is sent to the Client.
     *         The User is moved to the InGameState.
     *         The Match is started.
     *     </li>
     *
     *     <li>
     *         If the operation fails, a LobbyStartKOMessage is sent to the Client.
     *         The operation may fail for different reasons:
     *         <ul>
     *             <li>
     *                 There is no more space for new Matches.
     *             </li>
     *
     *             <li>
     *                 The User is not the Master of the Lobby.
     *             </li>
     *
     *             <li>
     *                 The state of the lobby is not eligible for starting a Match.
     *             </li>
     *         </ul>
     *
     *     <li>
     *         If an unexpected error occurs, an UnknownErrorMessage is sent to the Client.
     *     </li>
     * </ul>
     * @param message the StartLobbyMessage received from the Client.
     */
    @Override
    public void onStartLobbyMessage(StartLobbyMessage message) {
        try {
            MatchController.getInstance().startLobby(this.user.getUsername());
            this.user.send(new LobbyStartOKMessage());
            this.user.setState(new InGameState(this.user));
        } catch (MatchControllerException e) {
            switch (e.getReason()) {
                case NO_MORE_SPACE_FOR_NEW_MATCHES,
                        START_ATTEMPT_FROM_NON_MASTER,
                        BAD_STATE_FOR_START -> this.user.send(new LobbyStartKOMessage());
                default -> this.user.send(new UnknownErrorMessage());
            }
        }
    }

    /**
     * Handle the LobbyExitMessage from the Client.
     * <ul>
     *     <li>
     *         If the operation succeeds, a LobbyExitOKMessage is sent to the Client.
     *         The User is moved to the ChooseCreateJoinState.
     *         The User is removed from the Lobby.
     *     </li>
     *
     *     <li>
     *         If an unexpected error occurs, an UnknownErrorMessage is sent to the Client.
     *     </li>
     * </ul>
     * @param message the LobbyExitMessage received from the Client.
     */
    @Override
    public void onLobbyExitMessage(LobbyExitMessage message) {
        try {
            MatchController.getInstance().exitLobby(this.user);
            this.user.send(new LobbyExitOKMessage());

            this.user.setChatController(null);
            this.user.setGameController(null);

            this.user.setState(new ChooseCreateJoinState(this.user));
        } catch (MatchControllerException e) {
            // Specific MatchControllerException cases should not happen here and we can approximate
            // that getting this exception here is unexpected
            this.user.send(new UnknownErrorMessage());
        }
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
