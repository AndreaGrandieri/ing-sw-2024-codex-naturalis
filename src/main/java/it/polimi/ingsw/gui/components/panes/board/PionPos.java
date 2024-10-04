package it.polimi.ingsw.gui.components.panes.board;

import javafx.animation.Interpolatable;

public record PionPos(double num, double stackIndex) implements Interpolatable<PionPos> {
    @Override
    public PionPos interpolate(PionPos endValue, double t) {
        double deltaNum = endValue.num - num;
        double deltaStack = endValue.stackIndex - stackIndex;
        return new PionPos(
                num + deltaNum * t,
                stackIndex + deltaStack * t
        );
    }
}
