package it.polimi.ingsw.network;

import it.polimi.ingsw.cli.IOManager;
import it.polimi.ingsw.network.rmi.RMIServerHandler;
import it.polimi.ingsw.network.tcpip.Server;
import it.polimi.ingsw.network.tcpip.ServerException;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

/**
 * Main class to start the server.
 */
public class Main {
    public static void main(String[] args) throws
            ProfilesException,
            InterruptedException,
            RemoteException,
            MalformedURLException,
            ServerException {
        // Initialize default arguments
        int port = 40000;
        boolean rmi = false;

        // Check if we have to run help command
        for (String arg : args) {
            if (arg.equals("--help")) {
                showHelp();
                errorExit();
            }
        }

        if (args.length != 0 && args.length != 1 && args.length != 2) {
            showHelp();
            errorExit();
        } else {
            if (args.length >= 1) {
                try {
                    port = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    System.out.println("PORT must be a number");
                    errorExit();
                }

                if (args.length == 2) {
                    rmi = args[1].equals("--rmi");
                }
            }
        }

        // Logger.setLogLevel(Logger.LogLevel.NONE);

        System.out.println("Address: " + Server.staticGetPrivateIPAddress());
        IOManager.println("Port: " + port);
        IOManager.println("RMI: " + rmi);

        if (!rmi) {
            Server.getInstance(port);
            WelcomingThread.getInstance().start();

            System.out.println("Socket Server started!");
        } else {
            RMIServerHandler.createRegistry(port);

            // Exporting entry-point object
            RMIServerHandler.exportObject(Profiles.getInstance(), "Profiles");

            System.out.println("RMI Server started!");
        }
    }

    /**
     * Show the help message.
     */
    private static void showHelp() {
        System.out.println("""
                Usage: server.jar PORT [ARG]
                
                With zero arguments, default to port 40000 and socket mode.
                With no ARG, default is to use socket.
                Server always run on the host local-network IP address.
                
                Options:
                --socket        Use sockets for connection
                --rmi           Use RMI for connection
                --help          Show this help and exit
                """);
    }

    /**
     * Exit with error code 1.
     */
    private static void errorExit() {
        System.exit(1);
    }
}
