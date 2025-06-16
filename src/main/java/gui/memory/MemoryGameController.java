package gui.memory;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;

public class MemoryGameController implements Initializable {

    @FXML private GridPane gameGrid;
    @FXML private Label statusLabel;
    @FXML private Label attemptsLabel;
    @FXML private Label pairsFoundLabel;
    @FXML private Button restartButton;
    @FXML private ComboBox<String> difficultyBox;
    @FXML private VBox headerBox;
    @FXML private Button closeButton;
    @FXML private Label usernameLabel;
    @FXML private Button logoutButton;
    @FXML private Button leaderboardButton; // Add this if you want to reference by fx:id

    private String username = "";

    // Leaderboard callback
    private TriConsumer<String, Integer, String> leaderboardConsumer;
    private Runnable leaderboardButtonAction;

    private List<Card> cards;
    private Card firstCard = null;
    private Card secondCard = null;
    private boolean gameActive = true;

    private int attempts = 0;
    private int pairsFound = 0;
    private int totalPairs;

    // Card symbols for the game
    private final String[] SYMBOLS = {
            "🎮", "🎯", "🎲", "🎪", "🎨", "🎭", "🎪", "🎺",
            "🎸", "🎹", "🎤", "🎧", "📱", "💻", "🖥️", "⌚",
            "📷", "🎬", "📺", "🔮", "💎", "🏆", "🎁", "🌟"
    };

    // For window dragging
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupWindowBar();
        setupLogoutButton();
        setupLeaderboardButton();
        setupDifficultyBox();
        setupRestartButton();
        setupDraggableBackgroundAndTopBar(); // updated method
        initializeGame();
    }

    // Allow app to set leaderboard callback
    public void setLeaderboardConsumer(TriConsumer<String, Integer, String> leaderboardConsumer) {
        this.leaderboardConsumer = leaderboardConsumer;
    }
    public void setLeaderboardButtonAction(Runnable leaderboardButtonAction) {
        this.leaderboardButtonAction = leaderboardButtonAction;
    }

    private void setupWindowBar() {
        if (closeButton != null) {
            closeButton.setText("X");

            // Tooltip for accessibility
            Tooltip.install(closeButton, new Tooltip("Close"));

            closeButton.setOnAction(e -> {
                Stage stage = (Stage) closeButton.getScene().getWindow();
                stage.close();
            });

            // Minimal hover effect: only text color changes
            closeButton.setOnMouseEntered(e -> closeButton.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #c0392b; -fx-font-size: 22px; -fx-font-family: Roboto; -fx-min-width: 38; -fx-min-height: 38; -fx-max-width: 38; -fx-max-height: 38; -fx-padding: 0; -fx-cursor: hand; -fx-border-color: transparent;"
            ));
            closeButton.setOnMouseExited(e -> closeButton.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 22px; -fx-font-family: Roboto; -fx-min-width: 38; -fx-min-height: 38; -fx-max-width: 38; -fx-max-height: 38; -fx-padding: 0; -fx-cursor: hand; -fx-border-color: transparent;"
            ));
        }
        if (headerBox != null) {
            // Enable window dragging on the header VBox (headerBox)
            headerBox.setOnMousePressed((MouseEvent event) -> {
                Stage stage = (Stage) headerBox.getScene().getWindow();
                xOffset = stage.getX() - event.getScreenX();
                yOffset = stage.getY() - event.getScreenY();
            });
            headerBox.setOnMouseDragged((MouseEvent event) -> {
                Stage stage = (Stage) headerBox.getScene().getWindow();
                stage.setX(event.getScreenX() + xOffset);
                stage.setY(event.getScreenY() + yOffset);
            });
        }
    }

    // Enable dragging on the root VBox background and the top bar (username/logout HBox)
    private void setupDraggableBackgroundAndTopBar() {
        if (headerBox != null) {
            VBox root = (VBox) headerBox.getParent();
            // Drag on background
            root.setOnMousePressed((MouseEvent event) -> {
                if (event.getTarget() == root) {
                    Stage stage = (Stage) root.getScene().getWindow();
                    xOffset = stage.getX() - event.getScreenX();
                    yOffset = stage.getY() - event.getScreenY();
                }
            });
            root.setOnMouseDragged((MouseEvent event) -> {
                if (event.getTarget() == root) {
                    Stage stage = (Stage) root.getScene().getWindow();
                    stage.setX(event.getScreenX() + xOffset);
                    stage.setY(event.getScreenY() + yOffset);
                }
            });
            // Drag on top bar (username/logout HBox)
            if (root.getChildren().size() > 0 && root.getChildren().get(0) instanceof HBox) {
                HBox topBar = (HBox) root.getChildren().get(0);
                topBar.setOnMousePressed((MouseEvent event) -> {
                    Stage stage = (Stage) topBar.getScene().getWindow();
                    xOffset = stage.getX() - event.getScreenX();
                    yOffset = stage.getY() - event.getScreenY();
                });
                topBar.setOnMouseDragged((MouseEvent event) -> {
                    Stage stage = (Stage) topBar.getScene().getWindow();
                    stage.setX(event.getScreenX() + xOffset);
                    stage.setY(event.getScreenY() + yOffset);
                });
            }
        }
    }

    private void setupLogoutButton() {
        if (logoutButton != null) {
            Tooltip.install(logoutButton, new Tooltip("Logout"));
            logoutButton.setOnAction(e -> handleLogout());
            logoutButton.setOnMouseEntered(ev -> logoutButton.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 18px; -fx-font-family: Roboto; -fx-min-width: 28; -fx-min-height: 28; -fx-max-width: 28; -fx-max-height: 28; -fx-padding: 0 0 0 8; -fx-cursor: hand; -fx-border-color: transparent;"
            ));
            logoutButton.setOnMouseExited(ev -> logoutButton.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #bbbbbb; -fx-font-size: 18px; -fx-font-family: Roboto; -fx-min-width: 28; -fx-min-height: 28; -fx-max-width: 28; -fx-max-height: 28; -fx-padding: 0 0 0 8; -fx-cursor: hand; -fx-border-color: transparent;"
            ));
        }
    }

    private void setupLeaderboardButton() {
        // Dynamically add a leaderboard button to the top right, next to close button
        if (headerBox != null && leaderboardButton == null) {
            HBox topBar = (HBox) headerBox.getParent().lookup(".top-bar-leaderboard");
            if (topBar == null) {
                topBar = new HBox(8);
                topBar.setAlignment(Pos.CENTER_RIGHT);
                topBar.getStyleClass().add("top-bar-leaderboard");
                Button btn = new Button("🏅"); // simplified emoji
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #f1c40f; -fx-font-size: 22px; -fx-font-family: Roboto; -fx-min-width: 38; -fx-min-height: 38; -fx-max-width: 38; -fx-max-height: 38; -fx-padding: 0; -fx-cursor: hand; -fx-border-color: transparent;");
                btn.setTooltip(new Tooltip("Leaderboard"));
                btn.setOnAction(e -> {
                    if (leaderboardButtonAction != null) leaderboardButtonAction.run();
                });
                btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #f39c12; -fx-font-size: 22px; -fx-font-family: Roboto; -fx-min-width: 38; -fx-min-height: 38; -fx-max-width: 38; -fx-max-height: 38; -fx-padding: 0; -fx-cursor: hand; -fx-border-color: transparent;"));
                btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #f1c40f; -fx-font-size: 22px; -fx-font-family: Roboto; -fx-min-width: 38; -fx-min-height: 38; -fx-max-width: 38; -fx-max-height: 38; -fx-padding: 0; -fx-cursor: hand; -fx-border-color: transparent;"));
                leaderboardButton = btn;
                // Add to the main VBox (root) at the top right
                VBox root = (VBox) headerBox.getParent();
                HBox topHBox = (HBox) root.getChildren().get(0);
                topHBox.getChildren().add(topHBox.getChildren().size() - 1, btn); // before close button
            }
        }
    }

    private void setupDifficultyBox() {
        difficultyBox.getItems().addAll("4×4 (Easy)", "6×4 (Medium)", "6×6 (Hard)");
        difficultyBox.setValue("4×4 (Easy)");
        difficultyBox.setPrefWidth(220);
        
        // Style the ComboBox main button
        difficultyBox.setStyle(
                "-fx-background-color: #222222; " +
                "-fx-text-fill: #FFFFFF; " +
                "-fx-background-radius: 5; " +
                "-fx-border-radius: 5; " +
                "-fx-border-color: #555555; " +
                "-fx-border-width: 1; " +
                "-fx-font-family: Roboto; " +
                "-fx-font-size: 15px; " +
                "-fx-padding: 8 12;"
        );

        // Style the dropdown list cells for dark mode with hover effect
        difficultyBox.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    // Default style
                    setStyle("-fx-background-color: #222222; -fx-text-fill: #FFFFFF; -fx-font-size: 15px; -fx-font-family: Roboto;");
                    // Add hover effect
                    this.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
                        if (isNowHovered) {
                            setStyle("-fx-background-color: #444444; -fx-text-fill: #FFFFFF; -fx-font-size: 15px; -fx-font-family: Roboto;");
                        } else {
                            setStyle("-fx-background-color: #222222; -fx-text-fill: #FFFFFF; -fx-font-size: 15px; -fx-font-family: Roboto;");
                        }
                    });
                }
            }
        });

        // Style the button cell (selected value)
        difficultyBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-background-color: #222222; -fx-text-fill: #FFFFFF; -fx-font-size: 15px; -fx-font-family: Roboto;");
                }
            }
        });

        // Automatically start a new game when a new size is selected
        difficultyBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            initializeGame();
        });
    }

    private void setupRestartButton() {
        restartButton.setText("⬛ NEW GAME"); // changed to a simple dark emoji
        restartButton.setStyle(
                "-fx-background-color: #e74c3c; " +
                "-fx-text-fill: white; " +
                "-fx-font-family: Arial; " +
                "-fx-font-size: 13px; " +
                "-fx-background-radius: 5; " +
                "-fx-border-radius: 5; " +
                "-fx-padding: 12 24; " +
                "-fx-cursor: hand;"
        );
        
        // Add hover effect for restart button
        restartButton.setOnMouseEntered(e -> {
            restartButton.setStyle(
                    "-fx-background-color: #c0392b; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-family: Roboto; " +
                    "-fx-font-size: 13px; " +
                    "-fx-background-radius: 5; " +
                    "-fx-border-radius: 5; " +
                    "-fx-padding: 12 24; " +
                    "-fx-cursor: hand;"
            );
        });
        
        restartButton.setOnMouseExited(e -> {
            restartButton.setStyle(
                    "-fx-background-color: #e74c3c; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-family: Roboto; " +
                    "-fx-font-size: 13px; " +
                    "-fx-background-radius: 5; " +
                    "-fx-border-radius: 5; " +
                    "-fx-padding: 12 24; " +
                    "-fx-cursor: hand;"
            );
        });
    }

    @FXML
    private void handleRestartGame() {
        initializeGame();
    }

    private void initializeGame() {
        // Reset game state
        attempts = 0;
        pairsFound = 0;
        gameActive = true;
        firstCard = null;
        secondCard = null;

        // Get grid dimensions based on difficulty
        int[] dimensions = getGridDimensions();
        int rows = dimensions[0];
        int cols = dimensions[1];
        totalPairs = (rows * cols) / 2;

        // Clear existing cards
        gameGrid.getChildren().clear();
        cards = new ArrayList<>();

        // Configure grid
        gameGrid.setAlignment(Pos.CENTER);
        gameGrid.setHgap(8);
        gameGrid.setVgap(8);

        // Create symbol pairs
        List<String> gameSymbols = new ArrayList<>();
        for (int i = 0; i < totalPairs; i++) {
            gameSymbols.add(SYMBOLS[i]);
            gameSymbols.add(SYMBOLS[i]);
        }
        Collections.shuffle(gameSymbols);

        // Create and place cards
        int symbolIndex = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Card card = new Card(gameSymbols.get(symbolIndex++), this);
                cards.add(card);
                gameGrid.add(card.getButton(), col, row);
            }
        }

        // Adjust window height based on difficulty (only if scene/window is available)
        if (gameGrid.getScene() != null && gameGrid.getScene().getWindow() != null) {
            Stage stage = (Stage) gameGrid.getScene().getWindow();
            String difficulty = difficultyBox.getValue();
            if ("6×6 (Hard)".equals(difficulty)) {
                stage.setHeight(1000);
            } else {
                stage.setHeight(850);
            }
        }

        updateStatusLabels();
    }

    private int[] getGridDimensions() {
        String difficulty = difficultyBox.getValue();
        switch (difficulty) {
            case "6×4 (Medium)": return new int[]{4, 6};
            case "6×6 (Hard)": return new int[]{6, 6};
            default: return new int[]{4, 4};
        }
    }

    private void updateStatusLabels() {
        attemptsLabel.setText(String.valueOf(attempts));
        pairsFoundLabel.setText(pairsFound + "/" + totalPairs);

        if (pairsFound == totalPairs) {
            statusLabel.setText("🎉 Congratulations! Game Complete!");
            statusLabel.setTextFill(Color.web("#2ecc71"));
            // Add to leaderboard
            if (leaderboardConsumer != null) {
                leaderboardConsumer.accept(username, 10000 - attempts * 100, difficultyBox.getValue());
            }
        } else {
            statusLabel.setText("Find all pairs!");
            statusLabel.setTextFill(Color.web("#3498db"));
        }
    }

    public void onCardClick(Card card) {
        if (!gameActive || card.isRevealed() || card.isMatched()) {
            return;
        }

        card.reveal();

        if (firstCard == null) {
            firstCard = card;
        } else if (secondCard == null) {
            secondCard = card;
            attempts++;

            // Check for match after short delay
            PauseTransition pause = new PauseTransition(Duration.millis(1000));
            pause.setOnFinished(e -> checkMatch());
            pause.play();

            gameActive = false; // Prevent more clicks during evaluation
        }
    }

    private void checkMatch() {
        if (firstCard.getSymbol().equals(secondCard.getSymbol())) {
            // Match found
            firstCard.setMatched(true);
            secondCard.setMatched(true);
            pairsFound++;

            statusLabel.setText("✅ Perfect match!");
            statusLabel.setTextFill(Color.web("#56ab2f"));
        } else {
            // No match - hide cards
            firstCard.hide();
            secondCard.hide();

            statusLabel.setText("❌ Try again!");
            statusLabel.setTextFill(Color.web("#FF6B6B"));
        }

        firstCard = null;
        secondCard = null;
        gameActive = true;

        updateStatusLabels();
    }

    public void setUsername(String username) {
        this.username = username;
        if (usernameLabel != null) {
            usernameLabel.setText("⚫ " + username); // changed to a simple dark emoji
        }
    }

    private void handleLogout() {
        // Go back to the login screen in the same window
        Stage stage = (Stage) gameGrid.getScene().getWindow();
        stage.close(); // Close current stage to avoid duplicate Application instance

        // Relaunch the login screen in the same window
        gui.memory.MemoryGameApp app = new gui.memory.MemoryGameApp();
        try {
            app.start(new Stage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TriConsumer for leaderboard callback
    @FunctionalInterface
    public interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }
}