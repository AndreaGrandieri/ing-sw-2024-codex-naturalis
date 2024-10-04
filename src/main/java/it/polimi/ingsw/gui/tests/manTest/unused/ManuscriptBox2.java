package it.polimi.ingsw.gui.tests.manTest.unused;

import it.polimi.ingsw.gui.support.FXUtils;
import it.polimi.ingsw.gui.components.panes.manuscript.ManBackgroundRect;
import it.polimi.ingsw.gui.components.panes.manuscript.ManCardRect;
import it.polimi.ingsw.gui.components.panes.manuscript.ManPlaceholderRect;
import it.polimi.ingsw.gui.components.panes.manuscript.ManuscriptLayoutRectangle;
import it.polimi.ingsw.gui.support.helper.*;
import it.polimi.ingsw.model.card.PlayableCard;
import it.polimi.ingsw.model.player.PlayerManuscript;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.rangeClosed;
import static javafx.beans.binding.Bindings.createObjectBinding;

public class ManuscriptBox2 extends HBox {

    final int MANUSCRIPT_VIEW_ORDER = -1;
    final int PLACEHOLDER_VIEW_ORDER = -2;
    final int BACKGROUND_VIEW_ORDER = 0;

    private final Group group = new Group();
    private final ObservableList<Node> groupChildren = group.getChildren();
    private final Map<IntPoint, ManuscriptLayoutRectangle> manuscriptCardRectMap = new HashMap<>();
    private final Map<IntPoint, ManuscriptLayoutRectangle> placeholdersCardRectMap = new HashMap<>();
    private final Map<IntPoint, ManuscriptLayoutRectangle> backgroundCardRectMap = new HashMap<>();
    private final int prefCardHeight = 80;
    private final ObjectProperty<Card2D> cardSize = new SimpleObjectProperty<>(new Card2D(prefCardHeight));


    public ObjectProperty<PlayerManuscript> manuscriptProperty() {
        return manuscript;
    }

    private final ObjectProperty<PlayerManuscript> manuscript = new SimpleObjectProperty<>();
    private final BackgroundHelper bgHelper;
    private final DoubleProperty ratio = new SimpleDoubleProperty(1);
    private Timeline anim1 = new Timeline();
    private Timeline anim;


    public ObjectProperty<Dimension2D> sizeProperty() {
        return size;
    }

    private final ObjectProperty<Dimension2D> size = new SimpleObjectProperty<>(new Dimension2D(400, 300));

    public ManuscriptBox2() {
        cardSize.bind(createObjectBinding(() -> new Card2D(prefCardHeight * ratio.get()), ratio));
        setBackground(FXUtils.bgColor(Color.RED));

        // bgHelper
        bgHelper = new BackgroundHelper(0, cardSize,
                size
        );
        size.set(new Dimension2D(400, 500));
        changeInnerBoxSize(size.get(), false);


        bgHelper.backgroundBoundsProperty().addListener((observableValue, tableBounds, newTableBounds) -> {
            drawBackground(tableBounds.value(), newTableBounds.value(), newTableBounds.cause() == BackgroundHelper.MANUSCRIPT_CHANGED);
        });


        // manuscript change
        manuscript.addListener((observableValue, manuscript1, newManuscript) -> {
            bgHelper.setManuscriptBounds(new TableBounds(newManuscript));
            drawManuscript(newManuscript);
            drawPlaceholders(newManuscript);
        });

        bgHelper.contentSizeProperty().addListener((observableValue, dimension2D, t1) -> {
        });


        setOpacity(0.9);
        setBackground(FXUtils.bgColor(Color.web("#f3dfa6")));
        setAlignment(Pos.CENTER);

        minHeightProperty().bind(maxHeightProperty());
        minWidthProperty().bind(maxWidthProperty());


        bgHelper.contentSizeProperty().addListener((observableValue, dimension2DPropertyChange, t1) ->
                changeInnerBoxSize(t1.value(), t1.cause() == BackgroundHelper.MANUSCRIPT_CHANGED));


        Rectangle rect = new Rectangle();
        rect.heightProperty().bind(maxHeightProperty());
        setClip(rect);

        rect.widthProperty().bind(maxWidthProperty());

        getChildren().add(group);
    }


    private static void iterateBounds(TableBounds newBounds, Consumer<IntPoint> iterator) {
        rangeClosed(newBounds.getMinCol(), newBounds.getMaxCol()).forEach(x -> {
            rangeClosed(newBounds.getMinRow(), newBounds.getMaxRow()).forEach(y -> {
                if ((y % 2 == 0 && x % 2 == 0) || (y % 2 != 0 && x % 2 != 0)) {
                    iterator.accept(new IntPoint(x, y));
                }
            });
        });
    }

    private static List<KeyValue> prepareForAddRemoveAnimation(TableBoundsDelta delta, IntPoint pos, ManuscriptLayoutRectangle cr) {
        int x = pos.x(), y = pos.y();
        Point2D newPos = pos.toPoint2d();

        List<KeyValue> animValues = new ArrayList<>();

        if (delta.addedCols().contains(x) || delta.addedRows().contains(y)) {
            if (delta.addedCols().contains(x)) {
                newPos = newPos.add(x < 0 ? 1 : 0, 0);
                cr.scaleWidthProperty().set(Card2D.angleWidthToWidthRatio);
                animValues.add(new KeyValue(cr.scaleWidthProperty(), 1));
            }
            if (delta.addedRows().contains(y)) {
                newPos = newPos.add(0, y > 0 ? -1 : 0);
                cr.scaleHeightProperty().set(Card2D.angleHeightToHeightRatio);
                animValues.add(new KeyValue(cr.scaleHeightProperty(), 1));
            }
            cr.setManPosition(newPos);
            animValues.add(new KeyValue(cr.manPositionProperty(), pos.toPoint2d()));
        } else {
            if (delta.removedCols().contains(x)) {
                newPos = newPos.add(x < 0 ? 1 : 0, 0);
                animValues.add(new KeyValue(cr.scaleWidthProperty(), Card2D.angleWidthToWidthRatio));
            }
            if (delta.removedRows().contains(y)) {
                newPos = newPos.add(0, y > 0 ? -1 : 0);
                animValues.add(new KeyValue(cr.scaleHeightProperty(), Card2D.angleHeightToHeightRatio));
            }
            animValues.add(new KeyValue(cr.manPositionProperty(), newPos));
        }

        return animValues;
    }

    private static Map<IntPoint, ManuscriptLayoutRectangle> submapByKeys(Map<IntPoint, ManuscriptLayoutRectangle> map, List<IntPoint> currKeys) {
        return map.entrySet().stream()
                .filter(e -> !currKeys.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void changeInnerBoxSize(Dimension2D newSize, boolean animated) {
        if (animated) {
            anim1 = new Timeline(new KeyFrame(Duration.millis(300),
                    new KeyValue(maxHeightProperty(), newSize.getHeight())
                    , new KeyValue(maxWidthProperty(), newSize.getWidth())
            ));
            anim1.play();
        } else {
            anim1.stop();
            setMaxHeight(newSize.getHeight());
            setMaxWidth(newSize.getWidth());
            setPrefHeight(newSize.getHeight());
            setPrefWidth(newSize.getWidth());
        }
    }

    public DoubleProperty ratioProperty() {
        return ratio;
    }

    public void setManuscript(PlayerManuscript manuscript) {
        this.manuscript.set(new PlayerManuscript(manuscript));
    }

    private void drawManuscript(PlayerManuscript manuscript) {
        manuscript.getAllOccupiedPositions().forEach(manPos -> {

            PlayableCard card = manuscript.getCardAt(manPos);
            IntPoint pos = new IntPoint(manPos);

            if (!manuscriptCardRectMap.containsKey(pos)) {

                ManuscriptLayoutRectangle cr = new ManCardRect(card);
                cr.setViewOrder(MANUSCRIPT_VIEW_ORDER);
                cr.sizeProperty().bind(cardSize);
                cr.setManPosition(pos.toPoint2d());

                groupChildren.add(cr);
                manuscriptCardRectMap.put(pos, cr);
            }
        });
    }

    private void drawPlaceholders(PlayerManuscript manuscript) {
        List<IntPoint> currKeys = new ArrayList<>();
        manuscript.getAllAvailablePositions().forEach(manPos -> {

            IntPoint pos = new IntPoint(manPos);
            currKeys.add(pos);


            if (!placeholdersCardRectMap.containsKey(pos)) {

                ManuscriptLayoutRectangle cr = placeholderRect();
                cr.setOnMouseClicked(mouseEvent -> clickedPlaceholder.set(pos));
                cr.setManPosition(pos.toPoint2d());

                groupChildren.add(cr);
                placeholdersCardRectMap.put(pos, cr);
            }
        });

        List<ManuscriptLayoutRectangle> remRect = placeholdersCardRectMap.entrySet().stream()
                .filter(e -> !currKeys.contains(e.getKey()))
                .map(Map.Entry::getValue).toList();

        groupChildren.removeAll(remRect);

        placeholdersCardRectMap.keySet().retainAll(currKeys);
    }

    private final ObjectProperty<IntPoint> clickedPlaceholder = new SimpleObjectProperty<>(new IntPoint(0, 0));

    public ObjectProperty<IntPoint> clickedPlaceholderProperty() {
        return clickedPlaceholder;
    }

    private ManuscriptLayoutRectangle placeholderRect() {
        ManuscriptLayoutRectangle cr = new ManPlaceholderRect();
        cr.setViewOrder(PLACEHOLDER_VIEW_ORDER);
        cr.sizeProperty().bind(cardSize);
        return cr;
    }

    private void drawBackground(TableBounds oldBounds, TableBounds newBounds, boolean animated) {
        TableBoundsDelta delta = new TableBoundsDelta(oldBounds, newBounds);

        List<KeyValue> animValues = new ArrayList<>();
        List<IntPoint> currKeys = new ArrayList<>();

        // iterate in value positions
        iterateBounds(newBounds, (pos) -> {

            currKeys.add(pos);
            if (!backgroundCardRectMap.containsKey(pos)) {

                ManuscriptLayoutRectangle cr = backgroundRect();

                if (animated) {
                    animValues.addAll(prepareForAddRemoveAnimation(delta, pos, cr));
                } else {
                    cr.setManPosition(pos.toPoint2d());
                }

                group.getChildren().add(cr);
                backgroundCardRectMap.put(pos, cr);
            } else {
                backgroundCardRectMap.get(pos).setManPosition(pos.toPoint2d());

            }
        });

        Map<IntPoint, ManuscriptLayoutRectangle> cardRectToRemovetMap = submapByKeys(backgroundCardRectMap, currKeys);

        if (animated) {
            cardRectToRemovetMap.forEach((pos, rect) ->
                    animValues.addAll(prepareForAddRemoveAnimation(delta, pos, rect))
            );

            anim = new Timeline(new KeyFrame(Duration.millis(300), animValues.toArray(KeyValue[]::new)));

            anim.setOnFinished(actionEvent -> {
                groupChildren.removeAll(cardRectToRemovetMap.values());
            });
            anim.play();
        } else {
            groupChildren.removeAll(cardRectToRemovetMap.values());
            backgroundCardRectMap.keySet().retainAll(currKeys);
        }

        backgroundCardRectMap.keySet().retainAll(currKeys);
    }

    private ManuscriptLayoutRectangle backgroundRect() {
        ManuscriptLayoutRectangle cr = new ManBackgroundRect();
        cr.setViewOrder(BACKGROUND_VIEW_ORDER);
        cr.sizeProperty().bind(cardSize);
        return cr;
    }


}



