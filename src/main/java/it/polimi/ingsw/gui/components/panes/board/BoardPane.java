package it.polimi.ingsw.gui.components.panes.board;

import it.polimi.ingsw.model.player.PlayerColor;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.*;

import static it.polimi.ingsw.gui.components.panes.board.MyColors.MY_LIGHT_BROWN;
import static java.lang.Math.*;
import static java.util.Collections.frequency;
import static java.util.stream.IntStream.range;
import static javafx.beans.binding.Bindings.createDoubleBinding;

public class BoardPane extends StackPane {
    static final int LAYOUT_X = 0;
    static final int LAYOUT_Y = 1;
    static final Color MY_PINK = Color.web("E034F1");
    static final Color MY_YELLOW = Color.web("#ffcd7d");
    static final Color MY_RED = Color.web("F13434");
    static final Color MY_GREEN = Color.web("9AF134");
    static final Color MY_BLUE = Color.web("34AAF1");
    private static final int PION_MOVE_ANIM_DURATION = 300;
    private static final double prefLineWidth = 6;
    public final Dimension2D preferredSize = new Dimension2D(926.3, 200);
    private final Pane pionsPane;
    private final Map<PlayerColor, PionCircle> pionsMap;
    private final DoubleProperty ratio = new SimpleDoubleProperty(0.5);
    private final DoubleProperty lineWidth = new SimpleDoubleProperty(6);
    private final ObjectProperty<Map<PlayerColor, Integer>> points;
    private final int prefStackDeltaX = 9;
    private final List<PlayerColor> playerColorsOrderList;
    private final List<BoardCircle> circList = new ArrayList<>();

    public BoardPane() {
        paddingProperty().bind(Bindings.createObjectBinding(() -> {
            return new Insets(10 * ratio.get());
        }, ratioProperty()));


        GridPane circlesGrid = circlesGrid();

        initCirclesGrid(circlesGrid);

        GridPane vGrid = vGrid();

        GridPane hGrid = hGrid();

        lineWidthProperty().bind(ratioProperty().multiply(prefLineWidth));

        points = new SimpleObjectProperty<>(Map.of(PlayerColor.RED, 0, PlayerColor.YELLOW, 0, PlayerColor.BLUE, 0, PlayerColor.GREEN, 0));
        points.addListener((observableValue, playerColorIntegerMap, t1) -> {
            setPoints(playerColorIntegerMap, t1, true);
            System.out.println("POINTS CHANGE: " + t1 + playerColorIntegerMap);
        });


        pionsMap = Map.of(
                PlayerColor.RED, new PionCircle(20, MY_RED),
                PlayerColor.YELLOW, new PionCircle(20, MY_YELLOW),
                PlayerColor.BLUE, new PionCircle(20, MY_BLUE),
                PlayerColor.GREEN, new PionCircle(20, MY_GREEN)
        );

        playerColorsOrderList = List.of(PlayerColor.RED, PlayerColor.BLUE, PlayerColor.YELLOW, PlayerColor.GREEN);


        range(0, playerColorsOrderList.size()).forEach(i -> {
            PlayerColor color = playerColorsOrderList.get(i);
            pionsMap.get(color).setPos(new PionPos(0, i));
        });


        pionsPane = new Pane(pionsMap.values().toArray(PionCircle[]::new));


        pionsMap.values().forEach(pionCircle -> {

            pionCircle.radiusProperty().bind(boardInnerCircle(0).radiusProperty());

            pionCircle.layoutXProperty().bind(pionLayoutBinding(LAYOUT_X, pionCircle.posProperty()));
            pionCircle.layoutYProperty().bind(pionLayoutBinding(LAYOUT_Y, pionCircle.posProperty()));

            pionCircle.setViewOrder(-pionCircle.getPos().stackIndex());
        });


        // In order from bottom to top
        getChildren().addAll(hGrid, vGrid, circlesGrid, pionsPane);
    }

    public void setRatio(double ratio) {
        this.ratio.set(ratio);
    }

    private void initCirclesGrid(GridPane grid1) {
        int cols = 10;
        int rows = 3;
        range(0, cols).forEach(
                col -> range(0, rows).forEach(
                        row -> {

                            int circleNum = col * 3 + row;
                            BoardCircle circle = BoardCircle.boardCircleByNum(circleNum);
                            circle.ratioProperty().bind(ratioProperty());
                            circList.add(circle);
                            Pane box1 = circle.stackPane();

                            int numRow = (col % 2 == 1) ? row : (2 - row);
                            int numCol = cols - 1 - col;
                            grid1.add(box1, numCol, numRow);

                        }
                )
        );
    }

    private Map<PlayerColor, Integer> newPionsViewOrderList(PlayerColor newFirstPionColor) {

        List<Integer> list = new ArrayList<>(playerColorsOrderList.stream().map(pionsMap::get).map(Node::getViewOrder).map(Double::intValue).toList());
        list.set(playerColorsOrderList.indexOf(newFirstPionColor), 3);

        for (int i = -3; i < 0; i++) {
            int index = list.indexOf(i);
            if (index < 0) break;
            list.set(index, -(list.get(index) + 1));
        }

        List<Integer> finalList = list.stream().map(l -> min(-l, l)).toList();

        Map<PlayerColor, Integer> viewOrderMap = new HashMap<>();
        range(0, playerColorsOrderList.size()).forEach(i -> {
            viewOrderMap.put(playerColorsOrderList.get(i), finalList.get(i));
        });

        return viewOrderMap;
    }

    private DoubleProperty lineWidthProperty() {
        return lineWidth;
    }

    public DoubleProperty ratioProperty() {
        return ratio;
    }

    private Circle boardInnerCircle(int i) {
        return circList.get(i).innerCircle();
    }

    public void simulate() {

        new Thread(() ->
                range(0, 1000).forEach(i -> {

                    int pionIndex = i % 4;
                    PlayerColor color = playerColorsOrderList.get(pionIndex);

                    PionPos currPionPos1 = pionsMap.get(color).getPos();
                    int oldNum1 = (int) currPionPos1.num();

                    int toNum = oldNum1 + (int) (random() * 3);

                    if (toNum != oldNum1) {
                        Map<PlayerColor, Integer> newPoints = new HashMap<>(Map.copyOf(points.get()));
                        newPoints.replace(color, toNum);
                        points.set(newPoints);

                        try {
                            Thread.sleep(PION_MOVE_ANIM_DURATION + 100);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }

                })
        ).start();
    }

    public void setPoints(Map<PlayerColor, Integer> oldPoints, Map<PlayerColor, Integer> newPoints, boolean animated) {
        List<Integer> pionsNumList = new ArrayList<>();

        System.out.println(newPoints);
        newPoints.forEach((color, newNum) -> {

            int oldNum = 0;
            if (oldPoints != null && oldPoints.get(color) != null) {
                oldNum = oldPoints.get(color);
            }
            if (newNum != oldNum) {

                if (animated) {
                    Animation animation = animationToNum(newNum, color);
                    animation.play();
                } else {

                    int pionsOnNewNum = frequency(pionsNumList, newNum);
                    pionsNumList.add(newNum);

                    pionsMap.get(color).setPos(
                            new PionPos(newNum, pionsOnNewNum)
                    );
                }
            }




        });

    }


    private Animation animationToNum(int toNum, PlayerColor color) {

        PionPos currPionPos1 = pionsMap.get(color).getPos();
        int oldNum1 = (int) currPionPos1.num();

        int deltaNum = toNum - oldNum1;
        System.out.println("DELTAPOINT: " + deltaNum);


        List<Animation> animations = new ArrayList<>();

        // animate pion move one step at a time
        range(0, deltaNum).forEach(i1 -> {
            PionPos currPionPos = pionsMap.get(color).getPos();
            double oldNum = currPionPos.num();

            List<Animation> stepAnimations = new ArrayList<>();

            // if pion doesn't move no viewOrder change, if it moves change viewOrder only first time
            if (i1 == 0) {
                Map<PlayerColor, Integer> viewOrderList = newPionsViewOrderList(color);
                stepAnimations.add(
                        animationUpdateViewOrder(viewOrderList)
                );
            }

            double newNum = (oldNum + i1 + 1) % 30;

            List<Double> pionsNumList = pionsMap.values().stream().map(p -> p.getPos().num()).toList();

            int pionsOnOldNum = frequency(pionsNumList, oldNum);

            // if there are other pions from itself in older num, change stackIndexes
            if (pionsOnOldNum > 1) {
                stepAnimations.addAll(animationsFromStackIndexChange(currPionPos));
            }

            PionPos newPionPos = getNewPionPos(newNum);
            stepAnimations.add(
                    animationPionToPos(newPionPos, color)
            );

            ParallelTransition e = new ParallelTransition(stepAnimations.toArray(Animation[]::new));
            animations.add(e);
        });

        return new SequentialTransition(animations.toArray(Animation[]::new));
    }

    private PionPos getNewPionPos(double newNum) {
        List<Double> pionsNumList = pionsMap.values().stream().map(p -> p.getPos().num()).toList();

        int pionsOnNewNum = frequency(pionsNumList, newNum);

        return new PionPos(newNum, pionsOnNewNum);
    }

    private Timeline animationUpdateViewOrder(Map<PlayerColor, Integer> viewOrderMap) {
        EventHandler<ActionEvent> updateOrder = actionEvent ->
                viewOrderMap.forEach((c, i) ->
                        pionsMap.get(c).setViewOrder(i)
                );

        return new Timeline(new KeyFrame(Duration.millis(PION_MOVE_ANIM_DURATION * 0.5), updateOrder));
    }

    // Create animations of pion stackIndex change on pions with same number of the moving pion
    private List<Timeline> animationsFromStackIndexChange(PionPos currPionPos) {

        double oldNum = currPionPos.num();
        return pionsMap.keySet().stream().map(c -> {

            PionPos pionPos = pionsMap.get(c).getPos();
            if (pionPos.num() == oldNum && pionPos.stackIndex() > currPionPos.stackIndex()) {
                return animationPionToPos(new PionPos(pionPos.num(), pionPos.stackIndex() - 1), c);
            }
            return null;
        }).filter(Objects::nonNull).toList();
    }

    private void waitAnim(Animation a) {
        Object lock = new Object();
        a.setOnFinished(actionEvent -> {
            synchronized (lock) {
                lock.notifyAll();
            }
        });
        a.play();
        synchronized (lock) {
            try {
                lock.wait();
                //                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private Timeline animationPionToPos(PionPos pos, PlayerColor color) {

        return new Timeline(
                new KeyFrame(Duration.millis(PION_MOVE_ANIM_DURATION)
                        , new KeyValue(pionsMap.get(color).posProperty(), pos)
                )
        );
    }


    private Point2D boardInnerCirclePosOnHover(int num) {
        Circle innerCircle = boardInnerCircle(num);

        return pionsPane.sceneToLocal(innerCircle.localToScene(0, 0));
    }

    private DoubleBinding pionLayoutBinding(int layoutXY, ObjectProperty<PionPos> pionProp) {
        return createDoubleBinding(() -> {
            PionPos pionPos = pionProp.get();
            double num = pionPos.num();
            double stack = pionPos.stackIndex();

            int numStart = (int) num;
            int numEnd = (int) ceil(num);
            double deltaNum = num - numStart;

            Point2D posStart = boardInnerCirclePosOnHover(numStart);
            Point2D posEnd = boardInnerCirclePosOnHover(numEnd);


            double deltaX = stack * prefStackDeltaX * ratio.get();
            posStart = posStart.add(deltaX, 0);
            posEnd = posEnd.add(deltaX, 0);


            Point2D pos = posStart.interpolate(posEnd, deltaNum);


            return (layoutXY == LAYOUT_Y) ? pos.getY() : pos.getX();
        }, (layoutXY == LAYOUT_Y) ? heightProperty() : widthProperty(), ratio, pionProp);
    }


    private GridPane hGrid() {
        GridPane hGrid = new GridPane();

        // cols
        int numCols = 11;
        range(0, numCols).forEach(i -> {
            ColumnConstraints c = new ColumnConstraints();
            c.setHgrow(Priority.ALWAYS);
            c.setHalignment(HPos.CENTER);

            if (i == 0 || i == numCols - 1) {
                c.setPercentWidth(5);
            }

            hGrid.getColumnConstraints().add(c);
        });


        // rows
        range(0, 3).forEach(i -> {
            RowConstraints r = new RowConstraints();
            r.setVgrow(Priority.ALWAYS);
            r.setValignment(VPos.CENTER);

            hGrid.getRowConstraints().add(r);
        });


        range(1, numCols - 1).forEach(i -> {
            HBox hLine1 = hLine();
            hGrid.add(hLine1, i, ((i + 1) % 2) * 2);
        });
        return hGrid;
    }

    private GridPane vGrid() {
        GridPane vGrid;
        vGrid = new GridPane();
        // cols
        int vCols = 10;
        range(0, vCols).forEach(i -> vGrid.getColumnConstraints().add(
                new ColumnConstraints(-1, -1, -1, Priority.ALWAYS, HPos.CENTER, true)
        ));
        // rows
        int vRows = 3;
        List<RowConstraints> vRowsList = range(0, vRows).boxed().map(i -> {
            if (i == 0 || i == vRows - 1) {
                RowConstraints r = new RowConstraints();
                r.setPercentHeight(15);
                return r;
            }
            return new RowConstraints(-1, -1, -1, Priority.ALWAYS, VPos.CENTER, true);
        }).toList();
        vGrid.getRowConstraints().addAll(vRowsList);

        range(0, vCols).forEach(i -> {
            VBox vLine = vLine();
            vGrid.add(vLine, i, 1);
        });
        return vGrid;
    }

    private GridPane circlesGrid() {
        GridPane grid1 = new GridPane();
        // cols
        range(0, 10).forEach(i -> grid1.getColumnConstraints().add(
                new ColumnConstraints(-1, -1, -1, Priority.ALWAYS, HPos.CENTER, true)
        ));
        // rows
        range(0, 3).forEach(i -> grid1.getRowConstraints().add(
                new RowConstraints(-1, -1, -1, Priority.ALWAYS, VPos.CENTER, true)
        ));
        return grid1;
    }

    private HBox hLine() {
        HBox hLine = new HBox();
        hLine.setBackground(bgColor(MY_LIGHT_BROWN));
        hLine.maxHeightProperty().bind(lineWidthProperty());
        return hLine;
    }

    private VBox vLine() {
        VBox vLine = new VBox();
        vLine.setBackground(bgColor(MY_LIGHT_BROWN));

        vLine.maxWidthProperty().bind(lineWidthProperty());

        return vLine;
    }

    private Background bgColor(Color c) {
        return new Background(new BackgroundFill(c, null, null));
    }

    public Map<PlayerColor, Integer> getPoints() {
        return points.get();
    }

    public ObjectProperty<Map<PlayerColor, Integer>> pointsProperty() {
        return points;
    }
}

