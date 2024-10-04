package it.polimi.ingsw.model.card.properties;

import java.io.Serializable;

/**
 * Possible sides on which a card can be played
 * <ul>
 *     <li>Front</li>
 *     <li>Back</li>
 * </ul>
 */
public enum CardFace implements Serializable {
    FRONT("FRONT"),
    BACK("BACK");

    private final String description;

    CardFace(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}
