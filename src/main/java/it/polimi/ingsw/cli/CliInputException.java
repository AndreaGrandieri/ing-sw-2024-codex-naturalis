package it.polimi.ingsw.cli;

public class CliInputException extends Exception {
    private final String message;

    public CliInputException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
