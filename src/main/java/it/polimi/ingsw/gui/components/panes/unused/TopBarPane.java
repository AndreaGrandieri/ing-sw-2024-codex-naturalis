package it.polimi.ingsw.gui.components.panes.unused;

import it.polimi.ingsw.gui.components.panes.NewPointsPane;
import it.polimi.ingsw.gui.support.interfaces.CardSizeController;
import it.polimi.ingsw.gui.support.context.PlayerDataSource;
import it.polimi.ingsw.gui.support.interfaces.ResponsiveContainer;
import it.polimi.ingsw.model.card.properties.CardItem;
import it.polimi.ingsw.model.player.PlayerColor;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static javafx.beans.binding.Bindings.*;

public class TopBarPane extends HBox implements ResponsiveContainer {

    private final SimpleStringProperty username;
    private final ObjectProperty<PlayerColor> color;
    private final BooleanProperty pointsVisible;
    private final BooleanProperty active;
    @FXML
    private NewPointsPane pointsPane;
    @FXML
    private HBox usernameLetterBox;
    @FXML
    private Label usernameLetterLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private HBox pointsPaneBox;


    public TopBarPane() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("top-bar-pane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setControllerFactory(type -> this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        username = new SimpleStringProperty("Alex");

        color = new SimpleObjectProperty<>(PlayerColor.BLUE);

        pointsVisible = new SimpleBooleanProperty(true);
        active = new SimpleBooleanProperty(false);


    }

    public boolean isActive() {
        return active.get();
    }

    public BooleanProperty activeProperty() {
        return active;
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

    public String getUsername() {
        return username.get();
    }

    public SimpleStringProperty usernameProperty() {
        return username;
    }

    public PlayerColor getColor() {
        return color.get();
    }

    public ObjectProperty<PlayerColor> colorProperty() {
        return color;
    }

    public void setColor(PlayerColor color) {
        this.color.set(color);
    }

    public void setPoints(Map<CardItem, Integer> points) {
        pointsProperty().set(points);
    }

    public ObjectProperty<Map<CardItem, Integer>> pointsProperty() {
        return pointsPane.pointsProperty();
    }

    public void bindDataController(PlayerDataSource playerDataSource) {
        setVars();

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
        pointsProperty().addListener((observableValue, cardItemIntegerMap, t1) -> {
            System.out.println("--- changed points" + playerDataSource.getPlayerData().getUsername());
        });
        activeProperty().bind(createObjectBinding(
                () -> playerDataSource.gameFlowProperty().get().isCurrentPlayer(username.get()),
                playerDataSource.gameFlowProperty()
        ));

    }

    public void setVars() {
        username.addListener((observableValue, s, t1) -> {
            setPrivUsername(t1);
        });

        active.addListener((observableValue, aBoolean, t1) -> {
            setPrivActive(t1);
        });

        color.addListener((observableValue, color1, t1) -> {
            setPrivColor(t1);
        });

        pointsVisible.addListener((observableValue, color1, t1) -> {
            setPrivPointsVisible(t1);
        });

    }


    private void setPrivActive(boolean active) {
        System.out.println("---- setactive");
        if (active) {
            this.getStyleClass().removeAll("br");
            this.getStyleClass().add("active");
        } else {
            this.getStyleClass().removeAll("active");
            this.getStyleClass().add("br");

        }
        applyCss();
    }

    private void setPrivColor(PlayerColor color) {
        usernameLetterBox.getStyleClass().removeAll(Arrays.asList("green", "blue", "yellow", "red"));
        usernameLetterBox.getStyleClass().add(color.toString());
    }

    public void setPrivPointsVisible(boolean visible) {
        if (!visible) this.getChildren().remove(pointsPaneBox);

    }

    public void setPrivUsername(String username) {
        usernameLabel.setText(username);
        usernameLetterLabel.setText(username.substring(0, 1).toUpperCase());
    }


    @Override
    public void setCardSizeController(CardSizeController cctrl) {
        pointsPane.ratioProperty().bind(createDoubleBinding(
                () -> {
                    return cctrl.ratioProperty().get();
                },
                cctrl.ratioProperty()
        ));
    }

    @Override
    public void updatePanes() {

    }
}
