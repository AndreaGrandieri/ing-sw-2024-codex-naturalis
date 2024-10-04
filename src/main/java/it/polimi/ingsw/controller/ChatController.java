package it.polimi.ingsw.controller;

import it.polimi.ingsw.controller.event.Event;
import it.polimi.ingsw.controller.event.EventType;
import it.polimi.ingsw.controller.event.chat.BroadcastMesssageEvent;
import it.polimi.ingsw.controller.event.chat.ChatEvents;
import it.polimi.ingsw.controller.event.chat.PrivateMessageEvent;
import it.polimi.ingsw.model.game.gamelobby.LobbyInfo;
import it.polimi.ingsw.util.TextValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatController {
    List<String> usernames;
    private final Map<EventType<? extends Event>, List<EventHandler<? extends Event>>> eventHandlers = new HashMap<>();
    private final LobbyInfo lobbyInfo;

    public ChatController(LobbyInfo lobbyInfo) {
        this.usernames = lobbyInfo.getPlayerUsernames();
        this.lobbyInfo = lobbyInfo;
    }

    public boolean sendBroadcastMessage(String payload, String sender) {
        if (isPayloadValid(payload) && isUsernameValid(sender)) {
            BroadcastMesssageEvent e = new BroadcastMesssageEvent(new ChatMessage(payload, sender));
            getChatDistributionList().forEach(u -> executeHandlers(u, ChatEvents.BROADCAST_MESSAGE, e));

            return true;
        }
        return false;
    }

    public boolean sendPrivateMessage(String payload, String sender, String recipient) {
        if (
                isPayloadValid(payload) &&
                        isUsernameValid(sender) &&
                        isUsernameValid(recipient) &&
                        !recipient.equals(sender)               // You can't message yourself
        ) {
            PrivateMessageEvent e = new PrivateMessageEvent(new ChatMessage(payload, sender), recipient);
            executeHandlers(sender, ChatEvents.PRIVATE_MESSAGE, e);
            executeHandlers(recipient, ChatEvents.PRIVATE_MESSAGE, e);
            return true;
        }
        return false;
    }

    private boolean isPayloadValid(String payload) {
        Pattern pattern = Pattern.compile(TextValidator.chatMessageValidator);
        Matcher matcher = pattern.matcher(payload);

        return matcher.matches();
    }

    private boolean isUsernameValid(String username) {
        return usernames.contains(username);
    }

    /**
     * Adds the given {@link Consumer} to {@code listeners} of the specified {@link EventType}
     *
     * @param type     type of event to which add the given {@code Consumer}
     * @param consumer {@code Consumer} to add
     */
    public <T extends Event> void addEventHandler(String username, EventType<T> type, Consumer<T> consumer) {
        eventHandlers.computeIfAbsent(type, k -> new ArrayList<>());
        eventHandlers.get(type).add(new EventHandler<>(consumer, username));
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

    private <T extends Event> List<Consumer<T>> getEventHandlers(String username, EventType<T> type) {
        List<EventHandler<?>> presentConsumers = eventHandlers.get(type);
        if (presentConsumers != null) {
            return presentConsumers
                    .stream()
                    .map(h -> (EventHandler<T>) h)
                    .filter(h -> h.username() != null
                            && (username != null
                            && username.equals(h.username()))
                            || username == null)
                    .map(EventHandler::handler)
                    .toList();
        }
        return new ArrayList<>();
    }

    private <T extends Event> void removeAllHandlersOfUsername(String username) {
        for (EventType<? extends Event> eventType : eventHandlers.keySet()) {
            List<? extends Consumer<? extends Event>> eventHandlers = this.getEventHandlers(username, eventType);

            for (Consumer<? extends Event> consumer : eventHandlers) {
                this.removeEventHandler((EventType<T>) eventType, (Consumer<T>) consumer);
            }
        }
    }

    private <T extends Event> void executeHandlers(String username, EventType<T> type, T info) {
        getEventHandlers(username, type).forEach(consumer -> (new Thread(() -> consumer.accept(info))).start());
    }

    public void disconnectPlayer(String username) {
        if (this.usernames.contains(username)) {
            // Removing handlers of the disconnected user
            this.removeAllHandlersOfUsername(username);
        }
    }

        public List<String> getChatDistributionList() {
        // Returns the users (User) to which broadcast the message using the BroadcastMessage EventHandler
        // registered in the ChatController
        return this.lobbyInfo.getPlayerUsernames();
    }
}
