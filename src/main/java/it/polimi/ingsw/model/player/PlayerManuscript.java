package it.polimi.ingsw.model.player;

import it.polimi.ingsw.model.card.PlayableCard;
import it.polimi.ingsw.model.card.StarterCard;
import it.polimi.ingsw.model.card.TypedCard;
import it.polimi.ingsw.model.card.properties.*;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

import static it.polimi.ingsw.model.card.properties.CardKingdom.*;
import static it.polimi.ingsw.model.card.properties.CardObject.*;
import static it.polimi.ingsw.model.card.properties.CornerPosition.*;

/**
 * The {@code PlayerManuscript} class represents the board where each player can place their cards on.
 * Here is stored data about all {@link PlayableCard}s each player has placed and their {@link ManuscriptPosition}s into the board.
 * Furthermore, this class tracks the number of all active {@link CornerItem}s of the player, and updates these data after
 * every move of the player.
 */
public class PlayerManuscript implements Serializable {

    private final Map<ManuscriptPosition, PlayableCard> board;
    private final Map<CardItem, Integer> itemsNumber;

    public PlayerManuscript() {
        board = new LinkedHashMap<>();
        itemsNumber = new HashMap<>();
        initItemsNumber();
    }

    public PlayerManuscript(PlayerManuscript init) {
        this.board = new LinkedHashMap<>(init.board);
        this.itemsNumber = new HashMap<>(init.itemsNumber);
    }

    public PlayerManuscript(StarterCard starter) {
        board = new LinkedHashMap<>();
        itemsNumber = new HashMap<>();
        initItemsNumber();
        board.put(new ManuscriptPosition(0, 0), starter);
        addItemsOnCard(starter);
        if (starter.getFace() == CardFace.FRONT) {
            for (CardItem item : starter.getCenterResources()) {
                incrementItem(item);
            }
        }
    }

    /**
     * Tells if this {@link PlayerManuscript} contains cards or not
     *
     * @return {@code true} if this {@code PlayerManuscript} contains any cards; {@code false} otherwise
     */
    public boolean isEmpty() {
        return board.isEmpty();
    }

    /**
     * Given a {@link ManuscriptPosition}, picks and returns the card that stands into the board at that position;
     * returns null if there is no card at the specified position
     *
     * @param position {@code ManuscriptPosition} to get the card from
     * @return {@link PlayableCard} standing in the specified position
     */
    public PlayableCard getCardAt(ManuscriptPosition position) {
        return board.get(position);
    }

    /**
     * Returns the total amount of cards contained in {@code this PlayerManuscript}
     *
     * @return Total amount of cards contained in {@code this PlayerManuscript}
     */
    public int getNumberOfCards() {
        return board.size();
    }

    /**
     * Calculates and returns a {@link Set} of all {@link ManuscriptPosition}s in which a card can be placed
     *
     * @return {@link Set} of all {@link ManuscriptPosition}s in which a card can be placed
     */
    public Set<ManuscriptPosition> getAllAvailablePositions() {
        Set<ManuscriptPosition> availablePositions = new HashSet<>();

        board.keySet().forEach(boardPosition ->
                List.of(CornerPosition.values()).forEach(cornerPosition -> {

                    ManuscriptPosition newPosition = boardPosition.toPosition(cornerPosition);
                    if (isFree(newPosition) && hasGoodNearby(newPosition))
                        availablePositions.add(newPosition);
                })
        );

        return availablePositions;
    }

    /**
     * Uses {@code getAllAvailablePositions()} method and transforms its result into a {@link List}
     *
     * @return {@link List} of all {@link ManuscriptPosition}s in which a card can be placed
     */
    public List<ManuscriptPosition> getAllAvailablePositionsList() {
        return new ArrayList<>(getAllAvailablePositions());
    }

    private boolean isFree(ManuscriptPosition position) {
        return getCardAt(position) == null;
    }

    private boolean hasGoodNearby(ManuscriptPosition position) {
        return (isFree(position.topLeft()) || getCardAt(position.topLeft()).hasFreeCorner(BOTTOM_RIGHT)) &&
                (isFree(position.topRight()) || getCardAt(position.topRight()).hasFreeCorner(BOTTOM_LEFT)) &&
                (isFree(position.bottomLeft()) || getCardAt(position.bottomLeft()).hasFreeCorner(TOP_RIGHT)) &&
                (isFree(position.bottomRight()) || getCardAt(position.bottomRight()).hasFreeCorner(TOP_LEFT));
    }

    /**
     * Returns the {@code keySet()} of the {@code board}, indicating all positions that are already occupied
     *
     * @return {@link Set} of all {@link ManuscriptPosition}s in which a card is already placed
     */
    public Set<ManuscriptPosition> getAllOccupiedPositions() {
        return board.keySet();
    }

    /**
     * Returns a {@link Map} that tells, for every {@link CornerItem} in the game, how many of them are present in this {@code board}
     *
     * @return {@link Map} that tells, for every {@link CornerItem} in the game, how many of them are present in this {@code board}
     */
    public Map<CardItem, Integer> getItemsNumber() {
        return itemsNumber;
    }

    /**
     * Given a {@link CornerItem}, tells how many of them are present in this {@code board}
     *
     * @param item {@link CornerItem} to count instances of
     * @return Number of the specified {@link CornerItem} in this {@code board}
     */
    public Integer getNumberOfItem(CardItem item) {
        return itemsNumber.get(item);
    }

    public void insertStarterCard(StarterCard card) {
        if (isEmpty()) {
            board.put(new ManuscriptPosition(0, 0), card);
            addItemsOnCard(card);
            for (CardItem item : card.getCenterResources()) {
                incrementItem(item);
            }
        }
    }

    /**
     * Given a {@link ManuscriptPosition} and a {@link PlayableCard},
     * tells if that card can be placed in that position of this {@code board},
     * considering already present cards, already occupied positions, actual available positions,
     * face of given card, placing cost requirements for {@code GoldCards}
     *
     * @param position {@link ManuscriptPosition} in which to place the given card
     * @param card     {@link PlayableCard} to place in the given position
     * @return {@code true} if the card can be placed in that position; {@code false} otherwise
     */
    public boolean isPlaceable(ManuscriptPosition position, PlayableCard card) {
        // cannot place null values
        if (position == null || card == null)
            return false;

        // cannot place the same card more than once
        if (board.containsValue(card))
            return false;

        // if the position is available on the manuscript
        if (getAllAvailablePositions().contains(position)) {



            // we have to check for cost requirements
            return isCostSatisfied(card);

        }

        // position not available: return false
        return false;
    }

    public boolean isCostSatisfied(PlayableCard card) {
        // if the card is on the back, there are no cost requirements to fulfill
        if (card.getFace() == CardFace.BACK)
            return true;

        Map<CardKingdom, Integer> cost = card.getCost();
        // for all kingdoms, check if this manuscript has enough of them
        for (CardKingdom kingdom : cost.keySet()) {
            if (cost.get(kingdom) > itemsNumber.get(kingdom))
                return false;        // one unsatisfied requirements results in returning false
        }
        return true;         // if we got through it's all ok
    }

    /**
     * Given a {@link TypedCard}, tries to put it into the given {@link ManuscriptPosition}.
     * Throws {@link InvalidPlacingException} if the card cannot be placed in that position.
     * After placing the card, decreases the number of items that have been covered by the card,
     * increases the number of items present on the card, calculates and returns the points scored by this placement.
     *
     * @param position {@link ManuscriptPosition} in which to place the given card
     * @param card     {@link TypedCard} to place in the given position
     * @return Number of points scored by this placement
     */
    public int insertCard(ManuscriptPosition position, TypedCard card) {
        if (!isPlaceable(position, card))
            throw new InvalidPlacingException();

        board.put(position, card);

        // Remove items covered by the placed card.
        Stream.of(
                        getCardAt(position.topLeft()) == null ?
                                null : getCardAt(position.topLeft()).getCorner(BOTTOM_RIGHT),
                        getCardAt(position.topRight()) == null ?
                                null : getCardAt(position.topRight()).getCorner(BOTTOM_LEFT),
                        getCardAt(position.bottomLeft()) == null ?
                                null : getCardAt(position.bottomLeft()).getCorner(TOP_RIGHT),
                        getCardAt(position.bottomRight()) == null ?
                                null : getCardAt(position.bottomRight()).getCorner(TOP_LEFT)
                )
                .map(i -> i == null ? null : i.item())
                .filter(Objects::nonNull)
                .toList()
                .forEach(this::decrementItem);

        // Add items of placed card.
        addItemsOnCard(card);

        // if the card is on the back, it scores no points
        return (card.getFace() == CardFace.FRONT) ?
                card.getBonus().calculatePoints(this, position) : 0;
    }

    private void initItemsNumber() {
        itemsNumber.putAll(
                Map.of(
                        FUNGI, 0,
                        PLANT, 0,
                        INSECT, 0,
                        ANIMAL, 0,
                        MANUSCRIPT, 0,
                        QUILL, 0,
                        INKWELL, 0
                )
        );
    }

    private void addItemsOnCard(PlayableCard card) {
        card.getVisibleItems().stream()
                .toList()
                .forEach(this::incrementItem);
    }

    private void incrementItem(CardItem item) {
        itemsNumber.merge(item, 1, Integer::sum);
    }

    private void decrementItem(CardItem item) {
        itemsNumber.merge(item, -1, Integer::sum);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerManuscript that = (PlayerManuscript) o;

        for (ManuscriptPosition position : board.keySet()) {
            if (!board.get(position).equals(that.board.get(position)))
                return false;
        }
        for (ManuscriptPosition position : that.board.keySet()) {
            if (!that.board.get(position).equals(board.get(position)))
                return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();

        for (ManuscriptPosition pos : board.keySet()) {
            string.append(pos.toString()).append(" -> ").append(board.get(pos)).append("\n");
        }

        return string.toString();
    }
}