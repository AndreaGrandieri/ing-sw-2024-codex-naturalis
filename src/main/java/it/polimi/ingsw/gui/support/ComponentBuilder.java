package it.polimi.ingsw.gui.support;

import it.polimi.ingsw.controller.ChatMessage;
import it.polimi.ingsw.gui.components.panes.card.CardRect;
import it.polimi.ingsw.gui.support.helper.Card2D;
import it.polimi.ingsw.model.card.Card;
import it.polimi.ingsw.model.card.TypedCard;
import it.polimi.ingsw.model.game.gamelobby.LobbyInfo;
import it.polimi.ingsw.model.player.PlayerColor;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;

import static it.polimi.ingsw.gui.support.FXUtils.addStyle;
import static javafx.beans.binding.Bindings.createStringBinding;

public class ComponentBuilder {
    public static Shape rect() {
        return new Rectangle(100, 30);
    }

    // Setup view
    public static CardRect cardRect(Card card, double height) {
        CardRect rect = new CardRect(card);
        rect.setSize(new Card2D(height));
        return rect;
    }

    public static Pane box(Node... children) {
        HBox box = new HBox(children);
        box.getStyleClass().addAll("centered-box", "hoverable");

        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }


    public static Pane colorCircle(PlayerColor color) {
        Circle circle = new Circle(15);
        FXUtils.addStyle(circle, "circle", color.toString());
        circle.setDisable(true);
        Pane box = box(circle);
        box.setFocusTraversable(true);
        FXUtils.addStyle(box, "circle-box");
        enterToClick(box);
        return box;
    }
    // Game view

    public static void removeTabPaneClip(TabPane tabPane, Tab tab) {
        tabPane.getChildrenUnmodifiable().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                Parent parent = tab.getContent().getParent();
                if (parent != null) {
                    tabPane.setClip(null);
                    parent.setClip(null);
                    tabPane.getChildrenUnmodifiable().removeListener(this);
                }
            }
        });
    }

    public static void adjustTabButtonsWidth(TabPane tabPane) {
        tabPane.tabMinWidthProperty().bind(tabPane.widthProperty().divide(tabPane.getTabs().size()).subtract(40));
    }

    public static CardRect newCardRect(TypedCard card, Card2D size, Point2D pos) {
        CardRect animCardPane = new CardRect(card);
        animCardPane.setSize(size);
        animCardPane.setLayoutX(pos.getX());
        animCardPane.setLayoutY(pos.getY());
        return animCardPane;
    }

    // TitlesView

    public static Pane lobbyRow(ObjectProperty<LobbyInfo> info, EventHandler<MouseEvent> handler) {
        Label lobbyNameLabel = new Label();
        lobbyNameLabel.textProperty().bind(createStringBinding(
                () -> info.get().getName(), info
        ));

        Label ongoingLabel = addStyle(new Label(), "lobby-code");
        ongoingLabel.textProperty().bind(createStringBinding(
                () -> info.get().getGameOngoing() ? "In game" : "", info
        ));

        Label lobbyCodeLabel = addStyle(new Label(), "lobby-code");
        lobbyCodeLabel.textProperty().bind(createStringBinding(
                () -> " #" + info.get().getUuid(), info
        ));

        Label playerNumLabel = addStyle(new Label(), "lobby-title");
        playerNumLabel.textProperty().bind(createStringBinding(
                () -> String.valueOf(info.get().getPlayerUsernames().size()), info
        ));

        Label maxPlayersLabel = addStyle(new Label(), "lobby-title");
        maxPlayersLabel.textProperty().bind(createStringBinding(
                () -> String.valueOf(info.get().getMaxPlayers()), info
        ));

        Label playersLabel = addStyle(new Label(), "lobby-title");
        playersLabel.textProperty().bind(createStringBinding(
                () -> {
                    String lobbyPlayers = info.get().getPlayerUsernames().toString();
                    return lobbyPlayers.substring(1, lobbyPlayers.length() - 1);
                }, info
        ));

        HBox titleBox = addStyle(new HBox(
                lobbyNameLabel,
                lobbyCodeLabel,
                ongoingLabel
        ), "lobby-title");
        lobbyCodeLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(lobbyCodeLabel, Priority.ALWAYS);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        VBox mainBox = addStyle(new VBox(
                addStyle(new HBox(
                        titleBox,
                        playerNumLabel,
                        maxPlayersLabel
                ), "lobby-lines"),
                playersLabel
        ), "lobby-row");
        mainBox.setOnMouseClicked(handler);
        enterToClick(mainBox);
        mainBox.setFocusTraversable(true);

        return mainBox;
    }

    public static void enterToClick(Node node) {
        node.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ENTER)) node.fireEvent(newClickedEvent());
        });
    }

    private static MouseEvent newClickedEvent() {
        return new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0,
                0, MouseButton.PRIMARY, 1, false,
                false, false, false, false,
                false, false, false,
                false, false, null);
    }
    // Lobby view

    public static Pane lobbyPlayerRow(ObjectProperty<String> name) {
        ObjectBinding<String> nameFirst = FXBind.map(name, n -> n.substring(0, 1).toUpperCase());
        Pane circle = cirleName(nameFirst, new SimpleDoubleProperty(18));

        Label nameLabel = new Label();
        nameLabel.textProperty().bind(name);
        HBox box = new HBox(
                circle,
                nameLabel
        );
        box.getStyleClass().add("player-row");
        return box;
    }
    // End View

    public static Pane endPlayerRow(String name, String order, String points) {
        Pane circle = cirleName(name);

        Label nameLabel = new Label(name);

        HBox nameBox = addStyle(new HBox(
                circle,
                nameLabel
        ), "player-row");

        HBox.setHgrow(nameBox, Priority.ALWAYS);

        return FXUtils.addStyle(new HBox(
                FXUtils.addStyle(new Label(order), "lobby-title", "fixed-width"),
                nameBox,
                FXUtils.addStyle(new Label(points), "lobby-title", "fixed-width")
        ), "lobby-lines");
    }

    // Paths
    //      three angle rect path, r round
    //      r     r

    //      r     -
    public static Path threeRoundPath(double radius, Dimension2D size) {
        double totHeight = size.getHeight();
        double totWidth = size.getWidth();
        Path path = new Path(
                new MoveTo(0, radius),
                new ArcTo(radius, radius, 0, radius, 0, false, true),
                new HLineTo(totWidth - radius),
                new ArcTo(radius, radius, 0, totWidth, radius, false, true),
                new VLineTo(totHeight),
                new HLineTo(radius),
                new ArcTo(radius, radius, 0, 0, totHeight - radius, false, true),
                new VLineTo(radius)
        );
        path.setFill(Color.WHITE);
        return path;
    }
    //      one angle rect path
    //      -     -

    //      r     -

    public static Path oneRoundPath(double radius, Dimension2D size) {
        double totHeight = size.getHeight();
        double totWidth = size.getWidth();
        Path path = new Path(
                new MoveTo(0, 0),
                new HLineTo(totWidth),
                new VLineTo(totHeight),
                new HLineTo(radius),
                new ArcTo(radius, radius, 0, 0, totHeight - radius, false, true),
                new VLineTo(0)
        );
        path.setFill(Color.WHITE);
        return path;
    }
    // ChatView

    public static Pane messageMine(ChatMessage message) {
        Label label = new Label(message.message());
        label.getStyleClass().add("message");
        label.setWrapText(true);

        HBox box = new HBox(label);
        box.getStyleClass().add("message-mine");
        return box;
    }

    public static Pane messageOther(ChatMessage message, boolean showSender) {
        Pane circle = cirleName(message.sender());
        circle.setVisible(showSender);

        Label label = new Label(message.message());
        label.getStyleClass().add("message");
        label.setWrapText(true);

        HBox box = new HBox(circle, label);
        box.getStyleClass().add("message-other");
        return box;
    }

    public static Pane cirleName(String name) {
        Circle circle = new Circle(15);
        circle.getStyleClass().add("circle");

        Label label = new Label(name.substring(0, 1).toUpperCase());

        return new StackPane(circle, label);
    }

    public static Pane cirleName(ObservableValue<String> str, DoubleProperty unit) {
        Circle circle = new Circle();
        circle.radiusProperty().bind(unit.multiply(13.0 / 18.0));
        circle.getStyleClass().add("circle");

        Label label = new Label();
        label.textProperty().bind(str);
        label.setStyle("-fx-font-size: 0.6em");

        return new StackPane(circle, label);
    }

    public static Pane vSpacer() {
        HBox box = new HBox();
        box.setPrefHeight(15);
        return box;
    }
    // Common
    // speed up scrollPane

    public static void speedUpScroll(ScrollPane scrollPane) {
        final double SPEED = 0.001;
        scrollPane.getContent().setOnScroll(scrollEvent -> {
            double deltaY = scrollEvent.getDeltaY() * SPEED;
            scrollPane.setVvalue(scrollPane.getVvalue() - deltaY);
        });
    }

    public static Stage customStage(Stage stage, Point2D p) {
        stage.setTitle("Codex Naturalis");
        stage.getIcons().add(new Image(Util.getImage("codex_logo.png")));

        if (p != null) {
            stage.setX(p.getX());
            stage.setY(p.getY());
        }

        stage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });

        return stage;
    }
}
