package it.polimi.ingsw.gui.support.interfaces;

import it.polimi.ingsw.gui.support.info.CardPaneInfo;
import it.polimi.ingsw.model.player.ManuscriptPosition;

public interface ManuscriptController {

    CardPaneInfo getCardInfo(ManuscriptPosition mp);

    void hideCardAtPosition(ManuscriptPosition mp);

    void showCardAtPosition(ManuscriptPosition mp);

    void updateManuscript();

    void updateBackground();


}
