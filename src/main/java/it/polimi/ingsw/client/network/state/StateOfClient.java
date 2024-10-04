package it.polimi.ingsw.client.network.state;

import it.polimi.ingsw.client.network.liveloop.UserOfClient;
import it.polimi.ingsw.controller.event.chat.BroadcastMesssageEvent;
import it.polimi.ingsw.controller.event.chat.PrivateMessageEvent;
import it.polimi.ingsw.controller.event.game.*;
import it.polimi.ingsw.controller.event.lobby.LobbyStartEvent;
import it.polimi.ingsw.network.messages.SingleArgMessage;
import it.polimi.ingsw.network.messages.servertoclient.LobbyStartMessage;

/*
IMPORTANT:
Client received a message from the Server triggering an Event in the wrong state.
This should not happen and may indicate desynchronization between the Server and the Client
states. We will handle this by doing nothing, since there's no accurate way to recover from this.
This should never happen and its presence may indicate something really wrong is happening.

Refereed in this file with (*).
 */

/**
 * This class represents the state of the User.
 */
abstract public class StateOfClient {
    protected final UserOfClient userOfClient;

    public StateOfClient(UserOfClient userOfClient) {
        this.userOfClient = userOfClient;
    }

    /**
     * Handle the LobbyStartMessage from the Server.
     * @param message the LobbyStartMessage received from the Server.
     */
    public void onLobbyStartEvent(LobbyStartMessage message) {
        // (*)
    }

    // FOR RMI USE

    /**
     * Handle the LobbyStartEvent from the Server.
     * @param message the LobbyStartEvent received from the Server.
     */
    public void onLobbyStartEventWrapped(SingleArgMessage<LobbyStartEvent> message) {
        // (*)
    }

    /**
     * Handle the MatchCompositionChangeEvent from the Server.
     * @param message the MatchCompositionChangeEvent received from the Server.
     */
    public void onMatchCompositionChangeEvent(SingleArgMessage<MatchCompositionChangeEvent> message) {
        // (*)
    }

    /**
     * Handle the ChooseGoalEvent from the Server.
     * @param message the ChooseGoalEvent received from the Server.
     */
    public void onChooseGoalEvent(SingleArgMessage<ChooseGoalEvent> message) {
        // (*)
    }

    /**
     * Handle the DrawCoveredEvent from the Server.
     * @param message the DrawCoveredEvent received from the Server.
     */
    public void onDrawCoveredEvent(SingleArgMessage<DrawCoveredEvent> message) {
        // (*)
    }

    /**
     * Handle the DrawVisibleEvent from the Server.
     * @param message the DrawVisibleEvent received from the Server.
     */
    public void onDrawVisibleEvent(SingleArgMessage<DrawVisibleEvent> message) {
        // (*)
    }

    /**
     * Handle the PlaceCardEvent from the Server.
     * @param message the PlaceCardEvent received from the Server.
     */
    public void onPlaceCardEvent(SingleArgMessage<PlaceCardEvent> message) {
        // (*)
    }

    /**
     * Handle the SetColorEvent from the Server.
     * @param message the SetColorEvent received from the Server.
     */
    public void onSetColorEvent(SingleArgMessage<SetColorEvent> message) {
        // (*)
    }

    /**
     * Handle the SetStarterEvent from the Server.
     * @param message the SetStarterEvent received from the Server.
     */
    public void onSetStarterEvent(SingleArgMessage<SetStarterEvent> message) {
        // (*)
    }

    /**
     * Handle the StateChangeEvent from the Server.
     * @param message the StateChangeEvent received from the Server.
     */
    public void onStateChangeEvent(SingleArgMessage<StateChangeEvent> message) {
        // (*)
    }

    /**
     * Handle the TurnChangeEvent from the Server.
     * @param message the TurnChangeEvent received from the Server.
     */
    public void onTurnChangeEvent(SingleArgMessage<TurnChangeEvent> message) {
        // (*)
    }

    // Chat-related events

    /**
     * Handle the BroadcastMesssageEvent from the Server.
     * @param message the BroadcastMesssageEvent received from the Server.
     */
    public void onBroadcastMessageEvent(SingleArgMessage<BroadcastMesssageEvent> message) {
        // (*)
    }

    /**
     * Handle the PrivateMessageEvent from the Server.
     * @param message the PrivateMessageEvent received from the Server.
     */
    public void onPrivateMessageEvent(SingleArgMessage<PrivateMessageEvent> message) {
        // (*)
    }
}
