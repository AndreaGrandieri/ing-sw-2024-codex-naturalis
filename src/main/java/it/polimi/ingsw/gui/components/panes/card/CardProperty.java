package it.polimi.ingsw.gui.components.panes.card;

import it.polimi.ingsw.model.card.Card;
import it.polimi.ingsw.model.card.properties.CardFace;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

public class CardProperty extends ObjectBinding<Card> {
    private final IntegerProperty id;
    private final ObjectProperty<CardFace> face;
    private Card card;

    public CardProperty(Card card) {
        this.card = card;
        if (card != null) {
            id = new SimpleIntegerProperty(card.getId());
            face = new SimpleObjectProperty<>(card.getFace());
        } else {
            id = new SimpleIntegerProperty();
            face = new SimpleObjectProperty<>();
        }


        bind(id, face);

        // hack to make face updated
        face.addListener((a, b, c) -> {
        });
        id.addListener((a, b, c) -> {
        });
    }

    public void set(Card card) {
        this.card = card;
        id.set(card.getId());
        face.set(card.getFace());
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public ObjectProperty<CardFace> faceProperty() {
        return face;
    }


    @Override
    protected Card computeValue() {

        return card;
    }
}
