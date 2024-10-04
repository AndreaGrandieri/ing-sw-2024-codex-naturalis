package it.polimi.ingsw.cli;

import it.polimi.ingsw.logger.Logger;

public class Main {
    public static void main(String[] args) {
        // Initialize default arguments
        String address = "localhost";
        int port = 40000;
        boolean rmi = false;

        // Check if we have to run help command
        for (String arg : args) {
            if (arg.equals("--help")) {
                showHelp();
                errorExit();
            }
        }

        if (args.length != 0 && args.length != 2 && args.length != 3) {  // Because we have IP PORT [ARG]
            showHelp();
            errorExit();
        } else {
            if (args.length > 1) {
                address = args[0];
                try {
                    port = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    IOManager.error("PORT must be a number");
                    errorExit();
                }
                if (args.length == 3) {
                    rmi = args[2].equals("--rmi");
                }
            }
        }

        Logger.setLogLevel(Logger.LogLevel.NONE);

        IOManager.println("Address: " + address);
        IOManager.println("Port: " + port);
        IOManager.println("RMI: " + rmi);

        CliApplication app = new CliApplication(address, port, rmi);
        app.runGame();
    }

    private static void showHelp() {
        System.out.println("""
                Usage: cli.jar IP PORT [ARG].
                
                With zero arguments, default to localhost, port 40000 and socket mode.
                With no ARG, default is to use socket.
                
                Options:
                --socket        Use sockets for connection
                --rmi           Use RMI for connection
                --help          Show this help and exit""");
    }

    private static void errorExit() {
        System.exit(1);
    }
}
