package it.polimi.ingsw.gui.components.panes;

import it.polimi.ingsw.gui.components.panes.manuscript.ManuscriptBox;
import it.polimi.ingsw.gui.support.ComponentBuilder;
import it.polimi.ingsw.gui.support.helper.Card2D;
import it.polimi.ingsw.gui.support.interfaces.CardSizeController;
import it.polimi.ingsw.gui.support.context.PlayerDataSource;
import it.polimi.ingsw.gui.support.interfaces.ResponsiveContainer;
import it.polimi.ingsw.gui.support.helper.GuiGameState;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Dimension2D;
import javafx.scene.layout.StackPane;

import java.io.IOException;

import static javafx.beans.binding.Bindings.createObjectBinding;

public class MainManuscriptPane extends StackPane implements ResponsiveContainer {
    public NewTopBarPane topBarPane;
    @FXML
    private ManuscriptBox manuscriptPane;
    private PlayerDataSource playerDataSource;

    public MainManuscriptPane() {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main-manuscript-pane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setControllerFactory(type -> this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }


        manuscriptPane.setId(Integer.valueOf(0).toString());
    }

    public void setCardSizeController(CardSizeController cctrl) {
        manuscriptPane.cardSizeProperty().bind(createObjectBinding(
                () -> new Card2D(cctrl.ratioProperty().get() * 90),
                cctrl.ratioProperty()
        ));
        manuscriptPane.clipProperty().bind(createObjectBinding(
                () -> ComponentBuilder.threeRoundPath(10, new Dimension2D(manuscriptPane.getWidth(), manuscriptPane.getHeight())),
                manuscriptPane.widthProperty(), manuscriptPane.heightProperty()
        ));


        topBarPane.ratioProperty().bind(cctrl.ratioProperty().multiply(1.25));
    }

    @Override
    public void updatePanes() {

    }

    public void setDataController(PlayerDataSource playerDataSource) {
        this.playerDataSource = playerDataSource;
        setVars();
    }

    public void setVars() {
        topBarPane.bindDataController(playerDataSource);
        topBarPane.setPointsVisible(false);
    }

    public void setState(GuiGameState state) {
        manuscriptPane.setShowPlaceholders(state == GuiGameState.PLACE_CARD);
    }


    public ManuscriptBox getManuscriptPane() {
        return manuscriptPane;
    }
}
