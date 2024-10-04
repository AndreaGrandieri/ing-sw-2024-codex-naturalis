package it.polimi.ingsw.controller.interfaces;

import it.polimi.ingsw.controller.GameFlow;
import it.polimi.ingsw.controller.event.Event;
import it.polimi.ingsw.controller.event.EventType;
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

public interface ClientGameController {
    String getMyUsername();

        StarterCard getStarterCard(String username);

    boolean setStarterCard(CardFace face);

        List<PlayerColor> getAvailableColors();

    boolean setPlayerColor(PlayerColor color);

        List<GoalCard> getProposedPrivateGoals();

    boolean choosePrivateGoal(GoalCard goal);

        PlayerData getPlayerData(String username);

        List<PlayerData> getAllPlayerData(String username);

        PlayerManuscript getManuscript(String username);

        GameFlow getGameFlow();

        CommonBoard getGameBoard();

    boolean placeCard(TypedCard card, ManuscriptPosition position);

        TypedCard drawVisibleCard(CardType type, int index);

        TypedCard drawCoveredCard(CardType type);

    boolean gameEnded();

        String getWinner();

    <T extends Event> void addEventHandler(EventType<T> type, Consumer<T> consumer);

    <T extends Event> void removeEventHandler(EventType<T> type, Consumer<T> consumer);
}
