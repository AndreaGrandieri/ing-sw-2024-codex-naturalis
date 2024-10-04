package it.polimi.ingsw.network;

import it.polimi.ingsw.logger.Logger;
import it.polimi.ingsw.network.tcpip.InvalidTCPConnectionException;
import it.polimi.ingsw.network.tcpip.Server;
import it.polimi.ingsw.network.tcpip.ServerException;

import java.util.UUID;

/**
 * This class is a Thread that is responsible for welcoming new Users to the Server.
 * It is a singleton.
 */
public class WelcomingThread extends Thread {
    private static WelcomingThread instance;

    private WelcomingThread() {
    }

        public static WelcomingThread getInstance() {
        if (instance == null) {
            instance = new WelcomingThread();
        }
        return instance;
    }

    /* Start of the class implementation */
    @Override
    public void run() {
        while (true) {
            // Generate a new UUID for the upcoming new connection
            String connectionUUID = UUID.randomUUID().toString();

            try {
                // This suspends the thread until a new connection is established
                // This may throw an IOException
                Server.getInstance().seekReserve(connectionUUID);

                // Connection established. Welcome to the new Client!
                Profiles.getInstance().createUser(connectionUUID);

                Logger.logInfo("New user connected: " + connectionUUID.substring(0, 3));
            } catch (ServerException | InvalidTCPConnectionException e) {
                // Encountering an IOException means that a network releated error occurred while connecting
                // the new User to the server.
                // If seekReserve thrown it that means that a Socket releated error occured
                // and the User won't be connected to the server: no problem internally, since the User object creation
                // has not been yet issued to Profiles.
                // To handle the situation, we just need to keep on going as nothing happened: seekReserve self-sustains
                // the handling situation in its implementation; we are just gonna log the error.
                // The thread won't stop so the Server won't crash!
                Logger.logError("An IOException occurred while trying to connect a new user to the server with proposed connectionUUID " + connectionUUID.substring(0, 3) + ": " + e.getMessage());
            }
        }
    }
}
