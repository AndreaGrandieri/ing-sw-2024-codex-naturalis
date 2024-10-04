package it.polimi.ingsw.gui.components.views;

import it.polimi.ingsw.gui.support.FXUtils;
import it.polimi.ingsw.gui.support.interfaces.ViewRouter;
import javafx.scene.layout.GridPane;

public class CustomView extends GridPane {
    private ViewRouter viewRouter;

    public CustomView(ViewRouter viewRouter) {
        this();
        this.viewRouter = viewRouter;
    }

    public CustomView() {
        FXUtils.loadRootFXMLView(this);
    }

    public ViewRouter getRouter() {
        return viewRouter;
    }
}
