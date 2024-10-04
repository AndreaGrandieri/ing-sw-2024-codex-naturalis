package it.polimi.ingsw.gui.components.views;

import it.polimi.ingsw.gui.support.FXUtils;
import it.polimi.ingsw.gui.support.Util;
import it.polimi.ingsw.gui.support.ViewRoute;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import static javafx.beans.binding.Bindings.createDoubleBinding;

public class CenteredView extends StackPane {
    public HBox box;
    public GridPane grid;
    public ImageView backgroundView;
    public AnchorPane kingdomsPane;


    public CenteredView(Node view) {
        FXUtils.loadRootFXMLView(this);
        grid.add(view, 1, 1);
        backgroundView.fitHeightProperty().bind(createDoubleBinding(
                () -> (widthProperty().get() < 1920.0 / 1080.0 * heightProperty().get()) ? heightProperty().get() : USE_COMPUTED_SIZE
                , this.widthProperty(), heightProperty()));
        backgroundView.fitWidthProperty().bind(createDoubleBinding(
                () -> (widthProperty().get() > 1920.0 / 1080.0 * heightProperty().get()) ? widthProperty().get() : USE_COMPUTED_SIZE
                , this.widthProperty(), heightProperty()));


    }

    public CenteredView(ViewRoute routerID, Node node) {
        this(node);
        String formatted = "backgrounds/%s-bg.png".formatted(routerID.getValue());
        backgroundView.setImage(new Image(Util.getImage(formatted)));
        if (!routerID.equals(ViewRoute.USERNAME)) getChildren().remove(kingdomsPane);
    }


}
