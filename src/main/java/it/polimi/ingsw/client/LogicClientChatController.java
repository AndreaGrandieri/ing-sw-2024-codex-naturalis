package it.polimi.ingsw.client;

import it.polimi.ingsw.controller.ChatController;
import it.polimi.ingsw.controller.ChatMessage;
import it.polimi.ingsw.controller.EventHandler;
import it.polimi.ingsw.controller.event.Event;
import it.polimi.ingsw.controller.event.EventType;
import it.polimi.ingsw.controller.interfaces.ClientChatController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static it.polimi.ingsw.controller.event.chat.ChatEvents.BROADCAST_MESSAGE;
import static it.polimi.ingsw.controller.event.chat.ChatEvents.PRIVATE_MESSAGE;

public class LogicClientChatController implements ClientChatController {
    private final Map<EventType<? extends Event>, List<EventHandler<? extends Event>>> eventHandlers = new HashMap<>();
    private final Map<String, List<ChatMessage>> messagesMap;
    private final Map<String, Integer> unreadMap;
    private final ChatController cc;
    private String myUsername;

    public LogicClientChatController(ChatController cc, String username) {
        this.cc = cc;
        cc.addEventHandler(username, BROADCAST_MESSAGE, (i) -> executeHandlers(BROADCAST_MESSAGE, i));
        cc.addEventHandler(username, PRIVATE_MESSAGE, (i) -> executeHandlers(PRIVATE_MESSAGE, i));

        messagesMap = new HashMap<>();
        unreadMap = new HashMap<>();
        messagesMap.put(BROADCAST_SENDER, new ArrayList<>());

        addEventHandler(BROADCAST_MESSAGE, (e) -> messagesMap.get(BROADCAST_SENDER).add(e.message()));

        addEventHandler(PRIVATE_MESSAGE, (e) -> {
            String sender = e.message().sender();
            String recipient = e.recipient();

            if (sender.equals(myUsername)) {
                if (!messagesMap.containsKey(recipient)) {
                    messagesMap.put(recipient, new ArrayList<>());
                    unreadMap.put(recipient, 0);
                }
                messagesMap.get(recipient).add(e.message());
            } else if (recipient.equals(myUsername)) {
                if (!messagesMap.containsKey(sender)) {
                    messagesMap.put(sender, new ArrayList<>());
                    unreadMap.put(sender, 0);
                }
                messagesMap.get(sender).add(e.message());
                unreadMap.put(sender, unreadMap.get(sender) + 1);
            }

        });

        this.myUsername = username;
    }

    @Override
    public List<String> getRecipients() {
        return new ArrayList<>(cc.getChatDistributionList());
    }

    @Override
    public String getMyUsername() {
        return myUsername;
    }

    public boolean sendBroadcastMessage(String payload) {
        return cc.sendBroadcastMessage(payload, myUsername);
    }

    public boolean sendPrivateMessage(String payload, String username) {
        return cc.sendPrivateMessage(payload, myUsername, username);
    }

        public List<ChatMessage> getMessagesFrom(String sender) {
        return messagesMap.get(sender);
    }

    public int getNumberOfUnreadMessagesFrom(String sender) {
        if (unreadMap.containsKey(sender)) {
            int unread = unreadMap.get(sender);
            unreadMap.put(sender, 0);       // Reset counter
            return unread;
        }
        return 0;
    }

    /**
     * Adds the given {@link Consumer} to {@code listeners} of the specified {@link EventType}
     *
     * @param type     type of event to which add the given {@code Consumer}
     * @param consumer {@code Consumer} to add
     */
    public <T extends Event> void addEventHandler(EventType<T> type, Consumer<T> consumer) {
        eventHandlers.computeIfAbsent(type, k -> new ArrayList<>());
        eventHandlers.get(type).add(new EventHandler<>(consumer, null));
    }

    /**
     * Removes the given {@link Consumer} to {@code listeners} of the specified {@link EventType}
     *
     * @param type     type of event from which remove the given {@code Consumer}
     * @param consumer {@code Consumer} to remove
     */
    public <T extends Event> void removeEventHandler(EventType<T> type, Consumer<T> consumer) {
        List<EventHandler<? extends Event>> presentConsumers = eventHandlers.get(type);
        List<EventHandler<? extends Event>> toDelete = presentConsumers
                .stream()
                .filter(h -> h.handler().equals(consumer)).toList();
        presentConsumers.removeAll(toDelete);
    }

    private <T extends Event> List<Consumer<T>> getEventHandlers(EventType<T> type) {
        List<EventHandler<?>> presentConsumers = eventHandlers.get(type);
        if (presentConsumers != null) {
            return presentConsumers
                    .stream()
                    .map(h -> (EventHandler<T>) h)
                    .map(EventHandler::handler)
                    .toList();
        }
        return new ArrayList<>();
    }

    public <T extends Event> void executeHandlers(EventType<T> type, T info) {
        getEventHandlers(type).forEach(consumer -> {
            (new Thread(() -> consumer.accept(info))).start();
            //                consumer.accept(info);
        });
    }
}
