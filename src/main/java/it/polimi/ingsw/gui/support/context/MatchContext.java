package it.polimi.ingsw.gui.support.context;

import it.polimi.ingsw.controller.interfaces.ClientMatchController;
import it.polimi.ingsw.gui.support.FXBind;
import it.polimi.ingsw.model.game.gamelobby.LobbyInfo;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MatchContext {
    private final ObservableList<ObjectProperty<LobbyInfo>> lobbiesPropList;
    private final ObservableList<LobbyInfo> lobbiesList;
    private final ObjectProperty<LobbyInfo> currentLobby;
    private final ObservableList<ObjectProperty<String>> currentLobbyPlayers;
    private final ClientMatchController mc;


    public MatchContext(ClientMatchController mc) {
        this.mc = mc;
        lobbiesList = FXCollections.observableArrayList();
        List<LobbyInfo> lobbies = mc.getLobbies();
        lobbiesPropList = FXCollections.observableList(new ArrayList<>(), p -> new ObjectProperty[]{p});
        if (lobbies != null)
            lobbiesPropList.setAll(
                    lobbies.stream().map(SimpleObjectProperty::new).collect(Collectors.toList())
            );

        currentLobby = new SimpleObjectProperty<>();

        currentLobbyPlayers = FXCollections.observableList(new ArrayList<>(), p -> new ObjectProperty[]{p});
    }

    public ClientMatchController cc() {
        return mc;
    }

    public void updateLobbies() {
        List<LobbyInfo> lobbies = mc.getLobbies();
        if (lobbies != null) {

            FXBind.updateObservablePropList(lobbies, lobbiesPropList);


            FXBind.updateObservableList(lobbies, lobbiesList);
        }
        System.out.println(lobbiesList);
    }

    public ObservableList<ObjectProperty<LobbyInfo>> getLobbiesPropList() {
        return lobbiesPropList;
    }

    public ObservableList<LobbyInfo> getLobbiesList() {
        return lobbiesList;
    }

    public ClientMatchController mc() {
        return mc;
    }

    public boolean joinLobby(String uuid) {
        if (mc.joinLobby(uuid)) {
            updateCurrentLobby();
            return true;
        } else System.out.println("error joining lobby");
        return false;
    }

    public LobbyInfo getCurrentLobby() {
        return currentLobby.get();
    }

    public ObjectProperty<LobbyInfo> currentLobbyProperty() {
        return currentLobby;
    }

    public void updateCurrentLobby() {
        LobbyInfo lobbyInfo = mc.getLobbyInfo();
        if (lobbyInfo != null) {
            this.currentLobby.set(new LobbyInfo(lobbyInfo));
            System.out.println("usernames: " + lobbyInfo.getPlayerUsernames());
            FXBind.updateObservablePropList(lobbyInfo.getPlayerUsernames(), currentLobbyPlayers);
        } else this.currentLobby.set(null);
    }

    public ObservableList<ObjectProperty<String>> getCurrentLobbyPlayers() {
        return currentLobbyPlayers;
    }
}
