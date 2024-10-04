package it.polimi.ingsw.gui.components.panes.unused;

import it.polimi.ingsw.gui.support.Util;
import it.polimi.ingsw.gui.components.panes.card.CardRect;
import it.polimi.ingsw.gui.support.context.GameContext;
import it.polimi.ingsw.gui.support.interfaces.CardSizeController;
import it.polimi.ingsw.gui.support.interfaces.ResponsiveContainer;
import it.polimi.ingsw.model.card.Card;
import it.polimi.ingsw.model.game.CommonBoard;
import it.polimi.ingsw.model.player.PlayerColor;
import it.polimi.ingsw.model.player.PlayerData;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoalsPane2 extends VBox implements ResponsiveContainer {
    private final Map<PlayerColor, ImageView> pionViewsMap;
    private final List<CardRect> cardPaneList;
    private final List<CardRect> goalsPaneList;
    private final double stepXRatio = 51.8;
    private final double stepYRatio = 37.4;
    private final double boardHWRatio = 1056.0 / 228.0;
    private final double boardHeight = 111;
    private final double pionHeight = 30;
    public ImageView yellowView;
    public ImageView redView;
    public ImageView greenView;
    public ImageView blueView;
    public Pane pane;
    public CardRect goalPane1;
    public CardRect goalPane2;
    public CardRect personalGoalPane;
    public ImageView boardView;
    public GridPane grid;
    private Map<PlayerColor, Integer> points;
    private GameContext dataCtx;
    private double currBoardHeight = 111;
    private final double currBoardWidth = 500;

    private CardSizeController cctrl;

    public GoalsPane2() {
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
    }

    private void updatePointsBool(boolean animated) {
        if (animated) {
            for (PlayerData pd : dataCtx.getPlayersData()) {
                PlayerColor playerColor = pd.getColor();
                animateViewToPoints(points.get(playerColor), pd.getPoints(), pionViewsMap.get(playerColor));
            }
        } else {
            Util.runThread(() -> {
                for (PlayerData pd : dataCtx.getPlayersData()) {
                    ImageView view = pionViewsMap.get(pd.getColor());
                    Point2D pos = positionFromPoints(pd.getPoints());

                    Util.runWaitUI(() -> {
                        view.setLayoutX(pos.getX());
                        view.setLayoutY(pos.getY());
                    });
                }
            });
        }
    }

    private void updatePointsBool2(boolean animated) {
        Map<PlayerColor, Integer> newPoints = new HashMap<>();
        dataCtx.getPlayersData().forEach(pd -> newPoints.put(pd.getColor(), pd.getPoints()));

        if (!newPoints.equals(points)) {

            Util.runThread(() -> {

                for (PlayerColor color : PlayerColor.values()) {
                    if (!newPoints.get(color).equals(points.get(color))) {

                        if (animated) {
                            animateViewToPoints(points.get(color), newPoints.get(color), pionViewsMap.get(color));
                        } else {
                            ImageView view = pionViewsMap.get(color);
                            Point2D pos = positionFromPoints(newPoints.get(color));

                            Util.runWaitUI(() -> {
                                view.setLayoutX(pos.getX());
                                view.setLayoutY(pos.getY());
                            });
                        }
                    }
                }
                points.clear();
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

    public void setVars(List<PlayerData> players, CommonBoard board) {
        Map<PlayerColor, Integer> points = new HashMap<>();
        for (PlayerData p : players) points.put(p.getColor(), p.getPoints());
        setPoints(points, false);
        setGoals(board.getCommonGoals());
        personalGoalPane.cardProperty().set(players.get(0).getPrivateGoal());

    }

    public void setGoals(List<? extends Card> goals) {
        for (int i = 0; i < goals.size(); i++) goalsPaneList.get(i).cardProperty().set(goals.get(i));
    }

    private void setPoints(Map<PlayerColor, Integer> newPoints, Boolean animated) {

        if (animated) {
            for (PlayerColor color : PlayerColor.values()) {
                animateViewToPoints(points.get(color), newPoints.get(color), pionViewsMap.get(color));
            }
        } else {
            Util.runThread(() -> {
                for (PlayerColor color : PlayerColor.values()) {
                    ImageView view = pionViewsMap.get(color);
                    Point2D pos = positionFromPoints(newPoints.get(color));

                    Util.runWaitUI(() -> {
                        view.setLayoutX(pos.getX());
                        view.setLayoutY(pos.getY());
                    });
                }
            });
        }
        points = newPoints;
    }

    public void updateWithPlayer(PlayerData player) {
        new Thread(() -> {
            PlayerColor color = player.getColor();
            int newPoints = player.getPoints();
            animateViewToPoints(points.get(color), newPoints, pionViewsMap.get(color));
            points.put(color, newPoints);
        }).start();
    }

    private void tempPoisitions() {
        pionViewsMap.values().forEach(p -> p.relocate(0, 0));

    }


    private Point2D positionFromPoints(double point) {

        double currBoardRatio = currBoardHeight / boardHeight;

        double stepX = stepXRatio * currBoardRatio;
        double stepY = stepYRatio * currBoardRatio;

        int rows = 3;

        //Zero position
        double layoutX = stepX * 9;
        double layoutY = stepY * 2;

        double pNorm = point % 30;

        //p < 21
        double p1 = Math.min(20, pNorm);

        double stepsX = Math.floor(p1 / rows);
        double stepsY = p1 % rows;

        layoutX = layoutX - stepsX * stepX;
        layoutY = layoutY - (stepsX % 2 == 1 ? (2 - stepsY) : stepsY) * stepY;

        //p < 24
        if (pNorm > 20) {
            double p2 = Math.min(23, pNorm);
            stepsX = (p2 % rows) + 1;

            layoutX = layoutX - stepsX * stepX;
        }

        //p < 24
        if (pNorm > 23) {
            double p3 = Math.min(25, pNorm);
            stepsY = (p3 % rows) + 1;

            layoutY = layoutY + stepsY * stepY;
        }

        if (pNorm > 25) {
            double p4 = Math.min(27, pNorm);
            stepsX = (p4 - 1) % rows;

            layoutX = layoutX + stepsX * stepX;
        }

        if (pNorm > 27) layoutY = layoutY - stepY;

        if (pNorm > 28) layoutX = layoutX - stepX;

        return new Point2D(layoutX, layoutY);
    }

    private void animateViewToPoints(double fromPoint, double toPoint, ImageView view) {
        double time = 200;
        Timeline t = new Timeline();
        t.setOnFinished(actionEvent -> view.setViewOrder(0));


        for (double p = fromPoint; p <= toPoint; p++) {

            Point2D pos = positionFromPoints(p);
            double layoutX = pos.getX();
            double layoutY = pos.getY();

            t.getKeyFrames().add(
                    new KeyFrame(Duration.millis(time * (p - fromPoint)),
                            new KeyValue(view.layoutXProperty(), layoutX, Interpolator.EASE_BOTH),
                            new KeyValue(view.layoutYProperty(), layoutY, Interpolator.EASE_BOTH)
                    )
            );
        }

        Util.runWaitUI(() -> {
            view.setViewOrder(-1);
            t.play();
        });
        Util.sleep((long) (time * (toPoint - fromPoint)));

    }

    public void setCardSizeController(CardSizeController cctrl) {
        this.cctrl = cctrl;
    }

    public void updatePanes() {
        currBoardHeight = cctrl.getHeightFromPercentage(boardHeight);
        boardView.setFitHeight(currBoardHeight);
        pane.setMaxHeight(currBoardHeight);
        pane.setMaxWidth(currBoardHeight * boardHWRatio);
        grid.setMaxHeight(currBoardHeight);
        grid.setMaxWidth(currBoardHeight * boardHWRatio);


        setPoints(points, false);


        updatePionsHeight(cctrl.getHeightFromPercentage(pionHeight));
    }

    private void updatePionsHeight(double newHeight) {
        pionViewsMap.values().forEach(p -> p.setFitHeight(newHeight));
    }

}
