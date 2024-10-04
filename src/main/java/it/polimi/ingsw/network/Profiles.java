package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.MatchController;
import it.polimi.ingsw.controller.MatchControllerException;
import it.polimi.ingsw.logger.Logger;
import it.polimi.ingsw.network.liveloop.User;
import it.polimi.ingsw.network.liveloop.UserException;
import it.polimi.ingsw.network.rmi.ProfilesRMI;
import it.polimi.ingsw.network.rmi.RMIServerHandler;
import it.polimi.ingsw.network.rmi.UserStub;
import it.polimi.ingsw.network.tcpip.Server;
import it.polimi.ingsw.network.tcpip.ServerCriticalError;
import it.polimi.ingsw.network.tcpip.ServerException;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Class to save and handle all the Users connected to the Server.
 * When a Client connects and gets accepted to the Server, a User profile is created and saved in this class.
 * This class can be used to retrieve and manage the Users in the Server.
 * It is a singleton.
 */
public class Profiles implements ProfilesRMI {
    private static Profiles instance;

    private Profiles() {
        this.users = new HashSet<>();
        this.usersLock = new Object();
        this.heartbeatMs = HEARTBEAT_MS;
    }

        public static Profiles getInstance() {
        if (instance == null) {
            instance = new Profiles();
        }
        return instance;
    }

    /* Start of the class implementation */
    private final HashSet<User> users;
    private final Object usersLock;
    private final Integer heartbeatMs;
    private static final Integer HEARTBEAT_MS = 6500;

    /**
     * Get the User with the given connectionUUID.
     * @param connectionUUID The connectionUUID of the User to get.
     * @return The User with the given connectionUUID.
     * @throws ProfilesException If no User with the given connectionUUID is found.
     */
        public User getUserByConnectionUUID(String connectionUUID) throws ProfilesException {
        for (User user : this.users) {
            if (user.getConnectionUUID().equals(connectionUUID)) {
                return user;
            }
        }

        throw new ProfilesException("No user with the given connectionUUID found.", ProfilesException.Reason.ENTRY_NOT_FOUND);
    }

    /**
     * Get the User with the given username, provided that the User has a username associated.
     * @param username The username of the User to get.
     * @return The User with the given username.
     * @throws ProfilesException If no User with the given username is found.
     */
        public User getUserByUsername(String username) throws ProfilesException {
        for (User user : this.users) {
            if (username.equals(user.getUsername())) {
                return user;
            }
        }

        throw new ProfilesException("No user with the given username found.", ProfilesException.Reason.ENTRY_NOT_FOUND);
    }

    /**
     * Get the list of Users with the given usernames.
     * @param usernames The usernames of the Users to get.
     * @return The list of Users with the given usernames.
     */
        public List<User> getUsersByUsernameList(List<String> usernames) {
        List<User> users = new ArrayList<>();

        try {
            for (String username : usernames) {
                users.add(this.getUserByUsername(username));
            }
        } catch (ProfilesException e) {
            // Bad situation that should never happen and may indicate something is wrong with the Server
            // implementation. No way to safely recover from this. Will return an empty ArrayList and the
            // handler(s) using this function will not perform any action.
            return new ArrayList<>();
        }

        return users;
    }

    /**
     * Check if the given username is already taken by another User.
     * @param username The username to check.
     * @return True if the username is already taken, false otherwise.
     */
        public Boolean isUsernameTaken(String username) {
        for (User user : this.users) {
            if (user.getUsername() != null && user.getUsername().equals(username)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Create a new User with the given connectionUUID and add it to the list of Users.
     * The unicity of the connectionUUID is not responsibility of Profiles.
     * @param connectionUUID The connectionUUID of the new User.
     */
    public void createUser(String connectionUUID) {
        // Create a new User
        User user = new User(connectionUUID, false, null);

        synchronized (this.usersLock) {
            // Add the new User to the list
            this.users.add(user);
        }
    }

    /**
     * Create a new User with the given connectionUUID and add it to the list of Users.
     * To be used with RMI.
     * The unicity of the connectionUUID is not responsibility of Profiles.
     * @throws RemoteException If something goes wrong with RMI.
     * @throws MalformedURLException If something goes wrong with RMI.
     */
    @Override
    public void createUserRMI() throws RemoteException, MalformedURLException {
        // Create a new User
        String connectionUUID = UUID.randomUUID().toString();
        UserStub userStub = new UserStub(connectionUUID, this.heartbeatMs);

        synchronized (this.usersLock) {
            // Add the new User to the List
            this.users.add(userStub.getUser());
        }

        // Make the User obj available throught RMI
        RMIServerHandler.exportObject(userStub, "User");
    }

    /**
     * Set the username of the User with the given connectionUUID.
     * The unicity of the username is not responsibility of Profiles.
     * @param connectionUUID The connectionUUID of the User to set the username.
     * @param username The username to set.
     * @throws ProfilesException If no User with the given connectionUUID is found.
     * @throws UserException If the provided username is invalid based on the checks performed by User.
     */
    public void setUserUsername(String connectionUUID, String username) throws ProfilesException,
            UserException {
        User user = this.getUserByConnectionUUID(connectionUUID);

        synchronized (this.usersLock) {
            // Reached if ProfilesException is not thrown
            user.setUsername(username);
        }
    }

    /**
     * Removes the User with the given connectionUUID.
     * To remove an User it means to:
     * - Close the connection with the User.
     * - Remove it from the list of Users.
     * - Remove it from the MatchController (if it is in a Match).
     * - Remove it from the Lobby (if it is in a Lobby).
     * The method is silent: it does not throw any exception nor it logs any error if the pruning is unsuccessful.
     * @param connectionUUID The connectionUUID of the User to remove.
     * @param RMI True if the User is connected through RMI, false otherwise.
     */
    public void silentPruneUser(String connectionUUID, Boolean RMI) {
        Logger.logInfo("Pruning user " + connectionUUID);

        if (!RMI) {
            // First of all, let's disconnect the User (if it is not already disconnected).
            try {
                Server.getInstance().silentClose(connectionUUID);
            } catch (ServerException e) {
                throw new ServerCriticalError("Something went wrong with the Server network side.");
            }
        }

        // Then, the User may be in a Match (not for sure). So, let's remove it anyway.
        try {
            if (RMI) {
                MatchController.getInstance().exitMatchRMI(this.getUserByConnectionUUID(connectionUUID));
            } else {
                MatchController.getInstance().exitMatch(this.getUserByConnectionUUID(connectionUUID));
            }
        } catch (ProfilesException | MatchControllerException ignored) {
            // If here, it means that some search of the User in MatchController or/and Profiles failed: no problem:
            // the pruning may be already been called by someother Thread and thus already completed.
            // Silenced section.
        }

        // The user being in a Match and being removed from it causes its removal also from the associated lobby.
        // But... it may be that the above exitMatch thrown an exception. So, to be really sure, let's remove
        // the User also from the lobby.
        try {
            if (RMI) {
                MatchController.getInstance().exitLobbyRMI(this.getUserByConnectionUUID(connectionUUID));
            } else {
                MatchController.getInstance().exitLobby(this.getUserByConnectionUUID(connectionUUID));
            }
        } catch (ProfilesException | MatchControllerException ignored) {
            // If here, it means that some search of the User in MatchController or/and Profiles failed: no problem:
            // the pruning may be already been called by someother Thread and thus already completed.
            // Silenced section.
        }

        synchronized (this.usersLock) {
            // Then, the User may be on Profiles (it depends on what exception was thrown). So, let's remove it
            // anyway.
            // Find the User witn connectionUUID: connectionUUID and remove it from users.
            // If not found, do nothing.
            this.users.removeIf(user -> user.getConnectionUUID().equals(connectionUUID));
        }
    }
}
