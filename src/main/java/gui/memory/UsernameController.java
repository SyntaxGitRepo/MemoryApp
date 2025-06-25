package gui.memory;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.util.function.Consumer;

public class UsernameController extends gameController {
    @FXML private AnchorPane rootPane;
    @FXML private HBox topBar;
    @FXML private Label dummyUser;
    @FXML private Button dummyLogout;
    @FXML private Button exitBtn;
    @FXML private TextField usernameField;
    @FXML private Button loginBtn;
    @FXML private Button leaderboardBtn;
    @FXML private Label errorLabel;


    private void login() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            errorLabel.setText("Please enter a username.");
        } else if (sceneChanger != null) {
            errorLabel.setText("");
            Data.username = username;
            sceneChanger.accept("gameScene");
        }
    }

    @FXML
    private void initialize() {
        loginBtn.setOnAction(e -> login());
        leaderboardBtn.setOnAction(e -> {
            sceneChanger.accept("leaderboardScene");
            Data.fromScene = "usernameScene";
        });
        exitBtn.setOnAction(e -> onExit.run());

        usernameField.setOnAction(e -> loginBtn.fire());
    }

    @Override
    public void updateWindow() {

    }
}