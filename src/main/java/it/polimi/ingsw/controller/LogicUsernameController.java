package it.polimi.ingsw.controller;

import java.util.HashSet;
import java.util.Set;

public class LogicUsernameController {
    private final Set<String> usernames;

    public LogicUsernameController() {
        this.usernames = new HashSet<>();
    }

    public boolean registerUsername(String username) {
        if (usernames.contains(username)) return false;
        return usernames.add(username);
    }
}
