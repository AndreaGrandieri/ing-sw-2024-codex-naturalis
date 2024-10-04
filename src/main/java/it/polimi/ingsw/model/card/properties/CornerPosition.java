package it.polimi.ingsw.model.card.properties;

import java.io.Serializable;

/**
 * This enumeration represents the possible corners a card may have
 * <ul>
 *     <li>Top Right</li>
 *     <li>Top Left</li>
 *     <li>Bottom Right</li>
 *     <li>Bottom Left</li>
 * </ul>
 */
public enum CornerPosition implements Serializable {
    TOP_RIGHT(1, "TR"),
    TOP_LEFT(0, "TL"),
    BOTTOM_LEFT(2, "BL"),
    BOTTOM_RIGHT(3, "BR");

    private final int index;
    final String description;

    CornerPosition(int index, String description) {
        this.index = index;
        this.description = description;
    }

    /**
     * Returns the relative index of this {@code CornerPosition}, useful for iterating
     * <ul>
     *     <li>0: Top Left</li>
     *     <li>1: Top Right</li>
     *     <li>2: Bottom Left</li>
     *     <li>3: Bottom Right</li>
     * </ul>
     *
     * @return Relative index of this {@code CornerPosition}
     */
    public int getIndex() {
        return index;
    }

    public static CornerPosition getInstance(int index) {
        return switch (index) {
            case 0 -> TOP_LEFT;
            case 1 -> TOP_RIGHT;
            case 2 -> BOTTOM_LEFT;
            case 3 -> BOTTOM_RIGHT;
            default -> null;
        };
    }

    @Override
    public String toString() {
        return description;
    }
}
