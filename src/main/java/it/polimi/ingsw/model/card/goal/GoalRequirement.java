package it.polimi.ingsw.model.card.goal;

import it.polimi.ingsw.model.player.PlayerManuscript;

import java.io.Serializable;

public abstract class GoalRequirement implements Serializable {

    public abstract int findOccurrences(PlayerManuscript manuscript);
}
