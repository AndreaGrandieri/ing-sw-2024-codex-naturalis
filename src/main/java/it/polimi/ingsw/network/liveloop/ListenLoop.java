package it.polimi.ingsw.network.liveloop;

import it.polimi.ingsw.logger.Logger;
import it.polimi.ingsw.network.Profiles;
import it.polimi.ingsw.network.ProfilesException;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.tcpip.*;

/**
 * This class is a Thread that listens for messages from a specific User and dispatches them to the User's react method.
 */
public class ListenLoop extends Thread {
    private final String connectionUUID;
    private final Integer delayer;
    private Boolean keepAlive;
    private final Object keepAliveLock;

    /**
     * Creates a new ListenLoop.
     * @param connectionUUID The connectionUUID of the User to listen to.
     * @param delayer The time to wait between each message.
     */
    public ListenLoop(String connectionUUID, Integer delayer) {
        this.connectionUUID = connectionUUID;
        this.delayer = delayer;
        this.keepAlive = true;
        this.keepAliveLock = new Object();
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
     * The main loop of the ListenLoop. It listens for messages from the User and dispatches them to the User's react.
     * If connection failure is detected, the User's pruning process is invoked and the loop is stopped.
     */
    @Override
    public void run() {
        while (true) {
            synchronized (this.keepAliveLock) {
                if (!keepAlive) {
                    break;
                }
            }

            // Keep listening for messages from the client
            try {
                // listen(...) will fail if the connection is closed (gracefully or not). It is our guardian
                // to perceive "failed User(s)" and deallocate their resources.
                Object read = Server.getInstance().readSerializableObject(this.connectionUUID);

                if (!(read instanceof Heartbeat)) {
                    Message message = (Message) read;
                    Logger.logInfo("Received message: " + message.getType());

                    Profiles.getInstance().getUserByConnectionUUID(this.connectionUUID).react(message);
                } else {
                    HeartbeatSenser.sense(this.connectionUUID);
                }

                // Wait for delayer milliseconds before listening for another message
                if (delayer > 0) {
                    Thread.sleep(delayer);
                }
            } catch (ProfilesException | ServerException | InvalidTCPConnectionException | InterruptedException e) {
                // ProfilesException is thrown when the user is not found in this context. This is a critical error.
                // Not finding the user by its connectionUUID means that the User is not in Profiles.
                // InterruptedException is thrown when the Thread is interrupted. Something is wrong with the ListenLoop
                // itself. This is a critical error. Safely deallocate User resources and stop the loop.
                // ServerException | InvalidTCPConnectionException is thrown when the connection is closed (gracefully or not). This is a
                // critical error. The User is not reachable anymore. Safely deallocate User resources and stop the loop.

                Profiles.getInstance().silentPruneUser(this.connectionUUID, false);

                // Finally, let's stop this loop.
                this.stopLoop();

                // Any other Thread that maybe running for this User will be stopped by the thread itself in its
                // exception handling.
            }
        }
    }
}
