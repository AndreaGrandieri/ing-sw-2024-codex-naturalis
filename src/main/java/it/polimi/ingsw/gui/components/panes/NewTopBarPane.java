package it.polimi.ingsw.gui.components.panes;

import it.polimi.ingsw.gui.support.FXUtils;
import it.polimi.ingsw.gui.support.context.PlayerDataSource;
import it.polimi.ingsw.model.card.properties.CardItem;
import it.polimi.ingsw.model.player.PlayerColor;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Map;

import static javafx.beans.binding.Bindings.*;

public class NewTopBarPane extends HBox {

    private static final double prefFontSize = 20;
    private static final Map<PlayerColor, String> colorsMap = Map.of(
            PlayerColor.GREEN, "-fx-background-color: #c2f1be; -fx-border-color: #6eba67;",
            PlayerColor.RED, "-fx-background-color: #edcbc5; -fx-border-color: #ed8675;",
            PlayerColor.BLUE, "-fx-background-color: #b4ecec; -fx-border-color: #67baba;",
            PlayerColor.YELLOW, "-fx-background-color: #ffcd7d; -fx-border-color: #ffb02c;"
    );
    private static final String grayColor = "-fx-background-color: lightgray; -fx-border-color: gray;";
    private static final int prefSpacing = 8;
    private static final int prefMaxHeight = 35;
    private static final int prefBoxWidth = 40;
    private static final int prefBoxBorder = 4;
    private final NewPointsPane pointsPane;
    private final SimpleStringProperty username;
    private final ObjectProperty<PlayerColor> color;
    private final BooleanProperty active;
    private final BooleanProperty pointsVisible;
    private final SimpleDoubleProperty ratio;


    public NewTopBarPane() {
        ratio = new SimpleDoubleProperty(1.0);

        username = new SimpleStringProperty("Alex");

        color = new SimpleObjectProperty<>(PlayerColor.BLUE);

        active = new SimpleBooleanProperty(false);

        pointsVisible = new SimpleBooleanProperty(true);

        pointsPane = new NewPointsPane();
        pointsPane.setAlignment(Pos.CENTER_RIGHT);
        pointsPane.ratioProperty().bind(ratio.multiply(0.85));


        HBox usernameLetterBox = usernameBox();


        Label usernameLabel = usernameLabel();

        setAlignment(Pos.CENTER);
        setHgrow(pointsPane, Priority.ALWAYS);

        backgroundProperty().bind(createObjectBinding(
                () -> FXUtils.bgColor(Color.web(active.get() ? "#ffe181" : "#e5dfce")),
                active
        ));

        spacingProperty().bind(createDoubleBinding(
                () -> prefSpacing * ratio.get(),
                ratio
        ));

        paddingProperty().bind(createObjectBinding(
                () -> new Insets(0, prefSpacing * ratio.get(), 0, 0),
                ratio
        ));

        minHeightProperty().bind(maxHeightProperty());
        maxHeightProperty().bind(createDoubleBinding(
                () -> prefMaxHeight * ratio.get(),
                ratio
        ));

        styleProperty().bind(createStringBinding(
                () -> active.get() ?
                        "-fx-background-color: ffe181;-fx-border-width: 0 4 4 0;-fx-border-color: #b4a31f;" :
                        "-fx-border-width: 0 4 4 0;-fx-border-color: #908e82;",
                active
        ));

        pointsVisible.addListener((observableValue, aBoolean, t1) -> {
            if (getChildren().contains(pointsPane)) {
                if (!t1) getChildren().remove(pointsPane);
            } else if (t1) getChildren().add(pointsPane);


        });
        getChildren().addAll(usernameLetterBox, usernameLabel, pointsPane);
    }

        private Label usernameLabel() {
        Label usernameLabel = new Label();
        usernameLabel.textProperty().bind(username);
        usernameLabel.fontProperty().bind(createObjectBinding(
                () -> new Font("Lato Regular", prefFontSize * ratio.get()),
                ratio
        ));
        return usernameLabel;
    }

        private HBox usernameBox() {
        Label usernameLetterLabel = new Label();
        usernameLetterLabel.textProperty().bind(createStringBinding(
                () -> username.get().substring(0, 1).toUpperCase(),
                username
        ));
        usernameLetterLabel.fontProperty().bind(createObjectBinding(
                () -> new Font("Lato Regular", prefFontSize * ratio.get()),
                ratio
        ));

        HBox usernameLetterBox = new HBox(usernameLetterLabel);
        usernameLetterBox.setBackground(FXUtils.bgColor(Color.CYAN));
        usernameLetterBox.setAlignment(Pos.CENTER);
        usernameLetterBox.setPrefSize(40, 40);
        usernameLetterBox.setScaleY(1.2);
        usernameLetterBox.scaleXProperty().bind(usernameLetterBox.scaleYProperty());
        usernameLetterBox.setTranslateX(-3);
        usernameLetterBox.minHeightProperty().bind(usernameLetterBox.maxHeightProperty());
        usernameLetterBox.minWidthProperty().bind(usernameLetterBox.maxWidthProperty());
        usernameLetterBox.maxHeightProperty().bind(usernameLetterBox.maxWidthProperty());
        usernameLetterBox.maxWidthProperty().bind(createDoubleBinding(
                () -> prefBoxWidth * ratio.get(),
                ratio
        ));

        usernameLetterBox.styleProperty().bind(createStringBinding(
                () -> "-fx-background-radius: " + prefBoxWidth * ratio.get() / 2 + ";" +
                        "-fx-border-radius: " + prefBoxWidth * ratio.get() / 2 + ";" +
                        "-fx-border-width: " + prefBoxBorder * ratio.get() + ";" +
                        (color.get() == null ? grayColor : colorsMap.get(color.get())),
                ratio, color
        ));

        return usernameLetterBox;
    }

    public void bindDataController(PlayerDataSource playerDataSource) {
        usernameProperty().bind(createStringBinding(
                () -> playerDataSource.playerDataProperty().get().getUsername(),
                playerDataSource.playerDataProperty()
        ));
        colorProperty().bind(createObjectBinding(
                () -> playerDataSource.playerDataProperty().get().getColor(),
                playerDataSource.playerDataProperty()
        ));
        pointsProperty().bind(createObjectBinding(
                () -> playerDataSource.manuscriptProperty().get().getItemsNumber(),
                playerDataSource.manuscriptProperty()
        ));
        activeProperty().bind(createObjectBinding(
                () -> playerDataSource.gameFlowProperty().get().isCurrentPlayer(username.get()),
                playerDataSource.gameFlowProperty()
        ));
    }


    public String getUsername() {
        return username.get();
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public SimpleStringProperty usernameProperty() {
        return username;
    }

    public double getRatio() {
        return ratio.get();
    }

    public void setRatio(double ratio) {
        this.ratio.set(ratio);
    }

    public SimpleDoubleProperty ratioProperty() {
        return ratio;
    }

    public void setColor(PlayerColor color) {
        this.color.set(color);
    }

    public PlayerColor getColor() {
        return color.get();
    }

    public ObjectProperty<PlayerColor> colorProperty() {
        return color;
    }

    public void setPoints(Map<CardItem, Integer> points) {
        pointsProperty().set(points);
    }

    public ObjectProperty<Map<CardItem, Integer>> pointsProperty() {
        return pointsPane.pointsProperty();
    }

    public boolean isActive() {
        return active.get();
    }

    public BooleanProperty activeProperty() {
        return active;
    }

    public void setActive(boolean active) {
        this.active.set(active);
    }

    public boolean isPointsVisible() {
        return pointsVisible.get();
    }

    public BooleanProperty pointsVisibleProperty() {
        return pointsVisible;
    }

    public void setPointsVisible(boolean pointsVisible) {
        this.pointsVisible.set(pointsVisible);
    }
}
