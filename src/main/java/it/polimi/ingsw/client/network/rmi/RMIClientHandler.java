package it.polimi.ingsw.client.network.rmi;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * A class to handle the retrieval of remote stubs using RMI.
 */
public class RMIClientHandler {
    private static final int DEFAULT_REGISTRY_PORT = 1099;
    private static String registryIP = null;
    private static Integer registryPort = null;

    /**
     * Set the IP address and the port of the registry to connect to.
     *
     * @param registryIP the IP address of the registry
     * @param registryPort the port of the registry
     */
    public static void setRegistryIPPort(String registryIP, Integer registryPort) {
        RMIClientHandler.registryIP = registryIP;
        RMIClientHandler.registryPort = registryPort;
    }

    /**
     * Set the IP address of the registry to connect to.
     *
     * @param registryIP the IP address of the registry
     */
    public static void setRegistryIP(String registryIP) {
        RMIClientHandler.setRegistryIPPort(registryIP, DEFAULT_REGISTRY_PORT);
    }

    /**
     * Get the remote stub for the specified object.
     *
     * @param name the name of the object to get the remote stub for
     * @return the remote stub for the specified object. The caller will have to cast the stub appropriately.
     * @throws RemoteException if the registry IP has not been set yet or if the remote stub could not be found
     * @throws NotBoundException if the remote stub could not be found
     */
        public static Remote getRemoteStub(String name) throws RemoteException, NotBoundException {
        if (registryIP == null) {
            throw new RemoteException("Registry IP has not been set yet.");
        }

        Registry registry = LocateRegistry.getRegistry(registryIP, registryPort);

        return registry.lookup(name);
    }

    /**
     * Get the list of remote stubs in the registry.
     *
     * @return the list of remote stubs in the registry
     * @throws RemoteException if the registry IP has not been set yet
     * @throws RemoteException if the registry could not be found
     */
        public static List<String> getListOfRemoteStubs() throws RemoteException {
        if (registryIP == null) {
            throw new RemoteException("Registry IP has not been set yet.");
        }

        Registry registry = LocateRegistry.getRegistry(registryIP, registryPort);

        return List.of(registry.list());
    }

    /**
     * Export an object using RMI.
     *
     * @param obj the object to export. The object must implement the Remote interface.
     * @return the stub of the object. The caller will have to cast the stub appropriately.
     * @throws RemoteException if the registry has not been created yet or if the object could not be exported
     */
        public static Remote exportObject(Remote obj) throws RemoteException {
        if (registryIP == null) {
            throw new RemoteException("Registry IP has not been set yet.");
        }

        // Export the object
        return UnicastRemoteObject.exportObject(obj, 0);
    }
}
