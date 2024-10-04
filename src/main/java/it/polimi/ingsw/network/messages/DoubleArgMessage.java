package it.polimi.ingsw.network.messages;

public class DoubleArgMessage<T1, T2> extends Message {
    private final T1 arg1;

    private final T2 arg2;

    public DoubleArgMessage(MessageType messageType, T1 arg1, T2 arg2) {
        super(messageType);
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    public T1 get1() {
        return arg1;
    }

    public T2 get2() {
        return arg2;
    }
}
