package it.polimi.ingsw.gui.support;

public enum ViewRoute {
    USERNAME("username"),
    TITLES("titles"),
    LOBBY("lobby"),
    GAME_SETUP("setup"),
    GAME("game"),
    END("end");

    private final String value;

    ViewRoute(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
