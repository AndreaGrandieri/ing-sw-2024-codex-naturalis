package it.polimi.ingsw.gui.support.info;

import it.polimi.ingsw.gui.support.interfaces.DeckController;
import it.polimi.ingsw.gui.support.interfaces.HandController;
import it.polimi.ingsw.model.card.TypedCard;
import it.polimi.ingsw.model.game.CardType;

public record AnimDrawDeckToHandInfo(
        CardType deckType,
        TypedCard drawnCard,
        DeckController deckController,
        int handIndex,
        HandController handController
) {

}
