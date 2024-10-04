package it.polimi.ingsw.client.local;

import it.polimi.ingsw.controller.LogicMatchController;
import it.polimi.ingsw.controller.event.Event;
import it.polimi.ingsw.controller.event.EventType;
import it.polimi.ingsw.controller.interfaces.ClientMatchController;
import it.polimi.ingsw.model.game.gamelobby.LobbyInfo;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class LocalClientMatchController implements ClientMatchController {
    private final String currUsername;
    private final LogicMatchController mc;

    public LocalClientMatchController(String currUsername, LogicMatchController mc) {
        this.currUsername = currUsername;
        this.mc = mc;
    }

    private <T> T boxException(Callable<T> f) {
        try {
            return f.call();
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            return null;
        }
    }

    private <T> boolean boxExceptionBool(Callable<T> f) {
        return Boolean.TRUE.equals(boxException(f));
    }


    @Override
    public String getMyUsername() {
        return currUsername;
    }

    @Override
    public boolean createLobby(String name, int maxPlayers) {
        return boxExceptionBool(() -> {
            mc.createLobby(name, maxPlayers, currUsername);
            return true;
        });
    }

    @Override
    public List<LobbyInfo> getLobbies() {
        return mc.getInfoLobbies();
    }

    @Override
    public boolean joinLobby(String uuid) {
        return boxExceptionBool(() -> {
            mc.joinLobby(uuid, currUsername);
            return true;
        });
    }

    @Override
    public LobbyInfo getLobbyInfo() {
        return boxException(() -> mc.getLobbyInfoByPlayerUsername(currUsername));
    }

    @Override
    public boolean startLobby() {
        return boxExceptionBool(() -> {
            mc.startLobby(currUsername);
            return true;
        });
    }


    @Override
    public boolean exitLobby() {
        return boxExceptionBool(() -> {
            mc.exitLobby(currUsername);
            return true;
        });
    }

    @Override
    public boolean exitMatch() {
        return boxExceptionBool(() -> {
            mc.exitMatch(currUsername);
            return true;
        });
    }

    @Override
    public <T extends Event> void addEventHandler(EventType<T> type, Consumer<T> consumer) {
        mc.addEventHandler(type, consumer);
    }

    @Override
    public <T extends Event> void removeEventHandler(EventType<T> type, Consumer<T> consumer) {
        mc.removeEventHandler(type, consumer);
    }
}
