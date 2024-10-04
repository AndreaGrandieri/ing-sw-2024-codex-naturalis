package it.polimi.ingsw.gui.support.context;

import it.polimi.ingsw.controller.GameFlow;
import it.polimi.ingsw.controller.event.game.GameEvents;
import it.polimi.ingsw.controller.interfaces.ClientGameController;
import it.polimi.ingsw.gui.support.helper.Card2D;
import it.polimi.ingsw.gui.support.info.PlayerInfo;
import it.polimi.ingsw.gui.support.interfaces.CardSizeController;
import it.polimi.ingsw.model.game.CommonBoard;
import it.polimi.ingsw.model.player.PlayerData;
import it.polimi.ingsw.model.player.PlayerManuscript;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Dimension2D;

import java.util.List;
import java.util.Map;

import static java.lang.Math.min;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static javafx.beans.binding.Bindings.createDoubleBinding;

public class GameContext implements CardSizeController {
    private final List<String> playerNames;
    private final Map<String, ObjectProperty<PlayerData>> playerDataPropsMap;
    private final Map<String, ObjectProperty<PlayerInfo>> playerInfoPropsMap;
    private final Map<String, ObjectProperty<PlayerManuscript>> manuscriptPropsMap;
    private final DoubleProperty ratio;
    private final ObjectProperty<Dimension2D> screen;
    private final ObjectProperty<GameFlow> gameFlow;
    private final ObjectProperty<CommonBoard> board;
    private ClientGameController gc;
    private Dimension2D screenSize = new Dimension2D(0, 0);

    public GameContext(ClientGameController gc) {
        this.gc = gc;

        ratio = new SimpleDoubleProperty(1.0);
        screen = new SimpleObjectProperty<>(new Dimension2D(0, 0));

        playerNames = gc.getGameFlow().getAllUsernames();

        playerDataPropsMap = playerNames.stream().collect(toUnmodifiableMap(
                identity(),
                n -> new SimpleObjectProperty<>(gc.getPlayerData(n))
        ));

        playerInfoPropsMap = playerNames.stream().collect(toUnmodifiableMap(
                identity(),
                n -> new SimpleObjectProperty<>(new PlayerInfo())
        ));

        manuscriptPropsMap = playerNames.stream().collect(toUnmodifiableMap(
                identity(),
                n -> new SimpleObjectProperty<>(gc.getManuscript(n))
        ));

        board = new SimpleObjectProperty<>(gc.getGameBoard());
        gameFlow = new SimpleObjectProperty<>(gc.getGameFlow());

        manuscriptPropsMap.forEach((s, prop) -> {
            prop.addListener((observableValue, gameFlow1, t1) -> {
                System.out.println("----- man change" + s + t1.getItemsNumber());
            });
        });


        ratio.bind(createDoubleBinding(
                () -> getRatio(screen.get()),
                screen
        ));


        gc.addEventHandler(GameEvents.CHOOSE_GOAL, e ->
                Platform.runLater(() -> updatePlayerData(e.username()))
        );
        gc.addEventHandler(GameEvents.SET_STARTER, e ->
                Platform.runLater(() -> updateManuscript(e.username()))
        );
        gc.addEventHandler(GameEvents.SET_COLOR, e ->
                Platform.runLater(() -> updatePlayerData(e.username()))
        );
    }

    public void setGc(ClientGameController gc) {
        this.gc = gc;
    }

    public PlayerManuscript updateAndGetManuscript(String username) {
        updateManuscript(username);
        return getManuscript(username);
    }

    public PlayerManuscript getManuscript(String username) {
        return manuscriptProperty(username).get();
    }

    public ObjectProperty<PlayerManuscript> manuscriptProperty(String username) {
        return manuscriptPropsMap.get(username);
    }

    public List<PlayerManuscript> getManuscripts() {
        return manuscriptPropsMap.values().stream().map(ObjectProperty::get).toList();
    }

    public PlayerData updateAndGetPlayerData(String username) {
        updatePlayerData(username);
        return getPlayerData(username);
    }

    public PlayerData getPlayerData(String username) {
        return new PlayerData(playerDataProperty(username).get());
    }

    public ObjectProperty<PlayerData> playerDataProperty(String username) {
        return playerDataPropsMap.get(username);
    }

    public List<PlayerData> getPlayersData() {
        return playerDataPropsMap.values().stream().map(ObjectProperty::get).toList();
    }

    public void setPlayerData(String username, PlayerData playerData) {
        playerDataPropsMap.get(username).set(playerData);
    }


    public PlayerInfo getPlayerInfo(String username) {
        return new PlayerInfo(playerInfoProperty(username).get());
    }

    public void setPlayerInfo(String username, PlayerInfo playerInfo) {
        playerInfoProperty(username).set(playerInfo);
    }

    public ObjectProperty<PlayerInfo> playerInfoProperty(String username) {
        return playerInfoPropsMap.get(username);
    }

    public List<PlayerInfo> getPlayerInfos() {
        return playerInfoPropsMap.values().stream().map(ObjectProperty::get).toList();
    }


    public ObjectProperty<GameFlow> gameFlowProperty() {
        return gameFlow;
    }


    public ObjectProperty<CommonBoard> boardProperty() {
        return board;
    }

    public void setScreenSize(Dimension2D screenSize) {
        this.screenSize = screenSize;
    }

    public Dimension2D getScreenSize() {
        return screenSize;
    }

    public Card2D getSizeFromPercentage(double height) {
        double ratio = height / 1080.0;

        return new Card2D(getRatio() * height);
    }

    @Override
    public double getHeightFromPercentage(double height) {
        return getRatio() * height;
    }

    public double getRatio() {
        return min(screenSize.getHeight() / 1080.0, screenSize.getWidth() / 1920.0);
    }

    private double getRatio(Dimension2D screenSize) {
        return min(screenSize.getHeight() / 1080.0, screenSize.getWidth() / 1920.0);
    }

    public DoubleProperty ratioProperty() {
        return ratio;
    }

    public Dimension2D getScreen() {
        return screen.get();
    }

    public ObjectProperty<Dimension2D> screenProperty() {
        return screen;
    }

    public GameFlow getGameFlow() {
        return gameFlow.get();
    }

    public CommonBoard getBoard() {
        return board.get();
    }

    public PlayerDataSource getDataController(String currUsername) {
        return new PlayerDataSource(this, currUsername);
    }


    public void updateManuscript(String username) {
        manuscriptPropsMap.get(username).set(
                new PlayerManuscript(gc.getManuscript(username))
        );
    }

    public void updateManuscripts() {
        manuscriptPropsMap.keySet().forEach(this::updateManuscript);
    }

    public void updatePlayerData(String username) {
        playerDataPropsMap.get(username).set(
                new PlayerData(gc.getPlayerData(username))
        );
    }

    public void updatePlayersData() {
        playerDataPropsMap.keySet().forEach(this::updatePlayerData);

    }

    public void updateBoard() {
        board.set(gc.getGameBoard());
    }

    public void updateGameFlow() {
        gameFlow.set(new GameFlow(gc.getGameFlow()));
        System.out.println("----- set game flow" + gameFlow.get().getCurrentPlayer());
    }

    public ClientGameController gc() {
        return gc;
    }

    public String getMyUsername() {
        return gc.getMyUsername();
    }

    public boolean isSettingDone(String username) {
        PlayerData playerData = getPlayerData(username);
        PlayerManuscript manuscript = getManuscript(username);
        return isSettingDone(playerData, manuscript);
    }

    private static boolean isSettingDone(PlayerData playerData, PlayerManuscript manuscript) {
        return isColorSet(playerData) && isPrivateGoalSet(playerData) && isStarterCardSet(manuscript);
    }

    private static boolean isColorSet(PlayerData playerData) {
        return playerData.getColor() != null;
    }

    private static boolean isPrivateGoalSet(PlayerData playerData) {
        return playerData.getPrivateGoal() != null;
    }

    private static boolean isStarterCardSet(PlayerManuscript manuscript) {
        return !manuscript.isEmpty();
    }

}
