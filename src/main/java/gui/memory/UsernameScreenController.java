package gui.memory;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class UsernameScreenController {
    @FXML private AnchorPane rootPane;
    @FXML private HBox topBar;
    @FXML private Label dummyUser;
    @FXML private Button dummyLogout;
    @FXML private Button exitBtn;
    @FXML private TextField usernameField;
    @FXML private Button continueBtn;
    @FXML private Button leaderboardBtn;
    @FXML private Label errorLabel;

    private double xOffset = 0;
    private double yOffset = 0;

    private java.util.function.Consumer<String> onContinue;
    private Runnable onLeaderboard;
    private Runnable onExit;

    public void setOnContinue(java.util.function.Consumer<String> onContinue) {
        this.onContinue = onContinue;
    }
    public void setOnLeaderboard(Runnable onLeaderboard) {
        this.onLeaderboard = onLeaderboard;
    }
    public void setOnExit(Runnable onExit) {
        this.onExit = onExit;
    }

    public void setWindowDragHandlers(Stage stage) {
        topBar.setOnMousePressed((MouseEvent event) -> {
            xOffset = stage.getX() - event.getScreenX();
            yOffset = stage.getY() - event.getScreenY();
        });
        topBar.setOnMouseDragged((MouseEvent event) -> {
            stage.setX(event.getScreenX() + xOffset);
            stage.setY(event.getScreenY() + yOffset);
        });
        rootPane.setOnMousePressed((MouseEvent event) -> {
            if (event.getTarget() == rootPane) {
                xOffset = stage.getX() - event.getScreenX();
                yOffset = stage.getY() - event.getScreenY();
            }
        });
        rootPane.setOnMouseDragged((MouseEvent event) -> {
            if (event.getTarget() == rootPane) {
                stage.setX(event.getScreenX() + xOffset);
                stage.setY(event.getScreenY() + yOffset);
            }
        });
    }

    @FXML
    private void initialize() {
        continueBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            if (username.isEmpty()) {
                errorLabel.setText("Please enter a username.");
            } else if (onContinue != null) {
                errorLabel.setText("");
                onContinue.accept(username);
            }
        });
        leaderboardBtn.setOnAction(e -> {
            if (onLeaderboard != null) onLeaderboard.run();
        });
        exitBtn.setOnAction(e -> {
            if (onExit != null) onExit.run();
        });
    }
}
