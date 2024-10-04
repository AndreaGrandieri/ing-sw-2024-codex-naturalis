package it.polimi.ingsw.cli;

import it.polimi.ingsw.client.NetworkClientGameController;
import it.polimi.ingsw.controller.event.game.*;

import static it.polimi.ingsw.controller.GameState.*;
import static it.polimi.ingsw.controller.event.game.GameEvents.*;

/**
 * Manage handler connections with the server. Handlers are used to respond to asynchronous events.
 */
public class HandlerManager {

    private NetworkClientGameController cgc;

    private String user;

    private CliState state;

    private boolean lastTurn;

    public HandlerManager() {
        state = CliState.PRE_LOBBY;
        lastTurn = false;
    }

    private void chooseGoal(ChooseGoalEvent e) {

    }

    private void drawCovered(DrawCoveredEvent e) {

    }

    private void place(PlaceCardEvent e) {

    }

    private void setColor(SetColorEvent e) {

    }

    private void setStarter(SetStarterEvent e) {
    }

    private synchronized void stateChange(StateChangeEvent e) {
        if (e.newState() == PLAYING) {
            if (!cgc.getGameFlow().isCurrentPlayer(user)) {
                state = CliState.WAIT;
                IOManager.println("");
                IOManager.printPrompt(state);
            }
        } else if (e.newState() == POST_GAME) {
            state = CliState.END;
        } else if (e.newState() == LAST_ROUND) {
            IOManager.println("Last round");
            lastTurn = true;
        }
    }

    private synchronized void turnChange(TurnChangeEvent e) {
        if (e.currentUsername().equals(user)) {
            if (state != CliState.END) {
                state = CliState.PLAY;
            }
            IOManager.println("");
            IOManager.printPrompt(state);
        }
    }

    /**
     * Return the state of the CLI.
     */
    public CliState getState() {
        return state;
    }

    public void setState(CliState state) {
        this.state = state;
    }

    public void setCgc(NetworkClientGameController cgc) {
        this.cgc = cgc;
        cgc.addEventHandler(CHOOSE_GOAL, this::chooseGoal);
        cgc.addEventHandler(DRAW_COVERED, this::drawCovered);
        cgc.addEventHandler(PLACE_EVENT, this::place);
        cgc.addEventHandler(SET_COLOR, this::setColor);
        cgc.addEventHandler(SET_STARTER, this::setStarter);
        cgc.addEventHandler(STATE_CHANGE, this::stateChange);
        cgc.addEventHandler(TURN_CHANGE, this::turnChange);
    }

    public void setUser(String user) {
        this.user = user;
    }

    public boolean isLastTurn() {
        return lastTurn;
    }
}
