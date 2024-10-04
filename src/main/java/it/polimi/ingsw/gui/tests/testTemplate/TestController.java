package it.polimi.ingsw.gui.tests.testTemplate;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class TestController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onButtonClick() {
        welcomeText.setText("Button click!");
    }
}