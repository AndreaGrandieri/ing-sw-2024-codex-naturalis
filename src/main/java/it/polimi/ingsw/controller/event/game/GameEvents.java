package it.polimi.ingsw.controller.event.game;

import it.polimi.ingsw.controller.event.EventType;

import java.io.Serializable;

/**
 * Class that contains all the events that can be triggered during a game
 */
public class GameEvents implements Serializable {
    public static EventType<PlaceCardEvent> PLACE_EVENT = new EventType<>();
    public static EventType<DrawCoveredEvent> DRAW_COVERED = new EventType<>();
    public static EventType<DrawVisibleEvent> DRAW_VISIBLE = new EventType<>();
    public static EventType<TurnChangeEvent> TURN_CHANGE = new EventType<>();
    public static EventType<StateChangeEvent> STATE_CHANGE = new EventType<>();
    public static EventType<SetStarterEvent> SET_STARTER = new EventType<>();
    public static EventType<SetColorEvent> SET_COLOR = new EventType<>();
    public static EventType<ChooseGoalEvent> CHOOSE_GOAL = new EventType<>();
    public static EventType<MatchCompositionChangeEvent> MATCH_COMPOSITION_CHANGE = new EventType<>();
}
