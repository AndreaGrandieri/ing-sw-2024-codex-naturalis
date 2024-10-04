package it.polimi.ingsw.model.card.properties;

import java.io.Serializable;

/**
 * This class represents the {@link CardItem} that a corner of a card can contain. <br>
 * If this class contains a {@code null} item, that means the corner is available for placing but contains no item.
 */
public record CornerItem(CardItem item) implements Serializable {

    @Override
    public String toString() {
        return (item == null) ? "Free" : item.toString();
    }
}
