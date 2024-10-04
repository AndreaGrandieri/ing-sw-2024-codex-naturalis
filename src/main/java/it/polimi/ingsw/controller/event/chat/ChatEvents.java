package it.polimi.ingsw.controller.event.chat;

import it.polimi.ingsw.controller.event.EventType;

import java.io.Serializable;

/**
 * Class that contains the events related to the chat
 */
public class ChatEvents implements Serializable {

    public static EventType<BroadcastMesssageEvent> BROADCAST_MESSAGE = new EventType<>();
    public static EventType<PrivateMessageEvent> PRIVATE_MESSAGE = new EventType<>();
}
