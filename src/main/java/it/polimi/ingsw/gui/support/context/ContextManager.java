package it.polimi.ingsw.gui.support.context;

import it.polimi.ingsw.client.ConnectionManager;
import it.polimi.ingsw.controller.interfaces.ConnectionManagerI;

public class ContextManager {
    private ConnectionManagerI cm;
    private MatchContext matchCtx;
    private GameContext gameCtx;
    private ChatContext chatCtx;

    public ContextManager() {
    }

    public ContextManager(ConnectionManagerI cm) {
        this.cm = cm;
        setMc();
    }

    public GameContext gameCtx() {
        if (this.gameCtx == null && cm.clientGameController() != null)
            gameCtx = new GameContext(cm.clientGameController());
        return gameCtx;
    }

    public ChatContext chatCtx() {
        if (this.chatCtx == null && cm.clientChatController() != null)
            chatCtx = new ChatContext(cm.clientChatController());
        return chatCtx;
    }

    public MatchContext matchCtx() {
        return matchCtx;
    }

    public ConnectionManagerI getCm() {
        return cm;
    }

    public boolean registerUsername(String username) {
        if (cm.registerUsername(username)) {
            setMc();
            return true;
        }
        return false;
    }

    private void setMc() {
        if (cm.clientMatchController() != null) {
            this.matchCtx = new MatchContext(cm.clientMatchController());
            matchCtx.currentLobbyProperty().addListener((observable, oldValue, newValue) -> {
                if (oldValue != null && newValue != null && !oldValue.getUuid().equals(newValue.getUuid())) {
                    if (chatCtx != null) chatCtx.setCc(cm.clientChatController());
                    if (gameCtx != null) gameCtx.setGc(cm.clientGameController());
                }
            });
        }
    }

    public boolean createCm(String address, int port, Boolean RMI) {
        try {
            this.cm = new ConnectionManager(address, port, RMI, null);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}
