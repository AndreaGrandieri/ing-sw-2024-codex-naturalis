package it.polimi.ingsw.network.tcpip;

/**
 * Utility class to send a Sense object to a Client in answer to its Heartbeat that has been received by the Server.
 */
public class HeartbeatSenser {
    /**
     * Sends a Sense object to the Client identified by the connectionUUID.
     * @param connectionUUID the UUID of the connection to the Client.
     */
    public static void sense(String connectionUUID) {
        try {
            Server.getInstance().sendSerializableObject(connectionUUID, new Sense());
        } catch (ServerException | InvalidTCPConnectionException ignored) {
            // Sense failed. What does this mean?
            // Probably something is wrong with the connection, but is no HeartbeatSenser responsibility
            // to handle this. If no more heartbeat is gonna be received by the Client it will be disconnected
            // and pruned by the Socket timeout itself.
            // If heartbeats are still received but non sensed... probably the Client will disconnect from
            // the Server using its own Socket timeout: this will conseguently stop the heartbeats and so
            // finally the Client will also be pruned from here (the Server).
        }
    }
}
