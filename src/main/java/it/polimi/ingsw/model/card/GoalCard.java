package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.card.goal.GoalRequirement;
import it.polimi.ingsw.model.player.PlayerManuscript;

import java.io.Serializable;

/**
 * This class represents cards containing additional goals of the game
 */
public class GoalCard extends Card implements Serializable {
    private final int points;

    private final GoalRequirement requirement;

    public GoalCard(int id, int points, GoalRequirement requirement) {
        super(id);
        this.points = points;
        this.requirement = requirement;
    }

    public GoalCard(GoalCard other) {
        super(other.getId());
        this.points = other.points;
        this.requirement = other.requirement;
    }

    @Override
    public Card cloneCard() {
        return new GoalCard(this);
    }

    /**
     * Takes a {@code PlayerManuscript} and returns the amount of points the cards in it score for this goal
     *
     * @param manuscript {@link PlayerManuscript} to scan for goal occurrences
     * @return Amount of points scored
     */
    public int calculateScore(PlayerManuscript manuscript) {
        return points * requirement.findOccurrences(manuscript);
    }

    public GoalRequirement getRequirement() {
        return requirement;
    }

    @Override
    public String toString() {
        return "{Goal, Points: " + points + " for each " + requirement + "}";
    }
}
