package it.polimi.ingsw.gui.support.interfaces;

import it.polimi.ingsw.gui.support.info.CardPaneInfo;
import it.polimi.ingsw.model.game.CardType;

public interface DeckController {
    CardPaneInfo getDeckPaneInfo(CardType ct);

    CardPaneInfo getVisiblePaneInfo(CardType ct, int visibleIndex);

    void showVisiblePane(CardType ct, int visibleIndex);

    void hideVisiblePane(CardType ct, int visibleIndex);

    void showDeckPane(CardType ct);

    void hideDeckPane(CardType ct);

    void update();
}
