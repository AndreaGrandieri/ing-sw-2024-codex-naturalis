package it.polimi.ingsw.network.messages.util;

import it.polimi.ingsw.client.network.liveloop.UserOfClient;
import it.polimi.ingsw.network.messages.*;

import java.util.List;

@SuppressWarnings("unchecked")
public class Casting {

    public static <T> SingleArgMessage<T> singleCastAndSend(UserOfClient userOfClient, Message requestMessage, MessageType requestType) {
        Message message = userOfClient.sendAndWaitMultiple(requestMessage, List.of(requestType, MessageType.UNKNOWN_ERROR));
        return (SingleArgMessage<T>) message;
    }

    // public static <T, Q> DoubleArgMessage<T, Q> doubleCastAndSend(UserOfClient userOfClient, Message requestMessage, MessageType requestType) {
    //     return (DoubleArgMessage<T, Q>) userOfClient.sendAndWait(requestMessage, requestType);
    // }

    // public static <T, Q, R> TripleArgMessage<T, Q, R> tripleCastAndSend(UserOfClient userOfClient, Message requestMessage, MessageType requestType) {
    //     return (TripleArgMessage<T, Q, R>) userOfClient.sendAndWait(requestMessage, requestType);
    // }

    public static ZeroArgMessage zeroArgCast(Message message) {
        return (ZeroArgMessage) message;
    }

    public static <T> SingleArgMessage<T> singleArgCast(Message message) {
        return (SingleArgMessage<T>) message;
    }

    public static <T, Q> DoubleArgMessage<T, Q> doubleArgCast(Message message) {
        return (DoubleArgMessage<T, Q>) message;
    }

    public static <T, Q, R> TripleArgMessage<T, Q, R> tripleArgCast(Message message) {
        return (TripleArgMessage<T, Q, R>) message;
    }
}
