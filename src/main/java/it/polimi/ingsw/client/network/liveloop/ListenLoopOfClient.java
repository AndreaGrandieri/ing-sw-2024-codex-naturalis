package it.polimi.ingsw.client.network.liveloop;

import it.polimi.ingsw.client.network.ProfileOfClient;
import it.polimi.ingsw.client.network.ProfileOfClientException;
import it.polimi.ingsw.client.network.tcpip.ClientCriticalError;
import it.polimi.ingsw.client.network.tcpip.ClientException;
import it.polimi.ingsw.logger.Logger;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.MessageType;
import it.polimi.ingsw.network.tcpip.InvalidTCPConnectionException;
import it.polimi.ingsw.network.tcpip.Sense;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * This class is a Thread that listens for messages from the Server and dispatches them to the react method.
 */
public class ListenLoopOfClient extends Thread {
    private final Integer delayer;
    private Boolean keepAlive;
    private final Object keepAliveLock;
    private final Consumer<Void> deathByConnectionLost;

    public ListenLoopOfClient(Integer delayer, Consumer<Void> deathByConnectionLost) {
        this.delayer = delayer;
        this.keepAlive = true;
        this.keepAliveLock = new Object();
        this.deathByConnectionLost = deathByConnectionLost;
    }

    /**
     * Stops the ListenLoop.
     */
    public void stopLoop() {
        synchronized (this.keepAliveLock) {
            keepAlive = false;
        }
    }

    /**
     * The main loop of the ListenLoop. It listens for messages from the Server and dispatches them to the react.
     * If connection failure is detected, the thread stops throwing a ClientCriticalError.
     */
    @Override
    public void run() {
        // Keep listening for messages from the server
        while (true) {
            synchronized (this.keepAliveLock) {
                if (!keepAlive) {
                    break;
                }
            }

            try {
                Message message = null;
                Object read = ProfileOfClient.getInstance()
                        .getUserOfClient()
                        .getClient()
                        .readSerializableObject();

                if (!(read instanceof Sense)) {
                    message = (Message) read;

                    ArrayList<MessageType> messageTypesRegisteredForWait = ProfileOfClient.getInstance()
                            .getUserOfClient()
                            .getMessageTypesRegisteredForWait();

                    Logger.logInfo("Received message: " + message.getType());

                    // Check registered for wait
                    if (messageTypesRegisteredForWait.contains(message.getType())) {
                        Object registeredForWaitLock = ProfileOfClient.getInstance()
                                .getUserOfClient()
                                .registeredForWaitLock();

                        synchronized (registeredForWaitLock) {
                            ProfileOfClient.getInstance()
                                    .getUserOfClient()
                                    .setMessageForWait(message);

                            registeredForWaitLock.notifyAll();
                        }
                    } else {
                        ProfileOfClient.getInstance()
                                .getUserOfClient()
                                .react(message);
                    }
                }

                // Wait for delayer milliseconds before listening for another message
                if (this.delayer > 0) {
                    Thread.sleep(this.delayer);
                }
            } catch (InvalidTCPConnectionException | ClientException | InterruptedException | ProfileOfClientException e) {
                // ProfileOfClientException is thrown here if ProfileOfClient has not been yet initialized. This is a
                // critical error.
                // InterruptedException is thrown when the Thread is interrupted. Something is wrong with the ListenLoop
                // itself. This is a critical error.
                // ClientException | InvalidTCPConnectionException is thrown when the connection is closed (gracefully or not).
                // This is a critical error. The Server is not reachable anymore.

                // How to handle this? Basically we should ask ourselves if the Client is able to keep going after
                // encountering one of the exceptions above in this section: the answer is NO!
                // It cannot go on since something went wrong and to "fix" it basically a game restart is needed.

                // This will exit the Client application with an error state.
                // To override behaviour please handle using setUncaughtExceptionHandler on this Thread.
                if (this.deathByConnectionLost != null) {
                    this.deathByConnectionLost.accept(null);
                } else {
                    throw new ClientCriticalError("Client encountered a critical error. Please restart it and try again. " + e.getMessage());
                }
            }
        }
    }
}
