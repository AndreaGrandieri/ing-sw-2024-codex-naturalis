package it.polimi.ingsw.gui.components.panes.board;

import it.polimi.ingsw.gui.support.FXUtils;
import it.polimi.ingsw.gui.support.Util;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;

import java.util.Objects;

import static it.polimi.ingsw.gui.components.panes.board.MyColors.*;
import static javafx.beans.binding.Bindings.createDoubleBinding;
import static javafx.scene.paint.Color.RED;
import static javafx.scene.paint.Color.WHITE;

public final class BoardCircle {
    private final static String LUSITANA_FONT_URL = Util.getFont("Lusitana-Regular.ttf");
    private final Circle innerCircle;
    private final Circle outerCircle;
    private final Label label;
    private final Pane stackPane;
    private final SimpleDoubleProperty ratio;

    BoardCircle(int num, Color innerColor, Color outerColor) {

        innerCircle = new Circle(0, innerColor);
        innerCircle.setViewOrder(-1);
        innerCircle.setStroke(MY_BROWN);

        outerCircle = new Circle(0, outerColor);
        outerCircle.setStroke(MY_BROWN);

        label = new Label("" + num);
        label.setViewOrder(-2);


        // alternative
        label.layoutXProperty().bind(createDoubleBinding(
                () -> -label().widthProperty().get() / 2,
                label.widthProperty()
        ));

        label.layoutYProperty().bind(createDoubleBinding(
                () -> -label().heightProperty().get() / 2,
                label.heightProperty()
        ));
        stackPane = new Pane(outerCircle, innerCircle, label);
        stackPane.setMaxSize(0, 0);
        stackPane.setBackground(FXUtils.bgColor(RED));


        ratio = new SimpleDoubleProperty(1.0);
        setBindings();
    }

    public static BoardCircle boardCircleByNum(int num) {
        if (num == 0 || num == 20) return specialCircle(num);

        if (num > 20) return overTwentyCircle(num);

        return normalCircle(num);
    }

    private static BoardCircle normalCircle(int num) {
        return new BoardCircle(num, MY_LIGHT_YELLOW, MY_DARK_YELLOW);
    }

    private static BoardCircle overTwentyCircle(int num) {
        return new BoardCircle(num, MY_DARK_YELLOW, Color.WHITE);
    }

    private static BoardCircle specialCircle(int num) {
        return new BoardCircle(num, WHITE, MY_DARK_YELLOW);
    }

    public SimpleDoubleProperty ratioProperty() {
        return ratio;
    }

    private void setBindings() {
        // Inner
        innerCircle.radiusProperty().bind(
                ratioProperty().multiply((14 - 1.5) * 1.7)
        );
        innerCircle.strokeWidthProperty().bind(
                ratioProperty().multiply((1.5) * 1.7)
        );

        // Outer
        outerCircle.radiusProperty().bind(
                ratioProperty().multiply((14.0 + 3.0) * 1.7)
        );
        outerCircle.strokeWidthProperty().bind(
                ratioProperty().multiply((1) * 1.7))
        ;

        // Label
        label.fontProperty().bind(Bindings.createObjectBinding(() -> {
            double ratio = ratioProperty().get();
            return Font.loadFont(LUSITANA_FONT_URL, 14 * 1.7 * ratio);
        }, ratioProperty()));
    }

    public void resize2(double ratio) {
        ratio = ratio * 1.7;

        double firstR = (14 - 1.5) * ratio;
        double stroke1 = 1.5 * ratio;
        innerCircle.setRadius(firstR);
        innerCircle.setStrokeWidth(stroke1);


        double secondR = (14.0 + 3.0) * ratio;
        double stroke2 = 1 * ratio;
        outerCircle.setRadius(secondR);
        outerCircle.setStrokeWidth(stroke2);

        String fontUrl = BoardPane.class.getResource("Lusitana-Regular.ttf").toExternalForm();
        label.setFont(Font.loadFont(fontUrl, 14 * ratio));
    }

    public void resize(double rati) {

    }

    public void resize3(double rati) {
        rati = rati * 1.7;

        double firstR = (14 - 1.5) * rati;
        double stroke1 = 1.5 * rati;


        innerCircle.radiusProperty().bind(ratioProperty().multiply((14 - 1.5) * 1.7));
        innerCircle.strokeWidthProperty().bind(ratioProperty().multiply((1.5) * 1.7));


        double secondR = (14.0 + 3.0) * rati;
        double stroke2 = 1 * rati;

        outerCircle.radiusProperty().bind(ratioProperty().multiply((14.0 + 3.0) * 1.7));
        outerCircle.strokeWidthProperty().bind(ratioProperty().multiply((1) * 1.7));

        label.fontProperty().bind(Bindings.createObjectBinding(() -> {
            double ratio = ratioProperty().get();
            return Font.loadFont(LUSITANA_FONT_URL, 14 * 1.7 * ratio);
        }, ratioProperty()));
    }

    public Circle innerCircle() {
        return innerCircle;
    }

    public Circle outerCircle() {
        return outerCircle;
    }

    public Label label() {
        return label;
    }

    public Pane stackPane() {
        return stackPane;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BoardCircle) obj;
        return Objects.equals(this.innerCircle, that.innerCircle) &&
                Objects.equals(this.outerCircle, that.outerCircle) &&
                Objects.equals(this.label, that.label) &&
                Objects.equals(this.stackPane, that.stackPane);
    }

    @Override
    public int hashCode() {
        return Objects.hash(innerCircle, outerCircle, label, stackPane);
    }

    @Override
    public String toString() {
        return "BoardCircle[" +
                "innerCircle=" + innerCircle + ", " +
                "outerCircle=" + outerCircle + ", " +
                "label=" + label + ", " +
                "stackPane=" + stackPane + ']';
    }

}
