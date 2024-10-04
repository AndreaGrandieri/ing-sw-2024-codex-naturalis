package it.polimi.ingsw.gui.components.panes;

import it.polimi.ingsw.gui.support.FXBind;
import it.polimi.ingsw.gui.components.panes.card.CardRect;
import it.polimi.ingsw.gui.support.helper.Card2D;
import it.polimi.ingsw.gui.support.interfaces.CardSizeController;
import it.polimi.ingsw.gui.support.interfaces.HandController;
import it.polimi.ingsw.gui.support.context.PlayerDataSource;
import it.polimi.ingsw.gui.support.interfaces.ResponsiveContainer;
import it.polimi.ingsw.gui.support.info.CardPaneInfo;
import it.polimi.ingsw.gui.support.info.PlayerInfo;
import it.polimi.ingsw.gui.support.helper.GuiGameState;
import it.polimi.ingsw.model.card.Card;
import it.polimi.ingsw.model.card.TypedCard;
import it.polimi.ingsw.model.player.PlayerData;
import it.polimi.ingsw.model.player.PlayerManuscript;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static java.util.stream.IntStream.range;
import static javafx.beans.binding.Bindings.createDoubleBinding;
import static javafx.beans.binding.Bindings.createObjectBinding;


public class MainHandPane extends VBox implements HandController, ResponsiveContainer {
    private final List<CardRect> cardPaneList;
    private final List<HBox> boxPaneList;
    private final IntegerProperty selectedCard;
    private final ObjectProperty<List<? extends Card>> hand;
    public NewPointsPane pointsPane;
    @FXML
    private CardRect leftCardPane;
    @FXML
    private HBox leftBox;
    @FXML
    private CardRect centerCardPane;
    @FXML
    private HBox centerBox;
    @FXML
    private CardRect rightCardPane;
    @FXML
    private HBox rightBox;
    private GuiGameState state;
    private int selectedId;
    private Consumer<Integer> onCardSelected;
    private PlayerDataSource playerDataSource;


    public MainHandPane() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main-hand-pane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setControllerFactory(type -> this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }


        //id and OnMouseClink on fxml
        cardPaneList = Arrays.asList(leftCardPane, centerCardPane, rightCardPane);
        boxPaneList = Arrays.asList(leftBox, centerBox, rightBox);

        hand = new SimpleObjectProperty<>(List.of());
        selectedCard = new SimpleIntegerProperty(0);


        for (CardRect cp : cardPaneList) {
            cp.setReversable(true);
        }

    }

    public void setCardSizeController(CardSizeController cctrl) {
        cardPaneList.forEach(cp ->
                cp.sizeProperty().bind(createObjectBinding(
                        () -> new Card2D(cctrl.ratioProperty().get() * 123),
                        cctrl.ratioProperty()
                ))
        );
        pointsPane.ratioProperty().bind(cctrl.ratioProperty().multiply(1.2));
    }

    public void updatePanes() {
    }

    public void setDataController(PlayerDataSource playerDataSource) {
        this.playerDataSource = playerDataSource;
        setVars();
    }

    public void setVars() {
        PlayerData pd = playerDataSource.getPlayerData();
        PlayerInfo info = playerDataSource.getPlayerInfo();
        PlayerManuscript pm = playerDataSource.getManuscript();

        hand.addListener((observableValue, cards, newCards) -> {
            range(0, newCards.size())
                    .filter(i -> newCards.get(i) != null)
                    .forEach(i -> cardPaneList.get(i).setCard(newCards.get(i)));
        });
        range(0, cardPaneList.size()).forEach(i -> FXBind.subscribe(
                cardPaneList.get(i).cardFaceProperty(),
                f -> playerDataSource.setPlayerInfo(playerDataSource.getPlayerInfo().setHandFace(i, f))
        ));


        selectedCard.addListener((observableValue, number, t1) -> {
            selectCardPane(t1.intValue());
        });

        hand.bind(createObjectBinding(
                () -> playerDataSource.playerDataProperty().get().getHand(),
                playerDataSource.playerDataProperty()
        ));

        selectedCard.bind(createObjectBinding(
                () -> playerDataSource.playerInfoProperty().get().handIndex(),
                playerDataSource.playerInfoProperty()
        ));

        pointsPane.pointsProperty().bind(createObjectBinding(
                () -> playerDataSource.manuscriptProperty().get().getItemsNumber(),
                playerDataSource.manuscriptProperty()
        ));
        range(0, cardPaneList.size()).forEach(i -> {
            CardRect cardRect = cardPaneList.get(i);
            cardRect.cardProperty().addListener((observableValue) -> {
                PlayerData playerData = playerDataSource.getPlayerData();
                playerData.getHand().get(i).setFace(cardRect.cardProperty().get().getFace());
                playerDataSource.setPlayerData(new PlayerData(playerData));
            });

            cardRect.opacityProperty().bind(createDoubleBinding(
                    () -> {
                        TypedCard card1 = playerDataSource.getPlayerData().getHand().get(i);
                        if (card1 != null) {
                            TypedCard card = card1.cloneCard();
                            card.setFace(playerDataSource.getPlayerInfo().getHandFace(i));
                            return playerDataSource.getManuscript().isCostSatisfied(card) ? 1 : 0.6;
                        }
                        return 0.0;
                    },
                    playerDataSource.manuscriptProperty(), playerDataSource.playerDataProperty()
            ));
        });
    }

    private void selectCardPane(int id) {
        for (int i = 0; i < cardPaneList.size(); i++) {
            boxPaneList.get(i).getStyleClass().removeAll("selected");
        }

        boxPaneList.get(id).getStyleClass().add("selected");
    }

    public void onBoxClick(MouseEvent me) {

        if (!state.equals(GuiGameState.DRAW)) {
            HBox box = (HBox) me.getSource();
            int id = Integer.parseInt(box.getId());
            this.selectedId = id;

            selectCardPane(id);

            onCardSelected.accept(id);
        }
    }


    public void hideSelectedPane() {
        cardPaneList.get(this.selectedId).setVisible(false);
    }

    public CardPaneInfo getSelectedPaneInfo() {
        return cardPaneList.get(this.selectedId).getCardPaneInfo();
    }


    public Integer getSelectedCardIndex() {
        return this.selectedId;
    }

    public void setState(GuiGameState state) {
        this.state = state;
    }

    public void setOnCardSelected(Consumer<Integer> onCardSelected) {
        this.onCardSelected = onCardSelected;
    }

    public int getSelectedId() {
        return selectedId;
    }


    @Override
    public void showHandPane(int index) {
        cardPaneList.get(index).setVisible(true);
    }

    @Override
    public void hideHandPane(int index) {
        cardPaneList.get(index).setVisible(false);
    }

    @Override
    public CardPaneInfo getHandPaneInfo(int index) {
        return cardPaneList.get(index).getCardPaneInfo();
    }

    @Override
    public void updateHandPanes() {
    }
}
