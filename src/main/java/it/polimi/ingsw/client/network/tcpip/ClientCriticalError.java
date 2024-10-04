package it.polimi.ingsw.client.network.tcpip;

/**
 * Represents a critical Client error that should not be caught.
 * It indicates that the Client has encountered a critical problem that cannot be solved.
 */
public class ClientCriticalError extends Error {
    public ClientCriticalError(String message) {
        super(message);
    }
}
