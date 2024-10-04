package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.player.PlayerData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class holds data about the current status of a game:
 * <ul>
 *     <li>A {@code List} of all {@link PlayerData} into this game</li>
 *     <li>The current {@link GameState}</li>
 *     <li>The current {@code turn} number</li>
 * </ul>
 */
public class GameFlow implements Serializable {
    private final List<PlayerData> players;
    private GameState state;
    private int turn;
    private final List<String> disconnectedPlayers;
    private boolean isIdle;

    public GameFlow(List<String> playersNames) {
        if (playersNames == null)
            throw new IllegalArgumentException("playersNames is null");
        state = GameState.SETTING;
        players = playersNames
                .stream()
                .map(PlayerData::new)
                .toList();
        turn = 0;
        disconnectedPlayers = new ArrayList<>();
        isIdle = false;
    }

    public GameFlow(GameFlow other) {
        this.players = List.copyOf(other.players);
        this.state = other.state;
        this.turn = other.turn;
        this.disconnectedPlayers = List.copyOf(other.disconnectedPlayers);
        this.isIdle = other.isIdle;
    }

    /**
     * Increments {@code turn} number; checks and changes {@code state} if needed.
     *
     * @return {@code true} if {@code state} has been changed; {@code false} otherwise
     */
    public boolean nextTurn() {
        turn++;

        // if the last turn has been played by the last player in game
        if (turn % players.size() == 0) {

            // if it was the last round
            if (state == GameState.LAST_ROUND) {
                // we go to POST_GAME state
                setState(GameState.POST_GAME);
                return true;
            }

            // if decks finished
            else if (state == GameState.EMPTY_DECKS) {
                // go to last round
                setState(GameState.LAST_ROUND);
                return true;
            } else {
                // check if someone has reached 20 points
                for (PlayerData p : players) {
                    if (p.getPoints() >= 20) {
                        // if so, the next round is the last of the game
                        setState(GameState.LAST_ROUND);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns a copy of the {@link PlayerData} object for the specified username
     *
     * @param username name of player whose data is to be taken
     * @return {@code PlayerData} object for the specified username
     */
    public PlayerData getPlayerData(String username) {
        for (PlayerData p : players) {
            if (p.getUsername().equals(username))
                return p;
        }
        return null;
    }

    /**
     * Returns the {@code username} of the player that is allowed to play the current {@code turn}
     *
     * @return {@code username} of the player that is allowed to play the current {@code turn}
     */
    public String getCurrentPlayer() {
        return players.get(turn % players.size()).getUsername();
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    /**
     * Returns a List containing {@code usernames} of all players in the lobby
     *
     * @return List of all {@code usernames} in the lobby
     */
    public List<String> getAllUsernames() {
        return players
                .stream()
                .map(PlayerData::getUsername)
                .collect(Collectors.toList());
    }

    /**
     * Tells if the given player is allowed to play this turn or not
     *
     * @param username name of player to check
     * @return {@code true} if the given username is the playing player; {@code false} otherwise
     */
    public boolean isCurrentPlayer(String username) {
        return username.equals(getCurrentPlayer());
    }

    /**
     * Provides a List with all players that have the most points at the moment
     *
     * @return List with usernames of all players whit most points
     */
    public List<String> getLeadingPlayers() {

        List<String> leaders = new ArrayList<>();

        int maxScore = players.stream()
                .map(PlayerData::getPoints)
                .max(Integer::compare)
                .get();

        for (PlayerData player : players) {
            if (player.getPoints() == maxScore)
                leaders.add(player.getUsername());
        }

        return leaders;
    }

    /**
     * Provides a Map associating every player with their current score
     *
     * @return Map associating each username with its player's score
     */
    public Map<String, Integer> getPlayerScores() {
        Map<String, Integer> scores = new HashMap<>();

        for (PlayerData player : players)
            scores.put(player.getUsername(), player.getPoints());

        return scores;
    }

    public int getTurn() {
        return turn;
    }

    /**
     * Returns a {@code List} containing all usernames of players that are disconnected, and cannot play until they reconnect
     *
     * @return {@code List} containing all usernames of players that are disconnected, and cannot play until they reconnect
     */
    public List<String> getDisconnectedPlayers() {
        return List.copyOf(disconnectedPlayers);
    }

    public List<String> getConnectedPlayers() {
        List<String> cp = getAllUsernames();
        cp.removeAll(disconnectedPlayers);
        return cp;
    }

    /**
     * Tells if the given player is disconnected or not
     *
     * @param player {@code username} of player to check
     * @return {@code true} if {@code player} is disconnected; {@code false} otherwise
     */
    public boolean isDisconnected(String player) {
        return disconnectedPlayers.contains(player);
    }

    /**
     * Adds the given username to the disconnected players' {@code List}
     *
     * @param username {@code username} of player that has disconnected from this game
     */
        public Boolean disconnectPlayer(String username) {
        if (disconnectedPlayers.contains(username))
            return false;

        if (getAllUsernames().contains(username)) {
            disconnectedPlayers.add(username);
            if (getConnectedPlayers().size() == 1)
                isIdle = true;

            return true;
        }

        return false;
    }

    /**
     * Removes the given username from the disconnected players' {@code List}
     *
     * @param username {@code username} of player that has reconnected to this game
     */
        public Boolean reconnectPlayer(String username) {
        if (getAllUsernames().contains(username)) {
            disconnectedPlayers.remove(username);
            if (getConnectedPlayers().size() > 1)
                isIdle = false;

            return true;
        }

        return false;
    }

    public boolean isIdle() {
        return isIdle;
    }

    public boolean notEveryoneDisconnected() {
        return disconnectedPlayers.size() != players.size();
    }

    @Override
    public String toString() {
        return "GameFlow{" +
                "players=" + players +
                ", state=" + state +
                ", turn=" + turn +
                ", disconnectedPlayers=" + disconnectedPlayers +
                ", isIdle=" + isIdle +
                '}';
    }
}
