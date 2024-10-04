package it.polimi.ingsw.gui.support.context;

import it.polimi.ingsw.controller.event.chat.ChatEvents;
import it.polimi.ingsw.controller.interfaces.ClientChatController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatContext {
    final Map<String, Integer> unreadMap;

    private ClientChatController cc;

    public ChatContext(ClientChatController chatCtrl) {
        unreadMap = new HashMap<>();
        this.cc = chatCtrl;
        cc.addEventHandler(ChatEvents.PRIVATE_MESSAGE, e -> {
            if (e.recipient().equals(cc.getMyUsername()))
                addUnreadCount(e.message().sender());
        });
        cc.addEventHandler(ChatEvents.BROADCAST_MESSAGE, e -> {
            addUnreadCount(ClientChatController.BROADCAST_SENDER);
        });
    }

    private void addUnreadCount(String e) {
        unreadMap.compute(e, (k, v) -> v == null ? 1 : v + 1);
    }

    public List<String> getRecipients() {
        List<String> recipients = cc.getRecipients();
        recipients.remove(cc.getMyUsername());
        recipients.add(ClientChatController.BROADCAST_SENDER);

        return recipients;
    }

    public int sumUnreadMessages() {
        System.out.println(unreadMap);
        return getRecipients().stream().mapToInt(o -> {
            Integer i = unreadMap.get(o);
            return i == null ? 0 : i;
        }).sum();
    }

    public void readMessages(String sender) {
        unreadMap.put(sender, 0);
    }

    public ClientChatController cc() {
        return cc;
    }

    public void setCc(ClientChatController cc) {
        this.cc = cc;
    }
}
