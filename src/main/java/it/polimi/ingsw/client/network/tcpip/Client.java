package it.polimi.ingsw.client.network.tcpip;

import it.polimi.ingsw.network.tcpip.Heartbeat;
import it.polimi.ingsw.network.tcpip.InvalidTCPConnectionException;

import java.io.*;
import java.net.*;

/**
 * A class to handle the TCP/IP connection of the client to a TCP/IP server.
 */
public class Client {
    private final String publicIPAddress;
    private final String privateIPAddress;

    /**
     * The socket used to connect to the server.
     */
    private Socket socket;

    /**
     * Locks to prevent concurrent access to the socket when manipulating the object itself.
     */
    private final Object socketLock;

    /**
     * Locks to prevent concurrent access to the socket when reading data. Concurrent read may lead to threads
     * "stealing" data from each other.
     */
    private final Object readLock;

    /**
     * Locks to prevent concurrent access to the socket when sending data. Concurrent send may lead to data corruption.
     */
    private final Object sendLock;

    /**
     * Unidirectional stream: THIS_NODE -> OPPOSITE_NODE.
     * Used for serializable objects.
     */
    private ObjectOutputStream objectOutputStream;

    /**
     * Unidirectional stream: OPPOSITE_NODE -> THIS_NODE.
     * Used for serializable objects.
     */
    private ObjectInputStream objectInputStream;

    /**
     * A flag to check if the socket is reserved (connected to a server).
     */
    private Boolean reserved;

    // Heartbeat information
    private final Integer heartbeatMs;
    private Boolean heartbeatContinuitySignal;
    private final Object heartbeatContinuitySignalLock;
    private final Thread heartbeatThread;

    // Sense information
    private final Integer senseMs;

    /**
     * Create a new client with the specified heartbeat and sense intervals.
     * @param heartbeatMs the interval in milliseconds between heartbeats
     * @param senseMs the interval in milliseconds between sense checks before timeouting
     */
    public Client(Integer heartbeatMs, Integer senseMs) {
        this.objectInputStream = null;
        this.objectOutputStream = null;
        this.socket = null;

        this.reserved = false;

        this.socketLock = new Object();
        this.readLock = new Object();
        this.sendLock = new Object();

        this.heartbeatMs = heartbeatMs;
        this.heartbeatContinuitySignal = false;
        this.heartbeatContinuitySignalLock = new Object();
        this.heartbeatThread = new Thread(this::heartbeatThreadImplementation);
        this.senseMs = senseMs;

        this.heartbeatThread.setUncaughtExceptionHandler((t, e) -> {
            synchronized (this.heartbeatContinuitySignalLock) {
                this.heartbeatContinuitySignal = false;
            }
        });

        try {
            this.privateIPAddress = Inet4Address.getLocalHost().toString();
        } catch (UnknownHostException e) {
            throw new ClientCriticalError("Could not retrieve the private IP address of this client (UnknownHostException).");
        }

        String publicIPAddress = null;

        // Store the public IP address (the ip address of this client)
        try {
            publicIPAddress = new BufferedReader(new InputStreamReader(new URI("https://checkip.amazonaws.com").toURL().openStream())).readLine();
        } catch (URISyntaxException | IOException ignored) {
            // Nothing to handle nor to stop
        }

        // May be null if the public IP address could not be retrieved. This does not mean the client won't work for sure.
        this.publicIPAddress = publicIPAddress;
    }

    /**
     * The implementation of the heartbeat thread.
     * This thread sends a heartbeat to the server every heartbeatMs milliseconds.
     * The heartbeat is an Heartbeat object.
     */
    private void heartbeatThreadImplementation() {
        while (true) {
            synchronized (this.heartbeatContinuitySignalLock) {
                if (!this.heartbeatContinuitySignal) {
                    break;
                }
            }

            try {
                this.sendSerializableObject(new Heartbeat());
                Thread.sleep(this.heartbeatMs);
            } catch (ClientException | InvalidTCPConnectionException | InterruptedException e) {
                throw new RuntimeException("The used TCP connection may be invalid.");
            }
        }
    }

    /**
     * Connect to the specified IP address and port.
     * @param IPAddress the IP address of the server
     * @param port the port of the server
     * @throws ClientException if the client is already connected to a server
     * @throws InvalidTCPConnectionException if the connection could not be established
     */
    public void connectReserve(String IPAddress, Integer port)
            throws ClientException, InvalidTCPConnectionException {
        synchronized (this.socketLock) {
            if (this.isSocketExhausted()) {
                this.getFreshSocket();

                try {
                    this.socket.setSoTimeout(this.senseMs);
                    this.socket.connect(new InetSocketAddress(IPAddress, port));
                    this.attachStreams();
                    this.reserved = true;

                    synchronized (this.heartbeatContinuitySignalLock) {
                        this.heartbeatContinuitySignal = true;
                    }

                    this.heartbeatThread.start();
                } catch (IOException e) {
                    this._dispose();
                    throw new InvalidTCPConnectionException(e.getMessage());
                }
            } else {
                throw new ClientException("The socket is not exhausted (IllegalStateException).");
            }
        }
    }

    /**
     * Send a serializable object to the server.
     * @param serializableObject the object to send
     * @throws ClientException if the client is not connected to a server
     * @throws InvalidTCPConnectionException if the connection is invalid or the object could not be sent
     */
    public void sendSerializableObject(Object serializableObject)
            throws ClientException, InvalidTCPConnectionException {
        synchronized (this.sendLock) {
            if (!this.isSocketExhausted() && this.reserved) {
                Boolean sent = this.notthrow__sendSerializableObject(serializableObject);

                if (!sent) {
                    this._silentClose();
                    throw new InvalidTCPConnectionException("The used TCP connection may be invalid or the object could not be sent.");
                }
            } else {
                throw new ClientException("The socket is exhausted or not reserved (IllegalStateException).");
            }
        }
    }

    /**
     * Read a serializable object from the server.
     * @return the object read
     * @throws ClientException if the client is not connected to a server
     * @throws InvalidTCPConnectionException if the connection is invalid or the object could not be read
     */
        public Object readSerializableObject() throws ClientException, InvalidTCPConnectionException {
        // Always one thread at a time should lock-and-listen the same Socket.
        // This should not prevent replying.
        synchronized (this.readLock) {
            if (!this.isSocketExhausted() && this.reserved) {
                try {
                    // Read data
                    Object read = this.objectInputStream.readObject();

                    if (read == null) {
                        throw new InvalidTCPConnectionException("The used TCP connection may be invalid or the object could not be read.");
                    }

                    return read;
                } catch (IOException | ClassNotFoundException ignored) {
                    throw new InvalidTCPConnectionException("The used TCP connection may be invalid or the object could not be read.");
                }
            } else {
                throw new ClientException("The socket is exhausted or not reserved (IllegalStateException).");
            }
        }
    }

    /**
     * Sends a serializable object to the server.
     * This method does not throw exceptions upon failure.
     * @param serializableObject the object to send
     * @return true if the object was sent, false otherwise
     */
        private Boolean notthrow__sendSerializableObject(Object serializableObject) {
        // Always one thread at a time should reply using the same Socket.
        // This should not prevent listening.
        synchronized (this.sendLock) {
            try {
                // Writing the object to stream
                this.objectOutputStream.writeObject(serializableObject);
                this.objectOutputStream.flush();
                return true;
            } catch (IOException e) {
                // The object could not be written to the stream
                return false;
            }
        }
    }

    /**
     * Close the connection to the server.
     * This method is silent, that is it does not throw exceptions upon failure.
     */
    private void _silentClose() {
        try {
            if (!this.socket.isClosed()) {
                this.socket.close();
            }
        } catch (IOException ignored) {
            // Ignoring exceptions
        } finally {
            this._dispose();
        }
    }

    /**
     * Dispose of the socket and streams.
     */
    private void _dispose() {
        this.socket = null;
        this.objectOutputStream = null;
        this.objectInputStream = null;
        this.reserved = false;

        synchronized (this.heartbeatContinuitySignalLock) {
            this.heartbeatContinuitySignal = false;
        }
    }

    /**
     * Check if the socket is exhausted.
     * @return true if the socket is exhausted, false otherwise
     */
        private Boolean isSocketExhausted() {
        return this.socket == null &&
                this.objectInputStream == null &&
                this.objectOutputStream == null;
    }

    /**
     * Get a fresh socket.
     */
    private void getFreshSocket() {
        this.socket = new Socket();
    }

    /**
     * Attach the streams to the socket.
     * @throws IOException if the streams could not be attached
     */
    private void attachStreams() throws IOException {
        this.objectOutputStream = new ObjectOutputStream(this.socket.getOutputStream());
        this.objectInputStream = new ObjectInputStream(this.socket.getInputStream());
    }

    /**
     * Close the connection to the server.
     */
    public void close() {
        if (!this.isSocketExhausted() && this.reserved) {
            this._silentClose();
        }
    }

        public String getPublicIPAddress() {
        return this.publicIPAddress;
    }

        public String getPrivateIPAddress() {
        return this.privateIPAddress;
    }
}
