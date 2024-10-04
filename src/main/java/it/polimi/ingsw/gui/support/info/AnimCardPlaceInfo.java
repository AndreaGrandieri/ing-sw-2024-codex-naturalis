package it.polimi.ingsw.gui.support.info;

import it.polimi.ingsw.gui.support.interfaces.HandController;
import it.polimi.ingsw.gui.support.interfaces.ManuscriptController;
import it.polimi.ingsw.model.card.TypedCard;
import it.polimi.ingsw.model.player.ManuscriptPosition;

public record AnimCardPlaceInfo(
        TypedCard cardInHand,
        int handIndex,
        HandController handController,
        ManuscriptPosition position,
        ManuscriptController manuscriptController
) {
}
