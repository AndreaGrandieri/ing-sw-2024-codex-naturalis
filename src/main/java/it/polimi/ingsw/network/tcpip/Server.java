package it.polimi.ingsw.network.tcpip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashMap;

/**
 * Describes a TCPIP server.
 * Is a singleton.
 */
public class Server {
    private static Server instance;

    private Server(Integer port, Integer heartbeatMs) throws ServerException {
        if (port < 0 || port > 65353) {
            throw new ServerException("Invalid value specified for 'port'. Valid range: from 0 to 65353.");
        }

        this.port = port;
        this.heartbeatMs = heartbeatMs;

        try {
            // Creating the socket
            this.welcomeSocket = new ServerSocket(this.port);

            // Store the private IP address (the ip address of the server in the local network)
            this.privateIPAddress = Inet4Address.getLocalHost().toString();
        } catch (IOException e) {
            // Server cannot be started. The server won't work. Stopping execution.
            throw new ServerCriticalError("The server could not be started");
        }

        String publicIPAddress = null;
        boolean fetchPublicIPUsingAWS = false;

        if (fetchPublicIPUsingAWS) {
            // Store the public IP address (the ip address of the server used to contact it from the external Internet)
            try {
                publicIPAddress = new BufferedReader(
                        new InputStreamReader(
                                new URI("https://checkip.amazonaws.com").toURL().openStream())
                ).readLine();
            } catch (URISyntaxException | IOException ignored) {
                // Nothing to handle nor to stop
            }
        }

        // May be null if the public IP address could not be retrieved or fetchPublicIPUsingAWS is false.
        // This doesn't indicate that the server won't work.
        this.publicIPAddress = publicIPAddress;

        this.staledConnections = new HashMap<>();
    }

        public static Server getInstance(Integer port, Integer heartbeatMs) throws ServerException {
        if (instance == null) {
            instance = new Server(port, heartbeatMs);
        }
        return instance;
    }

        public static Server getInstance(Integer port) throws ServerException {
        if (instance == null) {
            instance = new Server(port, 6500);
        }
        return instance;
    }

        public static Server getInstance() throws ServerException {
        if (instance == null) {
            throw new ServerException("The server must be initialized with a port.");
        }
        return instance;
    }

    /* Start of the class implementation */
    private final String publicIPAddress;
    private final String privateIPAddress;
    private final Integer port;
    private final Integer heartbeatMs;

    /**
     * Welcome Socket. Used to welcome incoming TCP connections to this server.
     */
    private final ServerSocket welcomeSocket;

    /**
     * HashMap to track staled connections.
     *
     * Staled connection: an established TCP connection that may or may not be still valid.
     * If valid, it can be retrieved from "staledConnections" to be used.
     * The key is of type String and it is used to uniquely identify connections.
     * ClientController: corresponding instance of a ClientController to communicate with the
     * client that is connected to this server using the existing TCP session.
     *
     * Discriminate between "active staled" and "inactive staled" connections:
     * "active staled" connections are connections saved in "staledConnections" and still valid to
     * be used if retrieved.
     * "inactive staled" connections are connections saved in "staledConnections" but not valid anymore:
     * this connections cannot be used and get cleaned up (aka: deleted) periodically.
     */
    private final HashMap<String, ClientController> staledConnections;

    /**
     * Launches the Server welcoming session to welcome the first incoming TCP connection.
     * The incoming TCP connection is received on the welcomeSocket; once received, if the connection is considered
     * valid (based on different checks (not specified here)) a ClientController is created to separately handle
     * the specific connection. The ClientController may be referred in another methods of the Server using the
     * same connectionName, that must be unique.
     * @param connectionName The name of the connection to be created.
     * @throws InvalidTCPConnectionException If the incoming TCP connection is not valid.
     * @throws ServerException If the connectionName is not unique.
     */
    public void seekReserve(String connectionName) throws ServerException, InvalidTCPConnectionException {
        synchronized (this.staledConnections) {
            if (this.staledConnections.containsKey(connectionName)) {
                throw new ServerException("A stale connection with name 'connectionName' already exists." +
                        " Connection names must be unique.");
            }

            // Creating hollow space for ClientController
            this.staledConnections.put(connectionName, null);
        }

        // Creating Socket, specific for a single TCP session
        Socket socket;

        synchronized (this.welcomeSocket) {
            try {
                // Waiting... accepting... TCP connection is now alive
                socket = this.welcomeSocket.accept();

                // The Server will treat any connected Client that does not speak nor send heartbeat
                // within heartbeatMs milliseconds as disconnected due to lost connection
                socket.setSoTimeout(this.heartbeatMs);

                // Creating ClientController
                ClientController cc = new ClientController(socket);

                synchronized (this.staledConnections) {
                    // Filling the hollow
                    this.staledConnections.replace(connectionName, cc);
                }
            } catch (IOException e) {
                // Fail. Removing the connectionName
                this.silentDumpHollow(connectionName);
                throw new InvalidTCPConnectionException(e.getMessage());
            }
        }
    }

    /**
     * Closes the existing TCP connection with the client handled by the ClientController cc and dumps the
     * associated connectionName.
     * The method is silent: no exception is thrown if the closing procedure fails.
     *
     * @param connectionName The name of the connection to be closed.
     */
    public void silentClose(String connectionName) {
        synchronized (this.staledConnections) {
            ClientController cc = this.staledConnections.get(connectionName);

            if (cc != null) {
                cc.silentClose();
                this.staledConnections.remove(connectionName);
            }
        }
    }

    /**
     * Dumps the hollow connectionName allocated in the staledConnections map.
     * The method is silent: no exception is thrown if the dump-hollow procedure fails.
     *
     * @param connectionName The name of the holoow connection to be dumped.
     */
    private void silentDumpHollow(String connectionName) {
        synchronized (this.staledConnections) {
            if (this.staledConnections.containsKey(connectionName)) {
                if (this.staledConnections.get(connectionName) == null) {
                    this.staledConnections.remove(connectionName);
                }
            }
        }
    }

    /**
     * Serializes and sends a serializable object to the client handled by the ClientController cc.
     * @param connectionName The name of the connection to be used.
     * @param serializableObjectToSend The serializable object to be sent.
     * @throws ServerException If the connectionName is not valid.
     * @throws InvalidTCPConnectionException If the TCP connection is not valid.
     */
    public void sendSerializableObject(String connectionName, Object serializableObjectToSend)
    throws ServerException, InvalidTCPConnectionException {
        ClientController cc = null;

        synchronized (this.staledConnections) {
            cc = this.staledConnections.get(connectionName);
        }

        if (cc == null) {
            throw new ServerException("No ClientController is associated with the specified " +
                    "connectionName. A non-hollow connectionName is required.");
        }

        // Sending
        Boolean sendResult = cc.notthrow__sendSerializableObject(serializableObjectToSend);

        if (!sendResult) {
            // Failure in sending. The connection is probably not valid anymore. Closing...
            this.silentClose(connectionName);

            throw new InvalidTCPConnectionException("The used TCP connection may be invalid.");
        }
    }

    /**
     * Listens and deserializes an object received from the client handled by the ClientController cc.
     * @param connectionName The name of the connection to be used.
     * @return The deserialized object received.
     * @throws ServerException If the connectionName is not valid.
     * @throws InvalidTCPConnectionException If the TCP connection is not valid.
     */
        public Object readSerializableObject(String connectionName)
            throws ServerException, InvalidTCPConnectionException {
        ClientController cc = null;

        synchronized (this.staledConnections) {
            cc = this.staledConnections.get(connectionName);
        }

        if (cc == null) {
            throw new ServerException("No ClientController is associated with the specified " +
                    "connectionName. A non-hollow connectionName is required.");
        }

        // Listening for something (busy wait)
        try {
            return cc.readSerializableObject();
        } catch (InvalidTCPConnectionException e) {
            // Failure in listening. The connection is probably not valid anymore. Closing...
            this.silentClose(connectionName);

            throw new InvalidTCPConnectionException("The used TCP connection may be invalid.");
        }
    }

        public String getPublicIpAddress() {
        return this.publicIPAddress;
    }

        public String getPrivateIpAddress() {
        return this.privateIPAddress;
    }

        public Integer getPort() {
        return this.port;
    }

    /**
     * Calculates and returns the private IP address of the host.
     * @return The private IP address of the host.
     * @throws ServerCriticalError If the private IP address cannot be retrieved.
     */
        public static String staticGetPrivateIPAddress() {
        try {
            return Inet4Address.getLocalHost().toString();
        } catch (UnknownHostException e) {
            throw new ServerCriticalError("Could not retrieve host local-network IP address");
        }
    }
}
