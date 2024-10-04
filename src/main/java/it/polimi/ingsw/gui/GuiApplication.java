package it.polimi.ingsw.gui;

import it.polimi.ingsw.LocalBot;
import it.polimi.ingsw.controller.LocalServer2;
import it.polimi.ingsw.controller.interfaces.ConnectionManagerI;
import it.polimi.ingsw.gui.components.views.CenteredView;
import it.polimi.ingsw.gui.support.ComponentBuilder;
import it.polimi.ingsw.gui.support.ViewRoute;
import it.polimi.ingsw.gui.views.EndView;
import it.polimi.ingsw.logger.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class GuiApplication extends Application {
    private final static boolean USE_NETWORK = true;
    private final static boolean START_FAKE = false;
    public boolean USER2 = false;
    private static Dimension2D size = new Dimension2D(1400, 800);

    @Override
    public void start(Stage stage) throws IOException {
        if (START_FAKE) {
            startFake(stage);
            return;
        }

        if (USE_NETWORK) startNetwork(stage);
        else startLocal(stage);
    }

    private static void startFake(Stage stage) {
        ComponentBuilder.customStage(stage, null);
        stage.setScene(new Scene(new CenteredView(ViewRoute.GAME_SETUP, new EndView()), size.getWidth(), size.getHeight()));
        stage.show();
    }

    private void startNetwork(Stage stage) {
        try {
            Logger.setLogLevel(Logger.LogLevel.NONE);

//            size = new Dimension2D(960.0, 800);
//            size = new Dimension2D(1400, 800);
            Point2D p = new Point2D(0, 1);
            p = null;

            if (USER2) {
                p = new Point2D(960, 1);
                String USERNAME = "pippo";
            }

            GuiStageController stageCtl1 = new GuiStageController(stage, p, size);

            stage.show();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void startLocal(Stage stage) {
        LocalServer2 server = new LocalServer2();
        LocalBot bot = new LocalBot("aaa", server.newConnectionManager());

        size = new Dimension2D(960.0, 800);
        size = new Dimension2D(960.0, 800);

        ConnectionManagerI cm1 = server.newConnectionManager();
        cm1.registerUsername("ciccio");
        GuiStageController stageCtl1 = new GuiStageController(stage, new Point2D(0, 1), size);

        Stage stage2 = new Stage();
        ConnectionManagerI cm2 = server.newConnectionManager();
        cm2.registerUsername("enry");
        GuiStageController stageCtl2 = new GuiStageController(stage2, new Point2D(960, 1), size);

//        stageCtl1.getRouter().goTo(ViewRoute.USERNAME);
//        stageCtl2.getRouter().goTo(ViewRoute.TITLES);

        stage.show();
        stage2.show();
    }

}
