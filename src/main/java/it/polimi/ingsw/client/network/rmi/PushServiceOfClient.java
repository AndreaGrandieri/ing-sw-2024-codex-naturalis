package it.polimi.ingsw.client.network.rmi;

import it.polimi.ingsw.client.network.liveloop.UserOfClient;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.tcpip.Sense;

import java.rmi.RemoteException;

/**
 * This class represents the RMI implementation of the PushService exposed by the Client to the Server.
 */
public class PushServiceOfClient implements PushServiceOfClientRMI {
    private UserOfClient userOfClient;

    public PushServiceOfClient() {
        this.userOfClient = null;
    }

    /*
    THIS METHOD IS CLIENT-IMPLEMEMTED! SERVER MUST ASSUME HAVING NO CONTROL OVER THIS!

    Be aware of the delicate logic here: this method is called by the Server when it wants
    to send some asynchronous event to the Client. This seems harmless but IS the Server thread that
    is invoking this function that will be "halted" to wait till this function returns.
    "Slow or wait logic" here will propagate the "slow or wait logic" to the Server!
    To avoid this, it can be useful IN THE SERVER to call this function in a dedicated fresh THREAD so
    that the Server is safe and indipendent from "slow or wait logic" that may arise by bad Client
    implementation.
     */
    /**
     * Method to push an event to the Client.
     * The Server should use this method to send an event to the Client.
     * @param object the event to push
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    @Override
    public void pushEvent(Object object) throws RemoteException {
        if (!(object instanceof Sense)) {
            // Basically same as the "react" in UserOfClient
            this.userOfClient.react((Message) object);
        } else {
            this.userOfClient.getLastSense().set(System.currentTimeMillis());
        }
    }

    public void setUserOfClient(UserOfClient userOfClient) {
        this.userOfClient = userOfClient;
    }
}
