package it.polimi.ingsw;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.controller.event.game.GameEvents;
import it.polimi.ingsw.controller.event.game.TurnChangeEvent;
import it.polimi.ingsw.model.card.TypedCard;
import it.polimi.ingsw.model.card.properties.CardFace;
import it.polimi.ingsw.model.game.CardType;
import it.polimi.ingsw.model.player.ManuscriptPosition;
import it.polimi.ingsw.model.player.PlayerColor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static it.polimi.ingsw.controller.GameState.PLAYING;

public class MainBot {
    private final String username;
    private final GameController gc;
    private int waitTime;
    Consumer<TurnChangeEvent> handler = this::onTurnChange;

    public MainBot(String username, GameController gc) {
        this.username = username;
        this.gc = gc;
        waitTime = 1000;

        gc.addEventHandler(null, GameEvents.TURN_CHANGE, handler);
        //
        //        game.addEventHandler(username, Events.PLACE_EVENT, placeCardEvent -> {
        //            if (username.equals("you")) System.out.println(placeCardEvent);
        //        });
        //        game.addEventHandler(username, Events.DRAW_VISIBLE, placeCardEvent -> {
        //            if (username.equals("you")) System.out.println(placeCardEvent);
        //        });
        //        game.addEventHandler(username, Events.DRAW_COVERED, placeCardEvent -> {
        //            if (username.equals("you")) System.out.println(placeCardEvent);
        //        });
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public void setupGame() {

        gc.setStarterCard(username, CardFace.FRONT);
        waitFor(waitTime / 10);

        List<PlayerColor> availableColors = gc.getAvailableColors();
        gc.setPlayerColor(username, availableColors.get((int) (Math.random() * (availableColors.size() - 1))));


        gc.choosePrivateGoal(username, gc.getProposedPrivateGoals(username).get(0));
    }

    private void onTurnChange(TurnChangeEvent event) {
        if (event.currentUsername().equals(username)) {
            //            System.out.println(username+" playing");
            List<ManuscriptPosition> positions = new ArrayList<>(gc.getManuscript(username).getAllAvailablePositions());
            int pos = (int) (Math.random() * (positions.size() - 1));
            ManuscriptPosition position = positions.get(pos);

            TypedCard card;
            // At last round only same card are available in hand
            do {
                int handIndex = (int) (Math.random() * 2);
                card = gc.getPlayerData(username).getHand().get(handIndex);
            } while (card == null);

            if (!gc.getManuscript(username).isPlaceable(position, card)) {
                card.flip();
            }

            // Wait before place card
            waitFor(waitTime);
            gc.placeCard(username, card, position);


            // Draw card
            waitFor(waitTime);
            boolean drawOutcome;
            do {
                boolean fromDeck = ((int) (Math.random() * 2)) % 2 == 0;
                CardType type = ((int) (Math.random() * 2) % 2 == 0) ? CardType.GOLD : CardType.RESOURCE;

                if (fromDeck) {
                    drawOutcome = gc.drawCoveredCard(username, type) == null;
                } else {
                    int index = ((int) (Math.random() * 2));
                    drawOutcome = gc.drawVisibleCard(username, type, index) == null;
                }

            } while (gc.getGameFlow().getState() == PLAYING && gc.getGameFlow().isCurrentPlayer(username) && drawOutcome);

        }
    }

    private void waitFor(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException("Bot failed to wait");
        }
    }

    //    private void printStats() {
    //        System.out.println(gc.getGameFlow().getPlayerScores());
    //    }
}
