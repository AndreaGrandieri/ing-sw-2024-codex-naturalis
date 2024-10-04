package it.polimi.ingsw.controller.interfaces;

public interface ConnectionManagerI {
    ClientGameController clientGameController();

    ClientChatController clientChatController();

    ClientMatchController clientMatchController();

    boolean exitMatch();


    boolean registerUsername(String username);

}
