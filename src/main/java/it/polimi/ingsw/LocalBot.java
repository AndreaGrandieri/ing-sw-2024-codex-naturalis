package it.polimi.ingsw;

import it.polimi.ingsw.controller.interfaces.ClientMatchController;
import it.polimi.ingsw.controller.interfaces.ConnectionManagerI;

public class LocalBot {
    private final ConnectionManagerI cm;

    public LocalBot(String username, ConnectionManagerI cm) {
        this.cm = cm;
        cm.registerUsername(username);
    }

    public ClientMatchController mc() {
        return cm.clientMatchController();
    }

}
