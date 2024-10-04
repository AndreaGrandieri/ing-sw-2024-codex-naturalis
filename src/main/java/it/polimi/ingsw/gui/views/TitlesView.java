package it.polimi.ingsw.gui.views;

import it.polimi.ingsw.controller.GameState;
import it.polimi.ingsw.gui.components.views.CustomView;
import it.polimi.ingsw.gui.components.views.GroupPane;
import it.polimi.ingsw.gui.support.ComponentBuilder;
import it.polimi.ingsw.gui.support.FXUtils;
import it.polimi.ingsw.gui.support.ViewRoute;
import it.polimi.ingsw.gui.support.context.ContextManager;
import it.polimi.ingsw.gui.support.context.GameContext;
import it.polimi.ingsw.gui.support.context.MatchContext;
import it.polimi.ingsw.gui.support.interfaces.ViewRouter;
import it.polimi.ingsw.model.game.gamelobby.LobbyInfo;
import it.polimi.ingsw.util.TextValidator;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.converter.IntegerStringConverter;

import java.util.List;
import java.util.stream.Collectors;

public class TitlesView extends CustomView {
    private final MatchContext matchCtx;
    private final TextFormatter<Integer> lobbyNumFormatter;
    private final ContextManager ctxMan;
    private final Label noLobbyLabel = getLabel();
    public GroupPane createLobbyGroup;
    public GroupPane lobbiesGroup;
    public TextArea playersTextArea;
    public TextArea lobbyNameTextArea;
    public VBox lobbyList;
    public ScrollPane scrollPane;

    public TitlesView(ViewRouter viewRouter, ContextManager ctxMan) {
        super(viewRouter);
        this.matchCtx = ctxMan.matchCtx();
        this.ctxMan = ctxMan;

        ObservableList<ObjectProperty<LobbyInfo>> lobbies = matchCtx.getLobbiesPropList();

        List<Pane> userList = lobbies.stream().map(this::lobbyRow).collect(Collectors.toList());
        lobbyList.getChildren().addAll(userList);

        if (lobbyList.getChildren().isEmpty()) {
            lobbyList.getChildren().add(noLobbyLabel);
        } else lobbyList.getChildren().remove(noLobbyLabel);

        lobbiesGroup.defaultButtonProperty().bind(lobbiesGroup.focusedProperty());
        createLobbyGroup.defaultButtonProperty().bind(lobbyNameTextArea.focusedProperty()
                .or(playersTextArea.focusedProperty())
                .or(createLobbyGroup.focusedProperty())
        );

        lobbies.addListener((ListChangeListener<? super ObjectProperty<LobbyInfo>>) change -> {
            //            System.out.println("lobbylist: " + lobbies);
            change.next();
            if (lobbies.isEmpty()) {
                lobbyList.getChildren().add(noLobbyLabel);
            } else lobbyList.getChildren().remove(noLobbyLabel);


            if (!change.getAddedSubList().isEmpty()) {
                List<Pane> list = change.getAddedSubList().stream().map(this::lobbyRow).toList();
                lobbyList.getChildren().addAll(list);
                userList.addAll(list);
                scrollPane.setVvalue(1);
            }
            if (change.getRemovedSize() != 0) {
                List<Pane> panes = userList.subList(userList.size() - change.getRemovedSize(), userList.size());
                lobbyList.getChildren().removeAll(panes);
                panes.clear();
            }

        });

        lobbyNumFormatter = new TextFormatter<>(new IntegerStringConverter(), null, FXUtils.regexFilter(TextValidator.lobbyPlayersNumWritingValidator));
        playersTextArea.setTextFormatter(lobbyNumFormatter);
        lobbyNameTextArea.setTextFormatter(FXUtils.textFormatterFromRegEx(TextValidator.lobbyNameWritingValidator));

    }

    private static Label getLabel() {
        return FXUtils.addStyle(new Label("No lobby on server!"), "empty-list-info");
    }

    private EventHandler<MouseEvent> onLobbyClick(ObjectProperty<LobbyInfo> info) {
        return e -> joinLobby(info);
    }

    private Pane lobbyRow(ObjectProperty<LobbyInfo> info) {
        return ComponentBuilder.lobbyRow(info, onLobbyClick(info));
    }

    @FXML
    private void createLobby() {
        String playersNumText = playersTextArea.getText();
        String lobbyName = lobbyNameTextArea.getText();
        if (lobbyName.matches(TextValidator.lobbyNameValidator) && playersNumText.matches(TextValidator.lobbyPlayersNumValidator)) {

            int playersNum = Integer.parseInt(playersNumText);
            if (matchCtx.mc().createLobby(lobbyName, playersNum)) {
                lobbyNumFormatter.valueProperty().set(null);
                lobbyNameTextArea.setText(null);
                matchCtx.updateCurrentLobby();
                System.out.println(matchCtx.getCurrentLobby());
                goToLobby();
            } else System.out.println("Error creating lobby");
        }
    }

    @FXML
    private void joinLobby(ObjectProperty<LobbyInfo> info) {
        System.out.println("LOBBY INFO: " + info);
        String uuid = info.get().getUuid();
        if (matchCtx.joinLobby(uuid)) {
            if (matchCtx.getCurrentLobby().getGameOngoing()) {
                //                ctxMan.gameCtx().updateGameFlow();
                GameContext gameCtx = ctxMan.gameCtx();
                //                System.out.println(gameCtx.getGameFlow().getState());

                if (gameCtx.getGameFlow().getState() == GameState.SETTING
                        && !gameCtx.isSettingDone(gameCtx.getMyUsername())) getRouter().goTo(ViewRoute.GAME_SETUP);
                else getRouter().goTo(ViewRoute.GAME);
                return;
            }
            goToLobby();


        } else System.out.println("Join lobby failed " + uuid + info.get().getPlayerUsernames());
    }

    @FXML
    private void joinLobby(LobbyInfo info) {
        String uuid = info.getUuid();
        if (matchCtx.joinLobby(uuid)) {
            goToLobby();
        } else System.out.println("Join lobby failed " + uuid + info.getPlayerUsernames());
    }

    private void goToLobby() {
        getRouter().goTo(ViewRoute.LOBBY);
    }

    @FXML
    private void refreshLobbies() {
        System.out.println("called refresh");
        matchCtx.updateLobbies();
    }

    @FXML
    private void goToUsername() {
        getRouter().goTo(ViewRoute.USERNAME);
    }
}
