package it.polimi.ingsw.gui;

import it.polimi.ingsw.controller.interfaces.ConnectionManagerI;
import it.polimi.ingsw.gui.components.views.CenteredView;
import it.polimi.ingsw.gui.support.ComponentBuilder;
import it.polimi.ingsw.gui.support.ViewRoute;
import it.polimi.ingsw.gui.support.context.ChatContext;
import it.polimi.ingsw.gui.support.context.ContextManager;
import it.polimi.ingsw.gui.support.context.GameContext;
import it.polimi.ingsw.gui.support.context.MatchContext;
import it.polimi.ingsw.gui.support.interfaces.ViewRouter;
import it.polimi.ingsw.gui.views.*;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class GuiStageController {
    private final Map<ViewRoute, Parent> routes;
    private final Scene scene;
    private final HashMap<ViewRoute, Supplier<Pane>> routeBuilders;
    private ViewRoute currentViewRoute;
    private final ViewRouter viewRouter = this::goTo;
    private ConnectionManagerI cm;
    private GameContext gameCtx;
    private ChatContext chatCtx;
    private MatchContext matchCtx;

    GuiStageController(Stage stage, Dimension2D size) {
        this(stage, null, size);
    }

    GuiStageController(Stage stage, Point2D p, Dimension2D size) {
        ContextManager ctxMan = new ContextManager();

        routeBuilders = new HashMap<>(Map.of(
                ViewRoute.USERNAME, () -> new CenteredView(ViewRoute.USERNAME, new UsernameView(viewRouter, ctxMan))
                , ViewRoute.TITLES, () -> new CenteredView(ViewRoute.TITLES, new TitlesView(viewRouter, ctxMan))
                , ViewRoute.LOBBY, () -> new CenteredView(ViewRoute.LOBBY, new LobbyView(viewRouter, ctxMan))
                , ViewRoute.GAME_SETUP, () -> new CenteredView(ViewRoute.GAME_SETUP, new SetupView(viewRouter, ctxMan))
                , ViewRoute.GAME, () -> new GameView(viewRouter, ctxMan)
                , ViewRoute.END, () -> new CenteredView(ViewRoute.GAME_SETUP, new EndView(viewRouter, ctxMan))
        ));
        routes = new HashMap<>();

        currentViewRoute = ViewRoute.USERNAME;
        scene = new Scene(getRoute(currentViewRoute), size.getWidth(), size.getHeight());

        stage.setScene(scene);
        ComponentBuilder.customStage(stage, p);
    }

    public ViewRouter getRouter() {
        return viewRouter;
    }

    public ChatContext getChatCtx() {
        return chatCtx;
    }

    public GameContext getGameCtx() {
        return gameCtx;
    }

    public MatchContext getMatchCtx() {
        return matchCtx;
    }

    private GameContext gameDataContext() {
        if (gameCtx == null) gameCtx = new GameContext(cm.clientGameController());
        return gameCtx;
    }

    private ChatContext chatContext() {
        if (chatCtx == null) chatCtx = new ChatContext(cm.clientChatController());
        return chatCtx;
    }

    private void goTo(ViewRoute viewRoute) {
        List<ViewRoute> states = List.of(ViewRoute.GAME, ViewRoute.GAME_SETUP, ViewRoute.END);

        if ((states.contains(currentViewRoute))
                && viewRoute.equals(ViewRoute.TITLES)) {
            routes.remove(ViewRoute.GAME);
            routes.remove(ViewRoute.GAME_SETUP);
        }

        scene.setRoot(getRoute(viewRoute));
        currentViewRoute = viewRoute;

    }

    private Parent getRoute(ViewRoute viewRoute) {
        if (!routes.containsKey(viewRoute)) routes.put(viewRoute, routeBuilders.get(viewRoute).get());
        return routes.get(viewRoute);
    }

    public ConnectionManagerI getCm() {
        return cm;
    }
}
