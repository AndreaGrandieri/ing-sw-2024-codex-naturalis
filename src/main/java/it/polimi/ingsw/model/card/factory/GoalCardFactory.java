package it.polimi.ingsw.model.card.factory;

import it.polimi.ingsw.model.card.GoalCard;
import it.polimi.ingsw.model.card.goal.GoalRequirementItems;
import it.polimi.ingsw.model.card.goal.GoalRequirementPattern;
import it.polimi.ingsw.model.card.properties.CardKingdom;
import it.polimi.ingsw.model.card.properties.CardObject;
import it.polimi.ingsw.model.player.ManuscriptPosition;

import java.util.HashMap;
import java.util.Map;

public class GoalCardFactory implements CardFactory{
    private static final Map<Integer, GoalCard> cards = new HashMap<>() {{
        put(87, new GoalCard(
                87,
                2,
                new GoalRequirementPattern(Map.of(
                        new ManuscriptPosition(0, 0), CardKingdom.FUNGI,
                        new ManuscriptPosition(1, 0), CardKingdom.FUNGI,
                        new ManuscriptPosition(2, 0), CardKingdom.FUNGI)))
        );
        put(88, new GoalCard(
                88,
                2,
                new GoalRequirementPattern(Map.of(
                        new ManuscriptPosition(0, 0), CardKingdom.PLANT,
                        new ManuscriptPosition(0, 1), CardKingdom.PLANT,
                        new ManuscriptPosition(0, 2), CardKingdom.PLANT)))
        );
        put(89, new GoalCard(
                89,
                2,
                new GoalRequirementPattern(Map.of(
                        new ManuscriptPosition(0, 0), CardKingdom.ANIMAL,
                        new ManuscriptPosition(1, 0), CardKingdom.ANIMAL,
                        new ManuscriptPosition(2, 0), CardKingdom.ANIMAL)))
        );
        put(90, new GoalCard(
                90,
                2,
                new GoalRequirementPattern(Map.of(
                        new ManuscriptPosition(0, 0), CardKingdom.INSECT,
                        new ManuscriptPosition(0, 1), CardKingdom.INSECT,
                        new ManuscriptPosition(0, 2), CardKingdom.INSECT)))
        );
        put(91, new GoalCard(
                91,
                3,
                new GoalRequirementPattern(Map.of(
                        new ManuscriptPosition(0, 0), CardKingdom.PLANT,
                        new ManuscriptPosition(0, -1), CardKingdom.FUNGI,
                        new ManuscriptPosition(1, -2), CardKingdom.FUNGI)))
        );
        put(92, new GoalCard(
                92,
                3,
                new GoalRequirementPattern(Map.of(
                        new ManuscriptPosition(0, 0), CardKingdom.INSECT,
                        new ManuscriptPosition(1, 0), CardKingdom.PLANT,
                        new ManuscriptPosition(2, -1), CardKingdom.PLANT)))
        );
        put(93, new GoalCard(
                93,
                3,
                new GoalRequirementPattern(Map.of(
                        new ManuscriptPosition(0, 0), CardKingdom.ANIMAL,
                        new ManuscriptPosition(1, -1), CardKingdom.ANIMAL,
                        new ManuscriptPosition(2, -1), CardKingdom.FUNGI)))
        );
        put(94, new GoalCard(
                94,
                3,
                new GoalRequirementPattern(Map.of(
                        new ManuscriptPosition(0, 0), CardKingdom.INSECT,
                        new ManuscriptPosition(1, -1), CardKingdom.INSECT,
                        new ManuscriptPosition(1, -2), CardKingdom.ANIMAL)))
        );
        put(95, new GoalCard(
                95,
                2,
                new GoalRequirementItems(Map.of(
                        CardKingdom.FUNGI, 3)))
        );
        put(96, new GoalCard(
                96,
                2,
                new GoalRequirementItems(Map.of(
                        CardKingdom.PLANT, 3)))
        );
        put(97, new GoalCard(
                97,
                2,
                new GoalRequirementItems(Map.of(
                        CardKingdom.ANIMAL, 3)))
        );
        put(98, new GoalCard(
                98,
                2,
                new GoalRequirementItems(Map.of(
                        CardKingdom.INSECT, 3)))
        );
        put(99, new GoalCard(
                99,
                3,
                new GoalRequirementItems(Map.of(
                        CardObject.QUILL, 1,
                        CardObject.INKWELL, 1,
                        CardObject.MANUSCRIPT, 1)))
        );
        put(100, new GoalCard(
                100,
                2,
                new GoalRequirementItems(Map.of(
                        CardObject.MANUSCRIPT, 2)))
        );
        put(101, new GoalCard(
                101,
                2,
                new GoalRequirementItems(Map.of(
                        CardObject.INKWELL, 2)))
        );
        put(102, new GoalCard(
                102,
                2,
                new GoalRequirementItems(Map.of(
                        CardObject.QUILL, 2)))
        );
    }};

    /**
     * {@inheritDoc}
     *
     * @return The id of the first card that this {@code CardFactory} generates
     */
    @Override
    public int getFirstId() {
        return 87;
    }

    /**
     * {@inheritDoc}
     *
     * @return The total number of cards that this {@code CardFactory} generates
     */
    @Override
    public int getSize() {
        return cards.size();
    }

    /**
     * {@inheritDoc}
     * @param id {@code id} of card to generate
     * @throws WrongIdException if the given id does not match a card that this factory can generate
     * @return Generated {@code GoalCard}
     */
    public GoalCard generateCard(int id) {
        GoalCard card = cards.get(id);
        if (card == null)
            throw new WrongIdException();

        return new GoalCard(card);
    }
}
