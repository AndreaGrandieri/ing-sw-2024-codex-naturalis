package it.polimi.ingsw.network.tcpip;

import java.io.Serializable;

/**
 * This class represents a Heartbeat object, that the Server receives from the Client.
 * The Client has to send a Heartbeat object periodically to the Server otherwise it will eventually be disconnected.
 */
public class Heartbeat implements Serializable {
}
