package it.polimi.ingsw.model.card.goal;

import it.polimi.ingsw.model.card.properties.CardItem;
import it.polimi.ingsw.model.player.PlayerManuscript;

import java.io.Serializable;
import java.util.Map;

public class GoalRequirementItems extends GoalRequirement implements Serializable {
    private final Map<CardItem, Integer> requirement;

    public GoalRequirementItems(Map<CardItem, Integer> requirement) {
        this.requirement = requirement;
    }

    /**
     * This method scans the given {@link PlayerManuscript}, counts the amount of {@link CardItem}s in it and returns it
     * for the point calculation
     *
     * @param manuscript {@link PlayerManuscript} to scan for items
     * @return Number of occurrences of the required set of items
     */
    @Override
    public int findOccurrences(PlayerManuscript manuscript) {
        return requirement
                .keySet()
                .stream()
                .filter(requirement::containsKey)
                .map(i -> manuscript.getItemsNumber().get(i) / requirement.get(i))
                .min(Integer::compareTo)
                .orElse(0);
    }

    public Map<CardItem, Integer> getRequirement() {
        return requirement;
    }

    @Override
    public String toString() {
        return "Item";
    }
}
