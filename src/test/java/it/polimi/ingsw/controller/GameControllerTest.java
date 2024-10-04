package it.polimi.ingsw.controller;

import it.polimi.ingsw.controller.example.GameControllerExample;
import it.polimi.ingsw.model.card.GoalCard;
import it.polimi.ingsw.model.card.TypedCard;
import it.polimi.ingsw.model.card.factory.ResourceCardFactory;
import it.polimi.ingsw.model.card.properties.CardFace;
import it.polimi.ingsw.model.game.CardType;
import it.polimi.ingsw.model.player.ManuscriptPosition;
import it.polimi.ingsw.model.player.PlayerColor;
import it.polimi.ingsw.model.player.PlayerData;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class GameControllerTest {

    private final List<String> players4 = List.of("g0", "g1", "g2", "g3");
    private final List<String> players2 = List.of("g0", "g1");

    @Test
    public void getAllPlayerDataKeepsDetailedPlayerInFirstPositionTest() {
        GameController gc = new GameController(players4);

        for (String u : gc.getGameFlow().getAllUsernames())
            gc.choosePrivateGoal(u, gc.getProposedPrivateGoals(u).get(0));

        PlayerData p0 = gc.getPlayerData("g0");
        PlayerData p1 = gc.getPlayerData("g1");
        PlayerData p2 = gc.getPlayerData("g2");
        PlayerData p3 = gc.getPlayerData("g3");

        List<PlayerData> all0 = gc.getAllPlayerData("g0");
        List<PlayerData> all1 = gc.getAllPlayerData("g1");
        List<PlayerData> all2 = gc.getAllPlayerData("g2");
        List<PlayerData> all3 = gc.getAllPlayerData("g3");

        assertEquals(p0.getUsername(), all0.get(0).getUsername());
        assertEquals(112, all0.get(1).getPrivateGoal().getId());
        assertEquals(112, all0.get(2).getPrivateGoal().getId());
        assertEquals(112, all0.get(3).getPrivateGoal().getId());

        assertEquals(p1.getUsername(), all1.get(0).getUsername());
        assertEquals(112, all1.get(1).getPrivateGoal().getId());
        assertEquals(112, all1.get(2).getPrivateGoal().getId());
        assertEquals(112, all1.get(3).getPrivateGoal().getId());

        assertEquals(p2.getUsername(), all2.get(0).getUsername());
        assertEquals(112, all2.get(1).getPrivateGoal().getId());
        assertEquals(112, all2.get(2).getPrivateGoal().getId());
        assertEquals(112, all2.get(3).getPrivateGoal().getId());

        assertEquals(p3.getUsername(), all3.get(0).getUsername());
        assertEquals(112, all3.get(1).getPrivateGoal().getId());
        assertEquals(112, all3.get(2).getPrivateGoal().getId());
        assertEquals(112, all3.get(3).getPrivateGoal().getId());

        assertEquals("g1", all0.get(1).getUsername());
        assertEquals("g2", all0.get(2).getUsername());
        assertEquals("g3", all0.get(3).getUsername());

        assertEquals("g2", all1.get(1).getUsername());
        assertEquals("g3", all1.get(2).getUsername());
        assertEquals("g0", all1.get(3).getUsername());

        assertEquals("g3", all2.get(1).getUsername());
        assertEquals("g0", all2.get(2).getUsername());
        assertEquals("g1", all2.get(3).getUsername());

        assertEquals("g0", all3.get(1).getUsername());
        assertEquals("g1", all3.get(2).getUsername());
        assertEquals("g2", all3.get(3).getUsername());
    }

    @Test
    public void setStarterCardTest() {
        GameController gc = new GameController(players4);

        gc.setStarterCard("g2", CardFace.FRONT);
        gc.setStarterCard("g3", CardFace.BACK);

        assertTrue(gc.getManuscript("g0").isEmpty());
        assertTrue(gc.getManuscript("g1").isEmpty());
        assertEquals(gc.getStarterCard("g2"), gc.getManuscript("g2").getCardAt(new ManuscriptPosition(0, 0)));
        assertEquals(gc.getStarterCard("g3"), gc.getManuscript("g3").getCardAt(new ManuscriptPosition(0, 0)));

        assertFalse(gc.setStarterCard("g2", CardFace.BACK));

        assertTrue(gc.setStarterCard("g0", CardFace.FRONT));
        assertTrue(gc.setStarterCard("g1", CardFace.FRONT));

        gc.choosePrivateGoal("g0", gc.getProposedPrivateGoals("g0").get(0));
        gc.choosePrivateGoal("g1", gc.getProposedPrivateGoals("g1").get(0));
        gc.choosePrivateGoal("g2", gc.getProposedPrivateGoals("g2").get(0));
        gc.choosePrivateGoal("g3", gc.getProposedPrivateGoals("g3").get(0));

        assertEquals(GameState.SETTING, gc.getGameFlow().getState());

        assertFalse(gc.setStarterCard("g0", CardFace.BACK));
    }

    @Test
    public void getAvailableColorsReturnsEffectiveAvailableColorsTest() {

        GameController gc = new GameController(players4);

        assertTrue(gc.getAvailableColors().containsAll(List.of(
                PlayerColor.GREEN,
                PlayerColor.RED,
                PlayerColor.YELLOW,
                PlayerColor.BLUE)));
        assertTrue(List.of(
                PlayerColor.GREEN,
                PlayerColor.RED,
                PlayerColor.YELLOW,
                PlayerColor.BLUE).containsAll(gc.getAvailableColors()));

        gc.setPlayerColor("g0", PlayerColor.YELLOW);

        assertTrue(gc.getAvailableColors().containsAll(List.of(
                PlayerColor.GREEN,
                PlayerColor.RED,
                PlayerColor.BLUE)));
        assertTrue(List.of(
                PlayerColor.GREEN,
                PlayerColor.RED,
                PlayerColor.BLUE).containsAll(gc.getAvailableColors()));

    }

    @Test
    public void cannotSetColorTwiceTest() {
        GameController gc = new GameController(players4);

        assertTrue(gc.setPlayerColor("g1", PlayerColor.YELLOW));
        assertFalse(gc.setPlayerColor("g1", PlayerColor.GREEN));
    }

    @Test
    public void cannotSetColorIfPlayerDoesNotExistTest() {
        GameController gc = new GameController(players4);

        assertFalse(gc.setPlayerColor("pippo", PlayerColor.GREEN));
    }

    @Test
    public void cannotSetColorDuringPlayingStateTest() {
        GameController gc = GameControllerExample.gc(List.of("pippo", "pluto"));

        assertEquals(GameState.PLAYING, gc.getGameFlow().getState());
        assertFalse(gc.setPlayerColor("pippo", PlayerColor.GREEN));
    }

    @Test
    public void getProposedPrivateGoalsTest() {
        GameController gc = new GameController(players4);

        assertNotEquals(null, gc.getProposedPrivateGoals("g0"));
    }

    @Test
    public void choosePrivateGoalTest() {
        GameController gc = new GameController(players4);

        GoalCard expected = gc.getProposedPrivateGoals("g0").get(1);
        gc.choosePrivateGoal("g0", expected);

        assertEquals(expected, gc.getPlayerData("g0").getPrivateGoal());

        assertFalse(gc.choosePrivateGoal("pippo", expected));
        assertFalse(gc.choosePrivateGoal("g1", expected));

    }

    @Test
    public void cannotChoosePrivateGoalTwiceTest() {
        GameController gc = new GameController(players4);

        GoalCard goal = gc.getProposedPrivateGoals("g0").get(1);
        assertTrue(gc.choosePrivateGoal("g0", goal));

        goal = gc.getProposedPrivateGoals("g0").get(0);
        assertFalse(gc.choosePrivateGoal("g0", goal));
    }

    @Test
    public void getManuscriptTest() {
        GameController gc = new GameController(players4);

        gc.setStarterCard("g0", CardFace.FRONT);

        assertEquals(gc.getStarterCard("g0"), gc.getManuscript("g0").getCardAt(new ManuscriptPosition(0, 0)));
        assertTrue(gc.getManuscript("g1").isEmpty());
        assertThrows(RuntimeException.class, () -> gc.getManuscript("ciccio"));
    }

    @Test
    public void cannotPlaceNullCardTest() {
        GameController gc = GameControllerExample.gc(players4);

        assertFalse(gc.placeCard("g0", null, new ManuscriptPosition(1, 0)));

    }

    @Test
    public void cannotPlaceAtNullPositionTest() {
        GameController gc = GameControllerExample.gc(players4);

        TypedCard card = gc.getPlayerData("g0").getHand().get(0);
        card.flip();
        assertFalse(gc.placeCard("g0", card, null));

    }

    @Test
    public void cannotPlaceDuringSettingStateTest() {
        GameController gc = new GameController(players4);

        assertThrows(RuntimeException.class, () -> gc.placeCard("g0", new ResourceCardFactory().generateCard(1), new ManuscriptPosition(1, 0)));

    }

    @Test
    public void cannotPlaceCardDuringOtherPlayerTurnTest() {
        GameController gc = GameControllerExample.gc(players4);
        String currPlayer = gc.getGameFlow().getCurrentPlayer();
        String notCurrPlayer = gc.getGameFlow().getAllUsernames().get(gc.getGameFlow().getAllUsernames().indexOf(currPlayer) + 1);

        TypedCard card = gc.getPlayerData(notCurrPlayer).getHand().get(0);
        card.flip();
        assertFalse(gc.placeCard(notCurrPlayer, card, new ManuscriptPosition(1, 0)));

    }

    @Test
    public void cannotPlaceCardIfPlayerDoesNotExistTest() {
        GameController gc = GameControllerExample.gc(players4);

        assertFalse(gc.placeCard("ciccio", new ResourceCardFactory().generateCard(1), new ManuscriptPosition(1, 0)));

    }

    @Test
    public void cannotPlaceOtherPlayerCardTest() {
        GameController gc = GameControllerExample.gc(players4);

        String currPlayer = gc.getGameFlow().getCurrentPlayer();
        String notCurrPlayer = gc.getGameFlow().getAllUsernames().get(gc.getGameFlow().getAllUsernames().indexOf(currPlayer) + 1);


        TypedCard card = gc.getPlayerData(notCurrPlayer).getHand().get(0);
        card.flip();
        assertFalse(gc.placeCard(currPlayer, card, new ManuscriptPosition(1, 0)));

    }

    @Test
    public void cannotPlaceOverStarterCardPositionTest() {
        GameController gc = GameControllerExample.gc(players4);

        String currPlayer = gc.getGameFlow().getCurrentPlayer();

        TypedCard card = gc.getPlayerData(currPlayer).getHand().get(0);
        card.flip();
        assertFalse(gc.placeCard(currPlayer, card, new ManuscriptPosition(0, 0)));

    }

    @Test
    public void placeCardTest() {
        GameController gc = GameControllerExample.gc(players4);

        String currPlayer = gc.getGameFlow().getCurrentPlayer();

        TypedCard card = gc.getPlayerData(currPlayer).getHand().get(0);
        card.flip();
        assertTrue(gc.placeCard(currPlayer, card, new ManuscriptPosition(1, 0)));

    }

    @Test
    public void placingCardDuringLastRoundGivesTurnToNextPlayerTest() {
        GameController gc = GameControllerExample.lastRoundGc(players4);

        String currPlayer = gc.getGameFlow().getCurrentPlayer();

        assertEquals(GameState.LAST_ROUND, gc.getGameFlow().getState());
        TypedCard card = gc.getPlayerData(currPlayer).getHand().get(1);
        card.flip();
        ManuscriptPosition position = gc.getManuscript(currPlayer).getAllAvailablePositionsList().get(0);
        gc.placeCard(currPlayer, card, position);
        assertNotEquals(currPlayer, gc.getGameFlow().getCurrentPlayer());
    }


    @Test
    public void cannotDrawDuringSettingTest() {
        GameController gc = new GameController(players4);

        assertThrows(RuntimeException.class, () -> gc.drawVisibleCard("g0", CardType.GOLD, 0));
        assertThrows(RuntimeException.class, () -> gc.drawCoveredCard("g0", CardType.GOLD));

    }

    @Test
    public void cannotDrawIfPlayerHasAllCardsInHandTest() {
        GameController gc = GameControllerExample.gc(players4);

        assertThrows(RuntimeException.class, () -> gc.drawVisibleCard("g0", CardType.GOLD, 0));
        assertThrows(RuntimeException.class, () -> gc.drawCoveredCard("g0", CardType.GOLD));

    }

    @Test
    public void cannotDrawIfIsNotCurrentPlayerTest() {
        GameController gc = GameControllerExample.gc(players4);

        String currPlayer = gc.getGameFlow().getCurrentPlayer();
        String notCurrPlayer = gc.getGameFlow().getAllUsernames().get(gc.getGameFlow().getAllUsernames().indexOf(currPlayer) + 1);

        assertThrows(RuntimeException.class, () -> gc.drawVisibleCard(notCurrPlayer, CardType.GOLD, 0));
        assertThrows(RuntimeException.class, () -> gc.drawCoveredCard(notCurrPlayer, CardType.GOLD));

    }

    @Test
    public void cannotDrawIfPlayerDoesNotExistTest() {
        GameController gc = GameControllerExample.gc(players4);
        String currPlayer = gc.getGameFlow().getCurrentPlayer();

        TypedCard card = gc.getPlayerData(currPlayer).getHand().get(0);
        card.flip();
        gc.placeCard(currPlayer, card, new ManuscriptPosition(1, 0));

        assertThrows(RuntimeException.class, () -> gc.drawVisibleCard("ciccio", CardType.GOLD, -1));
        assertThrows(RuntimeException.class, () -> gc.drawCoveredCard("ciccio", CardType.GOLD));

    }

    @Test
    public void cannotDrawIfAllDecksAreEmptyTest() {
        GameController gc = GameControllerExample.emptyDecksGc();

        String currPlayer = gc.getGameFlow().getCurrentPlayer();

        TypedCard card = gc.getPlayerData(currPlayer).getHand().get(0);
        card.flip();
        gc.placeCard(currPlayer, card, gc.getManuscript(currPlayer).getAllAvailablePositionsList().get(0));

        assertThrows(RuntimeException.class, () -> gc.drawVisibleCard(currPlayer, CardType.GOLD, 1));
        assertThrows(RuntimeException.class, () -> gc.drawCoveredCard(currPlayer, CardType.GOLD));

    }

    @Test
    public void cannotDrawWrongIndexVisibleCardsTest() {
        GameController gc = GameControllerExample.gc(players4);
        String currPlayer = gc.getGameFlow().getCurrentPlayer();

        TypedCard card = gc.getPlayerData(currPlayer).getHand().get(0);
        card.flip();
        gc.placeCard(currPlayer, card, new ManuscriptPosition(1, 0));

        assertThrows(RuntimeException.class, () -> gc.drawVisibleCard(currPlayer, CardType.GOLD, -1));
        assertThrows(RuntimeException.class, () -> gc.drawVisibleCard(currPlayer, CardType.GOLD, 2));
    }

    @Test
    public void drawVisibleCardReturnsNextVisibleCardInSamePositionTest() {
        GameController gc = GameControllerExample.gc(players4);
        String currPlayer = gc.getGameFlow().getCurrentPlayer();

        TypedCard card = gc.getPlayerData(currPlayer).getHand().get(0);
        card.flip();
        gc.placeCard(currPlayer, card, new ManuscriptPosition(1, 0));

        TypedCard nextCard = gc.drawVisibleCard(currPlayer, CardType.GOLD, 1);

        assertEquals(gc.getGameBoard().getVisibleCards(CardType.GOLD).get(1), nextCard);
    }

    @Test
    public void drawVisibleCardAddsCardToHandTest() {
        GameController gc = GameControllerExample.gc(players4);
        String currPlayer = gc.getGameFlow().getCurrentPlayer();

        List<TypedCard> prePlaceHand = gc.getPlayerData(currPlayer).getHand();

        TypedCard card = gc.getPlayerData(currPlayer).getHand().get(0);
        card.flip();
        gc.placeCard(currPlayer, card, new ManuscriptPosition(1, 0));
        gc.drawVisibleCard(currPlayer, CardType.GOLD, 1);

        List<TypedCard> postPlaceHand = gc.getPlayerData(currPlayer).getHand();

        assertNotEquals(prePlaceHand.get(0), postPlaceHand.get(0));
        assertEquals(prePlaceHand.get(1), postPlaceHand.get(1));
        assertEquals(prePlaceHand.get(2), postPlaceHand.get(2));

    }

    @Test
    public void drawVisibleCardGoesToNextTurnTest() {
        GameController gc = GameControllerExample.gc(players4);
        String currPlayer = gc.getGameFlow().getCurrentPlayer();
        int turn = gc.getGameFlow().getTurn();

        TypedCard card = gc.getPlayerData(currPlayer).getHand().get(0);
        card.flip();
        gc.placeCard(currPlayer, card, new ManuscriptPosition(1, 0));
        gc.drawVisibleCard(currPlayer, CardType.GOLD, 1);

        String newCurrPlayer = gc.getGameFlow().getCurrentPlayer();
        int newTurn = gc.getGameFlow().getTurn();

        assertNotEquals(currPlayer, newCurrPlayer);
        assertEquals(turn + 1, newTurn);
    }

    @Test
    public void drawCoveredCardAddsCardToHandTest() {
        GameController gc = GameControllerExample.gc(players4);
        String currPlayer = gc.getGameFlow().getCurrentPlayer();

        List<TypedCard> prePlaceHand = gc.getPlayerData(currPlayer).getHand();

        TypedCard card = gc.getPlayerData(currPlayer).getHand().get(0);
        card.flip();
        gc.placeCard(currPlayer, card, new ManuscriptPosition(1, 0));
        gc.drawCoveredCard(currPlayer, CardType.GOLD);

        List<TypedCard> postPlaceHand = gc.getPlayerData(currPlayer).getHand();

        assertNotEquals(prePlaceHand.get(0), postPlaceHand.get(0));
        assertEquals(prePlaceHand.get(1), postPlaceHand.get(1));
        assertEquals(prePlaceHand.get(2), postPlaceHand.get(2));
    }

    @Test
    public void drawCoveredCardGoesToNextTurnTest() {
        GameController gc = GameControllerExample.gc(players4);
        String currPlayer = gc.getGameFlow().getCurrentPlayer();
        int turn = gc.getGameFlow().getTurn();

        TypedCard card = gc.getPlayerData(currPlayer).getHand().get(0);
        card.flip();
        gc.placeCard(currPlayer, card, new ManuscriptPosition(1, 0));
        gc.drawCoveredCard(currPlayer, CardType.GOLD);

        String newCurrPlayer = gc.getGameFlow().getCurrentPlayer();
        int newTurn = gc.getGameFlow().getTurn();

        assertNotEquals(currPlayer, newCurrPlayer);
        assertEquals(turn + 1, newTurn);
    }

    @Test
    public void drawCoveredCardReturnsTheCardDrawnTest() {
        GameController gc = GameControllerExample.gc(players4);
        String currPlayer = gc.getGameFlow().getCurrentPlayer();

        TypedCard card = gc.getPlayerData(currPlayer).getHand().get(0);
        card.flip();
        gc.placeCard(currPlayer, card, new ManuscriptPosition(1, 0));
        TypedCard drawn = gc.drawCoveredCard(currPlayer, CardType.GOLD);

        TypedCard effective = gc.getPlayerData(currPlayer).getHand().get(0);

        assertEquals(effective, drawn);
    }

    @Test
    public void gameEndedTest() {
        List<String> players = this.players4;
        GameController gc = GameControllerExample.gc(players);

        assertFalse(gc.gameEnded());

        String currPlayer = gc.getGameFlow().getCurrentPlayer();

        TypedCard card = gc.getPlayerData(currPlayer).getHand().get(0);
        card.flip();
        gc.placeCard(currPlayer, card, gc.getManuscript(currPlayer).getAllAvailablePositionsList().get(0));
        gc.drawCoveredCard(currPlayer, CardType.GOLD);
        assertFalse(gc.gameEnded());

        gc = GameControllerExample.gc(players);

        GameControllerExample.simulateFullGame(gc);
        assertTrue(gc.gameEnded());

        gc.getWinner();
        assertFalse(gc.gameEnded());

    }

    @Test
    public void getWinnerReturnsNullOrWinnerTest() {
        GameController gc = GameControllerExample.gc(players4);

        assertNull(gc.getWinner());

        String currPlayer = gc.getGameFlow().getCurrentPlayer();

        TypedCard card = gc.getPlayerData(currPlayer).getHand().get(0);
        card.flip();
        gc.placeCard(currPlayer, card, new ManuscriptPosition(1, 0));
        gc.drawCoveredCard(currPlayer, CardType.GOLD);
        assertNull(gc.getWinner());

        gc = GameControllerExample.gc(players4);

        GameControllerExample.simulateFullGame(gc);

        if (gc.getGameFlow().getLeadingPlayers().size() == 1) {
            System.out.println(gc.getWinner());
            assertNotNull(gc.getWinner());
            assertNotNull(gc.getWinner());
            assertNotNull(gc.getWinner());
        }

    }

    @Test
    public void cannotDisconnectNotExistingPlayerTest() {
        GameController gc = new GameController(players4);

        assertFalse(gc.disconnectPlayer("pippo"));
    }

    @Test
    public void disconnectingAfterPlacingAddsCardToYourHandTest() {
        GameController gc = GameControllerExample.gc(players4);
        assertEquals(GameState.PLAYING, gc.getGameFlow().getState());

        String currPlayer = gc.getGameFlow().getCurrentPlayer();
        TypedCard card = gc.getPlayerData(currPlayer).getHand().get(0);
        card.flip();

        assertTrue(gc.placeCard(currPlayer, card, new ManuscriptPosition(1, 0)));

        assertTrue(gc.getPlayerData(currPlayer).getHand().contains(null));

        assertTrue(gc.disconnectPlayer(currPlayer));

        assertFalse(gc.getPlayerData(currPlayer).getHand().contains(null));
    }

    @Test
    public void disconnectingDuringSettingCanMakeGameStartTest() {
        GameController gc = new GameController(players4);

        for (int i = 0; i < 3; i++) {
            String currPlayer = players4.get(i);
            assertTrue(gc.setPlayerColor(currPlayer, gc.getAvailableColors().get(0)));
            assertTrue(gc.setStarterCard(currPlayer, CardFace.FRONT));
            assertTrue(gc.choosePrivateGoal(currPlayer, gc.getProposedPrivateGoals(currPlayer).get(0)));
        }
        assertEquals(GameState.SETTING, gc.getGameFlow().getState());

        gc.disconnectPlayer("g3");

        assertEquals(GameState.PLAYING, gc.getGameFlow().getState());

    }

    @Test
    public void gameTurnsIdleIfOnlyOnePlayerIsPresentTest() {
        GameController gc = new GameController(players2);

        assertFalse(gc.getGameFlow().isIdle());

        gc.disconnectPlayer("g0");

        assertTrue(gc.getGameFlow().isIdle());
    }

    @Test
    public void cannotDoAnyOperationIfGameIsIdleTest() {
        GameController gc = new GameController(players2);
        assertFalse(gc.getGameFlow().isIdle());

        assertTrue(gc.setPlayerColor("g0", gc.getAvailableColors().get(0)));
        assertTrue(gc.setStarterCard("g0", CardFace.FRONT));
        assertTrue(gc.choosePrivateGoal("g0", gc.getProposedPrivateGoals("g0").get(0)));

        gc.disconnectPlayer("g1");
        assertTrue(gc.getGameFlow().isIdle());
        assertEquals(GameState.PLAYING, gc.getGameFlow().getState());

        assertEquals("g0", gc.getGameFlow().getCurrentPlayer());
        TypedCard card = gc.getPlayerData("g0").getHand().get(0);
        card.flip();
        assertFalse(gc.placeCard("g0", card, new ManuscriptPosition(0, 1)));

        GameController gc1 = GameControllerExample.gc(players2);
        TypedCard card1 = gc1.getPlayerData("g0").getHand().get(0);
        card1.flip();
        assertTrue(gc1.placeCard("g0", card1, new ManuscriptPosition(1, 0)));
        gc1.disconnectPlayer("g1");

        assertNull(gc1.drawCoveredCard("g0", CardType.GOLD));
        assertNull(gc1.drawVisibleCard("g0", CardType.GOLD, 0));
    }

    @Test
    public void cannotReconnectNotExistingPlayerTest() {
        GameController gc = new GameController(players4);

        assertFalse(gc.reconnectPlayer("pippo"));
    }

    @Test
    public void reconnectionWorksAsExpectedTest() {
        GameController gc = new GameController(players4);

        gc.disconnectPlayer("g0");
        assertEquals(3, gc.getGameFlow().getConnectedPlayers().size());

        assertTrue(gc.reconnectPlayer("g1"));
        assertEquals(3, gc.getGameFlow().getConnectedPlayers().size());

        assertTrue(gc.reconnectPlayer("g0"));
        assertEquals(4, gc.getGameFlow().getConnectedPlayers().size());
    }

    @Test
    public void reconnectionMakesYouOutOfIdleTest() {
        GameController gc = GameControllerExample.gc(players2);

        String currPlayer = gc.getGameFlow().getCurrentPlayer();
        TypedCard card = gc.getPlayerData(currPlayer).getHand().get(0);
        card.flip();
        assertTrue(gc.placeCard(currPlayer, card, new ManuscriptPosition(1, 0)));

        gc.disconnectPlayer("g1");
        assertTrue(gc.getGameFlow().isIdle());

        gc.reconnectPlayer("g1");
        assertFalse(gc.getGameFlow().isIdle());

        gc.drawCoveredCard(currPlayer, CardType.GOLD);

        assertEquals("g1", gc.getGameFlow().getCurrentPlayer());
    }

    @Test
    public void canReconnectAfterSkippingSettingTest() {
        GameController gc = new GameController(players4);

        for (int i = 0; i < 3; i++) {
            String currPlayer = players4.get(i);
            assertTrue(gc.setPlayerColor(currPlayer, gc.getAvailableColors().get(0)));
            assertTrue(gc.setStarterCard(currPlayer, CardFace.FRONT));
            assertTrue(gc.choosePrivateGoal(currPlayer, gc.getProposedPrivateGoals(currPlayer).get(0)));
        }
        assertEquals(GameState.SETTING, gc.getGameFlow().getState());

        gc.disconnectPlayer("g3");

        assertEquals(GameState.PLAYING, gc.getGameFlow().getState());

        GameControllerExample.playTurn(gc, "g0");
        GameControllerExample.playTurn(gc, "g1");
        GameControllerExample.playTurn(gc, "g2");
        GameControllerExample.playTurn(gc, "g0");
        GameControllerExample.playTurn(gc, "g1");
        GameControllerExample.playTurn(gc, "g2");
        GameControllerExample.playTurn(gc, "g0");
        GameControllerExample.playTurn(gc, "g1");

        gc.reconnectPlayer("g3");

        GameControllerExample.playTurn(gc, "g2");
        GameControllerExample.playTurn(gc, "g3");

        assertEquals(4, gc.getManuscript("g0").getNumberOfCards());
        assertEquals(4, gc.getManuscript("g1").getNumberOfCards());
        assertEquals(4, gc.getManuscript("g2").getNumberOfCards());
        assertEquals(2, gc.getManuscript("g3").getNumberOfCards());
    }

    @Test
    public void turnPassesToNextPlayerIfCurrentPlayerDisconnectsTest() {
        GameController gc = GameControllerExample.gc(players4);

        gc.disconnectPlayer("g0");
        assertEquals("g1", gc.getGameFlow().getCurrentPlayer());

        gc.disconnectPlayer("g2");
        assertEquals("g1", gc.getGameFlow().getCurrentPlayer());
    }
}
