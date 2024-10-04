package it.polimi.ingsw.controller.interfaces;

import it.polimi.ingsw.controller.event.Event;
import it.polimi.ingsw.controller.event.EventType;
import it.polimi.ingsw.model.game.gamelobby.LobbyInfo;

import java.util.List;
import java.util.function.Consumer;

public interface ClientMatchController {
    String getMyUsername();

    boolean createLobby(String name, int maxPlayers);

        List<LobbyInfo> getLobbies();

    boolean joinLobby(String uuid);

        LobbyInfo getLobbyInfo();

    boolean startLobby();

    boolean exitLobby();

    boolean exitMatch();

    <T extends Event> void addEventHandler(EventType<T> type, Consumer<T> consumer);

    <T extends Event> void removeEventHandler(EventType<T> type, Consumer<T> consumer);
}
