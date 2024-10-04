package it.polimi.ingsw.controller;

import java.io.Serializable;

public record ChatMessage(
        String message,
        String sender
) implements Serializable {
    @Override
    public String toString() {
        return "Message: " + message + ", sender: " + sender;
    }
}
