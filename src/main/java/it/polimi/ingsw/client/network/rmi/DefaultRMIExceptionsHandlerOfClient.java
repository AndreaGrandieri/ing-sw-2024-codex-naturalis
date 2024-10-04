package it.polimi.ingsw.client.network.rmi;

import it.polimi.ingsw.client.network.tcpip.ClientCriticalError;

/**
 * A class to handle the default critical exceptions of the Client when using RMI.
 */
public class DefaultRMIExceptionsHandlerOfClient {
    // To be used to handle critical RMI exceptions that prevent the Client from normal execution.
    // To be used when the Client encounters RMI exceptions that cannot safely recover from.
    // Equivalent of Socket centralized critical exceptions handling in Liveloop ecosystem.
    public static ClientCriticalError clientCriticalError = new ClientCriticalError("RMI communication failed. The RMI connection may have been lost.");
}
