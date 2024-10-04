package it.polimi.ingsw.client;

import it.polimi.ingsw.client.network.liveloop.UserOfClient;
import it.polimi.ingsw.client.network.rmi.DefaultRMIExceptionsHandlerOfClient;
import it.polimi.ingsw.controller.ChatMessage;
import it.polimi.ingsw.controller.EventHandler;
import it.polimi.ingsw.controller.event.Event;
import it.polimi.ingsw.controller.event.EventType;
import it.polimi.ingsw.controller.interfaces.ClientChatController;
import it.polimi.ingsw.network.messages.DoubleArgMessage;
import it.polimi.ingsw.network.messages.SingleArgMessage;
import it.polimi.ingsw.network.messages.ZeroArgMessage;
import it.polimi.ingsw.util.TextValidator;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static it.polimi.ingsw.controller.event.chat.ChatEvents.BROADCAST_MESSAGE;
import static it.polimi.ingsw.controller.event.chat.ChatEvents.PRIVATE_MESSAGE;
import static it.polimi.ingsw.network.messages.MessageType.*;
import static it.polimi.ingsw.network.messages.util.Casting.singleCastAndSend;

public class NetworkClientChatController implements ClientChatController {
    private final UserOfClient userOfClient;
    private final Map<EventType<? extends Event>, List<EventHandler<? extends Event>>> eventHandlers = new HashMap<>();
    private final Boolean RMI;
    private final Map<String, List<ChatMessage>> messagesMap;
    private final Map<String, Integer> unreadMap;
    private String myUsername;

    public NetworkClientChatController(UserOfClient userOfClient, String username, Boolean RMI) {
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
                unreadMap.put(sender, unreadMap.get(sender) +1);
            }

        });

        this.userOfClient = userOfClient;
        this.myUsername = username;
        this.RMI = RMI;
    }

    @Override
    public String getMyUsername() {
        return myUsername;
    }

    private boolean isPayloadValid(String payload) {
        Pattern pattern = Pattern.compile(TextValidator.chatMessageValidator);
        Matcher matcher = pattern.matcher(payload);

        return matcher.matches();
    }

    public boolean sendBroadcastMessage(String payload) {
        if (!this.isPayloadValid(payload)) {
            return false;
        }

        if (!this.RMI) {
            try {
                SingleArgMessage<Boolean> message = singleCastAndSend(userOfClient,
                        new SingleArgMessage<>(SEND_BROADCAST_MESSAGE, payload),
                        ANSWER_SEND_BROADCAST_MESSAGE
                );
                return message.get();
            } catch (ClassCastException e) {
                return false;
            }
        } else {
            try {
                return this.userOfClient.getUserStub().sendBroadcast(payload);
            } catch (RemoteException e) {
                throw DefaultRMIExceptionsHandlerOfClient.clientCriticalError;
            }
        }
    }

    public boolean sendPrivateMessage(String payload, String username) {
        if (!this.isPayloadValid(payload)) {
            return false;
        }

        if (!this.RMI) {
            try {
                SingleArgMessage<Boolean> message = singleCastAndSend(userOfClient,
                        new DoubleArgMessage<>(SEND_PRIVATE_MESSAGE, payload, username),
                        ANSWER_SEND_PRIVATE_MESSAGE
                );
                return message.get();
            } catch (ClassCastException e) {
                return false;
            }
        } else {
            try {
                return this.userOfClient.getUserStub().sendPrivate(payload, username);
            } catch (RemoteException e) {
                throw DefaultRMIExceptionsHandlerOfClient.clientCriticalError;
            }
        }
    }

        public List<String> getRecipients() {
        if (!this.RMI) {
            try {
                SingleArgMessage<List<String>> message = singleCastAndSend(userOfClient,
                        new ZeroArgMessage(GET_RECIPIENTS),
                        ANSWER_GET_RECIPIENTS
                );
                return new ArrayList<>(message.get());
            } catch (ClassCastException e) {
                return null;
            }
        } else {
            try {
                return new ArrayList<>(this.userOfClient.getUserStub().getRecipients());
            } catch (RemoteException e) {
                throw DefaultRMIExceptionsHandlerOfClient.clientCriticalError;
            }
        }
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
