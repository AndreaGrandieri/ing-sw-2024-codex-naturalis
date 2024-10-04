package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.client.network.rmi.PushServiceOfClientRMI;
import it.polimi.ingsw.controller.GameFlow;
import it.polimi.ingsw.controller.MatchController;
import it.polimi.ingsw.controller.MatchControllerException;
import it.polimi.ingsw.model.card.GoalCard;
import it.polimi.ingsw.model.card.StarterCard;
import it.polimi.ingsw.model.card.TypedCard;
import it.polimi.ingsw.model.card.properties.CardFace;
import it.polimi.ingsw.model.game.CardType;
import it.polimi.ingsw.model.game.CommonBoard;
import it.polimi.ingsw.model.game.gamelobby.GameLobbyException;
import it.polimi.ingsw.model.game.gamelobby.LobbyInfo;
import it.polimi.ingsw.model.player.ManuscriptPosition;
import it.polimi.ingsw.model.player.PlayerColor;
import it.polimi.ingsw.model.player.PlayerData;
import it.polimi.ingsw.model.player.PlayerManuscript;
import it.polimi.ingsw.network.Profiles;
import it.polimi.ingsw.network.ProfilesException;
import it.polimi.ingsw.network.liveloop.User;
import it.polimi.ingsw.network.liveloop.UserException;
import it.polimi.ingsw.network.messages.servertoclient.ListOfLobbyToJoinMessage;
import it.polimi.ingsw.network.state.ChooseCreateJoinState;
import it.polimi.ingsw.network.state.StateType;
import it.polimi.ingsw.network.tcpip.Heartbeat;
import it.polimi.ingsw.network.tcpip.Sense;
import it.polimi.ingsw.network.tcpip.ServerCriticalError;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class UserStub implements UserStubRMI {
    private final User user;
    private PushServiceOfClientRMI pushService;
    private final AtomicLong lastHeartbeat;
    private final ScheduledExecutorService executorService;

    public UserStub(String connectionUUID, Integer heartbeatMs) throws RemoteException {
        this.user = new User(connectionUUID, true, this);
        this.pushService = null;
        this.lastHeartbeat = new AtomicLong(System.currentTimeMillis());
        this.executorService = Executors.newSingleThreadScheduledExecutor();

        this.executorService.scheduleAtFixedRate(() -> {
            // If more than heartbeatMs have passed since the last heartbeat
            if (System.currentTimeMillis() - this.lastHeartbeat.get() > heartbeatMs) {
                // RMI disconnection: unexporting the stub
                try {
                    RMIServerHandler.unexportObject("User", true);
                } catch (NotBoundException | RemoteException e) {
                    throw new ServerCriticalError("Something went wrong in RMI resource management.");
                } finally {
                    // User pruning: this ensures no locking around the Server is caused by dead Client(s)
                    Profiles.getInstance().silentPruneUser(connectionUUID, true);
                    this.executorService.shutdownNow();
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

        public User getUser() {
        return user;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void registerPushService(PushServiceOfClientRMI pushService) throws RemoteException {
        if (this.pushService != null) {
            // registerPushService will reject other assignments after the first one
            return;
        }

        // registerPushService will reject null assignments
        if (pushService != null) {
            this.pushService = pushService;
        }
    }

    @Override
    public void heartbeat(Heartbeat heartbeat) throws RemoteException {
        this.lastHeartbeat.set(System.currentTimeMillis());
        this.pushEvent(new Sense());
    }

    public void pushEvent(Object object) throws RemoteException {
        if (this.pushService == null) {
            // pushEvent will reject event pushing if the pushService has not been
            // set yet.
            return;
        }

        // Logic assures that once the pushService has become != null it cannot be changed again
        // using registerPushService. Anyway, it can become invalid due to Client-side manipulations.

        // Thread run CANNOT throw checked exceptions.
        // Workaround is to use Callable and Future
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Void> future = executorService.submit(() -> {
            // This throws RemoteException checked exception
            pushService.pushEvent(object);
            return null;
        });

        try {
            future.get();
        } catch (InterruptedException e) {
            // An InterruptedException signals a critical error in the Server's thread(s) execution.
            // This situation ought to never be encountered and there's no precise way to recover from this.
            throw new ServerCriticalError("Server encountered an InterruptedException in its thread(s) flow.");
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RemoteException) {
                // Server could not push the event. This is a problem, but should absolutely not interfere
                // with the whole Server status. Usually this means that the Client's registered push service has
                // become invalid. If the Client attemps to take down the Server with push service invalidation
                // it will be handled here to protect the whole Server status.
                // Reaction depends on the application; in this case this is indicating that the Client disconnected
                // (gracefully or not doesn't matter). We are just gonna throw the exception and let the caller
                // handle the disconnection.
                throw (RemoteException) e.getCause();
            }

            // ExecutionException is not of type RemoteException
            // This situation can be akward and cannot be safely identified.
            // This situation ought to never be encountered and there's no precise way to recover from this.
            throw new ServerCriticalError("Server encountered an ExecutionException in its thread(s) flow.");
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*
    SettingUsernameState
     */

    @Override
        public Boolean setUsernameRMI(String username) throws RemoteException {
        if (this.user.getState().getStateType() != StateType.SETTINGUSERNAME) {
            return false;
        }

        if (!Profiles.getInstance().isUsernameTaken(username)) {
            try {
                Profiles.getInstance().setUserUsername(this.user.getConnectionUUID(), username);

                // State has to be here since the handling is out of the State override
                this.user.setState(new ChooseCreateJoinState(this.user));
                return true;
            } catch (ProfilesException | UserException e) {
                // Error, but preventing Server crash.
                return false;
            }
        }

        return false;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*
    ChooseCreateJoinState
     */

    @Override
        public Boolean createLobbyRMI(String name, Integer maxPlayers) throws RemoteException {
        if (this.user.getState().getStateType() != StateType.CHOOSECREATEJOIN) {
            return false;
        }

        try {
            MatchController.getInstance().createLobbyRMI(name, maxPlayers, this.user);
            return true;
        } catch (GameLobbyException | MatchControllerException e) {
            return false;
        }
    }

    @Override
        public Boolean joinLobbyRMI(String lobbyUUID) throws RemoteException {
        if (this.user.getState().getStateType() != StateType.CHOOSECREATEJOIN) {
            return false;
        }

        try {
            MatchController.getInstance().joinLobbyRMI(lobbyUUID, this.user);
            return true;
        } catch (GameLobbyException | MatchControllerException e) {
            return false;
        }
    }

    @Override
        public List<ListOfLobbyToJoinMessage.LobbyInfo> getListOfLobbyToJoinRMI() throws RemoteException {
        if (this.user.getState().getStateType() != StateType.CHOOSECREATEJOIN) {
            return null;
        }

        try {
            return MatchController.getInstance().getListOfLobbyToJoinRMI(this.user);
        } catch (MatchControllerException e) {
            return null;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*
    InLobbyState
     */

    @Override
        public LobbyInfo getLobbyInfoRMI() throws RemoteException {
        if (this.user.getState().getStateType() == StateType.INLOBBY ||
        this.user.getState().getStateType() == StateType.INGAME) {
            try {
                return MatchController.getInstance().getLobbyInfoRMI(this.user);
            } catch (MatchControllerException e) {
                // Error, but preventing Server crash.
                return null;
            }
        }

        return null;
    }

    @Override
        public Boolean startLobbyRMI() throws RemoteException {
        if (this.user.getState().getStateType() != StateType.INLOBBY) {
            return false;
        }

        try {
            MatchController.getInstance().startLobbyRMI(this);
            return true;
        } catch (MatchControllerException e) {
            // Error, but preventing Server crash.
            return false;
        }
    }

    @Override
        public Boolean exitLobbyRMI() throws RemoteException {
        if (this.user.getState().getStateType() != StateType.INLOBBY) {
            return false;
        }

        try {
            MatchController.getInstance().exitLobbyRMI(this.user);
            return true;
        } catch (MatchControllerException e) {
            return false;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*
    InGameState
     */

    @Override
        public StarterCard getStarterCard(String username) throws RemoteException {
        if (this.user.getState().getStateType() != StateType.INGAME) {
            return null;
        }

        try {
            return user.getGameController().getStarterCard(username);
        } catch (RuntimeException e) {
            // This handles any RuntimeException that may have not been catched (due to the unchecked nature
            // of the exception...)
            return null;
        }
    }

    @Override
        public Boolean setStarterCard(String username, CardFace cardFace) throws RemoteException {
        if (this.user.getState().getStateType() != StateType.INGAME) {
            return false;
        }

        try {
            return user.getGameController().setStarterCard(username, cardFace);
        } catch (RuntimeException e) {
            // This handles any RuntimeException that may have not been catched (due to the unchecked nature
            // of the exception...)
            return false;
        }
    }

    @Override
        public List<PlayerColor> getAvailableColors() throws RemoteException {
        if (this.user.getState().getStateType() != StateType.INGAME) {
            return null;
        }

        try {
            return user.getGameController().getAvailableColors();
        } catch (RuntimeException e) {
            // This handles any RuntimeException that may have not been catched (due to the unchecked nature
            // of the exception...)
            return null;
        }
    }

    @Override
        public Boolean setPlayerColor(String username, PlayerColor playerColor) throws RemoteException {
        if (this.user.getState().getStateType() != StateType.INGAME) {
            return false;
        }

        try {
            return user.getGameController().setPlayerColor(username, playerColor);
        } catch (RuntimeException e) {
            // This handles any RuntimeException that may have not been catched (due to the unchecked nature
            // of the exception...)
            return false;
        }
    }

    @Override
        public List<GoalCard> getProposedPrivateGoals(String username) throws RemoteException {
        if (this.user.getState().getStateType() != StateType.INGAME) {
            return null;
        }

        try {
            return user.getGameController().getProposedPrivateGoals(username);
        } catch (RuntimeException e) {
            // This handles any RuntimeException that may have not been catched (due to the unchecked nature
            // of the exception...)
            return null;
        }
    }

    @Override
        public Boolean choosePrivateGoal(String username, GoalCard goalCard) throws RemoteException {
        if (this.user.getState().getStateType() != StateType.INGAME) {
            return false;
        }

        try {
            return user.getGameController().choosePrivateGoal(username, goalCard);
        } catch (RuntimeException e) {
            // This handles any RuntimeException that may have not been catched (due to the unchecked nature
            // of the exception...)
            return false;
        }
    }

    @Override
        public PlayerData getPlayerData(String username) throws RemoteException {
        if (this.user.getState().getStateType() != StateType.INGAME) {
            return null;
        }

        try {
            PlayerData playerData;
            if (username.equals(user.getUsername())) {
                playerData = user.getGameController().getPlayerData(username);
            } else {
                playerData = user.getGameController().getCleanPlayerData(username);
            }

            return playerData;
        } catch (RuntimeException e) {
            // This handles any RuntimeException that may have not been catched (due to the unchecked nature
            // of the exception...)
            return null;
        }
    }

    @Override
        public List<PlayerData> getAllPlayerData(String username) throws RemoteException {
        if (this.user.getState().getStateType() != StateType.INGAME) {
            return null;
        }

        try {
            return user.getGameController().getAllPlayerData(username);
        } catch (RuntimeException e) {
            // This handles any RuntimeException that may have not been catched (due to the unchecked nature
            // of the exception...)
            return null;
        }
    }

    @Override
        public PlayerManuscript getManuscript(String username) throws RemoteException {
        if (this.user.getState().getStateType() != StateType.INGAME) {
            return null;
        }

        try {
            return user.getGameController().getManuscript(username);
        } catch (RuntimeException e) {
            // This handles any RuntimeException that may have not been catched (due to the unchecked nature
            // of the exception...)
            return null;
        }
    }

    @Override
        public GameFlow getGameFlow() throws RemoteException {
        if (this.user.getState().getStateType() != StateType.INGAME) {
            return null;
        }

        try {
            return user.getGameController().getGameFlow();
        } catch (RuntimeException e) {
            // This handles any RuntimeException that may have not been catched (due to the unchecked nature
            // of the exception...)
            return null;
        }
    }

    @Override
        public CommonBoard getGameBoard() throws RemoteException {
        if (this.user.getState().getStateType() != StateType.INGAME) {
            return null;
        }

        try {
            return user.getGameController().getGameBoard();
        } catch (RuntimeException e) {
            // This handles any RuntimeException that may have not been catched (due to the unchecked nature
            // of the exception...)
            return null;
        }
    }

    @Override
        public Boolean placeCard(String username, TypedCard card, ManuscriptPosition position)
            throws RemoteException {
        if (this.user.getState().getStateType() != StateType.INGAME) {
            return false;
        }

        try {
            return user.getGameController().placeCard(username, card, position);
        } catch (RuntimeException e) {
            // This handles any RuntimeException that may have not been catched (due to the unchecked nature
            // of the exception...)
            return false;
        }
    }

    @Override
        public TypedCard drawVisibleCard(String username, CardType type, int index) throws RemoteException {
        if (this.user.getState().getStateType() != StateType.INGAME) {
            return null;
        }

        try {
            return user.getGameController().drawVisibleCard(username, type, index);
        } catch (RuntimeException e) {
            // This handles any RuntimeException that may have not been catched (due to the unchecked nature
            // of the exception...)
            return null;
        }
    }

    @Override
        public TypedCard drawCoveredCard(String username, CardType type) throws RemoteException {
        if (this.user.getState().getStateType() != StateType.INGAME) {
            return null;
        }

        try {
            return user.getGameController().drawCoveredCard(username, type);
        } catch (RuntimeException e) {
            // This handles any RuntimeException that may have not been catched (due to the unchecked nature
            // of the exception...)
            return null;
        }
    }

    @Override
        public Boolean getGameEnded() throws RemoteException {
        if (this.user.getState().getStateType() != StateType.INGAME) {
            return false;
        }

        try {
            return user.getGameController().gameEnded();
        } catch (RuntimeException e) {
            // This handles any RuntimeException that may have not been catched (due to the unchecked nature
            // of the exception...)
            return false;
        }
    }

    @Override
        public String getWinner() throws RemoteException {
        if (this.user.getState().getStateType() != StateType.INGAME) {
            return null;
        }

        try {
            return user.getGameController().getWinner();
        } catch (RuntimeException e) {
            // This handles any RuntimeException that may have not been catched (due to the unchecked nature
            // of the exception...)
            return null;
        }
    }

    @Override
        public Boolean exitMatchRMI() throws RemoteException {
        if (this.user.getState().getStateType() != StateType.INGAME) {
            return false;
        }

        try {
            try {
                MatchController.getInstance().exitMatchRMI(this.user);
                return true;
            } catch (MatchControllerException e) {
                return false;
            }
        } catch (RuntimeException e) {
            // This handles any RuntimeException that may have not been catched (due to the unchecked nature
            // of the exception...)
            return false;
        }
    }

    // Chat-related commands

    @Override
        public Boolean sendBroadcast(String payload) throws RemoteException {
        if (this.user.getState().getStateType() != StateType.INLOBBY && this.user.getState().getStateType() != StateType.INGAME) {
            return false;
        }

        return user.getChatController().sendBroadcastMessage(payload, user.getUsername());
    }

    @Override
        public Boolean sendPrivate(String payload, String recipient) throws RemoteException {
        if (this.user.getState().getStateType() != StateType.INLOBBY && this.user.getState().getStateType() != StateType.INGAME) {
            return false;
        }

        return user.getChatController().sendPrivateMessage(payload, user.getUsername(), recipient);
    }

    @Override
    public List<String> getRecipients() throws RemoteException {
        if (this.user.getState().getStateType() != StateType.INLOBBY && this.user.getState().getStateType() != StateType.INGAME) {
            return null;
        }

        return user
                .getChatController()
                .getChatDistributionList()
                .stream()
                .filter(s -> !s.equals(user.getUsername()))
                .toList();
    }
}
