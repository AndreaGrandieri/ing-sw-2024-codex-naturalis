package it.polimi.ingsw.controller;

import it.polimi.ingsw.client.LogicClientChatController;
import it.polimi.ingsw.client.local.LocalClientGameController;
import it.polimi.ingsw.client.local.LocalClientMatchController;
import it.polimi.ingsw.controller.interfaces.ClientChatController;
import it.polimi.ingsw.controller.interfaces.ClientGameController;
import it.polimi.ingsw.controller.interfaces.ClientMatchController;
import it.polimi.ingsw.controller.interfaces.ConnectionManagerI;

public class LocalServer2 {
    private final LogicMatchController lmc;
    private final LogicUsernameController uc;

    public LocalServer2() {
        this.lmc = new LogicMatchController(300, 100);
        this.uc = new LogicUsernameController();
    }

    public ConnectionManagerI newConnectionManager() {
        return new ConnectionManagerI() {
            private String username;

            @Override
            public ClientGameController clientGameController() {
                try {
                    GameController gameController = lmc.getGameController(username);
                    //                    System.out.println("GAME CONTROLLER: " + gameController);
                    return new LocalClientGameController(gameController, username);
                } catch (MatchControllerException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public ClientChatController clientChatController() {
                LogicClientChatController localChatController = null;
                try {
                    ChatController chatController = lmc.getChatController(username);
                    //                    if (chatController == null) System.out.println("NULLLL");
                    localChatController = new LogicClientChatController(chatController, username);
                    System.out.println(chatController == null);
                } catch (MatchControllerException e) {
                    throw new RuntimeException(e);
                }
                return localChatController;
            }

            @Override
            public ClientMatchController clientMatchController() {
                return new LocalClientMatchController(username, lmc);
            }

            @Override
            public boolean exitMatch() {
                return false;
            }

            @Override
            public boolean registerUsername(String username) {
                this.username = username;
                return uc.registerUsername(username);
            }
        };
    }
}
