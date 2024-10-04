package it.polimi.ingsw.gui.components.panes.card;

import it.polimi.ingsw.gui.support.FXBind;
import it.polimi.ingsw.gui.support.Util;
import it.polimi.ingsw.gui.support.helper.Card2D;
import it.polimi.ingsw.gui.support.info.CardPaneInfo;
import it.polimi.ingsw.model.card.Card;
import it.polimi.ingsw.model.card.properties.CardFace;
import javafx.animation.*;
import javafx.beans.NamedArg;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.util.Duration;

import static javafx.beans.binding.Bindings.createDoubleBinding;
import static javafx.beans.binding.Bindings.createObjectBinding;

public class CardRect extends CardSizeRect {

    private static final double flipAnimationDuration = 300;
    private final ObjectProperty<Card> card;
    private final ObjectProperty<CardFace> cardFace;

    private final EventHandler<MouseEvent> flipHandler;

    private final BooleanProperty reversable;

    public CardRect(Card card) {
        setEffect(new DropShadow(BlurType.THREE_PASS_BOX, new Color(0, 0, 0, 0.3), 10, 0.5, 0, 0));


        this.card = new SimpleObjectProperty<>();
        if (card != null) setCard(card);

        this.cardFace = new SimpleObjectProperty<>();
        cardFace.bind(FXBind.map(this.card, Card::getFace));

        flipHandler = this::flipHandler;

        reversable = new SimpleBooleanProperty(false);


        fillProperty().bind(createObjectBinding(
                () -> fillFromCard(getCard()),
                this.card
        ));

        reversable.addListener((observableValue, aBoolean, t1) -> {
            if (t1) addEventHandler(MouseEvent.MOUSE_CLICKED, flipHandler);
            else removeEventHandler(MouseEvent.MOUSE_CLICKED, flipHandler);
        });
    }

    public CardRect(@NamedArg("cardHeight") double height) {
        this(null);
        setSize(new Card2D(height));
    }

    public void setReversable(boolean reversable) {
        this.reversable.set(reversable);
    }

    public boolean isReversable() {
        return reversable.get();
    }

    public BooleanProperty reversableProperty() {
        return reversable;
    }

    public void setCard(Card card) {
        cardProperty().set(card.cloneCard());
    }

    public Card getCard() {
        return card.get() == null ? null : card.get().cloneCard();
    }

    public ObjectProperty<Card> cardProperty() {
        return this.card;
    }

    private void flipHandler(MouseEvent me) {
        if (me.getButton().equals(MouseButton.PRIMARY)) {
            if (me.getClickCount() == 2) {
                flipAnimation().play();
            }
        }
    }


    private Paint fillFromCard(Card card) {
        if (card == null) return null;
        String cardPaddedId = String.format("%1$3s", card.getId()).replace(' ', '0');

        String cardName = "CODEX_cards_%s-%s.png".formatted(card.getFace().toString().toLowerCase(), cardPaddedId);
        String cardUrl = Util.getImage("cards/" + cardName);

        return new ImagePattern(new Image(cardUrl));
    }

    public Animation flipAnimation() {

        EventHandler<ActionEvent> flipImage = (e) -> {
            Card card = this.getCard();
            card.flip();
            System.out.println(getCard().getFace() + " " + card.getFace() + " " + card.cloneCard().getFace());
            setCard(card);
        };

        return new Timeline(
                new KeyFrame(Duration.millis(flipAnimationDuration / 2),
                        flipImage,
                        new KeyValue(this.scaleXProperty(), 0, Interpolator.EASE_IN)
                ),
                new KeyFrame(Duration.millis(flipAnimationDuration),
                        new KeyValue(this.scaleXProperty(), 1, Interpolator.EASE_OUT)
                )
        );
    }

    public DoubleProperty toPositionProp(ObservableValue<Point2D> toPos, Node node) {
        DoubleProperty progressionProp = new SimpleDoubleProperty(0);
        Point2D currentPos = new Point2D(getLayoutX(), getLayoutY());
        ObservableValue<Point2D> pos = createObjectBinding(
                () -> currentPos.interpolate(node.sceneToLocal(toPos.getValue()), progressionProp.get()),
                progressionProp, toPos);

        layoutXProperty().bind(createDoubleBinding(
                () -> pos.getValue().getX(),
                pos
        ));
        layoutYProperty().bind(createDoubleBinding(
                () -> pos.getValue().getY(),
                pos
        ));
        return progressionProp;
    }

    public KeyValue toPositionKeyValue(ObservableValue<Point2D> toPos, Node node) {
        return new KeyValue(toPositionProp(toPos, node), 1);
    }

    public CardPaneInfo getCardPaneInfo() {
        return new CardPaneInfo(
                localToScene(0, 0),
                sizeProperty().get()
        );
    }

    public CardFace getCardFace() {
        return cardFace.get();
    }

    public ObjectProperty<CardFace> cardFaceProperty() {
        return cardFace;
    }
}
