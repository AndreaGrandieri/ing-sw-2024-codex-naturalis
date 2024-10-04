package it.polimi.ingsw.controller;

import it.polimi.ingsw.controller.event.Event;

import java.util.function.Consumer;

public record EventHandler<T extends Event>(
        Consumer<T> handler,
        String username
) {
    @Override
    public String toString() {
        return "EventHandler{" +
                "handler=" + handler +
                ", username='" + username + '\'' +
                '}';
    }
}
