package it.polimi.ingsw.network.tcpip;

/**
 * Represents a specific Server and Client exception to be thrown whenever the network side
 * tries to use a non-working TCP connection.
 */
public class InvalidTCPConnectionException extends Exception {
    public InvalidTCPConnectionException(String message) {
        super(message);
    }
}
