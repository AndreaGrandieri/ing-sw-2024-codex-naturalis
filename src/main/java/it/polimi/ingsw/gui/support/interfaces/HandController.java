package it.polimi.ingsw.gui.support.interfaces;

import it.polimi.ingsw.gui.support.info.CardPaneInfo;

public interface HandController {

    void showHandPane(int index);

    void hideHandPane(int index);

    CardPaneInfo getHandPaneInfo(int index);

    void updateHandPanes();

}
