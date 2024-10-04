package it.polimi.ingsw.client.network.tcpip;

/**
 * Represents a generic Client exception in terms of its network side.
 * It is thrown whenever the Client encounters a network side problem that does not need
 * more details for diagnostic.
 */
public class ClientException extends Exception {
    public ClientException(String message) {
        super(message);
    }
}
