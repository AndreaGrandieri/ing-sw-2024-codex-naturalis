package it.polimi.ingsw.client;

import it.polimi.ingsw.client.network.liveloop.UserOfClient;
import it.polimi.ingsw.client.network.rmi.DefaultRMIExceptionsHandlerOfClient;
import it.polimi.ingsw.client.network.state.ChooseCreateJoinStateOfClient;
import it.polimi.ingsw.client.network.state.InGameStateOfClient;
import it.polimi.ingsw.client.network.state.InLobbyStateOfClient;
import it.polimi.ingsw.client.network.state.StateOfClient;
import it.polimi.ingsw.controller.EventHandler;
import it.polimi.ingsw.controller.event.Event;
import it.polimi.ingsw.controller.event.EventType;
import it.polimi.ingsw.controller.interfaces.ClientMatchController;
import it.polimi.ingsw.model.game.gamelobby.GameLobbyException;
import it.polimi.ingsw.model.game.gamelobby.LobbyInfo;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.clienttoserver.*;
import it.polimi.ingsw.network.messages.servertoclient.ListOfLobbyToJoinMessage;
import it.polimi.ingsw.network.messages.servertoclient.LobbyInfoMessage;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static it.polimi.ingsw.controller.event.lobby.LobbyEvents.LOBBY_START;
import static it.polimi.ingsw.network.messages.MessageType.*;

/**
 * This class is the implementation of the {@link ClientMatchController} interface for the network client.
 */
public class NetworkClientMatchController implements ClientMatchController {
    private final UserOfClient userOfClient;
    private final Map<EventType<? extends Event>, List<EventHandler<? extends Event>>> eventHandlers = new HashMap<>();
    private final Boolean RMI;

    /**
     * Constructor for the class.
     * Important event handlers are registered here.
     * @param userOfClient the user of the client
     * @param RMI whether the connection is RMI or not
     */
    public NetworkClientMatchController(UserOfClient userOfClient, Boolean RMI) {
        this.userOfClient = userOfClient;
        this.RMI = RMI;

        // When receiving notification of game start, we change our state into playing state
        addEventHandler(LOBBY_START, (e) -> userOfClient.setState(new InGameStateOfClient(userOfClient)));
    }

    /**
     * Adds the given {@link Consumer} to {@code listeners} of the specified {@link EventType}
     *
     * @param type     type of event to which add the given {@code Consumer}
     * @param consumer {@code Consumer} to add
     */
    public <T extends Event> void addEventHandler(EventType<T> type, Consumer<T> consumer) {
        eventHandlers.computeIfAbsent(type, k -> new ArrayList<>());
        eventHandlers.get(type).add(new EventHandler<>(consumer, null));
    }

    /**
     * Removes the given {@link Consumer} to {@code listeners} of the specified {@link EventType}
     *
     * @param type     type of event from which remove the given {@code Consumer}
     * @param consumer {@code Consumer} to remove
     */
    public <T extends Event> void removeEventHandler(EventType<T> type, Consumer<T> consumer) {
        List<EventHandler<? extends Event>> presentConsumers = eventHandlers.get(type);
        List<EventHandler<? extends Event>> toDelete = presentConsumers
                .stream()
                .filter(h -> h.handler().equals(consumer)).toList();
        presentConsumers.removeAll(toDelete);
    }

    private <T extends Event> List<Consumer<T>> getEventHandlers(EventType<T> type) {
        List<EventHandler<?>> presentConsumers = eventHandlers.get(type);
        if (presentConsumers != null) {
            return presentConsumers
                    .stream()
                    .map(h -> (EventHandler<T>) h)
                    .map(EventHandler::handler)
                    .toList();
        }
        return new ArrayList<>();
    }

    public <T extends Event> void executeHandlers(EventType<T> type, T info) {
        getEventHandlers(type).forEach(consumer -> {
            (new Thread(() -> consumer.accept(info))).start();
//                consumer.accept(info);
        });
    }

    @Override
    public String getMyUsername() {
        return userOfClient.getClientCC().getMyUsername();
    }

    /**
     * Asks the Server to create a new lobby with the provided information.
     * @param name the name of the lobby
     * @param maxPlayers the maximum number of players in the lobby
     * @return True if the lobby was created successfully, false otherwise
     */
    public synchronized boolean createLobby(String name, int maxPlayers) {
        StateOfClient oldState = this.userOfClient.getState();
        userOfClient.setState(new InLobbyStateOfClient(userOfClient));

        if (!this.RMI) {
            LobbyInfoForCreationMessage message = new LobbyInfoForCreationMessage(name, maxPlayers);
            Message answer = userOfClient.sendAndWaitMultiple(message, List.of(
                    LOBBY_JOIN_OK,
                    LOBBY_ALREADY_FULL,
                    INVALID_LOBBY_INFO_FOR_CREATION,
                    NO_MORE_SPACE_FOR_NEW_LOBBIES,
                    UNKNOWN_ERROR));

            boolean ok = answer.getType() == LOBBY_JOIN_OK;
            if (!ok) {
                this.userOfClient.setState(oldState);
            }

            return ok;
        } else {
            try {
                boolean ok = this.userOfClient.getUserStub().createLobbyRMI(name, maxPlayers);

                if (!ok) {
                    this.userOfClient.setState(oldState);
                }

                return ok;
            } catch (RemoteException e) {
                throw DefaultRMIExceptionsHandlerOfClient.clientCriticalError;
            }
        }
    }

    /**
     * Asks the Server for the list of lobbies that can be joined.
     * @return the list of lobbies that can be joined. This can be null (see: {@link it.polimi.ingsw.controller.MatchController} specifications).
     */
        public synchronized List<LobbyInfo> getLobbies() {
        List<ListOfLobbyToJoinMessage.LobbyInfo> lobbies;
        if (!this.RMI) {
            GetListOfLobbyToJoinMessage message = new GetListOfLobbyToJoinMessage();
            Message answer = userOfClient.sendAndWaitMultiple(message, List.of(
                    LIST_OF_LOBBY_TO_JOIN,
                    UNKNOWN_ERROR));

            if (answer.getType() == UNKNOWN_ERROR) {
                lobbies = null;
            } else {
                lobbies = ((ListOfLobbyToJoinMessage) answer).getLobbies();
            }
        } else {
            try {
                lobbies = this.userOfClient.getUserStub().getListOfLobbyToJoinRMI();
            } catch (RemoteException e) {
                throw DefaultRMIExceptionsHandlerOfClient.clientCriticalError;
            }
        }

        if (lobbies == null) {
            return null;
        }

        return lobbies.stream().map(l -> {
            try {
                return new LobbyInfo(l.uuid, l.name, l.maxPlayers, l.playerUsernames, l.gameOngoing);
            } catch (GameLobbyException e) {
                // Situation here is pretty akward and should never happen. Basically it means
                // that the Server returned lobbies that are not valid in terms of their names and/or
                // maxPlayers. We are just gonna use fallback safe values.
                try {
                    return new LobbyInfo(l.uuid, "fallback_name", 4, l.playerUsernames, false);
                } catch (GameLobbyException ignored) {
                    // Using fallback values that can never cause the throw of this exception.
                    // This return will never be reached.
                    return null;
                }
            }
        }).toList();
    }

    /**
     * Asks the Server to join the lobby with the provided UUID.
     * @param uuid the UUID of the lobby to join
     * @return True if the lobby was joined successfully, false otherwise
     */
    public synchronized boolean joinLobby(String uuid) {
        if (!this.RMI) {
            StateOfClient oldState = this.userOfClient.getState();
            userOfClient.setState(new InLobbyStateOfClient(userOfClient));

            WhatLobbyToJoinMessage message = new WhatLobbyToJoinMessage(uuid);
            Message answer = userOfClient.sendAndWaitMultiple(message, List.of(
                    LOBBY_JOIN_OK,
                    LOBBY_ALREADY_FULL,
                    UNKNOWN_ERROR));

            boolean ok = answer.getType() == LOBBY_JOIN_OK;

            if (!ok) {
                this.userOfClient.setState(oldState);
            }

            return ok;
        } else {
            try {
                StateOfClient oldState = this.userOfClient.getState();

                // Setting the state ASAP since pushEvents may arrive
                userOfClient.setState(new InLobbyStateOfClient(userOfClient));

                boolean ok = this.userOfClient.getUserStub().joinLobbyRMI(uuid);

                if (!ok) {
                    // Reverting back to previous state
                    this.userOfClient.setState(oldState);
                }

                return ok;
            } catch (RemoteException e) {
                throw DefaultRMIExceptionsHandlerOfClient.clientCriticalError;
            }
        }
    }

    /**
     * Asks the Server for the information of the lobby the client is in.
     * @return the information of the lobby the client is in. This can be null (see: {@link it.polimi.ingsw.controller.MatchController} specifications).
     */
        public synchronized LobbyInfo getLobbyInfo() {
        if (!this.RMI) {
            GetLobbyInfoMessage message = new GetLobbyInfoMessage();
            Message answer = userOfClient.sendAndWaitMultiple(message, List.of(
                    LOBBY_INFO,
                    UNKNOWN_ERROR));

            if (answer.getType() == UNKNOWN_ERROR) {
                return null;
            }

            return ((LobbyInfoMessage) answer).getLobbyInfo();
        } else {
            try {
                return this.userOfClient.getUserStub().getLobbyInfoRMI();
            } catch (RemoteException e) {
                throw DefaultRMIExceptionsHandlerOfClient.clientCriticalError;
            }
        }
    }

    /**
     * Asks the Server to start the lobby the client is in.
     * @return True if the lobby was started successfully, false otherwise
     */
    public synchronized boolean startLobby() {
        if (!this.RMI) {
            StateOfClient oldState = this.userOfClient.getState();
            userOfClient.setState(new InGameStateOfClient(userOfClient));

            StartLobbyMessage message = new StartLobbyMessage();
            Message answer = userOfClient.sendAndWaitMultiple(message, List.of(
                    LOBBY_START_OK,
                    LOBBY_START_KO,
                    UNKNOWN_ERROR));

            boolean ok = answer.getType() == LOBBY_START_OK;
            if (!ok) {
                this.userOfClient.setState(oldState);
            }

            return ok;
        } else {
            try {
                StateOfClient oldState = userOfClient.getState();

                // Setting the state ASAP since pushEvents may arrive
                userOfClient.setState(new InGameStateOfClient(userOfClient));

                boolean ok = this.userOfClient.getUserStub().startLobbyRMI();

                if (!ok) {
                    // Reverting back to previous state
                    userOfClient.setState(oldState);
                }

                return ok;
            } catch (RemoteException e) {
                throw DefaultRMIExceptionsHandlerOfClient.clientCriticalError;
            }
        }
    }

    /**
     * Asks the Server to exit the lobby the client is in.
     * @return True if the lobby was exited successfully, false otherwise
     */
    public synchronized boolean exitLobby() {
        StateOfClient oldState = this.userOfClient.getState();
        userOfClient.setState(new ChooseCreateJoinStateOfClient(userOfClient));

        if (!this.RMI) {
            LobbyExitMessage message = new LobbyExitMessage();
            Message answer = userOfClient.sendAndWaitMultiple(message, List.of(
                    LOBBY_EXIT_OK,
                    UNKNOWN_ERROR));

            boolean ok = answer.getType() == LOBBY_EXIT_OK;

            if (!ok) {
                this.userOfClient.setState(oldState);
            }

            return ok;
        } else {
            try {
                boolean ok = this.userOfClient.getUserStub().exitLobbyRMI();

                if (!ok) {
                    this.userOfClient.setState(oldState);
                }

                return ok;
            } catch (RemoteException e) {
                throw DefaultRMIExceptionsHandlerOfClient.clientCriticalError;
            }
        }
    }

    /**
     * Asks the Server to exit the match the client is in.
     * @return True if the match was exited successfully, false otherwise
     */
    public synchronized boolean exitMatch() {
        StateOfClient oldState = this.userOfClient.getState();
        userOfClient.setState(new ChooseCreateJoinStateOfClient(userOfClient));

        if (!this.RMI) {
            MatchExitMessage message = new MatchExitMessage();
            Message answer = userOfClient.sendAndWaitMultiple(message, List.of(
                    MATCH_EXIT_OK,
                    UNKNOWN_ERROR
            ));

            boolean ok = answer.getType() == MATCH_EXIT_OK;

            if (!ok) {
                this.userOfClient.setState(oldState);
            }

            return ok;
        } else {
            try {
                boolean ok = this.userOfClient.getUserStub().exitMatchRMI();

                if (!ok) {
                    this.userOfClient.setState(oldState);
                }

                return ok;
            } catch (RemoteException e) {
                throw DefaultRMIExceptionsHandlerOfClient.clientCriticalError;
            }
        }
    }
}
