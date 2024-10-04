package it.polimi.ingsw.client.local;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.controller.GameFlow;
import it.polimi.ingsw.controller.event.Event;
import it.polimi.ingsw.controller.event.EventType;
import it.polimi.ingsw.controller.interfaces.ClientGameController;
import it.polimi.ingsw.model.card.GoalCard;
import it.polimi.ingsw.model.card.StarterCard;
import it.polimi.ingsw.model.card.TypedCard;
import it.polimi.ingsw.model.card.properties.CardFace;
import it.polimi.ingsw.model.game.CardType;
import it.polimi.ingsw.model.game.CommonBoard;
import it.polimi.ingsw.model.player.ManuscriptPosition;
import it.polimi.ingsw.model.player.PlayerColor;
import it.polimi.ingsw.model.player.PlayerData;
import it.polimi.ingsw.model.player.PlayerManuscript;

import java.util.List;
import java.util.function.Consumer;

public class LocalClientGameController implements ClientGameController {

    private final GameController gc;
    private final String myUsername;

    public LocalClientGameController(GameController gc, String username) {
        this.gc = gc;
        this.myUsername = username;

    }

    public LocalClientGameController(GameController gc) {
        this.gc = gc;
        this.myUsername = gc.getGameFlow().getAllUsernames().get(0);
    }

    @Override
    public String getMyUsername() {
        return myUsername;
    }

    @Override
    public StarterCard getStarterCard(String username) {
        return gc.getStarterCard(username);
    }

    @Override
    public boolean setStarterCard(CardFace face) {
        return gc.setStarterCard(myUsername, face);
    }

    @Override
    public List<PlayerColor> getAvailableColors() {
        return gc.getAvailableColors();
    }

    @Override
    public boolean setPlayerColor(PlayerColor color) {
        return gc.setPlayerColor(myUsername, color);
    }

    @Override
    public List<GoalCard> getProposedPrivateGoals() {
        return gc.getProposedPrivateGoals(myUsername);
    }

    @Override
    public boolean choosePrivateGoal(GoalCard goal) {
        return gc.choosePrivateGoal(myUsername, goal);
    }

    @Override
    public PlayerData getPlayerData(String username) {
        if (myUsername.equals(username)) return gc.getPlayerData(username);
        return gc.getCleanPlayerData(username);
    }

    @Override
    public List<PlayerData> getAllPlayerData(String username) {
        return gc.getAllPlayerData(username);
    }

    @Override
    public PlayerManuscript getManuscript(String username) {
        return gc.getManuscript(username);
    }

    @Override
    public GameFlow getGameFlow() {
        return gc.getGameFlow();
    }

    @Override
    public CommonBoard getGameBoard() {
        return gc.getGameBoard();
    }

    @Override
    public boolean placeCard(TypedCard card, ManuscriptPosition position) {
        return gc.placeCard(myUsername, card, position);
    }

    @Override
    public TypedCard drawVisibleCard(CardType type, int index) {
        return gc.drawVisibleCard(myUsername, type, index);
    }

    @Override
    public TypedCard drawCoveredCard(CardType type) {
        return gc.drawCoveredCard(myUsername, type);
    }

    @Override
    public boolean gameEnded() {
        return gc.gameEnded();
    }

    @Override
    public String getWinner() {
        return gc.getWinner();
    }

    @Override
    @Deprecated
    public <T extends Event> void addEventHandler(EventType<T> type, Consumer<T> consumer) {
        gc.addEventHandler(this.myUsername, type, consumer);
    }

    @Override
    public <T extends Event> void removeEventHandler(EventType<T> type, Consumer<T> consumer) {
        gc.removeEventHandler(type, consumer);
    }


}
