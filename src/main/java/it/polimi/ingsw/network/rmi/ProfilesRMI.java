package it.polimi.ingsw.network.rmi;

import java.net.MalformedURLException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface to expose the methods of the Profiles class for RMI use.
 */
public interface ProfilesRMI extends Remote {
    /**
     * Create a user in an RMI communication.
     * @throws RemoteException if something goes wrong with the RMI communication
     * @throws MalformedURLException if something goes wrong with the RMI communication
     */
    void createUserRMI() throws RemoteException, MalformedURLException;
}
