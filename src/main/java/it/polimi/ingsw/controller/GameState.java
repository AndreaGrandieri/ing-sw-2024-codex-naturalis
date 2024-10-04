package it.polimi.ingsw.controller;

import java.io.Serializable;

public enum GameState implements Serializable {
    /**
     * Setting up the game's structure before players start playing; placing and drawing cards is not allowed
     */
    SETTING,
    /**
     * Middle phase of the game, players can place and draw cards
     */
    PLAYING,
    /**
     * Sub-phase of PLAYING, allowed actions are the same as PLAYING state
     */
    EMPTY_DECKS,
    /**
     * Last round of the game, only placeCard actions are allowed
     */
    LAST_ROUND,
    /**
     * Game has ended, no playing actions allowed, winner calculation is allowed
     */
    POST_GAME,
    /**
     * Winner has been elected, no action is allowed
     */
    END,
    /**
     * There is only one player left in the game: no operation is allowed until at least another player rejoins the game.
     * In fact, this state is only used for notifying clients that we are IDLE
     */
    IDLE
}
