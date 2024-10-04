package it.polimi.ingsw.controller.event.chat;

import it.polimi.ingsw.controller.ChatMessage;
import it.polimi.ingsw.controller.event.Event;

import java.io.Serializable;

/**
 * Event sent when a broadcast message is sent
 * @param message the message sent in broadcast
 */
public record BroadcastMesssageEvent(
        ChatMessage message
) implements Event, Serializable {
    @Override
    public String toString() {
        return "BroadcastMessageEvent{" +
                message.toString() +
                '}';
    }
}
