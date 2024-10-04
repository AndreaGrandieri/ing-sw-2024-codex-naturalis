package it.polimi.ingsw.gui.components.panes.manuscript;

import it.polimi.ingsw.gui.support.Util;
import it.polimi.ingsw.model.card.Card;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;

import static javafx.beans.binding.Bindings.createObjectBinding;

public class ManCardRect extends ManuscriptLayoutRectangle {
    private final ObjectProperty<Card> card;

    public ManCardRect(Card card) {
        setEffect(new DropShadow(BlurType.THREE_PASS_BOX, new Color(0, 0, 0, 0.3), 10, 0.5, 0, 0));

        this.card = new SimpleObjectProperty<>(card);

        fillProperty().bind(createObjectBinding(
                () -> fillFromCard(this.card.get()),
                this.card
        ));
    }

    public ObjectProperty<Card> cardProperty() {
        return card;
    }

    private Paint fillFromCard(Card card) {

        String paddedId = String.format("%1$3s", card.getId()).replace(' ', '0');

        String cardName = "CODEX_cards_%s-%s.png".formatted(card.getFace().toString().toLowerCase(), paddedId);
        String cardUrl = Util.getImage("cards/" + cardName);
        Image image = new Image(cardUrl);

        return new ImagePattern(image);
    }
}
