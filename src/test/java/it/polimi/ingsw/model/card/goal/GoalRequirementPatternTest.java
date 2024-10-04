package it.polimi.ingsw.model.card.goal;

import it.polimi.ingsw.model.card.GoalCard;
import it.polimi.ingsw.model.card.factory.GoalCardFactory;
import it.polimi.ingsw.model.example.PlayerManuscriptExample;
import it.polimi.ingsw.model.player.PlayerManuscript;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GoalRequirementPatternTest {
    @Test
    public void calculateScoreTest() {
        PlayerManuscript manuscript = PlayerManuscriptExample.manuscript3();
        GoalCard c1 = (new GoalCardFactory()).generateCard(91);
        assertEquals(0, c1.calculateScore(manuscript));
        GoalCard c2 = (new GoalCardFactory()).generateCard(92);
        assertEquals(6, c2.calculateScore(manuscript));
        GoalCard c3 = (new GoalCardFactory()).generateCard(93);
        assertEquals(3, c3.calculateScore(manuscript));
    }
}
