package it.polimi.ingsw.gui.views;

import it.polimi.ingsw.controller.interfaces.ClientGameController;
import it.polimi.ingsw.gui.components.views.ChatView;
import it.polimi.ingsw.gui.components.views.CustomView;
import it.polimi.ingsw.gui.support.ComponentBuilder;
import it.polimi.ingsw.gui.support.ViewRoute;
import it.polimi.ingsw.gui.support.context.ContextManager;
import it.polimi.ingsw.gui.support.context.MatchContext;
import it.polimi.ingsw.gui.support.interfaces.ViewRouter;
import it.polimi.ingsw.model.player.PlayerData;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EndView extends CustomView {

    public VBox lobbyPlayersBox;
    public ChatView chatView;
    public Pane chatBox;
    public HBox buttonsBox;
    public Label titleLabel;
    private MatchContext matchCtx;
    private ObservableList<Node> panesList;

    public EndView() {
        super();

    }

    public EndView(ViewRouter viewRouter, ContextManager ctxMan) {
        super(viewRouter);
        ClientGameController gc = ctxMan.gameCtx().gc();

        titleLabel.setText(gc.getWinner() + " won!");

        chatBox.getChildren().add(new VBox());

        matchCtx = ctxMan.matchCtx();

        chatView = new ChatView(ctxMan);
        chatView.getStyleClass().add("dark-border");
        chatBox.getChildren().set(0, chatView);

        List<PlayerData> users = new ArrayList<>(gc.getGameFlow().getAllUsernames().stream()
                .map(gc::getPlayerData).sorted(Comparator.comparingInt(PlayerData::getPoints)).toList());
        Collections.reverse(users);

        lobbyPlayersBox.getChildren().addAll(users.stream()
                .map(p -> ComponentBuilder.endPlayerRow(
                        p.getUsername(), String.valueOf(users.indexOf(p)+1), String.valueOf(p.getPoints()))
                ).toList());
    }

    private void bindProps() {
        panesList = FXCollections.observableArrayList();
        Bindings.bindContentBidirectional(panesList, lobbyPlayersBox.getChildren());

        panesList.addAll(matchCtx.getCurrentLobbyPlayers().stream().map(ComponentBuilder::lobbyPlayerRow).toList());
        matchCtx.getCurrentLobbyPlayers().addListener((ListChangeListener<? super ObjectProperty<String>>) change -> {
            change.next();
            if (!change.getAddedSubList().isEmpty()) {
                List<Pane> list = change.getAddedSubList().stream().map(ComponentBuilder::lobbyPlayerRow).toList();
                panesList.addAll(list);
            }
            if (change.getRemovedSize() != 0) {
                panesList.remove(panesList.size() - change.getRemovedSize(), panesList.size());
            }

        });
    }


    private void goToSetup() {
        getRouter().goTo(ViewRoute.GAME_SETUP);
    }

    @FXML
    private void leaveEnd() {
        if (matchCtx.mc().exitMatch())
            getRouter().goTo(ViewRoute.TITLES);
    }

    @FXML
    private void refreshLobby() {
        matchCtx.updateCurrentLobby();
    }
}
