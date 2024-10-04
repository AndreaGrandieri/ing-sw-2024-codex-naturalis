package it.polimi.ingsw.gui.views;

import it.polimi.ingsw.gui.support.FXUtils;
import it.polimi.ingsw.gui.support.ViewRoute;
import it.polimi.ingsw.gui.support.interfaces.ViewRouter;
import it.polimi.ingsw.gui.components.CustomTextArea;
import it.polimi.ingsw.gui.components.views.CustomView;
import it.polimi.ingsw.gui.components.views.GroupPane;
import it.polimi.ingsw.gui.support.context.ContextManager;
import it.polimi.ingsw.util.TextValidator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class UsernameView extends CustomView {
    private static final String RMI_STRING = "RMI";
    private final static String SERVER_ADDRESS = "localhost";
    private final static int SERVER_PORT = 40000;
    private final ContextManager ctxMan;
    public GroupPane serverPane;
    public GroupPane usernamePane;
    public VBox containerBox;
    public CustomTextArea serverIpArea;
    public CustomTextArea serverPortArea;
    public ComboBox<String> serverModeCombo;
    @FXML
    private TextArea textArea;


    public UsernameView(ViewRouter viewRouter, ContextManager ctxMan) {
        super(viewRouter);
        this.ctxMan = ctxMan;
        serverIpArea.setText(SERVER_ADDRESS);
        serverPortArea.setText(String.valueOf(SERVER_PORT));

        serverModeCombo.getSelectionModel().select("Socket");

        ObservableList<Integer> observableList = FXCollections.observableList(new ArrayList<>(List.of(1, 2, 3, 3)));

        textArea.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) goToTitles();
        });

        textArea.setTextFormatter(FXUtils.textFormatterFromRegEx(TextValidator.usernameWritingValidator));
    }

    private void switchToUsernameBox() {
        containerBox.getChildren().remove(serverPane);
        containerBox.getChildren().add(usernamePane);
    }


    @FXML
    private void goToTitles() {
        if (ctxMan.registerUsername(textArea.getText()))
            getRouter().goTo(ViewRoute.TITLES);
    }

    @FXML
    private void connectServer() {
        String connectionMode = serverModeCombo.getSelectionModel().selectedItemProperty().get();
        Boolean USE_RMI = connectionMode.equals(RMI_STRING);
        int port = Integer.parseInt(serverPortArea.getText());
        if (ctxMan.createCm(serverIpArea.getText(), port, USE_RMI))
            switchToUsernameBox();

    }
}
