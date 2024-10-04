package it.polimi.ingsw.client.network.liveloop;

import it.polimi.ingsw.client.NetworkClientChatController;
import it.polimi.ingsw.client.NetworkClientGameController;
import it.polimi.ingsw.client.NetworkClientMatchController;
import it.polimi.ingsw.client.network.state.SettingUsernameStateOfClient;
import it.polimi.ingsw.client.network.state.StateOfClient;
import it.polimi.ingsw.client.network.tcpip.Client;
import it.polimi.ingsw.client.network.tcpip.ClientCriticalError;
import it.polimi.ingsw.client.network.tcpip.ClientException;
import it.polimi.ingsw.logger.Logger;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.MessageType;
import it.polimi.ingsw.network.messages.servertoclient.LobbyStartMessage;
import it.polimi.ingsw.network.rmi.UserStubRMI;
import it.polimi.ingsw.network.tcpip.Heartbeat;
import it.polimi.ingsw.network.tcpip.InvalidTCPConnectionException;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static it.polimi.ingsw.network.messages.util.Casting.singleArgCast;

/**
 * This class represents the User concept (from the Server POV) in the Client.
 */
public class UserOfClient {
    private final Client client;
    private StateOfClient state;
    private final ListenLoopOfClient listenLoop;
    private final ArrayList<MessageType> messageTypesRegisteredForWait;
    private Message messageForWait;
    private final Object registeredForWaitLock;
    private NetworkClientGameController cgc;
    private NetworkClientChatController ccc;
    private NetworkClientMatchController cmc;
    private final Boolean RMI;
    private final UserStubRMI userStub;

    // Heartbeat information
    private final Integer heartbeatMs;
    private Boolean heartbeatContinuitySignal;
    private final Object heartbeatContinuitySignalLock;
    private final Thread heartbeatThread;

    // Sense information
    private final AtomicLong lastSense;
    private final ScheduledExecutorService executorService;

    /**
     * Callback to be called when the connection is lost.
     * This callback is called by the executorService upon the timeouting of the Sense checker.
     */
    private final Consumer<Void> deathByConnectionLost;

    /**
     * Constructor for the UserOfClient.
     * @param client The Client object.
     * @param RMI Whether the Client is using RMI or not.
     * @param userStub The UserStubRMI object if RMI is used, otherwise null.
     * @param heartbeatMs The heartbeat interval in milliseconds.
     * @param senseMs The sense interval in milliseconds.
     * @param deathByConnectionLost The callback to be called when the connection is lost.
     */
    public UserOfClient(Client client, Boolean RMI, UserStubRMI userStub,
                        Integer heartbeatMs, Integer senseMs,
                        Consumer<Void> deathByConnectionLost) {
        this.RMI = RMI;

        if (!RMI) {
            if (client == null) {
                throw new ClientCriticalError("UserOfClient did not receive a valid Client object.");
            }

            this.client = client;
            this.listenLoop = new ListenLoopOfClient(0, deathByConnectionLost);
            this.userStub = null;

            this.heartbeatMs = null;
            this.heartbeatContinuitySignal = null;
            this.heartbeatContinuitySignalLock = null;
            this.heartbeatThread = null;
            this.lastSense = null;
            this.executorService = null;
            this.deathByConnectionLost = deathByConnectionLost;
        } else {
            this.client = null;
            this.listenLoop = null;
            this.userStub = userStub;

            // Start heartbeat thread
            this.heartbeatMs = heartbeatMs;
            this.heartbeatContinuitySignal = false;
            this.heartbeatContinuitySignalLock = new Object();
            this.heartbeatThread = new Thread(this::heartbeatRMIThreadImplementation);

            this.lastSense = new AtomicLong(System.currentTimeMillis());
            this.executorService = Executors.newSingleThreadScheduledExecutor();
            this.deathByConnectionLost = deathByConnectionLost;

            this.heartbeatThread.setUncaughtExceptionHandler((t, e) -> {
                synchronized (this.heartbeatContinuitySignalLock) {
                    this.heartbeatContinuitySignal = false;
                }
            });

            this.executorService.scheduleAtFixedRate(() -> {
                // If more than senseMs have passed since the last sense
                if (System.currentTimeMillis() - this.lastSense.get() > senseMs) {
                    // Here i need to disconnect the Client from the RMI Server
                    this.executorService.shutdown();
                    this.deathByConnectionLost.accept(null);
                }
            }, 1, 1, TimeUnit.SECONDS);

            synchronized (this.heartbeatContinuitySignalLock) {
                this.heartbeatContinuitySignal = true;
            }

            this.heartbeatThread.start();
        }

        this.messageTypesRegisteredForWait = new ArrayList<>();
        this.messageForWait = null;
        this.registeredForWaitLock = new Object();

        this.cmc = null;
        this.ccc = null;
        this.cgc = null;
        this.setState(new SettingUsernameStateOfClient(this));
    }

    /**
     * Heartbeat thread implementation.
     * This thread guards for RMI connection loss.
     * For Socket, refer {@link Client} internal implementation: the Heartbeat mechanism is directly implemented there.
     */
    private void heartbeatRMIThreadImplementation() {
        while (true) {
            synchronized (this.heartbeatContinuitySignalLock) {
                if (!this.heartbeatContinuitySignal) {
                    break;
                }
            }

            try {
                this.userStub.heartbeat(new Heartbeat());
                Thread.sleep(this.heartbeatMs);
            } catch (InterruptedException | RemoteException e) {
                throw new RuntimeException("The used TCP connection may be invalid.");
            }
        }
    }

        public AtomicLong getLastSense() {
        return lastSense;
    }

        public UserStubRMI getUserStub() {
        return userStub;
    }

    public void setCgc(NetworkClientGameController cgc) {
        if (cgc == null) {
            this.cgc = null;
            return;
        }

        if (this.cgc == null) {
            this.cgc = cgc;
        }
    }

    public void setCcc(NetworkClientChatController ccc) {
        if (ccc == null) {
            this.ccc = null;
            return;
        }

        if (this.ccc == null) {
            this.ccc = ccc;
        }
    }

    public void setCmc(NetworkClientMatchController cmc) {
        if (this.cmc == null) {
            this.cmc = cmc;
        }
    }

        public NetworkClientGameController getClientGC() {
        return cgc;
    }

        public NetworkClientChatController getClientCC() {
        return ccc;
    }

        public NetworkClientMatchController getClientMC() {
        return this.cmc;
    }

    /**
     * Start the ListenLoop.
     */
    public void startListenLoop() {
        if (!this.listenLoop.isAlive()) {
            this.listenLoop.start();
        }
    }

        public Client getClient() {
        return this.client;
    }

        public StateOfClient getState() {
        return state;
    }

    public void setState(StateOfClient state) {
        this.state = state;
    }

    /**
     * Send a message to the Server using the network.
     * @param message The message to send.
     */
    public void send(Message message) {
        Logger.logInfo("Replying with message: " + message.getType());

        try {
            this.client.sendSerializableObject(message);
        } catch (ClientException | InvalidTCPConnectionException e) {
            // ClientException | InvalidTCPConnectionException is thrown when the connection is closed (gracefully or not).
            // This is a critical error. The Server is not reachable anymore.

            // How to handle this? Basically we should ask ourselves if the Client is able to keep going after
            // encountering one of the exceptions above in this section: the answer is NO!
            // It cannot go on since something went wrong and to "fix" it basically a game restart is needed.

            if (this.deathByConnectionLost != null) {
                this.deathByConnectionLost.accept(null);
            } else {
                throw new ClientCriticalError("Client encountered a critical error. Please restart it and try again. " + e.getMessage());
            }
        }
    }

    /**
     * Registers a MessageType to be blocking-waited for arrival.
     * @param messageType The MessageType to register.
     */
    private void registerForWait(MessageType messageType) {
        synchronized (this.registeredForWaitLock) {
            this.messageTypesRegisteredForWait.add(messageType);
        }
    }

    /**
     * Sends a message to the Server and waits for a response.
     * The wait is blocking.
     * The wait releases only when a message of type registered using registerForWait arrives!
     * @param message The message to send.
     * @return The message received.
     */
        private synchronized Message sendAndWaitSender(Message message) {
        this.send(message);

        synchronized (this.registeredForWaitLock) {
            while (this.messageForWait == null) {
                try {
                    this.registeredForWaitLock.wait();
                } catch (InterruptedException e) {
                    // InterruptedException is thrown when the Thread is interrupted. Something is wrong with the ListenLoop
                    // itself. This is a critical error.

                    // Why ListenLoopOfClient? Because it's the ListenLoop responsibility to reactivate the wait
                    // by calling notifyAll; so we can approximate that an InterruptedException here is the
                    // ListenLoopOfClient fault.

                    // How to handle this? Basically we should ask ourselves if the Client is able to keep going after
                    // encountering one of the exceptions above in this section: the answer is NO!
                    // It cannot go on since something went wrong and to "fix" it basically a game restart is needed.

                    if (this.deathByConnectionLost != null) {
                        this.deathByConnectionLost.accept(null);
                    } else {
                        throw new ClientCriticalError("Client encountered a critical error. Please restart it and try again. " + e.getMessage());
                    }
                }
            }

            this.messageTypesRegisteredForWait.clear();
        }

        // Consuming the message and returning it
        Message localMessage = this.messageForWait;
        this.messageForWait = null;

        return localMessage;
    }

    //     // public synchronized Message sendAndWait(Message message, MessageType messageType) {
    //     this.registerForWait(messageType);
    //     return this.sendAndWaitSender(message);
    // }

    /**
     * Sends a message to the Server and waits for a response.
     * Multiple MessageType(s) can be registered for wait.
     * The wait is blocking.
     * The wait releases only when a message of type in the list messageTypes arrives!
     * @param message The message to send.
     * @param messageTypes The list of MessageType(s) to register for wait.
     * @return The message received.
     */
        public synchronized Message sendAndWaitMultiple(Message message, List<MessageType> messageTypes) {
        for (MessageType messageType : messageTypes) {
            this.registerForWait(messageType);
        }
        return this.sendAndWaitSender(message);
    }

        public ArrayList<MessageType> getMessageTypesRegisteredForWait() {
        return this.messageTypesRegisteredForWait;
    }

        public Object registeredForWaitLock() {
        return this.registeredForWaitLock;
    }

    public void setMessageForWait(Message messageForWait) {
        this.messageForWait = messageForWait;
    }

    /**
     * Reacts to a message received from the Server.
     * The reaction is dynamic and depends on the current State.
     * @param message The message to react to.
     */
    public void react(Message message) {
        switch (message.getType()) {
            case LOBBY_START_WRAPPED -> this.state.onLobbyStartEventWrapped(singleArgCast(message));
            case LOBBY_START -> this.state.onLobbyStartEvent((LobbyStartMessage) message);
            case CHOOSE_GOAL_EVENT -> this.state.onChooseGoalEvent(singleArgCast(message));
            case DRAW_COVERED_EVENT -> this.state.onDrawCoveredEvent(singleArgCast(message));
            case DRAW_VISIBLE_EVENT -> this.state.onDrawVisibleEvent(singleArgCast(message));
            case PLACE_CARD_EVENT -> this.state.onPlaceCardEvent(singleArgCast(message));
            case SET_COLOR_EVENT -> this.state.onSetColorEvent(singleArgCast(message));
            case SET_STARTER_EVENT -> this.state.onSetStarterEvent(singleArgCast(message));
            case STATE_CHANGE_EVENT -> this.state.onStateChangeEvent(singleArgCast(message));
            case TURN_CHANGE_EVENT -> this.state.onTurnChangeEvent(singleArgCast(message));
            case BROADCAST_MESSAGE_EVENT -> this.state.onBroadcastMessageEvent(singleArgCast(message));
            case PRIVATE_MESSAGE_EVENT -> this.state.onPrivateMessageEvent(singleArgCast(message));
            case MATCH_COMPOSITION_CHANGE_EVENT -> this.state.onMatchCompositionChangeEvent(singleArgCast(message));

            case INVALID_LOBBY_INFO_FOR_CREATION,
                    LIST_OF_LOBBY_TO_JOIN,
                    LOBBY_START_OK,
                    LOBBY_START_KO,
                    LOBBY_EXIT_OK,
                    LOBBY_ALREADY_FULL,
                    LOBBY_JOIN_OK,
                    MATCH_EXIT_OK,
                    NO_MORE_SPACE_FOR_NEW_LOBBIES,
                    UNKNOWN_ERROR,
                    USERNAME_ALREADY_TAKEN,
                    USERNAME_CONFIRMED,
                    USERNAME_NOT_VALID
                    -> {
                // These messages are sent by the server to the client, but are handled synchronously.
                // Since react is designed for handling asynchronous messages, these messages does not need to be
                // handled here.

                // What happens if react catches one of these messages here? We are simply gonna ignore them since
                // it is ok to receive them but they are asynchronous so we expect that a asynchronous handler
                // is handling them. We won't be triggered by these messages, which are still valid.
            }

            case USERNAME,
                    LOBBY_INFO_FOR_CREATION,
                    WHAT_LOBBY_TO_JOIN,
                    START_LOBBY,
                    GET_LIST_OF_LOBBY_TO_JOIN,
                    GET_LOBBY_INFO_MESSAGE,
                    LOBBY_EXIT,
                    LOBBY_INFO,
                    MATCH_EXIT
                    -> {
                // All these messages are sent by the client to the server, so they should not be handled.
                // They should be handled by the server.
                // How to manage the receiving of these messages? From the POV of the Client... receiving one of these
                // messages should not be seen as a problem: just ignore them.
                // Of course... it does not end there. Receiving such messages is basically a signal of something really
                // bad going of with the Server! Ignoring is just the best solution client side.
            }
        }
    }

    public ListenLoopOfClient getListenLoop() {
        return listenLoop;
    }
}
