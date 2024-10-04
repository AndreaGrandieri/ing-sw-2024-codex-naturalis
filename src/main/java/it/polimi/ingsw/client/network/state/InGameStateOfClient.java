package it.polimi.ingsw.client.network.state;

import it.polimi.ingsw.client.network.liveloop.UserOfClient;
import it.polimi.ingsw.controller.event.chat.BroadcastMesssageEvent;
import it.polimi.ingsw.controller.event.chat.PrivateMessageEvent;
import it.polimi.ingsw.controller.event.game.*;
import it.polimi.ingsw.network.messages.SingleArgMessage;

import static it.polimi.ingsw.controller.event.chat.ChatEvents.BROADCAST_MESSAGE;
import static it.polimi.ingsw.controller.event.chat.ChatEvents.PRIVATE_MESSAGE;
import static it.polimi.ingsw.controller.event.game.GameEvents.*;

/*
IMPORTANT:
Client received a message from the Server triggering an Event in the correct state, but the GameController
obtained from userOfClient was null. The GameController obtained from userOfClient in this state on its methods
triggered by messages should never be null.
This may indicate state desynchronization in the Client (should not be the Server fault).
We will handle this by doing nothing, since there's no accurate way to recover from this.
This should never happen and its presence may indicate something really wrong is happening.

Refereed in this file with (*).
 */

/**
 * This class represents the InGameState of the User.
 * The User is in this State when it is in a Match.
 * The Match is ongoing.
 */
public class InGameStateOfClient extends StateOfClient {
    public InGameStateOfClient(UserOfClient userOfClient) {
        super(userOfClient);
    }

    /**
     * Handle the MatchCompositionChangeEvent from the Server.
     * The handling consists in executing the handlers of the MATCH_COMPOSITION_CHANGE event.
     * @param message the MatchCompositionChangeEvent received from the Server.
     */
    @Override
    public void onMatchCompositionChangeEvent(SingleArgMessage<MatchCompositionChangeEvent> message) {
        userOfClient.getClientGC().executeHandlers(GameEvents.MATCH_COMPOSITION_CHANGE, new MatchCompositionChangeEvent());
    }

    /**
     * Handle the ChooseGoalEvent from the Server.
     * The handling consists in executing the handlers of the CHOOSE_GOAL event.
     * @param message the ChooseGoalEvent received from the Server.
     */
    @Override
    public void onChooseGoalEvent(SingleArgMessage<ChooseGoalEvent> message) {
        try {
            userOfClient.getClientGC().executeHandlers(CHOOSE_GOAL, message.get());
        } catch (NullPointerException ignored) {
            // (*)
        }
    }

    /**
     * Handle the DrawCoveredEvent from the Server.
     * The handling consists in executing the handlers of the DRAW_COVERED event.
     * @param message the DrawCoveredEvent received from the Server.
     */
    @Override
    public void onDrawCoveredEvent(SingleArgMessage<DrawCoveredEvent> message) {
        try {
            userOfClient.getClientGC().executeHandlers(DRAW_COVERED, message.get());
        } catch (NullPointerException ignored) {
            // (*)
        }
    }

    /**
     * Handle the DrawVisibleEvent from the Server.
     * The handling consists in executing the handlers of the DRAW_VISIBLE event.
     * @param message the DrawVisibleEvent received from the Server.
     */
    @Override
    public void onDrawVisibleEvent(SingleArgMessage<DrawVisibleEvent> message) {
        try {
            userOfClient.getClientGC().executeHandlers(DRAW_VISIBLE, message.get());
        } catch (NullPointerException ignored) {
            // (*)
        }
    }

    /**
     * Handle the PlaceCardEvent from the Server.
     * The handling consists in executing the handlers of the PLACE_EVENT event.
     * @param message the PlaceCardEvent received from the Server.
     */
    @Override
    public void onPlaceCardEvent(SingleArgMessage<PlaceCardEvent> message) {
        try {
            userOfClient.getClientGC().executeHandlers(PLACE_EVENT, message.get());
        } catch (NullPointerException ignored) {
            // (*)
        }
    }

    /**
     * Handle the SetColorEvent from the Server.
     * The handling consists in executing the handlers of the SET_COLOR event.
     * @param message the SetColorEvent received from the Server.
     */
    @Override
    public void onSetColorEvent(SingleArgMessage<SetColorEvent> message) {
        try {
            userOfClient.getClientGC().executeHandlers(SET_COLOR, message.get());
        } catch (NullPointerException ignored) {
            // (*)
        }
    }

    /**
     * Handle the SetStarterEvent from the Server.
     * The handling consists in executing the handlers of the SET_STARTER event.
     * @param message the SetStarterEvent received from the Server.
     */
    @Override
    public void onSetStarterEvent(SingleArgMessage<SetStarterEvent> message) {
        try {
            userOfClient.getClientGC().executeHandlers(SET_STARTER, message.get());
        } catch (NullPointerException ignored) {
            // (*)
        }
    }

    /**
     * Handle the StateChangeEvent from the Server.
     * The handling consists in executing the handlers of the STATE_CHANGE event.
     * @param message the StateChangeEvent received from the Server.
     */
    @Override
    public void onStateChangeEvent(SingleArgMessage<StateChangeEvent> message) {
        try {
            userOfClient.getClientGC().executeHandlers(STATE_CHANGE, message.get());
        } catch (NullPointerException ignored) {
            // (*)
        }
    }

    /**
     * Handle the TurnChangeEvent from the Server.
     * The handling consists in executing the handlers of the TURN_CHANGE event.
     * @param message the TurnChangeEvent received from the Server.
     */
    @Override
    public void onTurnChangeEvent(SingleArgMessage<TurnChangeEvent> message) {
        try {
            userOfClient.getClientGC().executeHandlers(TURN_CHANGE, message.get());
        } catch (NullPointerException ignored) {
            // (*)
        }
    }

    // Chat-related events

    /**
     * Handle the BroadcastMesssageEvent from the Server.
     * The handling consists in executing the handlers of the BROADCAST_MESSAGE event.
     * @param message the BroadcastMesssageEvent received from the Server.
     */
    @Override
    public void onBroadcastMessageEvent(SingleArgMessage<BroadcastMesssageEvent> message) {
        try {
            userOfClient.getClientCC().executeHandlers(BROADCAST_MESSAGE, message.get());
        } catch (NullPointerException ignored) {
            // (*)
        }
    }

    /**
     * Handle the PrivateMessageEvent from the Server.
     * The handling consists in executing the handlers of the PRIVATE_MESSAGE event.
     * @param message the PrivateMessageEvent received from the Server.
     */
    @Override
    public void onPrivateMessageEvent(SingleArgMessage<PrivateMessageEvent> message) {
        try {
            userOfClient.getClientCC().executeHandlers(PRIVATE_MESSAGE, message.get());
        } catch (NullPointerException ignored) {
            // (*)
        }
    }
}
