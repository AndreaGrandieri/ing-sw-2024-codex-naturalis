package it.polimi.ingsw.model.card.bonus;

import it.polimi.ingsw.model.card.GoldCard;
import it.polimi.ingsw.model.card.factory.GoldCardFactory;
import it.polimi.ingsw.model.example.PlayerManuscriptExample;
import it.polimi.ingsw.model.player.ManuscriptPosition;
import it.polimi.ingsw.model.player.PlayerManuscript;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BonusObjectsTest {
    @Test
    public void calculatePointsTest() {
        PlayerManuscript manuscript = PlayerManuscriptExample.manuscript2();
        GoldCard card1 = (new GoldCardFactory().generateCard(63));
        assertEquals(1, card1.getBonus().calculatePoints(manuscript, new ManuscriptPosition(0, -1)));
        GoldCard card2 = (new GoldCardFactory().generateCard(62));
        assertEquals(2, card2.getBonus().calculatePoints(manuscript, new ManuscriptPosition(0, -1)));
        GoldCard card3 = (new GoldCardFactory().generateCard(61));
        assertEquals(2, card3.getBonus().calculatePoints(manuscript, new ManuscriptPosition(0, -1)));
    }
}
