package it.polimi.ingsw.model.card.properties;

import java.io.Serializable;

/**
 * Objects that can be used into the game
 * <ul>
 *     <li>Quill</li>
 *     <li>Inkwell</li>
 *     <li>Manuscript</li>
 * </ul>
 */
public enum CardObject implements CardItem, Serializable {
    QUILL("Quill"),
    INKWELL("Inkwell"),
    MANUSCRIPT("Manuscript");

    final String description;

    CardObject(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}
