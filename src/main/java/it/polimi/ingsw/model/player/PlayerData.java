package it.polimi.ingsw.model.player;

import it.polimi.ingsw.model.card.GoalCard;
import it.polimi.ingsw.model.card.TypedCard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class stores all player-related data during the game:
 * <ul>
 *     <li>Username</li>
 *     <li>{@link PlayerColor}</li>
 *     <li>Cards in hand</li>
 *     <li>Points scored</li>
 *     <li>Private {@link GoalCard}</li>
 * </ul>
 */
public class PlayerData implements Serializable {
    private final String username;
    private PlayerColor color;
    private List<TypedCard> hand;
    private int points;
    private GoalCard privateGoal;

    public PlayerData(String username) {
        hand = null;
        points = 0;
        privateGoal = null;
        color = null;
        this.username = username;
    }

    public PlayerData(String username, PlayerColor color, List<TypedCard> hand, int points, GoalCard privateGoal) {
        this.username = username;
        this.color = color;
        this.hand = hand;
        this.points = points;
        this.privateGoal = privateGoal;
    }

    public PlayerData(PlayerData other) {
        hand = (other.hand == null) ? null : other.hand.stream().map(c -> c == null ? null : c.cloneCard()).toList();
        points = other.points;
        privateGoal = other.privateGoal;
        color = other.color;
        username = other.username;
    }

    /**
     * Returns a clean instance of {@code this PlayerData}. <br>
     * A clean player is a player that has:
     * <ul>
     *     <li>Same username</li>
     *     <li>Same number of points</li>
     *     <li>Same {@link PlayerColor}</li>
     *     <li>Clean {@link GoalCard}</li>
     *     <li>All clean cards in hand</li>
     * </ul>
     *
     * @return Clean version of {@code this PlayerData}
     */
    public PlayerData cleanPlayerData() {
        // instantiate new empty PlayerData
        PlayerData cleanPlayer = new PlayerData(this.username);

        // copy public properties
        cleanPlayer.points = this.points;
        cleanPlayer.color = this.color;

        // hide private goal
        cleanPlayer.privateGoal = new GoalCard(112, 0, null);

        // clean player's hand
        List<TypedCard> cleanHand = new ArrayList<>();
        for(TypedCard card : this.hand) {
            if (card == null) {
                cleanHand.add(null);
            } else {
                cleanHand.add(card.cleanCard());
            }
        }
        cleanPlayer.hand = cleanHand;

        return cleanPlayer;
    }

    public String getUsername() {
        return username;
    }

    public PlayerColor getColor() {
        return color;
    }

    public void setColor(PlayerColor color) {
        if(this.color == null)
            this.color = color;
    }

    public void setPrivateGoal(GoalCard privateGoal) {
        if(this.privateGoal == null)
            this.privateGoal = privateGoal;
    }

    public int getPoints() {
        return points;
    }

    public void addPoints(int points) {
        this.points += points;
    }

    public List<TypedCard> getHand() {
        return (hand == null) ? null : hand.stream().map(c -> c == null ? null : c.cloneCard()).toList();
        //        return new ArrayList<>(hand);
    }

    public void setHand(List<? extends TypedCard> hand) {
        if(this.hand == null)
            this.hand = new ArrayList<>(hand);
    }


    public GoalCard getPrivateGoal() {
        return privateGoal;
    }

    /**
     * Sets the first {@code null} card in hand to the given card. <br>
     * If the given card is already present in the hand, this method does nothing.
     * @param card {@link TypedCard} to add to hand
     */
    public void addCardToHand(TypedCard card) {
        // if card is already present, we cannot add it twice
        if (hand.contains(card))
            return;
        // sets the first null position to card
        for(int i=0; i<hand.size(); i++) {
            if(hand.get(i) == null) {
                hand.set(i, card);
                break;
            }
        }
    }

    /**
     * Finds the given card inside this player's hand and sets that spot to {@code null}
     * @throws MissingCardException If this player's hand does not contain the given card
     * @param card {@link TypedCard} to remove from this player's hand
     */
    public void playCard(TypedCard card) {
        if (!hand.contains(card))
            throw new MissingCardException();
        // to maintain card order in hand
        hand.set(hand.indexOf(card), null);
    }

    @Override
    public String toString() {
        return "PlayerData{" +
                "username='" + username + '\'' +
                ", color=" + color +
                ", hand=" + hand +
                ", points=" + points +
                ", privateGoal=" + privateGoal +
                '}';
    }
}
