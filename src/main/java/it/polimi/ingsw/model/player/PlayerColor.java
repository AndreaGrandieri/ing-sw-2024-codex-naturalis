package it.polimi.ingsw.model.player;

import java.io.Serializable;

/**
 * Possible colors that players can choose at the start of the game
 * <ul>
 *     <li>Red</li>
 *     <li>Blue</li>
 *     <li>Green</li>
 *     <li>Yellow</li>
 * </ul>
 */
public enum PlayerColor implements Serializable {
    RED("red"), BLUE("blue"), GREEN("green"), YELLOW("yellow");

    private final String value;

    PlayerColor(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
