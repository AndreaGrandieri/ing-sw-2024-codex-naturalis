package it.polimi.ingsw.network.liveloop;

/**
 * Exception thrown when something goes wrong in the User class.
 */
public class UserException extends Exception {
    public enum Reason {
        INVALID_USERNAME,
    }

    private final Reason reason;

    public UserException(String message, Reason reason) {
        super(message);
        this.reason = reason;
    }

        public Reason getReason() {
        return reason;
    }
}
