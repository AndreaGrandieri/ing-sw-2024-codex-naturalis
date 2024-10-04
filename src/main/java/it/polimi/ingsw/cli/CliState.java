package it.polimi.ingsw.cli;

public enum CliState {
    PRE_LOBBY("pre-lobby"),
    LOBBY("lobby"),
    INIT("init"),
    WAIT("wait"),
    PLAY("play"),
    DRAW("draw"),
    END("end");

    private final String description;

    CliState(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}
