package it.polimi.ingsw.model.card.factory;

import it.polimi.ingsw.model.card.ResourceCard;
import it.polimi.ingsw.model.card.bonus.BonusFreePoints;
import it.polimi.ingsw.model.card.properties.CornerItem;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.polimi.ingsw.model.card.properties.CardKingdom.*;
import static it.polimi.ingsw.model.card.properties.CardObject.*;

public class ResourceCardFactory implements CardFactory {
    private static final Map<Integer, ResourceCard> cards = new HashMap<>() {{
        put(1, new ResourceCard(
                1,
                Arrays.asList(new CornerItem(FUNGI), new CornerItem(null), new CornerItem(FUNGI), null),
                FUNGI,
                new BonusFreePoints(0))
        );
        put(2, new ResourceCard(
                2,
                Arrays.asList(new CornerItem(FUNGI), new CornerItem(FUNGI), null, new CornerItem(null)),
                FUNGI,
                new BonusFreePoints(0))
        );
        put(3, new ResourceCard(
                3,
                Arrays.asList(new CornerItem(null), null, new CornerItem(FUNGI), new CornerItem(FUNGI)),
                FUNGI,
                new BonusFreePoints(0))
        );
        put(4, new ResourceCard(
                4,
                Arrays.asList(null, new CornerItem(FUNGI), new CornerItem(null), new CornerItem(FUNGI)),
                FUNGI,
                new BonusFreePoints(0))
        );
        put(5, new ResourceCard(
                5,
                Arrays.asList(null, new CornerItem(QUILL), new CornerItem(PLANT), new CornerItem(FUNGI)),
                FUNGI,
                new BonusFreePoints(0))
        );
        put(6, new ResourceCard(
                6,
                Arrays.asList(new CornerItem(INKWELL), new CornerItem(FUNGI), null, new CornerItem(ANIMAL)),
                FUNGI,
                new BonusFreePoints(0))
        );
        put(7, new ResourceCard(
                7,
                Arrays.asList(new CornerItem(FUNGI), new CornerItem(INSECT), new CornerItem(MANUSCRIPT), new CornerItem(null)),
                FUNGI,
                new BonusFreePoints(0))
        );
        put(8, new ResourceCard(
                8,
                Arrays.asList(new CornerItem(null), new CornerItem(FUNGI), new CornerItem(null), null),
                FUNGI,
                new BonusFreePoints(1))
        );
        put(9, new ResourceCard(
                9,
                Arrays.asList(new CornerItem(FUNGI), null, new CornerItem(null), new CornerItem(null)),
                FUNGI,
                new BonusFreePoints(1))
        );
        put(10, new ResourceCard(
                10,
                Arrays.asList(null, new CornerItem(null), new CornerItem(FUNGI), new CornerItem(null)),
                FUNGI,
                new BonusFreePoints(1))
        );
        put(11, new ResourceCard(
                11,
                Arrays.asList(new CornerItem(PLANT), new CornerItem(null), new CornerItem(PLANT), null),
                PLANT,
                new BonusFreePoints(0))
        );
        put(12, new ResourceCard(
                12,
                Arrays.asList(new CornerItem(PLANT), new CornerItem(PLANT), null, new CornerItem(null)),
                PLANT,
                new BonusFreePoints(0))
        );
        put(13, new ResourceCard(
                13,
                Arrays.asList(new CornerItem(null), null, new CornerItem(PLANT), new CornerItem(PLANT)),
                PLANT,
                new BonusFreePoints(0))
        );
        put(14, new ResourceCard(
                14,
                Arrays.asList(null, new CornerItem(PLANT), new CornerItem(null), new CornerItem(PLANT)),
                PLANT,
                new BonusFreePoints(0))
        );
        put(15, new ResourceCard(
                15,
                Arrays.asList(null, new CornerItem(INSECT), new CornerItem(QUILL), new CornerItem(PLANT)),
                PLANT,
                new BonusFreePoints(0))
        );
        put(16, new ResourceCard(
                16,
                Arrays.asList(new CornerItem(FUNGI), new CornerItem(PLANT), null, new CornerItem(INKWELL)),
                PLANT,
                new BonusFreePoints(0))
        );
        put(17, new ResourceCard(
                17,
                Arrays.asList(new CornerItem(MANUSCRIPT), null, new CornerItem(PLANT), new CornerItem(ANIMAL)),
                PLANT,
                new BonusFreePoints(0))
        );
        put(18, new ResourceCard(
                18,
                Arrays.asList(new CornerItem(null), new CornerItem(null), new CornerItem(PLANT), null),
                PLANT,
                new BonusFreePoints(1))
        );
        put(19, new ResourceCard(
                19,
                Arrays.asList(new CornerItem(null), new CornerItem(null), null, new CornerItem(PLANT)),
                PLANT,
                new BonusFreePoints(1))
        );
        put(20, new ResourceCard(
                20,
                Arrays.asList(null, new CornerItem(PLANT), new CornerItem(null), new CornerItem(null)),
                PLANT,
                new BonusFreePoints(1))
        );
        put(21, new ResourceCard(
                21,
                Arrays.asList(new CornerItem(ANIMAL), new CornerItem(ANIMAL), new CornerItem(null), null),
                ANIMAL,
                new BonusFreePoints(0))
        );
        put(22, new ResourceCard(
                22,
                Arrays.asList(null, new CornerItem(null), new CornerItem(ANIMAL), new CornerItem(ANIMAL)),
                ANIMAL,
                new BonusFreePoints(0))
        );
        put(23, new ResourceCard(
                23,
                Arrays.asList(new CornerItem(ANIMAL), null, new CornerItem(ANIMAL), new CornerItem(null)),
                ANIMAL,
                new BonusFreePoints(0))
        );
        put(24, new ResourceCard(
                24,
                Arrays.asList(new CornerItem(null), new CornerItem(ANIMAL), null, new CornerItem(ANIMAL)),
                ANIMAL,
                new BonusFreePoints(0))
        );
        put(25, new ResourceCard(
                25,
                Arrays.asList(null, new CornerItem(INSECT), new CornerItem(INKWELL), new CornerItem(ANIMAL)),
                ANIMAL,
                new BonusFreePoints(0))
        );
        put(26, new ResourceCard(
                26,
                Arrays.asList(new CornerItem(PLANT), new CornerItem(ANIMAL), null, new CornerItem(MANUSCRIPT)),
                ANIMAL,
                new BonusFreePoints(0))
        );
        put(27, new ResourceCard(
                27,
                Arrays.asList(new CornerItem(QUILL), null, new CornerItem(ANIMAL), new CornerItem(FUNGI)),
                ANIMAL,
                new BonusFreePoints(0))
        );
        put(28, new ResourceCard(
                28,
                Arrays.asList(null, new CornerItem(null), new CornerItem(ANIMAL), new CornerItem(null)),
                ANIMAL,
                new BonusFreePoints(1))
        );
        put(29, new ResourceCard(
                29,
                Arrays.asList(new CornerItem(null), null, new CornerItem(null), new CornerItem(ANIMAL)),
                ANIMAL,
                new BonusFreePoints(1))
        );
        put(30, new ResourceCard(
                30,
                Arrays.asList(new CornerItem(null), new CornerItem(ANIMAL), new CornerItem(null), null),
                ANIMAL,
                new BonusFreePoints(1))
        );
        put(31, new ResourceCard(
                31,
                Arrays.asList(new CornerItem(INSECT), new CornerItem(INSECT), new CornerItem(null), null),
                INSECT,
                new BonusFreePoints(0))
        );
        put(32, new ResourceCard(
                32,
                Arrays.asList(null, new CornerItem(null), new CornerItem(INSECT), new CornerItem(INSECT)),
                INSECT,
                new BonusFreePoints(0))
        );
        put(33, new ResourceCard(
                33,
                Arrays.asList(new CornerItem(INSECT), null, new CornerItem(INSECT), new CornerItem(null)),
                INSECT,
                new BonusFreePoints(0))
        );
        put(34, new ResourceCard(
                34,
                Arrays.asList(new CornerItem(null), new CornerItem(INSECT), null, new CornerItem(INSECT)),
                INSECT,
                new BonusFreePoints(0))
        );
        put(35, new ResourceCard(
                35,
                Arrays.asList(null, new CornerItem(QUILL), new CornerItem(ANIMAL), new CornerItem(INSECT)),
                INSECT,
                new BonusFreePoints(0))
        );
        put(36, new ResourceCard(
                36,
                Arrays.asList(new CornerItem(MANUSCRIPT), new CornerItem(INSECT), null, new CornerItem(FUNGI)),
                INSECT,
                new BonusFreePoints(0))
        );
        put(37, new ResourceCard(
                37,
                Arrays.asList(new CornerItem(INSECT), new CornerItem(PLANT), new CornerItem(INKWELL), null),
                INSECT,
                new BonusFreePoints(0))
        );
        put(38, new ResourceCard(
                38,
                Arrays.asList(new CornerItem(INSECT), null, new CornerItem(null), new CornerItem(null)),
                INSECT,
                new BonusFreePoints(1))
        );
        put(39, new ResourceCard(
                39,
                Arrays.asList(new CornerItem(null), new CornerItem(null), null, new CornerItem(INSECT)),
                INSECT,
                new BonusFreePoints(1))
        );
        put(40, new ResourceCard(
                40,
                Arrays.asList(null, new CornerItem(INSECT), new CornerItem(null), new CornerItem(null)),
                INSECT,
                new BonusFreePoints(1))
        );
    }};

    /**
     * {@inheritDoc}
     *
     * @return The id of the first card that this {@code CardFactory} generates
     */
    @Override
    public int getFirstId() {
        return 1;
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
     * @return Generated {@code ResourceCard}
     */
    public ResourceCard generateCard(int id) {
        ResourceCard card = cards.get(id);
        if (card == null)
            throw new WrongIdException();

        return new ResourceCard(card);
    }

    /**
     * Given a {@link ResourceCard}, generates and returns a clean {@code ResourceCard}, that has a different id
     * and contains only information about the kingdom
     * @param card {@code ResourceCard} to clean
     * @return Clean {@code ResourceCard}
     */
    public static ResourceCard generateCleanCard(ResourceCard card) {
        return new ResourceCard(103 + (card.getId() - 1) / 10,
                List.of(new CornerItem(null), new CornerItem(null), new CornerItem(null), new CornerItem(null)),
                card.getKingdom(),
                null);
    }

}
