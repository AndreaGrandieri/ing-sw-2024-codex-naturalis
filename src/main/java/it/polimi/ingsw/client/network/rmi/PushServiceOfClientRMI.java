package it.polimi.ingsw.client.network.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface to expose the methods of the {@link PushServiceOfClient} class for RMI use.
 */
public interface PushServiceOfClientRMI extends Remote {
    /**
     * Method to push an event to the Client.
     * This has to be called by the Server to send an event to the Client.
     * The Server will be blocked until this method returns, so it should implement fail-safe logic to prevent deadlocks
     * or starvation.
     * @param object the event to push
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    void pushEvent(Object object) throws RemoteException;
}
