package it.polimi.ingsw.gui.views;

import it.polimi.ingsw.controller.GameState;
import it.polimi.ingsw.controller.interfaces.ClientGameController;
import it.polimi.ingsw.gui.components.panes.card.CardRect;
import it.polimi.ingsw.gui.components.views.CustomView;
import it.polimi.ingsw.gui.support.ComponentBuilder;
import it.polimi.ingsw.gui.support.GenericBuilder;
import it.polimi.ingsw.gui.support.Util;
import it.polimi.ingsw.gui.support.ViewRoute;
import it.polimi.ingsw.gui.support.context.ContextManager;
import it.polimi.ingsw.gui.support.context.GameContext;
import it.polimi.ingsw.gui.support.context.MatchContext;
import it.polimi.ingsw.gui.support.interfaces.ViewRouter;
import it.polimi.ingsw.model.card.Card;
import it.polimi.ingsw.model.card.GoalCard;
import it.polimi.ingsw.model.card.StarterCard;
import it.polimi.ingsw.model.card.TypedCard;
import it.polimi.ingsw.model.card.properties.CardKingdom;
import it.polimi.ingsw.model.game.CardType;
import it.polimi.ingsw.model.player.PlayerColor;
import it.polimi.ingsw.model.player.PlayerData;
import it.polimi.ingsw.model.player.PlayerManuscript;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

import java.util.*;

import static java.util.stream.IntStream.range;

public class SetupView extends CustomView {
    private static final PseudoClass selectedClass = PseudoClass.getPseudoClass("selected");
    private static final PseudoClass errorClass = PseudoClass.getPseudoClass("error");
    private final GameContext gameCtx;
    private final CardRect starterRect;
    private final SimpleIntegerProperty selectedColor;
    private final MatchContext matchCtx;
    private Map<PlayerColor, Pane> colorCirclesMap;
    @FXML
    private GridPane colorsGrid;
    @FXML
    private HBox starterBox;
    @FXML
    private HBox goalsBox;
    @FXML
    private HBox handBox;
    @FXML
    private GridPane deckGrid;
    private String myUsername;

    public SetupView(ViewRouter viewRouter, ContextManager ctxMan) {
        super(viewRouter);
        this.matchCtx = ctxMan.matchCtx();
        this.gameCtx = ctxMan.gameCtx();
        myUsername = gameCtx.getMyUsername();

        if (gameCtx.getGameFlow().getState() != GameState.SETTING) getRouter().goTo(ViewRoute.GAME);


        selectedColor = new SimpleIntegerProperty(-1);

        range(0, 4).forEach(i -> {
            PlayerColor color = PlayerColor.values()[i];
            Pane colorPane = ComponentBuilder.colorCircle(color);

            colorsGrid.add(colorPane, i % 2, i / 2);

            selectedColor.addListener((ob, oldValue, newValue) -> {
                colorPane.pseudoClassStateChanged(selectedClass, newValue.equals(i));
            });

            colorPane.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                if (!isColorSet(gameCtx.updateAndGetPlayerData(myUsername))) {
                    if (gameCtx.gc().setPlayerColor(color)) {
                        //                    FXUtils.addStyle(colorPane, "selected");
                        System.out.println(myUsername + " Success");
                        selectedColor.set(i);
                    } else colorPane.pseudoClassStateChanged(errorClass, true);
                }
            });
        });


        ClientGameController gc = gameCtx.gc();

        StarterCard starterCard = gc.getStarterCard(myUsername);
        starterRect = cardRect(starterCard);
        starterRect.setReversable(true);

        starterBox.getChildren().add(starterRect);

        IntegerProperty selectedGoal = new SimpleIntegerProperty(-1);


        List<GoalCard> goals = gc.getProposedPrivateGoals();
        range(0, 2).forEach(i -> {
            GoalCard goal = goals.get(i);
            //            Pane goalPane = ComponentBuilder.box(cardRect(goal));
            Pane goalPane = GenericBuilder.of(HBox::new)
                    .setList(HBox::getChildren, ObservableList::addAll, cardRect(goal))
                    .setList(HBox::getStyleClass, ObservableList::addAll, "centered-box", "hoverable")
                    .apply(h -> HBox.setHgrow(h, Priority.ALWAYS))
                    .apply(ComponentBuilder::enterToClick)
                    .set(HBox::setFocusTraversable, true)
                    .build();

            goalsBox.getChildren().add(goalPane);

            selectedGoal.addListener((ob, oldValue, newValue) -> {
                System.out.println("CHANGED");
                goalPane.pseudoClassStateChanged(selectedClass, newValue.equals(i));
            });

            goalPane.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                if (!isPrivateGoalSet(gameCtx.updateAndGetPlayerData(myUsername))) {

                    if (gameCtx.gc().choosePrivateGoal(goal)) {
                        System.out.println(myUsername + " Success");
                        selectedGoal.set(i);
                    } else goalPane.pseudoClassStateChanged(errorClass, true);
                }
                //                System.out.println("handler" + i + goalPane.getPseudoClassStates());
            });
        });


        List<Pane> handCards = gameCtx.getPlayerData(myUsername).getHand().stream().map(SetupView::cardRect).map(ComponentBuilder::box).toList();
        handBox.getChildren().addAll(handCards);

        List<Pane> deckCards = Arrays.stream(CardType.values())
                .map(type -> {
                    List<TypedCard> cards = new ArrayList<>(gameCtx.getBoard().getVisibleCards(type));
                    CardKingdom kingdom = gameCtx.getBoard().peekTopCard(type);
                    cards.add(Util.cardFromKingdom(type, kingdom));

                    return cards;
                })
                .flatMap(Collection::stream)
                .map(SetupView::cardRect).map(ComponentBuilder::box).toList();
        range(0, 6).forEach(i -> deckGrid.add(deckCards.get(i), i % 3, i / 3));

    }

    private static CardRect cardRect(Card card) {
        return ComponentBuilder.cardRect(card, 90);
    }

    private boolean isSettingDone() {
        PlayerData playerData = gameCtx.updateAndGetPlayerData(myUsername);
        PlayerManuscript manuscript = gameCtx.updateAndGetManuscript(myUsername);
        return isSettingDone(playerData, manuscript);
    }

    private static boolean isSettingDone(PlayerData playerData, PlayerManuscript manuscript) {
        return isColorSet(playerData) && isPrivateGoalSet(playerData) && isStarterCardSet(manuscript);
    }

    private static boolean isColorSet(PlayerData playerData) {
        return playerData.getColor() != null;
    }

    private static boolean isPrivateGoalSet(PlayerData playerData) {
        return playerData.getPrivateGoal() != null;
    }

    private static boolean isStarterCardSet(PlayerManuscript manuscript) {
        return !manuscript.isEmpty();
    }

    @FXML
    private void leaveGame() {
        if (matchCtx.cc().exitMatch())
            getRouter().goTo(ViewRoute.TITLES);
    }

    @FXML
    private void startGame() {
        if (!isStarterCardSet(gameCtx.updateAndGetManuscript(myUsername)))
            gameCtx.gc().setStarterCard(starterRect.getCardFace());

        if (isSettingDone())
            getRouter().goTo(ViewRoute.GAME);
    }
}
