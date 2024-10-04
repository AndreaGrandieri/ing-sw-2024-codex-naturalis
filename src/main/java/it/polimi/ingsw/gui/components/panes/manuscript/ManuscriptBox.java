package it.polimi.ingsw.gui.components.panes.manuscript;

import it.polimi.ingsw.gui.support.FXUtils;
import it.polimi.ingsw.gui.support.Util;
import it.polimi.ingsw.gui.support.helper.*;
import it.polimi.ingsw.gui.support.interfaces.ManuscriptController;
import it.polimi.ingsw.gui.support.info.CardPaneInfo;
import it.polimi.ingsw.gui.support.helper.GuiGameState;
import it.polimi.ingsw.model.card.PlayableCard;
import it.polimi.ingsw.model.player.ManuscriptPosition;
import it.polimi.ingsw.model.player.PlayerManuscript;
import javafx.animation.*;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.stream.IntStream.rangeClosed;
import static javafx.beans.binding.Bindings.createObjectBinding;

public class ManuscriptBox extends ScrollPane implements ManuscriptController {
    public final static int MANUSCRIPT_UPDATE = 1;
    public final static int MANUSCRIPT_REDRAW = 0;
    private static final int MANUSCRIPT_VIEW_ORDER = -1;
    private static final int PLACEHOLDER_VIEW_ORDER = -2;
    private static final int BACKGROUND_VIEW_ORDER = 0;
    private static final double prefPlaceholderOpacity = 0.5;
    private final Group group = new Group();
    private final ObservableList<Node> groupChildren = group.getChildren();

    private final Map<IntPoint, ManuscriptLayoutRectangle> manuscriptCardRectMap = new HashMap<>();
    private final Map<IntPoint, ManuscriptLayoutRectangle> placeholdersCardRectMap = new HashMap<>();
    private final Map<IntPoint, ManuscriptLayoutRectangle> backgroundCardRectMap = new HashMap<>();

    private final int prefCardHeight = 80;
    private final BackgroundHelper bgHelper;

    // props
    private final DoubleProperty ratio = new SimpleDoubleProperty(1);
    private final ObjectProperty<Card2D> cardSize = new SimpleObjectProperty<>(new Card2D(prefCardHeight));
    private final ObjectProperty<PC<PlayerManuscript>> manuscript = new SimpleObjectProperty<>();
    private final ObjectProperty<IntPoint> clickedPlaceholder = new SimpleObjectProperty<>(new IntPoint(0, 0));
    private final BooleanProperty showPlaceholders = new SimpleBooleanProperty(true);

    private final HBox contentBox;
    private final List<IntPoint> hiddenPositions = new ArrayList<>();
    private IntPoint placeholderSelectedPos = new IntPoint(0, 0);
    private Timeline contentBoxAnim = new Timeline();
    private Timeline backgroundAnim;
    private final IntPoint focusPoint = new IntPoint(0, 0);

    public ManuscriptBox() {
        cardSize.bind(createObjectBinding(() -> new Card2D(prefCardHeight * ratio.get()), ratio));
        setBackground(FXUtils.bgColor(Color.RED));

        // bgHelper
        bgHelper = new BackgroundHelper(0, cardSize,
                createObjectBinding(() ->
                                new Dimension2D(widthProperty().get(), heightProperty().get()),
                        widthProperty(),
                        heightProperty()
                )
        );


        bgHelper.backgroundBoundsProperty().addListener((observableValue, tableBounds, newTableBounds) -> {
            drawBackground(tableBounds.value(), newTableBounds.value(), newTableBounds.cause() == BackgroundHelper.MANUSCRIPT_CHANGED);
        });


        // manuscript change
        manuscript.addListener((observableValue, manuscript1, newManuscript) -> {
            bgHelper.setManuscriptBounds(new TableBounds(newManuscript.value()));

            drawManuscript(newManuscript.value(), newManuscript.cause() == MANUSCRIPT_UPDATE);
            drawPlaceholders(newManuscript.value());
        });

        showPlaceholders.addListener((observableValue, aBoolean, t1) -> {
            showPlaceholders(t1, true);
        });

        bgHelper.contentSizeProperty().addListener((observableValue, dimension2D, t1) -> {
        });


        contentBox = new HBox(group);
        contentBox.setOpacity(0.9);
        contentBox.setBackground(FXUtils.bgColor(Color.web("#f3dfa6")));
        contentBox.setAlignment(Pos.CENTER);

        contentBox.minHeightProperty().bind(contentBox.maxHeightProperty());
        contentBox.minWidthProperty().bind(contentBox.maxWidthProperty());


        bgHelper.contentSizeProperty().addListener((observableValue, dimension2DPropertyChange, t1) ->
                changeInnerBoxSize(t1.value(), t1.cause() == BackgroundHelper.MANUSCRIPT_CHANGED));

        addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeys);

        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.NEVER);
        getStyleClass().add("edge-to-edge");
        setContent(contentBox);
        setPannable(true);
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

    private static Animation cardPlaceAnim(ManuscriptLayoutRectangle cr) {
        int strokeWidth = 5;
        cr.setStrokeWidth(strokeWidth);
        int duration = 150;

        Timeline enterAnim = new Timeline(
                new KeyFrame(Duration.millis(duration),
                        new KeyValue(cr.strokeWidthProperty(), strokeWidth))
                , new KeyFrame(Duration.millis(2 * duration),
                new KeyValue(cr.strokeWidthProperty(), 0))
        );
        return new SequentialTransition(enterAnim);
    }

    public void setManuscript(PC<PlayerManuscript> manuscript) {
        this.manuscript.set(new PC<>(new PlayerManuscript(manuscript.value()), manuscript.cause()));
    }

    public ObjectProperty<PC<PlayerManuscript>> manuscriptProperty() {
        return manuscript;
    }

    // Animations
    public DoubleProperty ratioProperty() {
        return ratio;
    }

    private List<KeyValue> prepareForAddRemoveAnimation(TableBoundsDelta delta, IntPoint pos, ManuscriptLayoutRectangle cr, ScrollPane sc) {
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

    private Animation scrollFocusAnim(ManuscriptLayoutRectangle cr, int duration) {
        Point2D value = scrollValueFromRect(cr);

        double vvalue = getVvalue();
        double hvalue = getHvalue();
        if (vvalue != value.getX() || hvalue != value.getY()) {
            return new Timeline(new KeyFrame(Duration.millis(duration),
                    new KeyValue(vvalueProperty(), value.getY()),
                    new KeyValue(hvalueProperty(), value.getX())
            ));
        }
        return new Timeline();
    }

    private void changeInnerBoxSize(Dimension2D newSize, boolean animated) {
        if (animated) {
            contentBoxAnim = new Timeline(new KeyFrame(Duration.millis(300),
                    new KeyValue(contentBox.maxHeightProperty(), newSize.getHeight())
                    , new KeyValue(contentBox.maxWidthProperty(), newSize.getWidth())
            ));
            contentBoxAnim.play();
        } else {
            contentBoxAnim.stop();
            contentBox.setMaxHeight(newSize.getHeight());
            contentBox.setMaxWidth(newSize.getWidth());
            contentBox.setPrefHeight(newSize.getHeight());
            contentBox.setPrefWidth(newSize.getWidth());
        }
    }

    // Draw manuscript
    private void drawManuscript(PlayerManuscript manuscript, boolean animated) {
        manuscript.getAllOccupiedPositions().forEach(manPos -> {

            PlayableCard card = manuscript.getCardAt(manPos);
            IntPoint pos = new IntPoint(manPos);

            if (!manuscriptCardRectMap.containsKey(pos)) {

                ManuscriptLayoutRectangle cr = cardRect(card);
                cr.setManPosition(pos.toPoint2d());

                groupChildren.add(cr);
                manuscriptCardRectMap.put(pos, cr);

                cr.setStroke(Color.YELLOW);
                cr.setStrokeWidth(0);

                if (animated)
                    (new SequentialTransition(scrollFocusAnim(cr, 300), cardPlaceAnim(cr))).play();

                if (hiddenPositions.contains(pos)) cr.setVisible(false);

            }
        });
    }

    // Draw placeholders
    private void showPlaceholders(boolean show, boolean animated) {
        if (animated) {
            (new Timeline(new KeyFrame(Duration.millis(300),
                    placeholdersCardRectMap.values().stream()
                            .map(cr -> new KeyValue(cr.opacityProperty(), show ? prefPlaceholderOpacity : 0))
                            .toArray(KeyValue[]::new)
            ))).play();

        } else
            placeholdersCardRectMap.values().forEach(cr -> cr.setOpacity(show ? prefPlaceholderOpacity : 0));
    }

    private void drawPlaceholders(PlayerManuscript manuscript) {
        List<IntPoint> currKeys = new ArrayList<>();
        manuscript.getAllAvailablePositions().forEach(manPos -> {

            IntPoint pos = new IntPoint(manPos);
            currKeys.add(pos);


            if (!placeholdersCardRectMap.containsKey(pos)) {

                ManuscriptLayoutRectangle cr = placeholderRect();
                cr.setOnMouseClicked(mouseEvent -> {
                    clickedPlaceholder.set(pos);
                });
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

    private Point2D scrollValueFromRect(ManuscriptLayoutRectangle cr) {
        Point2D pos2 = contentBox.sceneToLocal(cr.localToScene(0, 0));

        double y = pos2.getY(), x = pos2.getX();
        double contHeight = contentBox.getHeight(), contWidth = contentBox.getWidth();
        double vHeight = getHeight(), vWidth = getWidth();

        double vvalue = 0;
        double hvalue = 0;
        if (contHeight > vHeight) vvalue = (y - 0.5 * vHeight) / (contHeight - vHeight);
        if (contWidth > vWidth) hvalue = (x - 0.5 * vWidth) / (contWidth - vWidth);

        return new Point2D(max(min(hvalue, 1), 0), min(max(vvalue, 0), 1));
    }

    public ObjectProperty<IntPoint> clickedPlaceholderProperty() {
        return clickedPlaceholder;
    }

    // Draw background

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
                    animValues.addAll(prepareForAddRemoveAnimation(delta, pos, cr, this));
                } else {
                    cr.setManPosition(pos.toPoint2d());
                }

                group.getChildren().add(cr);
                backgroundCardRectMap.put(pos, cr);
            } else {
                backgroundCardRectMap.get(pos).setManPosition(pos.toPoint2d());

            }
        });

        Map<IntPoint, ManuscriptLayoutRectangle> cardRectToRemovetMap = Util.filterMapByKeys(backgroundCardRectMap, currKeys);

        if (animated) {
            cardRectToRemovetMap.forEach((pos, rect) ->
                    animValues.addAll(prepareForAddRemoveAnimation(delta, pos, rect, this))
            );

            backgroundAnim = new Timeline(new KeyFrame(Duration.millis(300), animValues.toArray(KeyValue[]::new)));

            backgroundAnim.setOnFinished(actionEvent -> {
                groupChildren.removeAll(cardRectToRemovetMap.values());
            });
            backgroundAnim.play();
        } else {
            groupChildren.removeAll(cardRectToRemovetMap.values());
            backgroundCardRectMap.keySet().retainAll(currKeys);
        }

        backgroundCardRectMap.keySet().retainAll(currKeys);
    }

    private ManuscriptLayoutRectangle cardRect(PlayableCard card) {
        ManuscriptLayoutRectangle cr = new ManCardRect(card);
        cr.setViewOrder(MANUSCRIPT_VIEW_ORDER);
        cr.sizeProperty().bind(cardSize);
        return cr;
    }

    private ManuscriptLayoutRectangle placeholderRect() {
        ManuscriptLayoutRectangle cr = new ManPlaceholderRect();
        cr.setViewOrder(PLACEHOLDER_VIEW_ORDER);
        cr.setOpacity(showPlaceholders.get() ? prefPlaceholderOpacity : 0);
        cr.sizeProperty().bind(cardSize);
        cr.setStroke(Color.GRAY);
        cr.setStrokeWidth(0);
        return cr;
    }

    private ManuscriptLayoutRectangle backgroundRect() {
        ManuscriptLayoutRectangle cr = new ManBackgroundRect();
        cr.setViewOrder(BACKGROUND_VIEW_ORDER);
        cr.sizeProperty().bind(cardSize);
        return cr;
    }


    private void handleKeys(KeyEvent keyEvent) {
        Set<IntPoint> keys = placeholdersCardRectMap.keySet();
        System.out.println("cr");

        IntPoint newPoint = placeholderSelectedPos;
        IntPoint phPos = placeholderSelectedPos;

        KeyCode keyCode = keyEvent.getCode();
        if (keyCode == KeyCode.ENTER) {
            clickedPlaceholder.set(placeholderSelectedPos);
            return;
        }

        List<KeyCode> keyCodes = List.of(
                KeyCode.RIGHT, KeyCode.LEFT, KeyCode.DOWN, KeyCode.UP
        );
        if (keyCodes.contains(keyCode)) {

            // keyCode == KeyCode.RIGHT
            Predicate<IntPoint> filter = p -> p.x() > phPos.x();
            if (keyCode == KeyCode.LEFT) filter = p -> p.x() < phPos.x();
            if (keyCode == KeyCode.UP) filter = p -> p.y() > phPos.y();
            if (keyCode == KeyCode.DOWN) filter = p -> p.y() < phPos.y();

            newPoint = keys.stream()
                    .filter(filter)
                    .reduce((p1, p2) -> phPos.distance(p1) > phPos.distance(p2) ? p2 : p1)
                    .orElse(phPos);

            ManuscriptLayoutRectangle cr = placeholdersCardRectMap.get(newPoint);
            if (placeholdersCardRectMap.get(phPos) != null) placeholdersCardRectMap.get(phPos).setStrokeWidth(0);
            cr.setStrokeWidth(4);
            placeholderSelectedPos = newPoint;

            (new SequentialTransition(scrollFocusAnim(cr, 100))).play();
        }

        keyEvent.consume();
    }

    @Override
    public CardPaneInfo getCardInfo(ManuscriptPosition mp) {
        IntPoint pos = new IntPoint(mp);
        ManuscriptLayoutRectangle rect = placeholdersCardRectMap.get(pos);
        if (rect == null) rect = manuscriptCardRectMap.get(pos);
        if (rect == null) rect = backgroundCardRectMap.get(pos);
        ManuscriptLayoutRectangle finalRect = rect;
        ObservableValue<Point2D> posInSceneProp = createObjectBinding(
                () -> finalRect.localToScene(0, 0),
                group.layoutXProperty(), group.layoutYProperty()
        );
        return new CardPaneInfo(rect.localToScene(0, 0), cardSize.get(), posInSceneProp);
    }

    @Override
    public void hideCardAtPosition(ManuscriptPosition mp) {
        hiddenPositions.add(new IntPoint(mp));

    }

    @Override
    public void showCardAtPosition(ManuscriptPosition mp) {
        IntPoint pos = new IntPoint(mp);
        if (manuscriptCardRectMap.containsKey(pos)) {
            hiddenPositions.remove(pos);
            manuscriptCardRectMap.get(pos).setVisible(true);
        }
    }

    public void updateManuscript() {

    }

    public void updateBackground() {

    }


    public void setState(GuiGameState state) {
    }

    public Card2D getCardSize() {
        return cardSize.get();
    }

    public ObjectProperty<Card2D> cardSizeProperty() {
        return cardSize;
    }

    public boolean isShowPlaceholders() {
        return showPlaceholders.get();
    }

    public BooleanProperty showPlaceholdersProperty() {
        return showPlaceholders;
    }

    public void setShowPlaceholders(boolean showPlaceholders) {
        this.showPlaceholders.set(showPlaceholders);
    }
}



