package it.polimi.ingsw.controller.interfaces;

import it.polimi.ingsw.controller.ChatMessage;
import it.polimi.ingsw.controller.event.Event;
import it.polimi.ingsw.controller.event.EventType;

import java.util.List;
import java.util.function.Consumer;

public interface ClientChatController {
    String BROADCAST_SENDER = "*";

    List<String> getRecipients();

    String getMyUsername();

    boolean sendBroadcastMessage(String payload);

    boolean sendPrivateMessage(String payload, String username);

        List<ChatMessage> getMessagesFrom(String sender);

    int getNumberOfUnreadMessagesFrom(String sender);

    <T extends Event> void addEventHandler(EventType<T> type, Consumer<T> consumer);

    <T extends Event> void removeEventHandler(EventType<T> type, Consumer<T> consumer);
}
