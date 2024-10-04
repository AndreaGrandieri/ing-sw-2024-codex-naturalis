package it.polimi.ingsw.gui.components.views;

import it.polimi.ingsw.gui.support.FXUtils;
import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

@DefaultProperty("inside")
public class GroupPane extends VBox implements Initializable {
    private final ObservableList<Node> inside = FXCollections.observableList(new ArrayList<>());
    private final ObservableList<Node> insideTitle = FXCollections.observableList(new ArrayList<>());
    public VBox contentBox;

    public Label titleLabel;
    public Button button;
    public HBox titleBox;

    public GroupPane() {
        FXUtils.loadRootFXMLView(this);
        inside.addListener((ListChangeListener<? super Node>) change -> {
            change.next();
            contentBox.getChildren().addAll(change.getAddedSubList());
        });

        insideTitle.addListener((ListChangeListener<? super Node>) change -> {
            change.next();
            titleBox.getChildren().addAll(change.getAddedSubList());
        });
        button.getStyleClass().removeAll("button");


    }

    public BooleanProperty defaultButtonProperty() {
        return button.defaultButtonProperty();
    }

    public String getTitle() {
        return titleLabel.textProperty().get();
    }

    public void setTitle(String title) {
        titleLabel.textProperty().set(title);
    }

    public String getButton() {
        return button.textProperty().get();
    }

    public void setButton(String title) {
        super.getChildren().add(button);
        button.textProperty().set(title);
    }

    public final void setOnButtonClicked(
            EventHandler<ActionEvent> value) {
        button.setOnAction(value);
    }

    public final EventHandler<? super MouseEvent> getOnButtonClicked() {
        return button.getOnMouseClicked();
    }

    public ObservableList<Node> getInside() {
        return inside;
    }

    public ObservableList<Node> getInsideTitle() {
        return insideTitle;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        getChildren().remove(button);
    }
}
