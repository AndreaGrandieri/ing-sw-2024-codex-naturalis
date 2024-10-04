package it.polimi.ingsw.network.messages;

public class SingleArgMessage<T> extends Message {
    private final T arg1;

    public SingleArgMessage(MessageType messageType, T arg1) {
        super(messageType);

        this.arg1 = arg1;
    }

    public T get() {
        return arg1;
    }
}
