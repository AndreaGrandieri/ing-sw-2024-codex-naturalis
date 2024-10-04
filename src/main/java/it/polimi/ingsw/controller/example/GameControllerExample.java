package it.polimi.ingsw.controller.example;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.controller.GameState;
import it.polimi.ingsw.model.card.TypedCard;
import it.polimi.ingsw.model.card.properties.CardFace;
import it.polimi.ingsw.model.game.CardType;
import it.polimi.ingsw.model.player.ManuscriptPosition;
import it.polimi.ingsw.model.player.PlayerColor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameControllerExample {

    /**
     * Creates and returns an initialized and ready-to-play {@link GameController}
     *
     * @param usernames List for in-game players
     * @return New ready-to-play {@code GameController} instance
     */
    public static GameController gc(List<String> usernames) {
        GameController gc = new GameController(usernames);

        // initializing players' manuscripts
        for (String player : gc.getGameFlow().getAllUsernames()) {
            gc.setStarterCard(player, CardFace.FRONT);
        }

        // give players random colors
        for (String player : gc.getGameFlow().getAllUsernames()) {
            List<PlayerColor> availableColors = gc.getAvailableColors();
            gc.setPlayerColor(player, availableColors.get((int) (Math.random() * (availableColors.size() - 1))));
        }

        // every player chooses first goal proposed
        for (String player : gc.getGameFlow().getAllUsernames()) {
            gc.choosePrivateGoal(player, gc.getProposedPrivateGoals(player).get(0));
        }

        return gc;
    }

    /**
     * Takes an initialized {@code GameController} and simulates a match on it
     *
     * @param gc {@code GameController} instance on which the game will be simulated
     */
    public static void simulateFullGame(GameController gc) {
        while (gc.getGameFlow().getState() != GameState.POST_GAME) {
            for (String player : gc.getGameFlow().getAllUsernames()) {

                playTurn(gc, player);
            }
        }
    }

    /**
     * Creates and returns a {@link GameController} instance that is in {@code LAST_ROUND GameState}
     *
     * @param players List for in-game players
     * @return New {@code GameController} instance in LAST_ROUND state
     */
    public static GameController lastRoundGc(List<String> players) {
        GameController gc = gc(players);

        while (gc.getGameFlow().getState() != GameState.LAST_ROUND) {
            for (String player : gc.getGameFlow().getAllUsernames()) {

                playTurn(gc, player);
            }
        }

        return gc;
    }

    /**
     * Creates and returns a {@link GameController} instance where all decks are empty
     *
     * @return New {@code GameController} instance with 4 players ("a", "b", "c", "d") and no cards in its decks
     * @throws RuntimeException If, after 100 trials, it was not possible to simulate a game that leaves all decks empty
     */
    public static GameController emptyDecksGc() {

        AtomicBoolean x = new AtomicBoolean(false);
        // max 100 attempts to get an empty decks game controller
        for (int i = 0; i < 100; i++) {

            GameController gc = gc(List.of("a", "b", "c", "d"));

            while (gc.getGameFlow().getState() != GameState.LAST_ROUND) {
                for (String player : gc.getGameFlow().getAllUsernames()) {

                    playTurn(gc, player);

                    if (gc.getGameBoard().allDecksEmpty())
                        return gc;
                }
            }
        }
        throw new RuntimeException("GameControllerExample: failed to create a empty-decks game controller, after 100 trials");
    }

    /**
     * Simulates the turn of the given {@code player} on the given {@code GameController}:
     * <ul>
     *     <li>Pick a random card from player's hand;</li>
     *     <li>Place it on a random available position of player's manuscript;</li>
     *     <li>Draw a visible card, choosing randomly between resource and gold cards</li>
     * </ul>
     *
     * @param gc     {@link GameController} on which to play a turn
     * @param player {@code username} of player to simulate turn of
     */
    public static void playTurn(GameController gc, String player) {
        List<ManuscriptPosition> positions = new ArrayList<>(gc.getManuscript(player).getAllAvailablePositions());
        int pos = (int) (Math.random() * (positions.size() - 1));
        ManuscriptPosition position = positions.get(pos);

        TypedCard card;
        do {
            int handIndex = (int) (Math.random() * 2);
            card = gc.getPlayerData(player).getHand().get(handIndex);
        } while (card == null);

        if (!gc.getManuscript(player).isPlaceable(position, card)) {
            card.flip();
        }


        gc.placeCard(player, card, position);

        while (gc.getGameFlow().getState() == GameState.PLAYING && gc.drawVisibleCard(player,
                ((int) (Math.random() * 2) % 2 == 0) ? CardType.GOLD : CardType.RESOURCE,
                (int) (Math.random() * 2)) == null) ;
    }

}
