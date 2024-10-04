package it.polimi.ingsw.gui.views;

import it.polimi.ingsw.controller.GameFlow;
import it.polimi.ingsw.controller.GameState;
import it.polimi.ingsw.controller.event.Event;
import it.polimi.ingsw.controller.event.EventType;
import it.polimi.ingsw.controller.event.game.*;
import it.polimi.ingsw.controller.interfaces.ClientGameController;
import it.polimi.ingsw.gui.components.panes.*;
import it.polimi.ingsw.gui.components.panes.manuscript.ManuscriptBox;
import it.polimi.ingsw.gui.components.views.ChatView;
import it.polimi.ingsw.gui.support.*;
import it.polimi.ingsw.gui.support.context.ContextManager;
import it.polimi.ingsw.gui.support.context.GameContext;
import it.polimi.ingsw.gui.support.context.PlayerDataSource;
import it.polimi.ingsw.gui.support.helper.GuiGameState;
import it.polimi.ingsw.gui.support.helper.PC;
import it.polimi.ingsw.gui.support.info.*;
import it.polimi.ingsw.gui.support.interfaces.HandController;
import it.polimi.ingsw.gui.support.interfaces.ManuscriptController;
import it.polimi.ingsw.gui.support.interfaces.ResponsiveContainer;
import it.polimi.ingsw.gui.support.interfaces.ViewRouter;
import it.polimi.ingsw.model.card.TypedCard;
import it.polimi.ingsw.model.game.CardType;
import it.polimi.ingsw.model.game.CommonBoard;
import it.polimi.ingsw.model.player.ManuscriptPosition;
import it.polimi.ingsw.model.player.PlayerData;
import javafx.animation.Animation;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Collections.nCopies;
import static javafx.beans.binding.Bindings.createObjectBinding;

public class GameView extends VBox implements Initializable {
    private final List<String> playerNames;
    private final String myUsername;
    private final ClientGameController gc;
    private final GameContext gameCtx;
    private final ViewRouter viewRouter;
    private final ContextManager ctxMan;
    public TabPane tabPane;
    public Tab tab;
    public Tab chatTab;
    @FXML
    private VBox mainBox;
    @FXML
    private GridPane otherPlayersGrid;
    @FXML
    private Pane hoverPane;
    @FXML
    private MainManuscriptPane mainPlayerPane;
    @FXML
    private MainHandPane mainHandPane;
    @FXML
    private DeckPane deckPane;
    @FXML
    private GoalsPane goalsPane;
    private List<OtherPlayerPane> otherPlayerList;
    private List<ResponsiveContainer> responsiveContainers;
    private List<String> otherPlayerNameList;
    private PlayerDataSource mainDataSource;
    private ObjectProperty<GuiGameState> state;

    public GameView(ViewRouter viewRouter, ContextManager ctxMan) {
        this.viewRouter = viewRouter;
        this.ctxMan = ctxMan;
        this.gameCtx = ctxMan.gameCtx();
        playerNames = gameCtx.getGameFlow().getAllUsernames();
        gc = gameCtx.gc();
        myUsername = gc.getMyUsername();

        FXUtils.loadRootFXMLView(this);

        state = new SimpleObjectProperty<>();
        state.bind(createObjectBinding(
                () -> {
                    PlayerData playerData = gameCtx.getPlayerData(myUsername);
                    GameFlow gameFlow = gameCtx.getGameFlow();
                    String currentPlayer = gameFlow.getCurrentPlayer();
                    GameState state = gameFlow.getState();
                    //                    System.out.println("UPDATE STATE " + state + " " + currentPlayer);

                    if (currentPlayer.equals(myUsername)) {
                        if (gameFlow.isIdle()) {
                            return GuiGameState.WAIT_TURN;
                        }

                        if (state == GameState.PLAYING) {
                            if (canDraw(playerData)) {
                                // Empty deck?
                                return GuiGameState.DRAW;
                            } else return GuiGameState.PLACE_CARD;
                        }

                        if (state == GameState.LAST_ROUND) {
                            return GuiGameState.PLACE_CARD;
                        }


                    }
                    return GuiGameState.WAIT_TURN;

                }, gameCtx.gameFlowProperty(), gameCtx.playerDataProperty(myUsername)
        ));
        FXBind.subscribe(state, this::setState);

        // remove tabPane clip
        ComponentBuilder.removeTabPaneClip(tabPane, tab);
        ComponentBuilder.adjustTabButtonsWidth(tabPane);

        ChatView chatView = new ChatView(ctxMan);
        chatView.getStyleClass().add("dark-border");
        chatView.setStyle("-fx-font-size:14px; -fx-border-radius:5 5 0 0");
        chatTab.setContent(chatView);

    }

    private static boolean canDraw(PlayerData playerData) {
        return playerData.getHand().contains(null);
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        CommonBoard board = gameCtx.getBoard();

        tabPane.setClip(null);
        tab.getContent().setClip(null);

        gc.addEventHandler(GameEvents.TURN_CHANGE, this::onGCTurnChange);
        gc.addEventHandler(GameEvents.PLACE_EVENT, this::onGCOtherPlayerPlace);
        gc.addEventHandler(GameEvents.DRAW_COVERED, this::onGCDrawCovered);
        gc.addEventHandler(GameEvents.DRAW_VISIBLE, this::onGCDrawVisible);
        gc.addEventHandler(GameEvents.STATE_CHANGE, this::onGCStateChange);

        // Data
        mainDataSource = gameCtx.getDataController(myUsername);
        mainPlayerPane.getManuscriptPane().manuscriptProperty().bind(
                createObjectBinding(
                        () -> new PC<>(gameCtx.manuscriptProperty(myUsername).get(), ManuscriptBox.MANUSCRIPT_UPDATE),
                        gameCtx.manuscriptProperty(myUsername))
        );
        mainPlayerPane.getManuscriptPane().ratioProperty().set(0.8);
        mainPlayerPane.setDataController(mainDataSource);

        mainHandPane.setDataController(mainDataSource);
        mainDataSource.getPlayerInfo().setMainPlayer(true);
        mainDataSource.getPlayerInfo().setManuscriptPosition(new Point2D(0, 1.0));

        otherPlayerNameList = playerNames.stream().filter(n -> !n.equals(myUsername)).toList();
        int numOtherPlayers = otherPlayerNameList.size();
        otherPlayersGrid.getRowConstraints().addAll(
                nCopies(numOtherPlayers, 100.0 / numOtherPlayers).stream()
                        .map(i -> Util.createInit(new RowConstraints(), o -> o.setPercentHeight(i)))
                        .toList()
        );
        //        System.out.println(otherPlayersGrid.getColumnConstraints());

        otherPlayerList = Stream.generate(OtherPlayerPane::new).limit(numOtherPlayers).toList();

        otherPlayerNameList.forEach(playerName -> {
            int playerIndex = otherPlayerNameList.indexOf(playerName);
            OtherPlayerPane opp = this.otherPlayerList.get(playerIndex);
            opp.setId(Integer.toString(playerIndex + 1));
            otherPlayersGrid.add(opp, 0, playerIndex);

            PlayerDataSource oppPlayerDataSource = gameCtx.getDataController(playerName);
            opp.setDataController(oppPlayerDataSource);

            if (playerIndex == 0) opp.setStyle("-fx-border-width: 4;-fx-border-radius: 10 10 0 0");


            if (playerIndex == numOtherPlayers) {
                ManuscriptBox manuscriptPane = opp.getManuscriptPane();
                opp.setStyle("-fx-border-width:0 4 0 4");
            }

            if (playerIndex == numOtherPlayers) {
                ManuscriptBox manuscriptPane = opp.getManuscriptPane();
                opp.getStyleClass().add("lastOther");

                manuscriptPane.clipProperty().bind(createObjectBinding(
                        () -> ComponentBuilder.oneRoundPath(
                                10,
                                new Dimension2D(manuscriptPane.getWidth(), manuscriptPane.getHeight())
                        ),
                        manuscriptPane.widthProperty(), manuscriptPane.heightProperty()
                ));
            }


        });

        // MainPlayerPane
        mainPlayerPane.getManuscriptPane().clickedPlaceholderProperty().addListener(
                (observableValue, intPoint, t1) -> onManuscriptPlaceholderClick(t1.toManuscriptPosition())
        );
        mainPlayerPane.getManuscriptPane().requestFocus();

        // Main Hand Pane
        mainHandPane.setOnCardSelected(id -> mainDataSource.setPlayerInfo(mainDataSource.getPlayerInfo().setHandIndex(id)));

        // DeckPane
        deckPane.setOnDrawVisible(this::onDeckPaneDrawVisible);
        deckPane.setOnDrawFromDeck(this::onDeckPaneDrawFromDeck);

        deckPane.setVars(board);
        deckPane.setDataCtx(gameCtx);

        // Goals Pane
        goalsPane.setDataCtx(gameCtx);

        // State
        //        setState(GuiGameState.WAIT_TURN);

        gameCtx.screenProperty().bind(createObjectBinding(
                () -> (mainBox.widthProperty().get() != 0.0 && mainBox.heightProperty().get() != 0.0) ?
                        new Dimension2D(mainBox.widthProperty().get(), mainBox.heightProperty().get()) :
                        new Dimension2D(1000, 800),
                mainBox.widthProperty(), mainBox.heightProperty()
        ));

        responsiveContainers = new ArrayList<>(List.of(mainHandPane, goalsPane, deckPane, mainPlayerPane));
        responsiveContainers.addAll(this.otherPlayerList);
        responsiveContainers.forEach(rc -> rc.setCardSizeController(gameCtx));

        //        gameCtx.updateManuscripts();

    }

    public void setState(GuiGameState state) {
        System.out.println("CHANGED STATE: " + state);

        deckPane.setState(state);
        mainHandPane.setState(state);
        mainPlayerPane.getManuscriptPane().setState(state);
        mainPlayerPane.setState(state);
    }

    private void onGCStateChange(StateChangeEvent e) {
        System.out.println("Called State Change " + e);

        GameState newState = e.newState();
        gameCtx.updateGameFlow();
        if (newState == GameState.IDLE) {
            System.out.println("IDLE " + gameCtx.getGameFlow().isIdle());
            //            setState(GuiGameState.WAIT_TURN);
        }
        //                if (newState == GameState.PLAYING)  onGC

        if (newState == GameState.POST_GAME) {
            System.out.println("END GAME");
            gc.getWinner();
            gameCtx.updatePlayersData();
            viewRouter.goTo(ViewRoute.END);

        }

    }

    // GameController Handlers
    private void onGCTurnChange(TurnChangeEvent e) {
        System.out.println("Called Turn Change " + e);
        gameCtx.updateGameFlow();
        String username = e.currentUsername();

        if (username.equals(myUsername)) {
            gameCtx.updateGameFlow();
            //            gameCtx.updatePlayersData();
            //            setState(GuiGameState.PLACE_CARD);
        }
    }

    private void onGCOtherPlayerPlace(PlaceCardEvent e) {
        if (e.username().equals(myUsername)) return;
        System.out.println(e);
        String currentPlayerName = e.username();
        int i = otherPlayerNameList.indexOf(currentPlayerName);
        System.out.println("search " + currentPlayerName + " in " + otherPlayerList);
        PlayerInfo currentInfo = gameCtx.getPlayerInfo(currentPlayerName);


        int handIndex = e.handIndex();


        gameCtx.setPlayerInfo(currentPlayerName, currentInfo.setHandIndex(handIndex));

        ManuscriptPosition mp = e.position();

        OtherPlayerPane opp = otherPlayerList.get(i);

        ManuscriptController manuscriptController = opp.getManuscriptPane();

        manuscriptController.hideCardAtPosition(mp);
        manuscriptController.updateManuscript();
        Util.runWaitUI(gameCtx::updateManuscripts);

        Animation anim = AnimationBuilder.placeCardAnimation(
                new AnimCardPlaceInfo(
                        e.card().cloneCard(),
                        handIndex,
                        opp,
                        mp,
                        manuscriptController
                ), hoverPane);

        anim.setOnFinished(actionEvent -> {

        });
        anim.play();

        commonPlace();
    }

    private void onGCDrawVisible(DrawVisibleEvent e) {
        if (e.username().equals(myUsername)) return;

        System.out.println(e);

        String currentPlayerName = e.username();

        int playerIndex = otherPlayerNameList.indexOf(currentPlayerName);
        PlayerInfo currentInfo = gameCtx.getPlayerInfo(currentPlayerName);

        TypedCard selectedCard = e.visibleCard();
        TypedCard newCard = e.newDeckCard();

        OtherPlayerPane opp = otherPlayerList.get(playerIndex);

        Animation an = AnimationBuilder.visibleDrawToHandAnimation(
                new AnimDrawVisibleToHandInfo(
                        e.visibleCardType(),
                        selectedCard.cloneCard(),
                        e.visibleCardIndex(),
                        newCard.cloneCard(),
                        gameCtx.getBoard().isDecksEmpty(e.visibleCardType()),
                        deckPane,
                        currentInfo.handIndex(),
                        opp
                ), hoverPane);
        an.setOnFinished(actionEvent -> {
        });

        commonDraw(opp, an);
    }

    private void onGCDrawCovered(DrawCoveredEvent e) {
        if (e.username().equals(myUsername)) return;

        System.out.println(e);

        List<PlayerData> playersData = gameCtx.getPlayersData();

        String currentPlayerName = e.username();

        int playerIndex = otherPlayerNameList.indexOf(currentPlayerName);


        OtherPlayerPane opp = otherPlayerList.get(playerIndex);
        PlayerInfo currentInfo = gameCtx.getPlayerInfo(currentPlayerName);

        HandController handController = opp;


        Animation an = AnimationBuilder.deckDrawToHandAnimation(
                new AnimDrawDeckToHandInfo(
                        e.drawnCardType(),
                        e.drawnDeckCard().cloneCard(),
                        deckPane,
                        currentInfo.handIndex(),
                        handController
                ), hoverPane);
        an.setOnFinished(actionEvent -> {
        });

        commonDraw(handController, an);


    }

    private void onManuscriptPlaceholderClick(ManuscriptPosition mp) {

        new Thread(() -> {
            Integer selectedCardIndex = mainHandPane.getSelectedCardIndex();
            TypedCard cardInHand = gameCtx.getPlayerData(myUsername).getHand().get(selectedCardIndex);

            TypedCard cardToDraw = cardInHand.cloneCard();
            cardToDraw.setFace(gameCtx.getPlayerInfo(myUsername).getHandFace(selectedCardIndex));
            if (!gc.placeCard(cardToDraw, mp)) return;

            HandController handController = mainHandPane;
            ManuscriptController manuscriptController = mainPlayerPane.getManuscriptPane();

            manuscriptController.hideCardAtPosition(mp);

            Util.runWaitUI(gameCtx::updateManuscripts);


            //            setState(GuiGameState.DRAWING_CARD);

            Animation anim = AnimationBuilder.placeCardAnimation(
                    new AnimCardPlaceInfo(
                            cardInHand.cloneCard(),
                            mainHandPane.getSelectedId(),
                            handController,
                            mp,
                            manuscriptController
                    ), hoverPane);

            anim.setOnFinished(actionEvent -> {
                //                setState(GuiGameState.DRAW);
            });

            anim.play();


            commonPlace();
        }).start();

    }

    private void onDeckPaneDrawVisible(DrawDeckInfo drawDeckInfo) {
        new Thread(() -> {
            CardType deckType = drawDeckInfo.type();
            int visibleIndex = drawDeckInfo.visibleIndex();
            TypedCard newCardInHand = gameCtx.getBoard().getVisibleCards(deckType).get(visibleIndex);

            System.out.println(visibleIndex);
            TypedCard newDeckCard = gc.drawVisibleCard(deckType, visibleIndex);
            if (newDeckCard == null) return;

            gameCtx.updateBoard();
            boolean decksEmpty = gameCtx.getBoard().isDecksEmpty(deckType) && gameCtx.getBoard().peekTopCard(deckType) == null;

            HandController handController = mainHandPane;

            int handIndex = mainHandPane.getSelectedId();


            Animation an = AnimationBuilder.visibleDrawToHandAnimation(
                    new AnimDrawVisibleToHandInfo(
                            deckType,
                            newCardInHand.cloneCard(),
                            visibleIndex,
                            newDeckCard.cloneCard(),
                            decksEmpty,
                            deckPane,
                            handIndex,
                            mainHandPane
                    ), hoverPane);
            an.setOnFinished(actionEvent -> {
                //                setState(GuiGameState.WAIT_TURN);
            });

            commonDraw(handController, an);

            // Animation start
        }).start();

    }

    private void onDeckPaneDrawFromDeck(CardType type) {
        // New drawnDeckCard drawn from deck or new drawnDeckCard on table
        new Thread(() -> {
            // New drawnDeckCard drawn from deck or new drawnDeckCard on table

            TypedCard newCard = gc.drawCoveredCard(type);
            if (newCard == null) return;


            HandController handController = mainHandPane;

            int handIndex = mainHandPane.getSelectedId();

            Animation an = AnimationBuilder.deckDrawToHandAnimation(
                    new AnimDrawDeckToHandInfo(
                            type,
                            newCard.cloneCard(),
                            deckPane,
                            handIndex,
                            mainHandPane
                    ), hoverPane);
            an.setOnFinished(actionEvent -> {
                //                setState(GuiGameState.WAIT_TURN);
            });

            commonDraw(handController, an);
        }).start();

    }

    private void commonDraw(HandController handController, Animation anim) {
        // Get new board with updated visible and deck
        System.out.println("update Board");
        gameCtx.updateBoard();
        // first update gameflow, after update playerdata, so state goes from Draw to WaitTurn
        gameCtx.updateGameFlow();
        gameCtx.updatePlayersData();
        gameCtx.updateManuscripts();

        handController.updateHandPanes();

        // Animation start
        anim.play();
    }

    private void commonPlace() {
        //        System.out.println("P1 " + gameCtx.getPlayerData(gameCtx.getGameFlow().getCurrentPlayer()).getPoints());
        gameCtx.updatePlayersData();
        //        System.out.println("P2" + gameCtx.getPlayerData(gameCtx.getGameFlow().getCurrentPlayer()).getPoints());
    }

    @FXML
    private void goBack(MouseEvent e) {
        if (ctxMan.getCm().exitMatch()) {
            ctxMan.matchCtx().updateLobbies();
            viewRouter.goTo(ViewRoute.TITLES);
        }
    }
}
