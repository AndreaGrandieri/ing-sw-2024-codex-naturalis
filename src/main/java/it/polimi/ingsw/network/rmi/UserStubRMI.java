package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.client.network.rmi.PushServiceOfClientRMI;
import it.polimi.ingsw.controller.GameFlow;
import it.polimi.ingsw.model.card.GoalCard;
import it.polimi.ingsw.model.card.StarterCard;
import it.polimi.ingsw.model.card.TypedCard;
import it.polimi.ingsw.model.card.properties.CardFace;
import it.polimi.ingsw.model.game.CardType;
import it.polimi.ingsw.model.game.CommonBoard;
import it.polimi.ingsw.model.game.gamelobby.LobbyInfo;
import it.polimi.ingsw.model.player.ManuscriptPosition;
import it.polimi.ingsw.model.player.PlayerColor;
import it.polimi.ingsw.model.player.PlayerData;
import it.polimi.ingsw.model.player.PlayerManuscript;
import it.polimi.ingsw.network.messages.servertoclient.ListOfLobbyToJoinMessage;
import it.polimi.ingsw.network.tcpip.Heartbeat;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Interface to expose the methods of the UserStub class for RMI use.
 */
public interface UserStubRMI extends Remote {
    /**
     * Register a PushServiceOfClient to the UserStub.
     * @param pushService the PushServiceOfClient to register
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    void registerPushService(PushServiceOfClientRMI pushService) throws RemoteException;

    /**
     * Sends an heartbeat to the RMI Server.
     * @param heartbeat the Heartbeat object
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    void heartbeat(Heartbeat heartbeat) throws RemoteException;

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*
    SettingUsernameState
     */

    /**
     * Set the username of the User in an RMI communication.
     * @param username the username to set
     * @return true if the username has been set, false otherwise
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    Boolean setUsernameRMI(String username) throws RemoteException;

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*
    ChooseCreateJoinState
     */

    /**
     * Create a lobby in an RMI communication.
     * @param name the name of the lobby
     * @param maxPlayers the maximum number of players in the lobby
     * @return true if the lobby has been created, false otherwise
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    Boolean createLobbyRMI(String name, Integer maxPlayers) throws RemoteException;

    /**
     * Join a lobby in an RMI communication.
     * @param lobbyUUID the UUID of the lobby to join
     * @return true if the user has joined the lobby, false otherwise
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    Boolean joinLobbyRMI(String lobbyUUID) throws RemoteException;

    /**
     * Get the list of lobbies to join in an RMI communication.
     * @return the list of lobbies to join. The list may be null (see {@link it.polimi.ingsw.controller.MatchController} specifications)
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    List<ListOfLobbyToJoinMessage.LobbyInfo> getListOfLobbyToJoinRMI()
            throws RemoteException;

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*
    InLobbyState
     */

    /**
     * Get the LobbyInfo of the lobby in an RMI communication.
     * @return the LobbyInfo of the lobby. The LobbyInfo may be null (see {@link it.polimi.ingsw.controller.MatchController} specifications)
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    LobbyInfo getLobbyInfoRMI() throws RemoteException;

    /**
     * Start the lobby in an RMI communication.
     * @return true if the lobby has been started, false otherwise
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    Boolean startLobbyRMI() throws RemoteException;

    /**
     * Exit the lobby in an RMI communication.
     * @return true if the user has exited the lobby, false otherwise
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    Boolean exitLobbyRMI() throws RemoteException;

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*
    InGameState
     */

    /**
     * Get the StarterCard of the player in an RMI communication.
     * @param username the username of the player to get the StarterCard of
     * @return the StarterCard of the player. The StarterCard may be null (see {@link it.polimi.ingsw.controller.GameController} specifications)
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    StarterCard getStarterCard(String username) throws RemoteException;

    /**
     * Set the StarterCard of the player in an RMI communication.
     * @param username the username of the player to set the StarterCard of
     * @param cardFace the CardFace of the StarterCard to set
     * @return true if the StarterCard has been set, false otherwise
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    Boolean setStarterCard(String username, CardFace cardFace) throws RemoteException;

    /**
     * Get the available colors in an RMI communication.
     * @return the list of available colors. The list may be null (see {@link it.polimi.ingsw.controller.GameController} specifications)
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    List<PlayerColor> getAvailableColors() throws RemoteException;

    /**
     * Set the player color in an RMI communication.
     * @param username the username of the player to set the color of
     * @param playerColor the PlayerColor to set
     * @return true if the color has been set, false otherwise
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    Boolean setPlayerColor(String username, PlayerColor playerColor) throws RemoteException;

    /**
     * Get the proposed private goals in an RMI communication.
     * @param username the username of the player to get the proposed private goals of
     * @return the list of proposed private goals. The list may be null (see {@link it.polimi.ingsw.controller.GameController} specifications)
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    List<GoalCard> getProposedPrivateGoals(String username) throws RemoteException;

    /**
     * Choose the private goal in an RMI communication.
     * @param username the username of the player to choose the private goal of
     * @param goalCard the GoalCard to choose
     * @return true if the private goal has been chosen, false otherwise
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    Boolean choosePrivateGoal(String username, GoalCard goalCard) throws RemoteException;

    /**
     * Get the player data in an RMI communication.
     * @param username the username of the player to get the data of
     * @return the PlayerData of the player. The PlayerData may be null (see {@link it.polimi.ingsw.controller.GameController} specifications)
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    PlayerData getPlayerData(String username) throws RemoteException;

    /**
     * Get all the player data in an RMI communication.
     * @param username the username of the player to get the data of
     * @return the list of PlayerData of the player. The list may be null (see {@link it.polimi.ingsw.controller.GameController} specifications)
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    List<PlayerData> getAllPlayerData(String username) throws RemoteException;

    /**
     * Get the manuscript of the player in an RMI communication.
     * @param username the username of the player to get the manuscript of
     * @return the PlayerManuscript of the player. The PlayerManuscript may be null (see {@link it.polimi.ingsw.controller.GameController} specifications)
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    PlayerManuscript getManuscript(String username) throws RemoteException;

    /**
     * Get the game flow in an RMI communication.
     * @return the GameFlow of the game. The GameFlow may be null (see {@link it.polimi.ingsw.controller.GameController} specifications)
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    GameFlow getGameFlow() throws RemoteException;

    /**
     * Get the game board in an RMI communication.
     * @return the CommonBoard of the game. The CommonBoard may be null (see {@link it.polimi.ingsw.controller.GameController} specifications)
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    CommonBoard getGameBoard() throws RemoteException;

    /**
     * Place a card in the manuscript in an RMI communication.
     * @param username the username of the player to place the card of
     * @param card the TypedCard to place
     * @param position the ManuscriptPosition to place the card in
     * @return true if the card has been placed, false otherwise
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    Boolean placeCard(String username, TypedCard card, ManuscriptPosition position)
            throws RemoteException;

    /**
     * Draw a visible card in an RMI communication.
     * @param username the username of the player to draw the card of
     * @param type the CardType of the card to draw
     * @param index the index of the card to draw
     * @return the TypedCard drawn. The TypedCard may be null (see {@link it.polimi.ingsw.controller.GameController} specifications)
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    TypedCard drawVisibleCard(String username, CardType type, int index) throws RemoteException;

    /**
     * Draw a covered card in an RMI communication.
     * @param username the username of the player to draw the card of
     * @param type the CardType of the card to draw
     * @return the TypedCard drawn. The TypedCard may be null (see {@link it.polimi.ingsw.controller.GameController} specifications)
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    TypedCard drawCoveredCard(String username, CardType type) throws RemoteException;

    /**
     * Get if the game has ended in an RMI communication.
     * @return true if the game has ended, false otherwise
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    Boolean getGameEnded() throws RemoteException;

    /**
     * Get the winner of the game in an RMI communication.
     * @return the winner of the game. The winner may be null (see {@link it.polimi.ingsw.controller.GameController} specifications)
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    String getWinner() throws RemoteException;

    /**
     * Exit the match in an RMI communication.
     * @return true if the user has exited the match, false otherwise
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    Boolean exitMatchRMI() throws RemoteException;

    // Chat-related commands

    /**
     * Send a broadcast message in an RMI communication.
     * @param payload the message to send
     * @return true if the message has been sent, false otherwise
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    Boolean sendBroadcast(String payload) throws RemoteException;

    /**
     * Send a private message in an RMI communication.
     * @param payload the message to send
     * @param recipient the recipient of the message
     * @return true if the message has been sent, false otherwise
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    Boolean sendPrivate(String payload, String recipient) throws RemoteException;

    /**
     * Get the recipients of the messages in an RMI communication.
     * @return the list of recipients. The list may be null (see {@link it.polimi.ingsw.controller.ChatController} specifications)
     * @throws RemoteException if something goes wrong with the RMI communication
     */
    List<String> getRecipients() throws RemoteException;
}
