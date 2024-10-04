package it.polimi.ingsw.client;

import it.polimi.ingsw.client.network.ProfileOfClient;
import it.polimi.ingsw.client.network.liveloop.UserOfClient;
import it.polimi.ingsw.client.network.rmi.DefaultRMIExceptionsHandlerOfClient;
import it.polimi.ingsw.client.network.rmi.PushServiceOfClient;
import it.polimi.ingsw.client.network.rmi.PushServiceOfClientRMI;
import it.polimi.ingsw.client.network.rmi.RMIClientHandler;
import it.polimi.ingsw.client.network.state.ChooseCreateJoinStateOfClient;
import it.polimi.ingsw.client.network.state.StateOfClient;
import it.polimi.ingsw.client.network.tcpip.Client;
import it.polimi.ingsw.client.network.tcpip.ClientException;
import it.polimi.ingsw.controller.interfaces.ConnectionManagerI;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.clienttoserver.UsernameMessage;
import it.polimi.ingsw.network.rmi.ProfilesRMI;
import it.polimi.ingsw.network.rmi.UserStubRMI;
import it.polimi.ingsw.network.tcpip.InvalidTCPConnectionException;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.function.Consumer;

import static it.polimi.ingsw.network.messages.MessageType.*;

public class ConnectionManager implements ConnectionManagerI {
    private final UserOfClient user;
    private NetworkClientGameController clientGameController;
    private NetworkClientChatController clientChatController;
    private final NetworkClientMatchController clientMatchController;
    private final Boolean RMI;
    private final UserStubRMI userStub;
    private String username;
    private static final Integer HEARTBEAT_MS = 3250;
    private static final Integer SENSE_MS = 6500;

    public ConnectionManager(String address, int port, Boolean RMI,
                             Consumer<Void> deathByConnectionLost) throws
            InvalidTCPConnectionException,
            ClientException,
            NotBoundException,
            RemoteException,
            MalformedURLException {
        if (deathByConnectionLost == null) {
            deathByConnectionLost = (Void v) -> {
                System.out.println("Connection with the Server was lost. Please check your network.");
                System.exit(-1);
            };
        }

        this.RMI = RMI;

        if (!RMI) {
            this.userStub = null;

            // Connect
            Client client = new Client(3250, 6500);
            client.connectReserve(address, port);
            user = new UserOfClient(client, false, null, null, null, deathByConnectionLost);
            ProfileOfClient.restartSingletonAndGetInstance(user);
            user.startListenLoop();
        } else {
            // Creating PushServiceOfClient
            PushServiceOfClient p = new PushServiceOfClient();

            // Connecting to RMI
            RMIClientHandler.setRegistryIPPort(address, port);

            // Getting Profiles obj
            ProfilesRMI profiles = (ProfilesRMI) RMIClientHandler.getRemoteStub("Profiles");

            // Creating User
            profiles.createUserRMI();
            UserStubRMI myUser = (UserStubRMI) RMIClientHandler.getRemoteStub("User");

            this.userStub = myUser;

            // Setting push service
            PushServiceOfClientRMI pRMI = (PushServiceOfClientRMI) RMIClientHandler.exportObject(p);
            myUser.registerPushService(pRMI);

            user = new UserOfClient(null, true, myUser, HEARTBEAT_MS, SENSE_MS, deathByConnectionLost);
            p.setUserOfClient(user);
            ProfileOfClient.restartSingletonAndGetInstance(user);
        }

        clientGameController = null;
        clientChatController = null;
        clientMatchController = new NetworkClientMatchController(user, this.RMI);
        user.setCmc(clientMatchController);
    }

    public NetworkClientGameController clientGameController() {
        return clientGameController;
    }

    public NetworkClientChatController clientChatController() {
        return clientChatController;
    }

    public NetworkClientMatchController clientMatchController() {
        return clientMatchController;
    }

    public synchronized boolean exitMatch() {
        if (clientMatchController.exitMatch()) {
            destroyAndRebuildClientChatController(username);
            destroyAndRebuildClientGameController(username);
            return true;
        }
        return false;
    }

    public synchronized boolean exitLobby() {
        if (clientMatchController.exitLobby()) {
            destroyAndRebuildClientChatController(username);
            destroyAndRebuildClientGameController(username);
            return true;
        }
        return false;
    }

    public synchronized boolean registerUsername(String username) {
        StateOfClient oldState = this.user.getState();
        user.setState(new ChooseCreateJoinStateOfClient(user));

        boolean accepted;

        if (clientGameController != null)
            return false;

        if (!this.RMI) {
            this.username = username;
            UsernameMessage message = new UsernameMessage(this.username);
            Message answer = user.sendAndWaitMultiple(message, List.of(
                    USERNAME_CONFIRMED,
                    USERNAME_ALREADY_TAKEN,
                    USERNAME_NOT_VALID,
                    UNKNOWN_ERROR));
            accepted = answer.getType() == USERNAME_CONFIRMED;
        } else {
            try {
                accepted = this.userStub.setUsernameRMI(username);
            } catch (RemoteException e) {
                throw DefaultRMIExceptionsHandlerOfClient.clientCriticalError;
            }
        }

        if (accepted) {
            clientGameController = new NetworkClientGameController(username, user, this.RMI);
            user.setCgc(clientGameController);
            clientChatController = new NetworkClientChatController(user, username, this.RMI);
            user.setCcc(clientChatController);
        } else {
            this.user.setState(oldState);
        }

        return accepted;
    }

    public void destroyAndRebuildClientChatController(String username) {
        clientChatController = null;
        user.setCcc(null);
        clientChatController = new NetworkClientChatController(user, username, this.RMI);
        user.setCcc(clientChatController);
    }

    public void destroyAndRebuildClientGameController(String username) {
        clientGameController = null;
        user.setCgc(null);
        clientGameController = new NetworkClientGameController(username, user, this.RMI);
        user.setCgc(clientGameController);
    }

    public UserOfClient getUser() {
        return user;
    }
}
