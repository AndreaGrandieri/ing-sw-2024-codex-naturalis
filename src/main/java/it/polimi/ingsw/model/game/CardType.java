package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.card.TypedCard;

import java.io.Serializable;

/**
 * Possible types that a {@link TypedCard} can have
 * <ul>
 *     <li>Gold</li>
 *     <li>Resource</li>
 * </ul>
 */
public enum CardType implements Serializable {
    RESOURCE,
    GOLD
}
