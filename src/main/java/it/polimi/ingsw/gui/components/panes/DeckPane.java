package it.polimi.ingsw.gui.components.panes;

import it.polimi.ingsw.gui.support.Util;
import it.polimi.ingsw.gui.components.panes.card.CardRect;
import it.polimi.ingsw.gui.support.helper.Card2D;
import it.polimi.ingsw.gui.support.context.GameContext;
import it.polimi.ingsw.gui.support.interfaces.CardSizeController;
import it.polimi.ingsw.gui.support.interfaces.DeckController;
import it.polimi.ingsw.gui.support.interfaces.ResponsiveContainer;
import it.polimi.ingsw.gui.support.info.CardPaneInfo;
import it.polimi.ingsw.gui.support.info.DrawDeckInfo;
import it.polimi.ingsw.gui.support.helper.GuiGameState;
import it.polimi.ingsw.model.card.TypedCard;
import it.polimi.ingsw.model.card.properties.CardFace;
import it.polimi.ingsw.model.card.properties.CardKingdom;
import it.polimi.ingsw.model.game.CardType;
import it.polimi.ingsw.model.game.CommonBoard;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static javafx.beans.binding.Bindings.createObjectBinding;

public class DeckPane extends GridPane implements DeckController, ResponsiveContainer {
    private final Map<CardType, List<CardRect>> cardPaneMap;
    private final List<CardRect> cardPaneList;
    private final List<HBox> cardBoxList;
    @FXML
    private CardRect resourceCardRect1;
    @FXML
    private HBox resourceBox1;
    @FXML
    private CardRect resourceCardRect2;
    @FXML
    private HBox resourceBox2;
    @FXML
    private CardRect resourceCardRect3;
    @FXML
    private HBox resourceBox3;
    @FXML
    private CardRect goldCardRect1;
    @FXML
    private HBox goldBox1;
    @FXML
    private CardRect goldCardRect2;
    @FXML
    private HBox goldBox2;
    @FXML
    private CardRect goldCardRect3;
    @FXML
    private HBox goldBox3;
    private Consumer<DrawDeckInfo> onDrawVisible;
    private Consumer<CardType> onDrawFromDeck;

    private GuiGameState state;

    private int selectedCard;

    // Handlers
    final private EventHandler<MouseEvent> onDeckCardClick = this::onDeckCardClick;
    private GameContext dataCtx;


    public DeckPane() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("deck-pane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setControllerFactory(type -> this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        cardPaneMap = new HashMap<>() {{
            put(CardType.RESOURCE, Arrays.asList(
                    resourceCardRect1,
                    resourceCardRect2,
                    resourceCardRect3
            ));
            put(CardType.GOLD, Arrays.asList(
                    goldCardRect1,
                    goldCardRect2,
                    goldCardRect3
            ));
        }};

        cardPaneList = Arrays.asList(resourceCardRect1, resourceCardRect2, resourceCardRect3, goldCardRect1, goldCardRect2, goldCardRect3);
        cardBoxList = Arrays.asList(resourceBox1, resourceBox2, resourceBox3, goldBox1, goldBox2, goldBox3);
    }

    public void setState(GuiGameState state) {
        this.state = state;
        if (state.equals(GuiGameState.DRAW)) {
            for (HBox box : cardBoxList) {
                box.getStyleClass().add("hoverable");
                box.addEventHandler(MouseEvent.MOUSE_CLICKED, onDeckCardClick);
            }
        } else {
            for (HBox box : cardBoxList) {
                box.getStyleClass().remove("hoverable");
                box.removeEventHandler(MouseEvent.MOUSE_CLICKED, onDeckCardClick);
            }
        }
    }

    public void setVars(CommonBoard board) {
        setVisiblePanes(board);
        setDecks(board);
    }

    public void setDataCtx(GameContext dataCtx) {
        this.dataCtx = dataCtx;
    }

    public void update() {
        setVars(dataCtx.getBoard());
    }

    private void setVisiblePanes(CommonBoard board) {
        for (CardType ct : CardType.values()) {
            List<TypedCard> cards = board.getVisibleCards(ct);
            for (int i = 0; i < cards.size(); i++) {
                TypedCard card = cards.get(i);
                if (card != null) cardPaneMap.get(ct).get(i).setCard(card);
                else cardPaneMap.get(ct).get(i).setVisible(false);
            }
        }
    }

    private void setDecks(CommonBoard board) {
        for (CardType ct : CardType.values()) {
            CardKingdom kingdom = board.peekTopCard(ct);
            TypedCard c = Util.cardFromKingdom(ct, kingdom);
            if (c != null) {
                if (CardFace.FRONT == c.getFace()) c.flip();
                cardPaneMap.get(ct).get(2).setCard(c);
            } else cardPaneList.get(ct == CardType.RESOURCE ? 2 : 5).setVisible(false);
        }
    }

    // hoverable


    public void onDeckCardClick(MouseEvent me) {
        HBox box = (HBox) me.getSource();
        int id = Integer.parseInt(box.getId());

        selectedCard = id;

        for (int i = 0; i < cardPaneList.size(); i++) {
            //cardPaneList.get(i).setVisible(true);
        }

        boolean fromDeck = id == 2 || id == 5;


        CardType type = (id < 3) ? CardType.RESOURCE : CardType.GOLD;
        if (!fromDeck) {
            int visibleIndex = id % 3;

            onDrawVisible.accept(new DrawDeckInfo(
                    type, visibleIndex
            ));
        } else {
            onDrawFromDeck.accept(type);
        }

    }

    @Override
    public CardPaneInfo getDeckPaneInfo(CardType ct) {
        return cardPaneMap.get(ct).get(2).getCardPaneInfo();
    }

    @Override
    public CardPaneInfo getVisiblePaneInfo(CardType ct, int visibleIndex) {
        return cardPaneMap.get(ct).get(visibleIndex).getCardPaneInfo();
    }

    @Override
    public void showVisiblePane(CardType ct, int visibleIndex) {
        cardPaneMap.get(ct).get(visibleIndex).setVisible(true);
    }

    @Override
    public void hideVisiblePane(CardType ct, int visibleIndex) {
        cardPaneMap.get(ct).get(visibleIndex).setVisible(false);
    }

    @Override
    public void showDeckPane(CardType ct) {
        cardPaneMap.get(ct).get(2).setVisible(true);
    }

    @Override
    public void hideDeckPane(CardType ct) {
        cardPaneMap.get(ct).get(2).setVisible(false);

    }

    // Handlers
    public void setOnDrawVisible(Consumer<DrawDeckInfo> onDrawVisible) {
        this.onDrawVisible = onDrawVisible;
    }

    public void setOnDrawFromDeck(Consumer<CardType> onDrawFromDeck) {
        this.onDrawFromDeck = onDrawFromDeck;
    }

    @Override
    public void setCardSizeController(CardSizeController cctrl) {
        cardPaneList.forEach(cp ->
                cp.sizeProperty().bind(createObjectBinding(
                        () -> new Card2D(cctrl.ratioProperty().get() * 87),
                        cctrl.ratioProperty()
                ))
        );
    }

    public void updatePanes() {

    }
}
