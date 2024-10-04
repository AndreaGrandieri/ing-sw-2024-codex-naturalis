package it.polimi.ingsw.model.card.goal;

import it.polimi.ingsw.model.card.PlayableCard;
import it.polimi.ingsw.model.card.properties.CardKingdom;
import it.polimi.ingsw.model.player.ManuscriptPosition;
import it.polimi.ingsw.model.player.PlayerManuscript;

import java.io.Serializable;
import java.util.Map;

public class GoalRequirementPattern extends GoalRequirement implements Serializable {
    private final Map<ManuscriptPosition, CardKingdom> requirement;

    public GoalRequirementPattern(Map<ManuscriptPosition, CardKingdom> requirement) {
        this.requirement = requirement;
    }

    /**
     * This method scans the given {@link PlayerManuscript}, finds the required card pattern and returns the number of
     * times this pattern is found
     *
     * @param manuscript {@link PlayerManuscript} to scan for pattern
     * @return Number of times the required pattern is found
     */
    public int findOccurrences(PlayerManuscript manuscript) {
        return (int) manuscript
                .getAllOccupiedPositions()
                .stream()
                .filter(p -> manuscript.getCardAt(p).matchKingdom(requirement.get(new ManuscriptPosition(0, 0))))
                .filter(p -> matchPattern(p, manuscript))
                .count();
    }

    private boolean matchPattern(ManuscriptPosition p, PlayerManuscript manuscript) {
        for (ManuscriptPosition i : requirement.keySet()) {
            PlayableCard c = manuscript.getCardAt(p.addPosition(i));
            if (c == null)
                return false;
            if (!c.matchKingdom(requirement.get(i))) {
                return false;
            }
        }

        return true;
    }

    public Map<ManuscriptPosition, CardKingdom> getRequirement() {
        return requirement;
    }

    @Override
    public String toString() {
        return "Pattern";
    }
}
