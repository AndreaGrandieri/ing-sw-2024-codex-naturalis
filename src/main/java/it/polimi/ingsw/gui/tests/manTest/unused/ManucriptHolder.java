package it.polimi.ingsw.gui.tests.manTest.unused;

import javafx.geometry.Dimension2D;
import javafx.scene.control.ScrollPane;

import static javafx.beans.binding.Bindings.createObjectBinding;

public class ManucriptHolder extends ScrollPane {
    private final ManuscriptBox2 manBox = new ManuscriptBox2();

    public ManucriptHolder() {
        manBox.sizeProperty().bind(createObjectBinding(() -> new Dimension2D(widthProperty().get(), heightProperty().get()), widthProperty(), heightProperty()));
    }
}
