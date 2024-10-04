package it.polimi.ingsw.gui.components.panes;

import it.polimi.ingsw.gui.components.panes.manuscript.ManuscriptBox;
import it.polimi.ingsw.gui.support.FXBind;
import it.polimi.ingsw.gui.components.panes.card.CardRect;
import it.polimi.ingsw.gui.support.helper.Card2D;
import it.polimi.ingsw.gui.support.helper.PC;
import it.polimi.ingsw.gui.support.interfaces.CardSizeController;
import it.polimi.ingsw.gui.support.interfaces.HandController;
import it.polimi.ingsw.gui.support.context.PlayerDataSource;
import it.polimi.ingsw.gui.support.interfaces.ResponsiveContainer;
import it.polimi.ingsw.gui.support.info.CardPaneInfo;
import it.polimi.ingsw.gui.support.helper.GuiGameState;
import it.polimi.ingsw.model.card.TypedCard;
import it.polimi.ingsw.model.player.PlayerColor;
import javafx.beans.NamedArg;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static java.util.stream.IntStream.range;
import static javafx.beans.binding.Bindings.createObjectBinding;

public class OtherPlayerPane extends GridPane implements HandController, ResponsiveContainer {
    private final static double cardHeight = 42;
    private final List<VBox> boxList;
    private final List<CardRect> handPaneList;
    private final List<CardRect> cardPaneList = new ArrayList<>();
    @FXML
    private VBox box1;
    @FXML
    private VBox box2;
    @FXML
    private VBox box3;
    @FXML
    private VBox box4;
    @FXML
    private NewTopBarPane topBarPane;
    @FXML
    private CardRect cardPane1;
    @FXML
    private CardRect cardPane2;
    @FXML
    private CardRect cardPane3;
    @FXML
    private CardRect goalPane;
    @FXML
    private ManuscriptBox manuscriptPane;
    private GuiGameState state;
    private Consumer<OtherPlayerPane> onSelected;
    private PlayerDataSource playerDataSource;


    public OtherPlayerPane() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("other-player-pane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setControllerFactory(type -> this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.setOnMouseClicked(this::onClick);

        handPaneList = Arrays.asList(cardPane1, cardPane2, cardPane3);
        boxList = Arrays.asList(box1, box2, box3);
        cardPaneList.addAll(handPaneList);
        cardPaneList.add(goalPane);
    }

    public OtherPlayerPane(@NamedArg("username") String username, @NamedArg("color") String color, @NamedArg("active") Boolean active) {
        this();

        topBarPane.setColor(PlayerColor.BLUE);
    }

    public void setDataController(PlayerDataSource playerDataSource) {
        this.playerDataSource = playerDataSource;
        setVars();
    }


    private void setVars() {
        FXBind.subscribe(playerDataSource.playerDataProperty(),
                pd -> {
                    List<TypedCard> newCards = pd.getHand();
                    range(0, newCards.size())
                            .filter(i -> newCards.get(i) != null)
                            .forEach(i -> handPaneList.get(i).setCard(newCards.get(i)));
                }
        );

        FXBind.subscribe(playerDataSource.playerInfoProperty(),
                pd -> selectCard(pd.handIndex())
        );

        FXBind.subscribe(playerDataSource.playerDataProperty(),
                pd -> goalPane.setCard(pd.getPrivateGoal())
        );

        manuscriptPane.manuscriptProperty().bind(createObjectBinding(
                () -> new PC<>(playerDataSource.manuscriptProperty().get(), ManuscriptBox.MANUSCRIPT_UPDATE),
                playerDataSource.manuscriptProperty())
        );

        manuscriptPane.setShowPlaceholders(false);


        topBarPane.bindDataController(playerDataSource);
    }

    @Override
    public void hideHandPane(int index) {
        handPaneList.get(index).setVisible(false);
    }

    @Override
    public void showHandPane(int index) {
        CardRect cp = handPaneList.get(index);
        cp.setVisible(true);
    }

    @Override
    public CardPaneInfo getHandPaneInfo(int index) {
        return handPaneList.get(index).getCardPaneInfo();
    }

    @Override
    public void updateHandPanes() {
    }

    private void selectCard(int index) {
        System.out.println("SELECTED CARD CALLED");
        for (VBox box : boxList) box.getStyleClass().remove("selected");
        boxList.get(index).getStyleClass().add("selected");
    }

    private void onClick(MouseEvent me) {
        if (onSelected != null) onSelected.accept(this);
    }

    public ManuscriptBox getManuscriptPane() {
        return manuscriptPane;
    }

    public void setState(GuiGameState state) {
        this.state = state;
    }

    public void setOnSelected(Consumer<OtherPlayerPane> onSelected) {
        this.onSelected = onSelected;
    }

    @Override
    public void setCardSizeController(CardSizeController cctrl) {
        cardPaneList.forEach(cp ->
                cp.sizeProperty().bind(createObjectBinding(
                        () -> new Card2D(cctrl.ratioProperty().get() * 42),
                        cctrl.ratioProperty()
                ))
        );
        manuscriptPane.cardSizeProperty().bind(createObjectBinding(
                () -> new Card2D(cctrl.ratioProperty().get() * 32),
                cctrl.ratioProperty()
        ));
        topBarPane.setViewOrder(-2);
        topBarPane.ratioProperty().bind(cctrl.ratioProperty().multiply(1.2));
    }

    public void updatePanes() {
    }
}
