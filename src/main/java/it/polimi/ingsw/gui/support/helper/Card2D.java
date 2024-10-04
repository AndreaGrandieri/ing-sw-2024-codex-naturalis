package it.polimi.ingsw.gui.support.helper;

import javafx.animation.Interpolatable;

public class Card2D implements Interpolatable<Card2D> {
    public static final double angleWidthToWidthRatio = (120.0 - 90.0) / 120.0;
    public static final double angleHeightToHeightRatio = (80.0 - 45.0) / 80.0;
    //final double cardRatio = 745.0 / 497.0;
    private static final double borderWidthToHeightRatio = 3.0 / 80.0;
    private static final double roundToHeightRatio = 20.0 / 80.0;
    private static final double widthToHeightRatio = 120.0 / 80.0;
    private final double height;
    private final double width;
    private final double freeWidthFromOverlap;
    private final double freeHeightFromOverlap;
    private final double roundLength;

    public Card2D(double height) {
        this.height = height;
        this.width = widthToHeightRatio * this.height;
        this.freeWidthFromOverlap = this.width - angleWidthToWidthRatio * this.width;
        this.freeHeightFromOverlap = this.height - angleHeightToHeightRatio * this.height;
        this.roundLength = roundToHeightRatio * this.height;
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }

    public double getFreeWidthFromOverlap() {
        return freeWidthFromOverlap;
    }

    public double getFreeHeightFromOverlap() {
        return freeHeightFromOverlap;
    }

    public double getAngleWidth() {
        return angleWidthToWidthRatio * this.width;
    }

    public double getAngleHeight() {
        return angleHeightToHeightRatio * this.height;
    }

    public double getRoundLength() {
        return roundLength;
    }

    public double getBorderWidth() {
        return borderWidthToHeightRatio * height;
    }

    @Override
    public String toString() {
        return "Card2D{" +
                "height=" + height +
                ", width=" + width +
                ", freeWidthFromOverlap=" + freeWidthFromOverlap +
                ", freeHeightFromOverlap=" + freeHeightFromOverlap +
                ", roundLength=" + roundLength +
                '}';
    }

    @Override
    public Card2D interpolate(Card2D endValue, double t) {
        return new Card2D(height + (endValue.height - height) * t);
    }
}
