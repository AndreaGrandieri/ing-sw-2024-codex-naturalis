module it.polimi.ingsw {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.jetbrains.annotations;
    requires java.rmi;

    requires org.fusesource.jansi;
    requires java.desktop;

    exports it.polimi.ingsw.controller.event.game;
    exports it.polimi.ingsw.controller.event.lobby;
    exports it.polimi.ingsw.controller.event;

    exports it.polimi.ingsw.model.card;
    exports it.polimi.ingsw.model.card.bonus;
    exports it.polimi.ingsw.model.card.deck;
    exports it.polimi.ingsw.model.card.factory;
    exports it.polimi.ingsw.model.card.goal;
    exports it.polimi.ingsw.model.card.properties;

    exports it.polimi.ingsw.model.player;

    exports it.polimi.ingsw.model.game.gamelobby;

    exports it.polimi.ingsw.gui.tests.boardTest;
    opens it.polimi.ingsw.gui.tests.boardTest to javafx.fxml;
    exports it.polimi.ingsw.gui.tests.testTemplate;
    opens it.polimi.ingsw.gui.tests.testTemplate to javafx.fxml;
    exports it.polimi.ingsw.gui.tests.manTest;
    opens it.polimi.ingsw.gui.tests.manTest to javafx.fxml;

    exports it.polimi.ingsw.gui;
    opens it.polimi.ingsw.gui to javafx.fxml;
    exports it.polimi.ingsw.gui.support.info;
    opens it.polimi.ingsw.gui.support.info to javafx.fxml;
    exports it.polimi.ingsw.gui.support.helper;
    opens it.polimi.ingsw.gui.support.helper to javafx.fxml;
    exports it.polimi.ingsw.gui.support.interfaces;
    opens it.polimi.ingsw.gui.support.interfaces to javafx.fxml;
    exports it.polimi.ingsw.gui.components.panes.board;
    opens it.polimi.ingsw.gui.components.panes.board to javafx.fxml;

    exports it.polimi.ingsw.gui.views;
    opens it.polimi.ingsw.gui.views to javafx.fxml;

    exports it.polimi.ingsw.gui.components;
    opens it.polimi.ingsw.gui.components to javafx.fxml;


    exports it.polimi.ingsw.model.game;
    exports it.polimi.ingsw.controller;

    exports it.polimi.ingsw.gui.components.panes.card;
    opens it.polimi.ingsw.gui.components.panes.card to javafx.fxml;
    exports it.polimi.ingsw.gui.tests.manTest.unused;
    opens it.polimi.ingsw.gui.tests.manTest.unused to javafx.fxml;
    exports it.polimi.ingsw.gui.components.panes.unused;
    opens it.polimi.ingsw.gui.components.panes.unused to javafx.fxml;

    exports it.polimi.ingsw.network.rmi;
    exports it.polimi.ingsw.network.tcpip;
    exports it.polimi.ingsw.network;
    exports it.polimi.ingsw.gui.components.views;
    opens it.polimi.ingsw.gui.components.views to javafx.fxml;
    exports it.polimi.ingsw.gui.components.panes;
    opens it.polimi.ingsw.gui.components.panes to javafx.fxml;

    exports it.polimi.ingsw.client.network.rmi;
    exports it.polimi.ingsw.controller.interfaces;
    exports it.polimi.ingsw.gui.support.context;
    opens it.polimi.ingsw.gui.support.context to javafx.fxml;
    exports it.polimi.ingsw.gui.support;
    opens it.polimi.ingsw.gui.support to javafx.fxml;
    exports it.polimi.ingsw.gui.components.panes.manuscript;
    opens it.polimi.ingsw.gui.components.panes.manuscript to javafx.fxml;
}
