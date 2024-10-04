package it.polimi.ingsw.gui.support.interfaces;

import it.polimi.ingsw.gui.support.helper.Card2D;
import javafx.beans.property.DoubleProperty;

public interface CardSizeController {
    Card2D getSizeFromPercentage(double height);

    double getHeightFromPercentage(double height);

    double getRatio();

    DoubleProperty ratioProperty();

}
