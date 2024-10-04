package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.network.tcpip.ServerCriticalError;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * A class to handle the creation and destruction of RMI registries, and the exporting of objects.
 */
public class RMIServerHandler {
    private static final int DEFAULT_REGISTRY_PORT = 1099;
    private static Boolean registryCreated = false;
    private static Integer currentPort = -1;

    /**
     * Create a registry on the specified port.
     *
     * @param port the port to create the registry on
     * @throws RemoteException if the registry has already been created
     * @throws RemoteException if the registry could not be created
     */
    public static void createRegistry(Integer port) throws RemoteException {
        if (!registryCreated) {
            try {
                String localIP = InetAddress.getLocalHost().getHostAddress();
                System.setProperty("java.rmi.server.hostname", localIP);
            } catch (UnknownHostException e) {
                // Server cannot be started. The server won't work. Stopping execution.
                throw new ServerCriticalError("The server could not be started");
            }

            // Registry creation
            LocateRegistry.createRegistry(port);

            currentPort = port;
            registryCreated = true;
        } else {
            throw new RemoteException("Registry has already been created.");
        }
    }

    /**
     * Create a registry on the default port (1099).
     *
     * @throws RemoteException if the registry has already been created
     * @throws RemoteException if the registry could not be created
     */
    public static void createRegistry() throws RemoteException {
        RMIServerHandler.createRegistry(DEFAULT_REGISTRY_PORT);
    }

    /**
     * Destroy the existing registry.
     *
     * @throws RemoteException if the registry has not been created yet
     * @throws RemoteException if the registry could not be destroyed
     */
    public static void destroyRegistry() throws RemoteException {
        if (registryCreated) {
            // Locating the register
            Registry registry = LocateRegistry.getRegistry(currentPort);

            // Unexporting the registry
            UnicastRemoteObject.unexportObject(registry, true);

            currentPort = -1;
            registryCreated = false;
        } else {
            throw new RemoteException("Registry has not been created yet.");
        }
    }

    /**
     * Export an object using RMI.
     *
     * @param obj the object to export. The object must implement the Remote interface.
     * @throws RemoteException       if the registry has not been created yet
     * @throws RemoteException       if the object could not be exported
     * @throws MalformedURLException if the name is not an appropriately formatted URL
     */
    public static void exportObject(Remote obj, String name) throws RemoteException, MalformedURLException {
        if (!registryCreated) {
            throw new RemoteException("Registry has not been created yet.");
        }

        // Export the object
        Remote stub = UnicastRemoteObject.exportObject(obj, 0);

        // Bind the object to the registry
        Naming.rebind("rmi://localhost:" + currentPort + "/" + name, obj);
    }

    /**
     * Unexport an already exported object using RMI.
     *
     * @param name the name of the object to unexport
     * @param force if true, unexports the object even if there are pending calls;
     *              if false, only unexports the object if there are no pending calls
     * @return true if the object was unexported successfully, false otherwise
     * @throws NotBoundException if the object is not bound to the registry
     * @throws RemoteException if the registry has not been created yet
     * @throws RemoteException if the object could not be unexported
     */
        public static Boolean unexportObject(String name, Boolean force) throws NotBoundException, RemoteException {
        if (registryCreated) {
            // Getting the stub
            Remote stub = getStub(name);

            // Unexporting
            return UnicastRemoteObject.unexportObject(stub, force);
        } else {
            throw new RemoteException("Registry has not been created yet.");
        }
    }

    /**
     * Get the stub of an already exported object using RMI.
     *
     * @param name the name of the object to get the stub of
     * @return the stub of the object. The caller will have to cast the stub appropriately.
     * @throws RemoteException if the registry has not been created yet
     * @throws RemoteException if the object could not be get
     * @throws NotBoundException if the object is not bound to the registry
     */
        public static Remote getStub(String name) throws RemoteException, NotBoundException {
        if (registryCreated) {
            // Locating the register
            Registry registry = LocateRegistry.getRegistry(currentPort);

            return registry.lookup(name);
        } else {
            throw new RemoteException("Registry has not been created yet.");
        }
    }
}
