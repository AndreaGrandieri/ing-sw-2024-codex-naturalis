package it.polimi.ingsw.model.card.properties;

import java.io.Serializable;

/**
 * Kingdoms that can be used into the game
 * <ul>
 *     <li>Fungi</li>
 *     <li>Animal</li>
 *     <li>Plant</li>
 *     <li>Insect</li>
 * </ul>
 */
public enum CardKingdom implements CardItem, Serializable {
    FUNGI("Fungi"),
    ANIMAL("Animal"),
    PLANT("Plant"),
    INSECT("Insect");

    final String description;

    CardKingdom(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}
