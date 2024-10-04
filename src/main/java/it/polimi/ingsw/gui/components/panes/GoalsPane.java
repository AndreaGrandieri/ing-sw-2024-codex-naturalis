package it.polimi.ingsw.gui.components.panes;

import it.polimi.ingsw.gui.components.panes.board.BoardPane;
import it.polimi.ingsw.gui.components.panes.card.CardRect;
import it.polimi.ingsw.gui.support.FXBind;
import it.polimi.ingsw.gui.support.Util;
import it.polimi.ingsw.gui.support.context.GameContext;
import it.polimi.ingsw.gui.support.helper.Card2D;
import it.polimi.ingsw.gui.support.interfaces.CardSizeController;
import it.polimi.ingsw.gui.support.interfaces.ResponsiveContainer;
import it.polimi.ingsw.model.card.Card;
import it.polimi.ingsw.model.game.CommonBoard;
import it.polimi.ingsw.model.player.PlayerColor;
import it.polimi.ingsw.model.player.PlayerData;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javafx.beans.binding.Bindings.createObjectBinding;

public class GoalsPane extends GridPane implements ResponsiveContainer {
    private final Map<PlayerColor, ImageView> pionViewsMap;
    private final List<CardRect> cardPaneList;
    private final List<CardRect> goalsPaneList;
    private final double stepXRatio = 51.8;
    private final double stepYRatio = 37.4;
    private final double boardHWRatio = 1056.0 / 228.0;
    private final double boardHeight = 111;
    private final double pionHeight = 30;
    private final Map<PlayerColor, Integer> points;
    private final double currBoardWidth = 500;
    public ImageView yellowView = new ImageView();
    public ImageView redView = new ImageView();
    public ImageView greenView = new ImageView();
    public ImageView blueView = new ImageView();
    public Pane pane = new Pane();
    public CardRect goalPane1;
    public CardRect goalPane2;
    public CardRect personalGoalPane;
    public ImageView boardView = new ImageView();
    public BoardPane boardPane;
    private GameContext dataCtx;
    private double currBoardHeight = 111;
    private CardSizeController cctrl;

    public GoalsPane() {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("goals-pane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setControllerFactory(type -> this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        goalsPaneList = Arrays.asList(goalPane1, goalPane2);
        cardPaneList = Arrays.asList(goalPane1, goalPane2, personalGoalPane);

        pionViewsMap = new HashMap<>() {{
            put(PlayerColor.GREEN, greenView);
            put(PlayerColor.YELLOW, yellowView);
            put(PlayerColor.RED, redView);
            put(PlayerColor.BLUE, blueView);
        }};

        points = new HashMap<>() {{
            put(PlayerColor.GREEN, 0);
            put(PlayerColor.YELLOW, 0);
            put(PlayerColor.RED, 0);
            put(PlayerColor.BLUE, 0);
        }};

        setPoints(points, false);
    }

    public void setDataCtx(GameContext dataCtx) {
        this.dataCtx = dataCtx;
        setVars();
    }


    private void updatePointsBool2(boolean animated) {
        Map<PlayerColor, Integer> newPoints = new HashMap<>();
        dataCtx.getPlayersData().forEach(pd -> {
            PlayerColor color = pd.getColor();
            int newPoint = pd.getPoints();
            System.out.println(points);
            if (points.get(color) != null && newPoint != points.get(color)) {
                newPoints.put(color, newPoint);
            }
        });

        if (!newPoints.isEmpty()) {

            Util.runThread(() -> {

                setPoints(newPoints, animated);
                points.putAll(newPoints);
            });
        }
    }


    public void updatePoints() {
        updatePointsBool2(true);
    }


    private void simulate() {

        Util.runThread(() -> {
            for (int i = 0; i < 10; i++) {
                Util.sleep(300);
                int finalI = i * 3;
                Map<PlayerColor, Integer> points2 = new HashMap<>() {{
                    put(PlayerColor.GREEN, 10 + finalI);
                    put(PlayerColor.YELLOW, 7 + finalI);
                    put(PlayerColor.RED, 5 + finalI);
                    put(PlayerColor.BLUE, 2 + finalI);
                }};
                setPoints(points2, true);
            }
        });
    }

    public void setVars() {
        pointsProperty().bind(createObjectBinding(
                () -> dataCtx.getGameFlow().getAllUsernames().stream()
                        .map(dataCtx::getPlayerData)
                        .filter(p -> p.getColor() != null).
                        collect(Collectors.toMap(PlayerData::getColor, PlayerData::getPoints)),

                dataCtx.getGameFlow().getAllUsernames().stream().map(dataCtx::playerDataProperty).toArray(Observable[]::new)
        ));

        List<PlayerData> players = dataCtx.getPlayersData();
        CommonBoard board = dataCtx.getBoard();

        Map<PlayerColor, Integer> points = new HashMap<>();
        for (PlayerData p : players) points.put(p.getColor(), p.getPoints());
        setPoints(points, false);
        setGoals(board.getCommonGoals());

        FXBind.subscribe(
                dataCtx.playerDataProperty(dataCtx.getMyUsername()),
                p -> {
                    if (p.getPrivateGoal() != null)
                        personalGoalPane.setCard(p.getPrivateGoal());
                }
        );


    }

    public void setGoals(List<? extends Card> goals) {
        for (int i = 0; i < goals.size(); i++) goalsPaneList.get(i).setCard(goals.get(i));
    }

    private void setPoints(Map<PlayerColor, Integer> newPoints, Boolean animated) {
    }

    public ObjectProperty<Map<PlayerColor, Integer>> pointsProperty() {
        return boardPane.pointsProperty();
    }


    public void setCardSizeController(CardSizeController cctrl) {
        cardPaneList.forEach(cp ->
                cp.sizeProperty().bind(createObjectBinding(
                        () -> new Card2D(cctrl.ratioProperty().get() * 99),
                        cctrl.ratioProperty()
                ))
        );
        boardPane.ratioProperty().bind(cctrl.ratioProperty().multiply(0.6));
        this.cctrl = cctrl;
    }

    public void updatePanes() {
        currBoardHeight = cctrl.getHeightFromPercentage(boardHeight);
        boardView.setFitHeight(currBoardHeight);
        pane.setMaxHeight(currBoardHeight);
        pane.setMaxWidth(currBoardHeight * boardHWRatio);
        setPoints(points, false);

        updatePionsHeight(cctrl.getHeightFromPercentage(pionHeight));
    }

    private void updatePionsHeight(double newHeight) {
        pionViewsMap.values().forEach(p -> p.setFitHeight(newHeight));
    }

}
