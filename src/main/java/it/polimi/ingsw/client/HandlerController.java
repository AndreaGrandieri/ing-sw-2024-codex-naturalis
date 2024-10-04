package it.polimi.ingsw.client;

import it.polimi.ingsw.controller.EventHandler;
import it.polimi.ingsw.controller.event.Event;
import it.polimi.ingsw.controller.event.EventType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class HandlerController {
    private final Map<EventType<? extends Event>, List<EventHandler<? extends Event>>> eventHandlers = new HashMap<>();

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
