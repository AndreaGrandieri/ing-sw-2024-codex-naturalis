package it.polimi.ingsw.client.network;

/**
 * This exception is thrown in case something is wrong in the ProfileOfClient class.
 */
public class ProfileOfClientException extends Exception {
    public enum Reason {
        PROFILE_OF_CLIENT_NOT_INITIALIZED,
    }

    private final Reason reason;

    public ProfileOfClientException(String message, Reason reason) {
        super(message);
        this.reason = reason;
    }

        public Reason getReason() {
        return reason;
    }
}
