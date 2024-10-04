package it.polimi.ingsw.model.card.bonus;

import it.polimi.ingsw.model.card.GoldCard;
import it.polimi.ingsw.model.card.factory.GoldCardFactory;
import it.polimi.ingsw.model.example.PlayerManuscriptExample;
import it.polimi.ingsw.model.player.ManuscriptPosition;
import it.polimi.ingsw.model.player.PlayerManuscript;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BonusCornerTest {
    @Test
    public void calculatePointsTest() {
        PlayerManuscript manuscript = PlayerManuscriptExample.manuscript2();
        System.out.println(manuscript.getItemsNumber());
        GoldCard card1 = (new GoldCardFactory().generateCard(75));
        assertEquals(6, card1.getBonus().calculatePoints(manuscript, new ManuscriptPosition(1, 1)));
        assertEquals(2, card1.getBonus().calculatePoints(manuscript, new ManuscriptPosition(0, -1)));
    }
}
