package it.polimi.ingsw.network.tcpip;

/**
 * This class is used to throw an error when a critical server error occurs.
 * The application is not meant to recover from this error.
 */
public class ServerCriticalError extends Error {
    public ServerCriticalError(String message) {
        super(message);
    }
}
