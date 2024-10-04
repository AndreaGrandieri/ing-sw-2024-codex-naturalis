package it.polimi.ingsw.client.network.state;

import it.polimi.ingsw.client.network.liveloop.UserOfClient;
import it.polimi.ingsw.controller.event.chat.BroadcastMesssageEvent;
import it.polimi.ingsw.controller.event.chat.PrivateMessageEvent;
import it.polimi.ingsw.controller.event.game.GameEvents;
import it.polimi.ingsw.controller.event.game.MatchCompositionChangeEvent;
import it.polimi.ingsw.controller.event.lobby.LobbyStartEvent;
import it.polimi.ingsw.network.messages.SingleArgMessage;
import it.polimi.ingsw.network.messages.servertoclient.LobbyStartMessage;

import static it.polimi.ingsw.controller.event.chat.ChatEvents.BROADCAST_MESSAGE;
import static it.polimi.ingsw.controller.event.chat.ChatEvents.PRIVATE_MESSAGE;
import static it.polimi.ingsw.controller.event.lobby.LobbyEvents.LOBBY_START;

/**
 * This class represents the InLobbyState of the User.
 * The User is in this State when it is in a Lobby.
 * The Match has not started yet.
 */
public class InLobbyStateOfClient extends StateOfClient {
    public InLobbyStateOfClient(UserOfClient userOfClient) {
        super(userOfClient);
    }

    /**
     * Handle the LobbyStartMessage from the Server.
     * The handling consists in executing the handlers of the LOBBY_START event.
     * @param message the LobbyStartMessage received from the Server.
     */
    @Override
    public void onLobbyStartEvent(LobbyStartMessage message) {
        userOfClient.getClientMC().executeHandlers(LOBBY_START, new LobbyStartEvent());
    }

    /**
     * Handle the LobbyStartEvent from the Server.
     * The handling consists in executing the handlers of the LOBBY_START event.
     * @param message the LobbyStartEvent received from the Server.
     */
    @Override
    public void onLobbyStartEventWrapped(SingleArgMessage<LobbyStartEvent> message) {
        userOfClient.getClientMC().executeHandlers(LOBBY_START, new LobbyStartEvent());
    }

    /**
     * Handle the MatchCompositionChangeEvent from the Server.
     * The handling consists in executing the handlers of the MATCH_COMPOSITION_CHANGE event.
     * @param message the MatchCompositionChangeEvent received from the Server.
     */
    @Override
    public void onMatchCompositionChangeEvent(SingleArgMessage<MatchCompositionChangeEvent> message) {
        userOfClient.getClientMC().executeHandlers(GameEvents.MATCH_COMPOSITION_CHANGE, new MatchCompositionChangeEvent());
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
