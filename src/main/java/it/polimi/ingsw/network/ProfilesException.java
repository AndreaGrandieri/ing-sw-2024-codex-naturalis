package it.polimi.ingsw.network;

/**
 * Exception thrown when an error occurs while using Profiles.
 */
public class ProfilesException extends Exception {
    public enum Reason {
        ENTRY_NOT_FOUND,
    }

    private final Reason reason;

    public ProfilesException(String message, Reason reason) {
        super(message);
        this.reason = reason;
    }

        public Reason getReason() {
        return reason;
    }
}
