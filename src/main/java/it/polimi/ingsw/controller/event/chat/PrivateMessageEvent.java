package it.polimi.ingsw.controller.event.chat;

import it.polimi.ingsw.controller.ChatMessage;
import it.polimi.ingsw.controller.event.Event;

import java.io.Serializable;

/**
 * Event sent when a private message is sent to a player
 * @param message the message
 * @param recipient the recipient of the message
 */
public record PrivateMessageEvent(
        ChatMessage message,
        String recipient
) implements Event, Serializable {
    @Override
    public String toString() {
        return "PrivateMessageEvent{" +
                message.toString() +
                ", recipient='" + recipient + '\'' +
                '}';
    }
}
