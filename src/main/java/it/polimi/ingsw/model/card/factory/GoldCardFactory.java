package it.polimi.ingsw.model.card.factory;

import it.polimi.ingsw.model.card.GoldCard;
import it.polimi.ingsw.model.card.bonus.BonusCorners;
import it.polimi.ingsw.model.card.bonus.BonusFreePoints;
import it.polimi.ingsw.model.card.bonus.BonusObjects;
import it.polimi.ingsw.model.card.properties.CornerItem;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.polimi.ingsw.model.card.properties.CardKingdom.*;
import static it.polimi.ingsw.model.card.properties.CardObject.*;

public class GoldCardFactory implements CardFactory{
    private static final Map<Integer, GoldCard> cards = new HashMap<>() {{
        put(41, new GoldCard(
                41,
                Arrays.asList(null, new CornerItem(null), new CornerItem(null), new CornerItem(QUILL)),
                FUNGI,
                new BonusObjects(1, QUILL),
                Map.of(FUNGI, 2, ANIMAL, 1))
        );
        put(42, new GoldCard(
                42,
                Arrays.asList(new CornerItem(null), new CornerItem(INKWELL), null, new CornerItem(null)),
                FUNGI,
                new BonusObjects(1, INKWELL),
                Map.of(FUNGI, 2, PLANT, 1))
        );
        put(43, new GoldCard(
                43,
                Arrays.asList(new CornerItem(MANUSCRIPT), new CornerItem(null), new CornerItem(null), null),
                FUNGI,
                new BonusObjects(1, MANUSCRIPT),
                Map.of(FUNGI, 2, INSECT, 1))
        );
        put(44, new GoldCard(
                44,
                Arrays.asList(new CornerItem(null), new CornerItem(null), null, new CornerItem(null)),
                FUNGI,
                new BonusCorners(2),
                Map.of(FUNGI, 3, ANIMAL, 1))
        );
        put(45, new GoldCard(
                45,
                Arrays.asList(new CornerItem(null), new CornerItem(null), new CornerItem(null), null),
                FUNGI,
                new BonusCorners(2),
                Map.of(FUNGI, 3, PLANT, 1))
        );
        put(46, new GoldCard(
                46,
                Arrays.asList(new CornerItem(null), null, new CornerItem(null), new CornerItem(null)),
                FUNGI,
                new BonusCorners(2),
                Map.of(FUNGI, 3, INSECT, 1))
        );
        put(47, new GoldCard(
                47,
                Arrays.asList(new CornerItem(null), null, new CornerItem(INKWELL), null),
                FUNGI,
                new BonusFreePoints(3),
                Map.of(FUNGI, 3))
        );
        put(48, new GoldCard(
                48,
                Arrays.asList(new CornerItem(QUILL), new CornerItem(null), null, null),
                FUNGI,
                new BonusFreePoints(3),
                Map.of(FUNGI, 3))
        );
        put(49, new GoldCard(
                49,
                Arrays.asList(null, new CornerItem(MANUSCRIPT), null, new CornerItem(null)),
                FUNGI,
                new BonusFreePoints(3),
                Map.of(FUNGI, 3))
        );
        put(50, new GoldCard(
                50,
                Arrays.asList(new CornerItem(null), null, new CornerItem(null), null),
                FUNGI,
                new BonusFreePoints(5),
                Map.of(FUNGI, 5))
        );
        put(51, new GoldCard(
                51,
                Arrays.asList(new CornerItem(QUILL), new CornerItem(null), new CornerItem(null), null),
                PLANT,
                new BonusObjects(1, QUILL),
                Map.of(PLANT, 2, INSECT, 1))
        );
        put(52, new GoldCard(
                52,
                Arrays.asList(new CornerItem(null), new CornerItem(MANUSCRIPT), null, new CornerItem(null)),
                PLANT,
                new BonusObjects(1, MANUSCRIPT),
                Map.of(PLANT, 2, FUNGI, 1))
        );
        put(53, new GoldCard(
                53,
                Arrays.asList(new CornerItem(null), null, new CornerItem(INKWELL), new CornerItem(null)),
                PLANT,
                new BonusObjects(1, INKWELL),
                Map.of(PLANT, 2, ANIMAL, 1))
        );
        put(54, new GoldCard(
                54,
                Arrays.asList(null, new CornerItem(null), new CornerItem(null), new CornerItem(null)),
                PLANT,
                new BonusCorners(2),
                Map.of(PLANT, 3, INSECT, 1))
        );
        put(55, new GoldCard(
                55,
                Arrays.asList(new CornerItem(null), new CornerItem(null), new CornerItem(null), null),
                PLANT,
                new BonusCorners(2),
                Map.of(PLANT, 3, ANIMAL, 1))
        );
        put(56, new GoldCard(
                56,
                Arrays.asList(new CornerItem(null), null, new CornerItem(null), new CornerItem(null)),
                PLANT,
                new BonusCorners(2),
                Map.of(PLANT, 3, FUNGI, 1))
        );
        put(57, new GoldCard(
                57,
                Arrays.asList(new CornerItem(null), null, new CornerItem(QUILL), null),
                PLANT,
                new BonusFreePoints(3),
                Map.of(PLANT, 3))
        );
        put(58, new GoldCard(
                58,
                Arrays.asList(new CornerItem(MANUSCRIPT), new CornerItem(null), null, null),
                PLANT,
                new BonusFreePoints(3),
                Map.of(PLANT, 3))
        );
        put(59, new GoldCard(
                59,
                Arrays.asList(null, new CornerItem(INKWELL), null, new CornerItem(null)),
                PLANT,
                new BonusFreePoints(3),
                Map.of(PLANT, 3))
        );
        put(60, new GoldCard(
                60,
                Arrays.asList(new CornerItem(null), new CornerItem(null), null, null),
                PLANT,
                new BonusFreePoints(5),
                Map.of(PLANT, 5))
        );
        put(61, new GoldCard(
                61,
                Arrays.asList(new CornerItem(INKWELL), new CornerItem(null), new CornerItem(null), null),
                ANIMAL,
                new BonusObjects(1, INKWELL),
                Map.of(ANIMAL, 2, INSECT, 1))
        );
        put(62, new GoldCard(
                62,
                Arrays.asList(null, new CornerItem(null), new CornerItem(null), new CornerItem(MANUSCRIPT)),
                ANIMAL,
                new BonusObjects(1, MANUSCRIPT),
                Map.of(ANIMAL, 2, PLANT, 1))
        );
        put(63, new GoldCard(
                63,
                Arrays.asList(new CornerItem(null), null, new CornerItem(QUILL), new CornerItem(null)),
                ANIMAL,
                new BonusObjects(1, QUILL),
                Map.of(ANIMAL, 2, FUNGI, 1))
        );
        put(64, new GoldCard(
                64,
                Arrays.asList(new CornerItem(null), new CornerItem(null), null, new CornerItem(null)),
                ANIMAL,
                new BonusCorners(2),
                Map.of(ANIMAL, 3, INSECT, 1))
        );
        put(65, new GoldCard(
                65,
                Arrays.asList(new CornerItem(null), null, new CornerItem(null), new CornerItem(null)),
                ANIMAL,
                new BonusCorners(2),
                Map.of(ANIMAL, 3, FUNGI, 1))
        );
        put(66, new GoldCard(
                66,
                Arrays.asList(null, new CornerItem(null), new CornerItem(null), new CornerItem(null)),
                ANIMAL,
                new BonusCorners(2),
                Map.of(ANIMAL, 3, PLANT, 1))
        );
        put(67, new GoldCard(
                67,
                Arrays.asList(new CornerItem(null), null, new CornerItem(MANUSCRIPT), null),
                ANIMAL,
                new BonusFreePoints(3),
                Map.of(ANIMAL, 3))
        );
        put(68, new GoldCard(
                68,
                Arrays.asList(new CornerItem(null), new CornerItem(INKWELL), null, null),
                ANIMAL,
                new BonusFreePoints(3),
                Map.of(ANIMAL, 3))
        );
        put(69, new GoldCard(
                69,
                Arrays.asList(null, new CornerItem(null), null, new CornerItem(QUILL)),
                ANIMAL,
                new BonusFreePoints(3),
                Map.of(ANIMAL, 3))
        );
        put(70, new GoldCard(
                70,
                Arrays.asList(null, new CornerItem(null), null, new CornerItem(null)),
                ANIMAL,
                new BonusFreePoints(5),
                Map.of(ANIMAL, 5))
        );
        put(71, new GoldCard(
                71,
                Arrays.asList(new CornerItem(null), new CornerItem(QUILL), null, new CornerItem(null)),
                INSECT,
                new BonusObjects(1, QUILL),
                Map.of(INSECT, 2, PLANT, 1))
        );
        put(72, new GoldCard(
                72,
                Arrays.asList(new CornerItem(null), null, new CornerItem(MANUSCRIPT), new CornerItem(null)),
                INSECT,
                new BonusObjects(1, MANUSCRIPT),
                Map.of(INSECT, 2, ANIMAL, 1))
        );
        put(73, new GoldCard(
                73,
                Arrays.asList(null, new CornerItem(null), new CornerItem(null), new CornerItem(INKWELL)),
                INSECT,
                new BonusObjects(1, INKWELL),
                Map.of(INSECT, 2, FUNGI, 1))
        );
        put(74, new GoldCard(
                74,
                Arrays.asList(new CornerItem(null), new CornerItem(null), null, new CornerItem(null)),
                INSECT,
                new BonusCorners(2),
                Map.of(INSECT, 3, ANIMAL, 1))
        );
        put(75, new GoldCard(
                75,
                Arrays.asList(new CornerItem(null), new CornerItem(null), new CornerItem(null), null),
                INSECT,
                new BonusCorners(2),
                Map.of(INSECT, 3, PLANT, 1))
        );
        put(76, new GoldCard(
                76,
                Arrays.asList(new CornerItem(null), null, new CornerItem(null), new CornerItem(null)),
                INSECT,
                new BonusCorners(2),
                Map.of(INSECT, 3, FUNGI, 1))
        );
        put(77, new GoldCard(
                77,
                Arrays.asList(new CornerItem(INKWELL), null, new CornerItem(null), null),
                INSECT,
                new BonusFreePoints(3),
                Map.of(INSECT, 3))
        );
        put(78, new GoldCard(
                78,
                Arrays.asList(new CornerItem(null), new CornerItem(MANUSCRIPT), null, null),
                INSECT,
                new BonusFreePoints(3),
                Map.of(INSECT, 3))
        );
        put(79, new GoldCard(
                79,
                Arrays.asList(null, null, new CornerItem(QUILL), new CornerItem(null)),
                INSECT,
                new BonusFreePoints(3),
                Map.of(INSECT, 3))
        );
        put(80, new GoldCard(
                80,
                Arrays.asList(new CornerItem(null), new CornerItem(null), null, null),
                INSECT,
                new BonusFreePoints(5),
                Map.of(INSECT, 5))
        );
    }};

    /**
     * {@inheritDoc}
     *
     * @return The id of the first card that this {@code CardFactory} generates
     */
    @Override
    public int getFirstId() {
        return 41;
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
     * @return Generated {@code GoldCard}
     */
    public GoldCard generateCard(int id) {
        GoldCard card = cards.get(id);
        if (card == null)
            throw new WrongIdException();

        return new GoldCard(card);
    }

    /**
     * Given a {@link GoldCard}, generates and returns a clean {@code GoldCard}, that has a different id
     * and contains only information about the kingdom
     * @param card {@code GoldCard} to clean
     * @return Clean {@code GoldCard}
     */
    public static GoldCard generateCleanCard(GoldCard card) {
        return new GoldCard(103 + (card.getId() - 1) / 10,
                List.of(new CornerItem(null), new CornerItem(null), new CornerItem(null), new CornerItem(null)),
                card.getKingdom(),
                null, new HashMap<>());
    }
}
