package it.polimi.ingsw.model.card.goal;

import it.polimi.ingsw.model.card.GoalCard;
import it.polimi.ingsw.model.card.factory.GoalCardFactory;
import it.polimi.ingsw.model.example.PlayerManuscriptExample;
import it.polimi.ingsw.model.player.PlayerManuscript;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GoalRequirementItemTest {
    @Test
    public void calculateScoreTest() {
        PlayerManuscript manuscript = PlayerManuscriptExample.manuscript1();
        GoalCard c1 = (new GoalCardFactory()).generateCard(98);
        assertEquals(2, c1.calculateScore(manuscript));
        GoalCard c2 = (new GoalCardFactory()).generateCard(100);
        assertEquals(0, c2.calculateScore(manuscript));
    }
}
