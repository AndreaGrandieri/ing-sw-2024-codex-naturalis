package it.polimi.ingsw.gui.support.info;

import it.polimi.ingsw.gui.support.interfaces.DeckController;
import it.polimi.ingsw.gui.support.interfaces.HandController;
import it.polimi.ingsw.model.card.TypedCard;
import it.polimi.ingsw.model.game.CardType;

public record AnimDrawVisibleToHandInfo(
        CardType selectedType,
        TypedCard selectedCard,
        Integer visibleIndex,
        TypedCard newDeckCard,
        boolean emptyDeck,
        DeckController deckController,
        int handIndex,
        HandController handController
) {


}
