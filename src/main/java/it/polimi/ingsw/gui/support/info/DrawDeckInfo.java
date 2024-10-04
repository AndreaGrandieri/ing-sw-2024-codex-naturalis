package it.polimi.ingsw.gui.support.info;

import it.polimi.ingsw.model.game.CardType;

public record DrawDeckInfo(
        CardType type,
        int visibleIndex) {
}
