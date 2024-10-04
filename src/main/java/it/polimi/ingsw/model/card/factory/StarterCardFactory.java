package it.polimi.ingsw.model.card.factory;

import it.polimi.ingsw.model.card.StarterCard;
import it.polimi.ingsw.model.card.properties.CardKingdom;
import it.polimi.ingsw.model.card.properties.CornerItem;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StarterCardFactory implements CardFactory {
    private static final Map<Integer, StarterCard> cards = new HashMap<>() {{
        put(81, new StarterCard(
                81,
                Arrays.asList(new CornerItem(null), new CornerItem(CardKingdom.PLANT), new CornerItem(CardKingdom.INSECT), new CornerItem(null)),
                List.of(CardKingdom.INSECT),
                List.of(CardKingdom.FUNGI, CardKingdom.PLANT, CardKingdom.INSECT, CardKingdom.ANIMAL))
        );
        put(82, new StarterCard(
                82,
                Arrays.asList(new CornerItem(CardKingdom.ANIMAL), new CornerItem(null), new CornerItem(null), new CornerItem(CardKingdom.FUNGI)),
                List.of(CardKingdom.FUNGI),
                List.of(CardKingdom.PLANT, CardKingdom.ANIMAL, CardKingdom.FUNGI, CardKingdom.INSECT))
        );
        put(83, new StarterCard(
                83,
                Arrays.asList(new CornerItem(null), new CornerItem(null), new CornerItem(null), new CornerItem(null)),
                List.of(CardKingdom.PLANT, CardKingdom.FUNGI),
                List.of(CardKingdom.INSECT, CardKingdom.ANIMAL, CardKingdom.FUNGI, CardKingdom.PLANT))
        );
        put(84, new StarterCard(
                84,
                Arrays.asList(new CornerItem(null), new CornerItem(null), new CornerItem(null), new CornerItem(null)),
                List.of(CardKingdom.ANIMAL, CardKingdom.INSECT),
                List.of(CardKingdom.PLANT, CardKingdom.INSECT, CardKingdom.ANIMAL, CardKingdom.FUNGI))
        );
        put(85, new StarterCard(
                85,
                Arrays.asList(new CornerItem(null), new CornerItem(null), null, null),
                List.of(CardKingdom.ANIMAL, CardKingdom.INSECT, CardKingdom.PLANT),
                List.of(CardKingdom.INSECT, CardKingdom.FUNGI, CardKingdom.PLANT, CardKingdom.ANIMAL))
        );
        put(86, new StarterCard(
                86,
                Arrays.asList(new CornerItem(null), new CornerItem(null), null, null),
                List.of(CardKingdom.PLANT, CardKingdom.ANIMAL, CardKingdom.FUNGI),
                List.of(CardKingdom.FUNGI, CardKingdom.ANIMAL, CardKingdom.PLANT, CardKingdom.INSECT))
        );
    }};

    /**
     * {@inheritDoc}
     *
     * @return The id of the first card that this {@code CardFactory} generates
     */
    @Override
    public int getFirstId() {
        return 81;
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
     * @return Generated {@code StarterCard}
     */
    public StarterCard generateCard(int id) {
        StarterCard card = cards.get(id);
        if (card == null)
            throw new WrongIdException();

        return new StarterCard(card);
    }
}
