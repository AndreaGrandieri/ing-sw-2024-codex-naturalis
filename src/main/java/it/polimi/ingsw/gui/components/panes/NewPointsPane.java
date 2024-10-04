package it.polimi.ingsw.gui.components.panes;

import it.polimi.ingsw.gui.support.Util;
import it.polimi.ingsw.model.card.properties.CardItem;
import it.polimi.ingsw.model.card.properties.CardKingdom;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.polimi.ingsw.model.card.properties.CardKingdom.*;
import static javafx.beans.binding.Bindings.*;

public class NewPointsPane extends HBox {
    private final static String LUSITANA_FONT_URL = Util.getFont("Lusitana-Regular.ttf");
    private final static int prefImageHeight = 23;
    private final static double prefFontSize = 23;
    private final static int prefSpacing = 5;
    private final Map<CardKingdom, ImageView> imageViewMap;
    private final Map<CardKingdom, Label> labelMap;
    private final ObjectProperty<Map<CardItem, Integer>> points;
    private final DoubleProperty ratio;


    public NewPointsPane() {
        points = new SimpleObjectProperty<>(
                Map.of(ANIMAL, 0, INSECT, 0, FUNGI, 0, PLANT, 0)
        );
        ratio = new SimpleDoubleProperty(1.0);


        Map<CardKingdom, ImageView> imageViewMap = new HashMap<>();
        Map<CardKingdom, Label> labelMap = new HashMap<>();

        List.of(CardKingdom.values()).forEach(ck -> {
            ImageView imageView = imageView(ck);
            Label label = label(ck);

            imageViewMap.put(ck, imageView);
            labelMap.put(ck, label);

            getChildren().addAll(label, imageView);
        });

        this.imageViewMap = Map.copyOf(imageViewMap);
        this.labelMap = Map.copyOf(labelMap);


        setAlignment(Pos.CENTER);
        spacingProperty().bind(createDoubleBinding(
                () -> prefSpacing * ratio.get(),
                ratio
        ));
    }

    private static String kingdomUrl(CardKingdom ck) {
        String formatted = "kingdoms/%s.png".formatted(ck.toString().toLowerCase());
        return String.valueOf(Util.getImage(formatted));
    }

    private ImageView imageView(CardKingdom animal) {
        ImageView imageView = new ImageView(kingdomUrl(animal));
        imageView.setPreserveRatio(true);
        imageView.fitHeightProperty().bind(createDoubleBinding(
                () -> prefImageHeight * ratio.get(),
                ratio
        ));
        return imageView;
    }

    private Label label(CardKingdom ck) {
        Label label = new Label();
        label.textProperty().bind(createStringBinding(
                () -> points.get().get(ck).toString(),
                points
        ));
        label.fontProperty().bind(createObjectBinding(
                () -> Font.loadFont(LUSITANA_FONT_URL, prefFontSize * ratio.get()),
                ratio
        ));
        return label;
    }

    public void setPoints(Map<CardItem, Integer> points) {
        this.points.set(points);
    }

    public Map<CardItem, Integer> getPoints() {
        return points.get();
    }

    public ObjectProperty<Map<CardItem, Integer>> pointsProperty() {
        return points;
    }

    public void setRatio(double ratio) {
        this.ratio.set(ratio);
    }

    public double getRatio() {
        return ratio.get();
    }

    public DoubleProperty ratioProperty() {
        return ratio;
    }


}
