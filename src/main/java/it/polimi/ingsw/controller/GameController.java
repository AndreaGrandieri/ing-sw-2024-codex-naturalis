package it.polimi.ingsw.controller;

import it.polimi.ingsw.controller.event.Event;
import it.polimi.ingsw.controller.event.EventType;
import it.polimi.ingsw.controller.event.game.*;
import it.polimi.ingsw.model.card.GoalCard;
import it.polimi.ingsw.model.card.StarterCard;
import it.polimi.ingsw.model.card.TypedCard;
import it.polimi.ingsw.model.card.deck.CardDeck;
import it.polimi.ingsw.model.card.deck.EmptyDeckException;
import it.polimi.ingsw.model.card.factory.GoalCardFactory;
import it.polimi.ingsw.model.card.factory.StarterCardFactory;
import it.polimi.ingsw.model.card.properties.CardFace;
import it.polimi.ingsw.model.game.CardType;
import it.polimi.ingsw.model.game.CommonBoard;
import it.polimi.ingsw.model.player.ManuscriptPosition;
import it.polimi.ingsw.model.player.PlayerColor;
import it.polimi.ingsw.model.player.PlayerData;
import it.polimi.ingsw.model.player.PlayerManuscript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static it.polimi.ingsw.controller.GameState.*;

public class GameController {
    private final Map<String, PlayerManuscript> manuscripts;

    private final Map<String, List<GoalCard>> personalGoals;

    private final Map<String, StarterCard> personalStarters;

    private final CommonBoard gameBoard;

    private final GameFlow gameFlow;
    private final Map<EventType<? extends Event>, List<EventHandler<? extends Event>>> eventHandlers = new HashMap<>();
    private String winner;

    /**
     * This class implements the controller of our application. <br>
     * It has methods to act on the model and change it during all game phases, from the set-up to the end of the game. <br><br>
     * Game set-up flow:
     * <ul>
     *     <li>Shuffle decks, pick visibleCards</li>
     *     <li>Deal and place {@code StarterCards}</li>
     *     <li>Choose {@code PlayerColor}</li>
     *     <li>Draw 3 hand cards</li>
     *     <li>Pick 2 {@code CommonGoals}</li>
     *     <li>Deal and choose {@code privateGoal}</li>
     * </ul>
     * <p>
     * Players flow:
     * <ul>
     *     <li>Choose {@code StarterCards} face</li>
     *     <li>Choose {@code PlayerColor}</li>
     *     <li>Draw 3 hand cards</li>
     *     <li>Choose {@code privateGoal}</li>
     * </ul>
     * (Operation order is not enforced)
     */
    public GameController(List<String> usernames) {
        int players = usernames.size();

        winner = null;

        if (players < 2 || players > 4) {
            throw new IllegalArgumentException("Invalid number of players. Valid range from 2 to 4.");
        }

        manuscripts = new HashMap<>();
        for (String player : usernames)
            manuscripts.put(player, null);

        CardDeck<StarterCard> startersDeck = new CardDeck<>(new StarterCardFactory());
        CardDeck<GoalCard> goalsDeck = new CardDeck<>(new GoalCardFactory());

        gameBoard = new CommonBoard(goalsDeck.drawNextNCards(2));

        gameFlow = new GameFlow(new ArrayList<>(usernames));

        personalGoals = new HashMap<>();
        personalStarters = new HashMap<>();

        // for all players: propose them 2 goal cards, give them a starter card to place, give them 3 cards for their hand
        for (String player : gameFlow.getAllUsernames()) {
            personalStarters.put(player, startersDeck.drawNextCard());
            initPlayerHand(player);
            personalGoals.put(player, new ArrayList<>(goalsDeck.drawNextNCards(2)));
        }

    }

    /**
     * Tells if all <b><u>connected players</u></b> have completed all of their setting phase operations
     *
     * @return {@code true} if all connected players have completed their setting phase;
     * {@code false} otherwise.
     */
    private boolean settingStateComplete() {
        return gameFlow.getConnectedPlayers().stream().allMatch(player ->
                manuscripts.get(player) != null &&
                        getPlayerData(player).getPrivateGoal() != null &&
                        getPlayerData(player).getColor() != null
        );
    }

    private void goToPlayingState() {
        gameFlow.setState(PLAYING);
        // notify listeners
        StateChangeEvent stateInfo = new StateChangeEvent(gameFlow.getState());
        TurnChangeEvent turnInfo = new TurnChangeEvent(gameFlow.getCurrentPlayer());
        executeHandlers(GameEvents.STATE_CHANGE, stateInfo);
        executeHandlers(GameEvents.TURN_CHANGE, turnInfo);

        if (gameFlow.isDisconnected(gameFlow.getCurrentPlayer())) {
            goToNextTurn();
        }
    }

    private void completeDisconnectedSettingAndStart() {
        for (String player : gameFlow.getDisconnectedPlayers()) {
            setStarterCard(player, CardFace.FRONT);
            setPlayerColor(player, getAvailableColors().get(0));
            choosePrivateGoal(player, getProposedPrivateGoals(player).get(0));
        }
        goToPlayingState();
    }

    /**
     * If the player has not placed their {@link StarterCard} yet, returns the card randomly assigned to them during the {@code SETTING} state;
     * otherwise, returns the {@link StarterCard} placed in their {@code manuscript}
     *
     * @param username name of player to get the {@code starterCard}
     * @return {@code starterCard} associated to the specified player
     */
    public synchronized StarterCard getStarterCard(String username) {
        // if player has not placed his starter card, return the card dealt to him in the constructor
        if (manuscripts.get(username) == null)
            return new StarterCard(personalStarters.get(username));

        // else, return the card he placed in the manuscript (should be the same, but may be flipped)
        StarterCard starterCard = (StarterCard) manuscripts.get(username).getCardAt(new ManuscriptPosition(0, 0));
        return new StarterCard(starterCard);
    }

    /**
     * Initializes the specified player's {@link PlayerManuscript}
     *
     * @param username name of player to initialize
     * @param face     starterCard's placing face
     * @return {@code true} if the operation is successful;
     * {@code false} if manuscript was already initialized or if {@code gameState} is not {@code SETTING}
     */
    public synchronized boolean setStarterCard(String username, CardFace face) {
        // if player does not exist, you cannot set its starter card
        if (!gameFlow.getAllUsernames().contains(username))
            return false;

        // If a manuscript already exists, you cannot set its starter card (PlayerData is immutable).
        if (manuscripts.get(username) != null)
            return false;

        // if game has already started, you cannot initialize a new manuscript
        if (gameFlow.getState() != SETTING) {
            return false;
        }

        StarterCard playerStarter = personalStarters.get(username);
        // if the card is bad positioned, we have to flip it
        if (face == CardFace.BACK)
            playerStarter.flip();

        // we initialize this player's manuscript
        manuscripts.put(username, new PlayerManuscript(playerStarter));

        // notify listeners
        SetStarterEvent setInfo = new SetStarterEvent(username, playerStarter);
        executeOthersHandlers(username, GameEvents.SET_STARTER, setInfo);

        if (settingStateComplete() && !gameFlow.getDisconnectedPlayers().contains(username))
            completeDisconnectedSettingAndStart();

        return true;
    }

    /**
     * Returns a List of all instances of {@link PlayerColor} that are not associated with any of the players
     *
     * @return List containing all available {@code PlayerColors}
     */
    public synchronized List<PlayerColor> getAvailableColors() {
        List<PlayerColor> availableColors = new ArrayList<>(List.of(PlayerColor.values()));

        for (String player : gameFlow.getAllUsernames())
            availableColors.remove(gameFlow.getPlayerData(player).getColor());

        return availableColors;
    }

    /**
     * Sets the specified {@link PlayerColor} to be the specified player's {@code color} property;
     * notifies listeners after the operation
     *
     * @param username name of player to set
     * @param color    {@link PlayerColor} to set
     * @return {@code true} if operation was successful;
     * {@code false} if {@code GameState} is not {@code SETTING}, given {@code username} does not exist,
     * given {@code color} is not available, given player has already a color set
     */
    public synchronized boolean setPlayerColor(String username, PlayerColor color) {
        if (gameFlow.getState() == SETTING && gameFlow.getAllUsernames().contains(username)) {

            if (getAvailableColors().contains(color) && getPlayerData(username).getColor() == null) {
                gameFlow.getPlayerData(username).setColor(color);

                //notify listeners
                SetColorEvent colorInfo = new SetColorEvent(username, color);
                executeHandlers(GameEvents.SET_COLOR, colorInfo);

                if (settingStateComplete() && !gameFlow.getDisconnectedPlayers().contains(username))
                    completeDisconnectedSettingAndStart();

                return true;
            }
        }

        return false;
    }

    private synchronized void initPlayerHand(String username) {

        TypedCard card1 = gameBoard.drawCovered(CardType.RESOURCE);
        TypedCard card2 = gameBoard.drawCovered(CardType.RESOURCE);
        TypedCard card3 = gameBoard.drawCovered(CardType.GOLD);

        ArrayList<TypedCard> hand = new ArrayList<>(List.of(card1, card2, card3));
        gameFlow.getPlayerData(username).setHand(hand);

    }

    /**
     * Returns the random {@link GoalCard} couple given to the specified player;
     * throws {@link RuntimeException} if {@link GameState} is not SETTING;
     * does not protect privacy of private goals
     *
     * @param username name of player whose to get random goals
     * @return List containing the couple of random goals for the given player
     */
    public synchronized List<GoalCard> getProposedPrivateGoals(String username) {
        // all checks to avoid players getting others' goals are demanded to server

        if (gameFlow.getState() == SETTING) {
            return new ArrayList<>(personalGoals.get(username));
        }
        throw new RuntimeException("Invalid getProposedPrivateGoals - GameState is not SETTING");
    }

    /**
     * Sets the specified {@link GoalCard} to be the specified player's {@code privateGoal}
     *
     * @param username name of player to set
     * @param goal     {@link GoalCard} to set
     * @return {@code true} if operation was successful;
     * {@code false} if {@code GameState} is not {@code SETTING}, given {@code username} does not exist,
     * given {@code GoalCard} is not among the given player's proposals, given player has already a private goal set
     */
    public synchronized boolean choosePrivateGoal(String username, GoalCard goal) {

        // if game is still setting up
        if (gameFlow.getState() == SETTING && gameFlow.getAllUsernames().contains(username)) {

            // if the goal has already been set
            if (getPlayerData(username).getPrivateGoal() != null)
                return false;

            // if the goal passed has been proposed to the player
            if (personalGoals.get(username).contains(goal)) {
                // set the goal to player's private goal
                setPlayerPrivateGoal(username, goal);

                // notify listeners
                ChooseGoalEvent goalInfo = new ChooseGoalEvent(username);
                executeHandlers(GameEvents.CHOOSE_GOAL, goalInfo);
            } else return false;

            // if all players have chosen their goals, we can pass to PLAYING state
            if (settingStateComplete() && !gameFlow.getDisconnectedPlayers().contains(username))
                completeDisconnectedSettingAndStart();

            return true;
        }
        return false;
    }


    private void setPlayerPrivateGoal(String username, GoalCard card) {
        PlayerData playerToSet = gameFlow.getPlayerData(username);
        if (playerToSet != null) {
            playerToSet.setPrivateGoal(card);
        }
    }

    /**
     * Returns a copy of the full {@link PlayerData} related to the specified {@code username};
     * throws {@link RuntimeException} if that player does not exist
     *
     * @param username name of player to get data
     * @return The specified {@code username}'s {@link PlayerData}
     */
    public synchronized PlayerData getPlayerData(String username) {
        if (!gameFlow.getAllUsernames().contains(username))
            throw new RuntimeException("Requested player does not exist");
        return new PlayerData(gameFlow.getPlayerData(username));
    }

    /**
     * Returns a partial {@link PlayerData}, cleaned of all private data, related to the specified {@code username};
     * throws {@link RuntimeException} if that player does not exist
     *
     * @param username name of player to get data
     * @return The specified {@code username}'s clean {@link PlayerData}
     */
    public synchronized PlayerData getCleanPlayerData(String username) {
        if (!gameFlow.getAllUsernames().contains(username))
            throw new RuntimeException("Requested player does not exist");
        return gameFlow.getPlayerData(username).cleanPlayerData();
    }

    /**
     * Returns a list containing all copies of player's {@link PlayerData}; data of the passed username's player is full,
     * other player's data lacks of private information; List is ordered with playing turns order; first player of the List is the
     * one whose username was passed to this method
     *
     * @param username player whose information is full
     * @return List with information about all players, ordered by game turn; detailed player at index 0, other players' data is cleaned
     */
    public synchronized List<PlayerData> getAllPlayerData(String username) {
        List<PlayerData> players = new ArrayList<>();

        for (String name : gameFlow.getAllUsernames()) {
            if (name.equals(username))
                players.add(getPlayerData(name));
            else
                players.add(getCleanPlayerData(name));
        }

        // shifting players to have the detailed one in first place
        while (!players.get(0).getUsername().equals(username)) {
            PlayerData first = players.get(0);
            players.set(0, players.get(1));
            players.set(1, players.get(2));
            players.set(2, players.get(3));
            players.set(3, first);
        }

        return players;
    }

    /**
     * Returns a copy of the specified player's {@link PlayerManuscript}, if they already set their {@code starterCard};
     * returns an empty {@code manuscript} otherwise;
     * throws {@link RuntimeException} if the given player does not exist
     *
     * @param username name of player whose to get the {@code manuscript}
     * @return A copy of the given player's {@code PlayerManuscript};
     * an empty {@code PLayerManuscript}, if that player has not initialized their manuscript yet
     */
    public synchronized PlayerManuscript getManuscript(String username) {
        if (!gameFlow.getAllUsernames().contains(username))
            throw new RuntimeException("Invalid getManuscript - player does not exist");

        return (manuscripts.get(username) != null) ?
                new PlayerManuscript(manuscripts.get(username)) :
                new PlayerManuscript();
    }

    public synchronized GameFlow getGameFlow() {
        return new GameFlow(gameFlow);
    }

    public synchronized CommonBoard getGameBoard() {
        return new CommonBoard(gameBoard);
    }

    /**
     * If possible, places the given {@link TypedCard},
     * in the given {@link ManuscriptPosition}
     * of the given {@code username}'s {@link PlayerManuscript}.
     * Updates turns if game state is not {@code PLAYING}.
     * Notifies listeners after every operation.
     *
     * @param username name of user that is playing the card
     * @param card     card that is being played
     * @param position manuscript coordinates where to place the card
     * @return {@code true} if operation is successful;
     * {@code false} otherwise
     * @throws RuntimeException If the given player is disconnected
     */
    public synchronized boolean placeCard(String username, TypedCard card, ManuscriptPosition position) {
        // if there is one player left in game, game operations are not allowed
        if (gameFlow.isIdle())
            return false;

        // if the given player is disconnected, they cannot place any card
        if (gameFlow.isDisconnected(username))
            throw new RuntimeException(username + " is disconnected and cannot place any card");

        if (card == null || position == null)   // if parameters are invalid you cannot place any card
            return false;

        // if we're not playing you cannot place any card
        if (!List.of(PLAYING, EMPTY_DECKS, LAST_ROUND).contains(gameFlow.getState())) {
            throw new RuntimeException("Cannot place any card during " + gameFlow.getState() + " state");
        }

        if (!gameFlow.isCurrentPlayer(username)) {  // if you are not playing, you cannot place any card
            return false;
        }

        PlayerData player = gameFlow.getPlayerData(username);
        PlayerManuscript manuscript = manuscripts.get(username);

        if (player != null && manuscript != null) {                 // check: the player exists and has an initialized manuscript

            if (!manuscripts.get(username).isEmpty()) {                         // if player's manuscript has some starter card placed

                // check if this placement is legit
                if (!player.getHand().contains(card)) return false;              // card must be in player's hand

                int handIndex = player.getHand().indexOf(card);

                if (!manuscript.isPlaceable(position, card))
                    return false;       // card must be placeable in that position of manuscript

                player.playCard(card);                                          // remove card from player's hand
                player.addPoints(manuscript.insertCard(position, card));        // place card on the manuscript and claim the points this move scored

                // notify listeners
                PlaceCardEvent placeInfo = new PlaceCardEvent(username, card, handIndex, position);
                executeOthersHandlers(username, GameEvents.PLACE_EVENT, placeInfo);

                if (gameFlow.getState() != PLAYING) {         // if we don't have to draw afterward, our turn ends here
                    goToNextTurn();
                }

                return true;
            }
        }
        return false;
    }

    private String canDraw(String username) {
        // if there is one player left in game, game operations are not allowed
        if (gameFlow.isIdle())
            return "Game is idle: drawing is not allowed";

        // if you are disconnected, you cannot draw
        if (gameFlow.isDisconnected(username))
            return username + " is disconnected and cannot draw";

        // if we're not playing you cannot draw any card (even during LAST_ROUND it is useless to draw)
        if (gameFlow.getState() != PLAYING)
            return username + " cannot draw: GameState is not PLAYING";

        // if you have all 3 cards in hand, you cannot draw any card
        if (!getPlayerData(username).getHand().contains(null))
            return username + " cannot draw: " + username + " hand already contains 3 cards";

        // if you are not playing, you cannot draw any card
        return (gameFlow.isCurrentPlayer(username)) ? "ok" : username + " cannot draw: it is not their turn";
    }

    /**
     * Draws a {@code visibleCard} from {@link CommonBoard} and puts it in the specified player's hand;
     * returns the new visible card, that replaces the taken one on the board;
     * if there are no more cards in the deck to replace it, returns the card drew;
     * returns null if the requested card was missing (but there are still other cards to draw);
     * throws {@link RuntimeException} if the operation is not successful
     *
     * @param username username of the player that is drawing
     * @param type     {@link CardType} of card the player wants to draw
     * @param index    index of Visible Card to draw
     * @return the new visible card, that replaces the taken one on the board;
     * the visible card drew, if it is the last one of the deck;
     * null, if requested card was missing
     */
        public synchronized TypedCard drawVisibleCard(String username, CardType type, int index) {
        String drawChallengeMessage = canDraw(username);
        if (drawChallengeMessage.equals("Game is idle: drawing is not allowed")) {
            return null;
        }

        if (!drawChallengeMessage.equals("ok"))
            throw new RuntimeException(drawChallengeMessage);

        // only 2 visible cards are present
        if (index < 0 || index > 1)
            throw new RuntimeException("Invalid index for drawVisibleCard - allowed indexes are: (0, 1)");

        PlayerData player = gameFlow.getPlayerData(username);

        // if this player exists
        if (player != null) {
            // try to draw the requested card
            TypedCard drawn = gameBoard.drawVisibleCard(type, index);
            // if the card was present, just add it to player's card and pass turn to next player
            if (drawn != null) {

                // add the card to player's hand
                player.addCardToHand(drawn);

                // if we just drew the last card in the game, we warn GameFlow that the next
                // round must be the last round, so nobody draws anymore
                if (gameBoard.allDecksEmpty()) {
                    gameFlow.setState(EMPTY_DECKS);

                    // notify listeners
                    StateChangeEvent stateInfo = new StateChangeEvent(gameFlow.getState());
                    executeHandlers(GameEvents.STATE_CHANGE, stateInfo);
                }

                // choose the card to return (the new one or the drew one if there are no more cards in that spot)
                TypedCard newCard = gameBoard.getVisibleCards(type).get(index);
                TypedCard returnCard = (newCard == null) ? drawn : newCard;

                // notify listeners
                DrawVisibleEvent dvInfo = new DrawVisibleEvent(username, index, type, drawn, returnCard);

                executeOthersHandlers(username, GameEvents.DRAW_VISIBLE, dvInfo);

                goToNextTurn();

                return returnCard;
            } else {

                // if we drew null but there are still cards on the board,
                // current player has to try to draw again
                if (!gameBoard.allDecksEmpty())
                    return null;
                else
                    throw new RuntimeException("Invalid drawVisibleCard operation - cannot draw if decks are empty");
            }
        }
        throw new RuntimeException("Invalid drawVisibleCard operation - given player does not exist");
    }

    /**
     * Draws a {@code coveredCard} from {@link CommonBoard} and puts it in the specified player's hand;
     * returns the drawn card;
     * returns null if the requested deck is empty (but there are still other cards to draw);
     * throws {@link RuntimeException} if the operation is not successful
     *
     * @param username username of the player that is drawing
     * @param type     type of deck to be drawn
     * @return the drawn card; null, if the requested deck is empty
     */
    public synchronized TypedCard drawCoveredCard(String username, CardType type) {
        String drawChallengeMessage = canDraw(username);
        if (drawChallengeMessage.equals("Game is idle: drawing is not allowed")) {
            return null;
        }

        if (!drawChallengeMessage.equals("ok"))
            throw new RuntimeException(drawChallengeMessage);

        PlayerData player = gameFlow.getPlayerData(username);

        // if this player exists
        if (player != null) {

            // make it draw a covered card from the board
            TypedCard drawn;
            try {
                drawn = gameBoard.drawCovered(type);
            } catch (EmptyDeckException e) {
                // if there are still cards to draw, we cannot pass the turn to next player:
                // we return null to make current player draw again
                if (!gameBoard.allDecksEmpty())
                    return null;

                // if decks are empty, you should not get here
                throw new RuntimeException("Invalid drawCoveredCard operation - cannot draw if decks are empty");
            }

            // add drawn card to player's hand
            player.addCardToHand(drawn);

            //notify listeners
            DrawCoveredEvent dcInfo = new DrawCoveredEvent(username, drawn.cleanCard(), type);

            executeOthersHandlers(username, GameEvents.DRAW_COVERED, dcInfo);
            goToNextTurn();

            // return the card drawn
            return drawn;
        }
        throw new RuntimeException("Invalid drawCoveredCard operation - given player does not exist");
    }

    /**
     * <ul>
     *     <li>Increments turn number using {@link GameFlow}'s {@code nextTurn()}</li>
     *     <li>notifies turn changing to listeners</li>
     *     <li>if game state has changed, notifies listeners</li>
     * </ul>
     */
    private void goToNextTurn() {
        // pass turn to next player
        boolean stateChange = gameFlow.nextTurn();
        // notify listeners
        TurnChangeEvent turnInfo = new TurnChangeEvent(gameFlow.getCurrentPlayer());
        executeHandlers(GameEvents.TURN_CHANGE, turnInfo);

        if (stateChange) {
            StateChangeEvent stateInfo = new StateChangeEvent(gameFlow.getState());
            executeHandlers(GameEvents.STATE_CHANGE, stateInfo);
        }

        // if the new current player is disconnected, pass to the next player
        if (gameFlow.isDisconnected(gameFlow.getCurrentPlayer()) && gameFlow.notEveryoneDisconnected()) {
            goToNextTurn();
        }
    }

    /**
     * Tells if the game has ended or not
     *
     * @return {@code true} if {@link GameState} is {@code POST_GAME};
     * {@code false} otherwise
     */
    public synchronized boolean gameEnded() {
        return gameFlow.getState() == POST_GAME;
    }

    /**
     * If the game has ended, this method calculates, for each player, points scored with goals;
     * then solves eventual draw situations (see rulebook);
     * finally, returns the {@code username} of the player with most points;
     * returns {@code null} if the draw situation was not solvable, and the game ended in a draw;
     * returns {@code null} if called when the game is not finished yet.
     *
     * @return {@code username} of the player with most points;
     * {@code null} if match ends in a draw, or if this method is called before the game ends
     */
    public synchronized String getWinner() {

        // if match has ended (no one can play anymore)
        if (gameEnded()) {

            // save every player's score before considering goals
            Map<String, Integer> preGoalScores = gameFlow.getPlayerScores();

            // for all players, add goals scores
            addGoalsPoints();

            // get players with max points after goals calculation
            List<String> leadingPlayers = gameFlow.getLeadingPlayers();

            // if there is only one, he wins
            if (leadingPlayers.size() == 1) {
                // from now on, no operation is possible on this GameController
                gameFlow.setState(END);

                // notify listeners
                executeHandlers(
                        GameEvents.STATE_CHANGE,
                        new StateChangeEvent(gameFlow.getState())
                );
                winner = leadingPlayers.get(0);
            }

            // if there are more players with same points, the one who got more points from goals wins
            else {
                winner = solveDraw(preGoalScores, leadingPlayers);
            }
        }
        return winner;
    }

    private void addGoalsPoints() {
        if (gameEnded()) {
            for (String player : gameFlow.getAllUsernames()) {
                PlayerData data = gameFlow.getPlayerData(player);

                // add private goal's points to their score
                data.addPoints(data.getPrivateGoal().calculateScore(manuscripts.get(player)));

                // add public goal's points to their score
                for (GoalCard goal : gameBoard.getCommonGoals()) {
                    data.addPoints(goal.calculateScore(manuscripts.get(player)));
                }
            }
            return;
        }
        throw (gameFlow.getState() == END) ?
                new RuntimeException("Invalid getWinner call - winner has already been calculated") :
                new RuntimeException("Invalid getWinner call - game has not finished yet");
    }

    private String solveDraw(Map<String, Integer> preGoalScores, List<String> leadingPlayers) {
        if (gameEnded()) {
            // get points after goals
            Map<String, Integer> postGoalScores = gameFlow.getPlayerScores();
            // we don't know the winner yet
            String winner = null;
            // dummy initial difference between points post- and pre-goals
            int maxDiff = -1;

            // we scan all players searching for the one who got the most points from goals
            for (String player : leadingPlayers) {
                int currDiff = postGoalScores.get(player) - preGoalScores.get(player);
                // if the current player has gained more points than the ones before him,
                // his difference becomes the new benchmark,
                // and he is candidate to win
                if (maxDiff < currDiff) {
                    maxDiff = currDiff;
                    winner = player;
                } else if (maxDiff == currDiff)
                    // if we find two players that have the same amount of points gained from goals,
                    // we are going to draw the match
                    winner = null;
            }

            // from now on, no operation is possible on this GameController
            gameFlow.setState(END);

            // notify listeners
            executeHandlers(
                    GameEvents.STATE_CHANGE,
                    new StateChangeEvent(gameFlow.getState())
            );
            return winner;
        }
        throw (gameFlow.getState() == END) ?
                new RuntimeException("Invalid getWinner call - winner has already been calculated") :
                new RuntimeException("Invalid getWinner call - game has not finished yet");
    }

    /**
     * Adds the given player to the disconnected players list and reports on the outcome of this operation.
     * If the given player was already disconnected, nothing happens and operation results successful.
     * If the disconnected player was between placing and drawing during {@code PLAYING} state,
     * a random {@code visibleCard} is drawn and added to their hand <b>BEFORE</b> disconnecting the player.
     *
     * @param username {@code username} of player that has been disconnected
     * @return {@code true} if player can be added to or is already in disconnected list;
     * {@code false} if the given player is not playing this game
     */
    public synchronized boolean disconnectPlayer(String username) {
        // cannot disconnect a player that is not in this game
        if (!gameFlow.getAllUsernames().contains(username)) return false;

        // if disconnected player has placed but not drew, draw for him and pass turn before disconnecting
        if (gameFlow.getState() == PLAYING) {
            // if player has null cards during PLAYING, he must be between placing and drawing:
            // make them draw a random card
            if (getPlayerData(username).getHand().contains(null)) {
                TypedCard c;
                do {
                    c = drawVisibleCard(
                            username,
                            ((int) (Math.random() * 2) % 2 == 0) ? CardType.GOLD : CardType.RESOURCE,
                            (int) (Math.random() * 2)
                    );
                } while (c == null);
            }
        }


        Boolean disconnectStatus = gameFlow.disconnectPlayer(username);

        if (disconnectStatus) {
            executeOthersHandlers(username, GameEvents.MATCH_COMPOSITION_CHANGE, new MatchCompositionChangeEvent());
        }

        if (gameFlow.getState() == SETTING) {                    // during SETTING phase,
            if (settingStateComplete())                         // after a disconnection, remaining players may be all set:
                completeDisconnectedSettingAndStart();          // the game can start
        }

        if (gameFlow.isIdle() && gameFlow.notEveryoneDisconnected()) {            // if after the disconnection we fall into IDLE,
            StateChangeEvent stateInfo = new StateChangeEvent(IDLE);            // we notify all listeners
            executeHandlers(GameEvents.STATE_CHANGE, stateInfo);
        }

        if (gameFlow.getCurrentPlayer().equals(username) && gameFlow.getState() != SETTING) {
            goToNextTurn();
        }

        // Removing handlers of the disconnected user
        this.removeAllHandlersOfUsername(username);

        return true;
    }

    /**
     * Removes the given player from the disconnected players list and reports on the outcome of this operation.
     * If the given player was already connected, nothing happens and operation results successful.
     *
     * @param username {@code username} of player that has been disconnected
     * @return {@code true} if player can be removed from or is already out from disconnected list;
     * {@code false} if the given player is not playing this game
     */
    public synchronized boolean reconnectPlayer(String username) {
        // cannot reconnect a player that is not in this game
        if (!gameFlow.getAllUsernames().contains(username)) return false;

        Boolean reconnectStatus = gameFlow.reconnectPlayer(username);

        if (reconnectStatus) {
            executeOthersHandlers(username, GameEvents.MATCH_COMPOSITION_CHANGE, new MatchCompositionChangeEvent());
        }

        if (gameFlow.getConnectedPlayers().size() == 2) {                                // if after the reconnection there are 2 players connected, that means we were IDLE before:
            StateChangeEvent stateInfo = new StateChangeEvent(gameFlow.getState());     // notify listeners that we are out of IDLE now
            executeHandlers(GameEvents.STATE_CHANGE, stateInfo);
        }
        if (gameFlow.getConnectedPlayers().size() == 1) {
            if (!gameFlow.getCurrentPlayer().equals(username)) {    // if after the reconnection this player is the only one in game, we have to make sure it becomes the current player
                goToNextTurn();                                     // so if it's not the current player already, we go to next turn until he becomes so
                // IT SHOULD BE IMPOSSIBLE TO GET HERE: MATCHES GET KILLED WHEN EVERY PLAYER QUITS
            }
        }

        return true;
    }

    /**
     * Adds the given {@link Consumer} to {@code listeners} of the specified {@link EventType}
     *
     * @param type     type of event to which add the given {@code Consumer}
     * @param consumer {@code Consumer} to add
     */
    public <T extends Event> void addEventHandler(String username, EventType<T> type, Consumer<T> consumer) {
        eventHandlers.computeIfAbsent(type, k -> new ArrayList<>());
        eventHandlers.get(type).add(new EventHandler<>(consumer, username));
    }

    /**
     * Removes the given {@link Consumer} to {@code listeners} of the specified {@link EventType}
     *
     * @param type     type of event from which remove the given {@code Consumer}
     * @param consumer {@code Consumer} to remove
     */
    public <T extends Event> void removeEventHandler(EventType<T> type, Consumer<T> consumer) {
        List<EventHandler<? extends Event>> presentConsumers = eventHandlers.get(type);
        List<EventHandler<? extends Event>> toDelete = presentConsumers
                .stream()
                .filter(h -> h.handler().equals(consumer)).toList();
        presentConsumers.removeAll(toDelete);
    }

    private <T extends Event> List<Consumer<T>> getEventHandlers(String username, EventType<T> type) {
        List<EventHandler<?>> presentConsumers = eventHandlers.get(type);
        if (presentConsumers != null) {
            return presentConsumers
                    .stream()
                    .map(h -> (EventHandler<T>) h)
                    .filter(h -> h.username() != null
                            && (username != null
                            && username.equals(h.username()))
                            || username == null)
                    .map(EventHandler::handler)
                    .toList();
        }
        return new ArrayList<>();
    }

    private <T extends Event> void removeAllHandlersOfUsername(String username) {
        for (EventType<? extends Event> eventType : eventHandlers.keySet()) {
            List<? extends Consumer<? extends Event>> eventHandlers = this.getEventHandlers(username, eventType);

            for (Consumer<? extends Event> consumer : eventHandlers) {
                this.removeEventHandler((EventType<T>) eventType, (Consumer<T>) consumer);
            }
        }
    }

    private <T extends Event> void executeHandlers(EventType<T> type, T info) {
        getEventHandlers(null, type).forEach(consumer -> (new Thread(() -> consumer.accept(info))).start());
    }

    private <T extends Event> void executeOthersHandlers(String excludedUsername, EventType<T> type, T info) {
        List<Consumer<T>> allEventHandlers = new ArrayList<>(this.getEventHandlers(null, type));
        List<Consumer<T>> mineEventHandlers = this.getEventHandlers(excludedUsername, type);
        allEventHandlers.removeAll(mineEventHandlers);

        allEventHandlers.forEach(consumer -> (new Thread(() -> consumer.accept(info))).start());
    }
}
