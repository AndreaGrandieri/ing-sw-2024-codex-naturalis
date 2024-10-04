package it.polimi.ingsw.gui.support;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class GenericBuilder<T> {
    private final Supplier<T> supplier;

    private GenericBuilder(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public static <T> GenericBuilder<T> of(Supplier<T> supplier) {
        return new GenericBuilder<>(supplier);
    }

    public <P> GenericBuilder<T> set(BiConsumer<T, P> consumer, P value) {
        return new GenericBuilder<>(() -> {
            T object = supplier.get();
            consumer.accept(object, value);
            return object;
        });
    }

    public GenericBuilder<T> apply(Consumer<T> consumer) {
        return new GenericBuilder<>(() -> {
            T object = supplier.get();
            consumer.accept(object);
            return object;
        });
    }

    public <P, L extends List<P>> GenericBuilder<T> setList(Function<T, L> function, BiConsumer<L, P[]> consumer, P... value) {
        return new GenericBuilder<>(() -> {
            T object = supplier.get();
            consumer.accept(function.apply(object), value);
            return object;
        });
    }

    public T build() {
        return supplier.get();
    }
}

