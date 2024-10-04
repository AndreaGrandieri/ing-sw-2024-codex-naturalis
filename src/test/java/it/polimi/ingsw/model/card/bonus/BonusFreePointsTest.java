package it.polimi.ingsw.model.card.bonus;

import it.polimi.ingsw.model.card.ResourceCard;
import it.polimi.ingsw.model.card.factory.ResourceCardFactory;
import it.polimi.ingsw.model.example.PlayerManuscriptExample;
import it.polimi.ingsw.model.player.ManuscriptPosition;
import it.polimi.ingsw.model.player.PlayerManuscript;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BonusFreePointsTest {
    @Test
    public void calculatePointsTest() {
        PlayerManuscript manuscript = PlayerManuscriptExample.manuscript1();
        ResourceCard card1 = (new ResourceCardFactory().generateCard(38));
        assertEquals(1, card1.getBonus().calculatePoints(manuscript, new ManuscriptPosition(0, 1)));
        ResourceCard card2 = (new ResourceCardFactory().generateCard(37));
        assertEquals(0, card2.getBonus().calculatePoints(manuscript, new ManuscriptPosition(0, 1)));
    }
}
