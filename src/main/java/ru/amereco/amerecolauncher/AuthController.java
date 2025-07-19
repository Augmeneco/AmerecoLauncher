package ru.amereco.amerecolauncher;

import java.io.IOException;
import javafx.fxml.FXML;

public class AuthController {

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("main");
    }
}
