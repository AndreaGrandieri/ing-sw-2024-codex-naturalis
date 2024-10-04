package it.polimi.ingsw.network.messages;

public class TripleArgMessage<T1, T2, T3> extends Message {
    private final T1 arg1;

    private final T2 arg2;

    private final T3 arg3;

    public TripleArgMessage(MessageType messageType, T1 arg1, T2 arg2, T3 arg3) {
        super(messageType);

        this.arg1 = arg1;
        this.arg2 = arg2;
        this.arg3 = arg3;
    }

    public T1 get1() {
        return arg1;
    }

    public T2 get2() {
        return arg2;
    }

    public T3 get3() {
        return arg3;
    }
}
