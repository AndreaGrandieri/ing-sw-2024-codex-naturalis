package it.polimi.ingsw.gui.tests.boardTest;

import it.polimi.ingsw.gui.components.panes.board.BoardPane;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

import static java.lang.Math.*;

public class TestController implements Initializable {
    private final Dimension2D preferredSize = new Dimension2D(1226, 649);
    public Rectangle rect1;
    public BoardPane custom;
    public VBox main;
    public Rectangle overPane;
    @FXML
    private Label welcomeText;
    private int xTrail = 10;
    private final Pane p = new Pane();
    private Dimension2D screenSize = new Dimension2D(0, 0);

    @FXML
    protected void onHelloButtonClick() {
        custom.simulate();

        Rectangle rect2 = new Rectangle(50, 50);
        rect1.setClip(rect2);
        Timeline t = new Timeline(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(rect2.widthProperty(), 100)
                )
        );
        t.play();
    }


    protected void onHelloButtonClick2() {


        xTrail = xTrail + 200;
        Point2D endPos = new Point2D(xTrail, 50);
        Point2D startPos = new Point2D(0, 0);
        double distance = Math.sqrt(Math.pow(endPos.getX() - startPos.getX(), 2) + Math.pow(endPos.getY() - startPos.getY(), 2));

        System.out.println(distance);
        Timeline a = new Timeline();
        a.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(distance)
                        , new KeyValue(rect1.layoutXProperty(), endPos.getX())
                        , new KeyValue(rect1.layoutYProperty(), endPos.getY())
                ),
                new KeyFrame(Duration.millis(distance * 2))
        );
        a.setOnFinished(actionEvent -> {
            rect1.relocate(0, 0);
        });
        a.play();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        main.getChildren().add(p);
        main.layoutBoundsProperty().addListener((observableValue, bounds, t1) -> {
            double deltaW = abs(t1.getWidth() - screenSize.getWidth());
            double deltaH = abs(t1.getHeight() - screenSize.getHeight());
            int delta = 10;
            if (deltaW > delta || deltaH > delta) {

                double ratio = round(min(t1.getHeight() / preferredSize.getHeight(), t1.getWidth() / preferredSize.getWidth()) * 100.0) / 100.0;
                double newWidth = custom.preferredSize.getWidth() * ratio;
                double newHeight = custom.preferredSize.getHeight() * ratio;
                custom.ratioProperty().set(ratio);

                overPane.setWidth(newWidth);
                overPane.setHeight(newHeight);

                screenSize = new Dimension2D(t1.getWidth(), t1.getHeight());
            }
        });

        Rectangle rect2 = new Rectangle(50, 50);
        rect2.setLayoutY(25);
        rect2.setLayoutX(-25);
        rect2.setRotate(45);
        rect1.setClip(rect2);
    }
}
