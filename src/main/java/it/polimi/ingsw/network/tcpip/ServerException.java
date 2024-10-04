package it.polimi.ingsw.network.tcpip;

/**
 * Represents a generic Server exception in terms of its network side.
 * It is thrown whenever the Server encounters a network side problem that does not need
 * more details for diagnostic.
 */
public class ServerException extends Exception {
    public ServerException(String message) {
        super(message);
    }
}
