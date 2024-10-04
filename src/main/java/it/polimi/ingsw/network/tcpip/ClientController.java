package it.polimi.ingsw.network.tcpip;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Describes a representation of a client in the server side.
 */
public class ClientController {
    /**
     * Socket for TCP communication with the client (OPPOSITE_NODE).
     */
    private Socket socket;

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
     * Lock for sending operations.
     * To be used to prevent multiple threads from sending data at the same time: this is a problem and could
     * lead to data corruption.
     */
    private final Object sendLock;

    /**
     * Lock for reading operations.
     * To be used to prevent multiple threads from reading data at the same time: data won't be corrupted, but
     * threads could "steal" data from each other.
     */
    private final Object readLock;

    /**
     * Creates a new client controller with the provided socket.
     * @param socket Socket for TCP communication with the client.
     * @throws IOException If the streams could not be created.
     */
    ClientController(Socket socket) throws IOException {
        this.socket = socket;

        this.objectOutputStream = new ObjectOutputStream(this.socket.getOutputStream());
        this.objectInputStream = new ObjectInputStream(this.socket.getInputStream());

        this.sendLock = new Object();
        this.readLock = new Object();
    }

    /**
     * Reads a serializable object from the Client.
     * This method is blocking: it will wait until an object is received or disconnection is detected.
     * @return The object read from the Client.
     * @throws InvalidTCPConnectionException If the connection is invalid or the object could not be read.
     */
        Object readSerializableObject() throws InvalidTCPConnectionException {
        // Always one thread at a time should lock-and-listen the same Socket.
        // This should not prevent replying.
        synchronized (this.readLock) {
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
        }
    }

    /**
     * Sends a serializable object to the Client.
     * This method will return upon finishing the write operation with success or failure.
     * This method will not throw exceptions, but return a boolean indicating success or failure.
     * @param serializableObjectOutLocal The object to send.
     * @return True if the object was sent successfully, false otherwise.
     */
        Boolean notthrow__sendSerializableObject(Object serializableObjectOutLocal) {
        // Always one thread at a time should reply using the same Socket.
        // This should not prevent listening.
        synchronized (this.sendLock) {
            try {
                // Writing the object to stream
                this.objectOutputStream.writeObject(serializableObjectOutLocal);
                this.objectOutputStream.flush();
                return true;
            } catch (IOException e) {
                // The object could not be written to the stream
                return false;
            }
        }
    }

    /**
     * Closes the connection with the Client.
     * It is silent, meaning that it does not throw exceptions upon failure.
     */
    void silentClose() {
        // Closing everything possible, all together
        try {
            this.objectInputStream.close();
        } catch (IOException ignored) {
            // Ignoring exceptions
        } finally {
            this.objectInputStream = null;
        }

        try {
            this.objectOutputStream.close();
        } catch (IOException ignored) {
            // Ignoring exceptions
        } finally {
            this.objectOutputStream = null;
        }

        try {
            this.socket.close();
        } catch (IOException ignored) {
            // Ignoring exceptions
        } finally {
            this.socket = null;
        }
    }

    /**
     * Gets the socket of the Client.
     * @return The socket of the Client.
     */
        Socket getSocket() {
        return this.socket;
    }
}
