package it.polimi.ingsw.gui.views;

import it.polimi.ingsw.controller.event.game.GameEvents;
import it.polimi.ingsw.controller.event.game.MatchCompositionChangeEvent;
import it.polimi.ingsw.controller.event.lobby.LobbyEvents;
import it.polimi.ingsw.controller.event.lobby.LobbyStartEvent;
import it.polimi.ingsw.gui.components.views.ChatView;
import it.polimi.ingsw.gui.components.views.CustomView;
import it.polimi.ingsw.gui.support.ComponentBuilder;
import it.polimi.ingsw.gui.support.FXBind;
import it.polimi.ingsw.gui.support.ViewRoute;
import it.polimi.ingsw.gui.support.context.ChatContext;
import it.polimi.ingsw.gui.support.context.ContextManager;
import it.polimi.ingsw.gui.support.context.MatchContext;
import it.polimi.ingsw.gui.support.interfaces.ViewRouter;
import it.polimi.ingsw.model.game.gamelobby.LobbyInfo;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

import static javafx.beans.binding.Bindings.createStringBinding;

public class LobbyView extends CustomView {

    private final MatchContext matchCtx;
    private final Consumer<LobbyStartEvent> lobbyStartHandler;
    private final Consumer<MatchCompositionChangeEvent> compositionHandler;
    public Label lobbyTitleLabel;
    public Label lobbyCodeLabel;
    public Label lobbyPlayersNumLabel;
    public Label lobbyMaxPlayersLabel;
    public VBox lobbyPlayersBox;
    public ChatView chatView;
    public Pane chatBox;
    public Button startButton;
    public HBox buttonsBox;
    private ObservableList<Node> panesList;

    public LobbyView(ViewRouter viewRouter, ContextManager ctxMan) {
        super(viewRouter);
        this.matchCtx = ctxMan.matchCtx();

        //        System.out.println("REGISTERED LOBBYSTART");
        //        System.out.println(matchCtx.getCurrentLobby());
        //        System.out.println(matchCtx.getCurrentLobby().getGameOngoing());
        if (matchCtx.getCurrentLobby().getGameOngoing()) getRouter().goTo(ViewRoute.GAME);

        lobbyStartHandler = e -> {
            System.out.println("HANDLER LOBBY STAR");
            goToSetup();
        };
        compositionHandler = e -> Platform.runLater(matchCtx::updateCurrentLobby);

        matchCtx.cc().addEventHandler(LobbyEvents.LOBBY_START, lobbyStartHandler);
        matchCtx.cc().addEventHandler(GameEvents.MATCH_COMPOSITION_CHANGE, compositionHandler);

        chatBox.getChildren().add(new VBox());


        ChatContext chatCtrl = ctxMan.chatCtx();
        matchCtx.currentLobbyProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == null || (newValue != null && !oldValue.getUuid().equals(newValue.getUuid()))) {
                System.out.println("RESETTING CHAT");
                chatView = new ChatView(ctxMan);
                chatView.getStyleClass().add("dark-border");
                chatBox.getChildren().set(0, chatView);
            }
        });

        bindProps();

        chatView = new ChatView(ctxMan);
        chatView.getStyleClass().add("dark-border");
        chatBox.getChildren().set(0, chatView);
    }

    private void bindProps() {

        lobbyTitleLabel.textProperty().bind(
                FXBind.map(matchCtx.currentLobbyProperty(), LobbyInfo::getName)
        );
        lobbyCodeLabel.textProperty().bind(
                FXBind.map(matchCtx.currentLobbyProperty(), info1 -> "#".concat(info1.getUuid()))
        );

        FXBind.subscribe(matchCtx.currentLobbyProperty(),
                info -> {
                    System.out.println("EQUALS: " + matchCtx.cc().getMyUsername().equals(info.getMasterUsername()));

                    if (matchCtx.cc().getMyUsername().equals(info.getMasterUsername())) {
                        if (!buttonsBox.getChildren().contains(startButton))
                            buttonsBox.getChildren().add(0, startButton);
                    } else buttonsBox.getChildren().remove(startButton);
                }
        );

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

        lobbyMaxPlayersLabel.textProperty().bind(createStringBinding(
                () -> matchCtx.currentLobbyProperty().get().getMaxPlayers().toString(),
                matchCtx.currentLobbyProperty()
        ));
        lobbyPlayersNumLabel.textProperty().bind(createStringBinding(
                () -> String.valueOf(matchCtx.currentLobbyProperty().get().getPlayerUsernames().size()),
                matchCtx.currentLobbyProperty()
        ));
    }

    @FXML
    private void startLobby() {
        if (matchCtx.cc().startLobby())
            goToSetup();
    }

    private void goToSetup() {
        matchCtx.cc().removeEventHandler(LobbyEvents.LOBBY_START, lobbyStartHandler);
        matchCtx.cc().removeEventHandler(GameEvents.MATCH_COMPOSITION_CHANGE, compositionHandler);

        getRouter().goTo(ViewRoute.GAME_SETUP);
    }

    @FXML
    private void leaveLobby() {
        if (matchCtx.mc().exitLobby()) {
            matchCtx.updateLobbies();
            getRouter().goTo(ViewRoute.TITLES);
        }
    }

    @FXML
    private void refreshLobby() {
        matchCtx.updateCurrentLobby();
    }
}
