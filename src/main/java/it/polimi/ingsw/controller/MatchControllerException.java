package it.polimi.ingsw.controller;

/**
 * Exception thrown by the MatchController when an error occurs.
 */
public class MatchControllerException extends Exception {
    public enum Reason {
        INVALID_PARAMETER,
        NO_ENTRY_FOUND,
        NO_MORE_SPACE_FOR_NEW_MATCHES,
        START_ATTEMPT_FROM_NON_MASTER,
        BAD_STATE_FOR_START,
        BAD_STATE_FOR_JOIN
    }

    private final Reason reason;

    public MatchControllerException(String message, Reason reason) {
        super(message);
        this.reason = reason;
    }

        public Reason getReason() {
        return reason;
    }
}
