package gui.memory;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.ListCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class MemoryGameController extends gameController implements Initializable {
    @FXML public AnchorPane rootPane;
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
    @FXML private Button leaderboardButton;

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
    
    // Add this field:
    private TriConsumer<String, Integer, String> leaderboardConsumer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        closeButton.setOnAction(event -> {onExit.run();});
        logoutButton.setOnAction(event -> {sceneChanger.accept("usernameScene");});
        leaderboardButton.setOnAction(event -> {
            sceneChanger.accept("leaderboardScene");
            Data.fromScene = "gameScene";
        });

        // Set leaderboardConsumer to add score and show leaderboard
        leaderboardConsumer = (username, score, mode) -> {
            // Add score to leaderboard and show leaderboard screen
            LeaderboardController.addScoreStatic(username, score, mode);
            sceneChanger.accept("leaderboardScene");
            Data.fromScene = "gameScene";
        };

        setupDifficultyBox();
        initializeGame();
    }

    private void setupDifficultyBox() {
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
        return switch (difficulty) {
            case "6×4 (Medium)" -> new int[]{4, 6};
            case "6×6 (Hard)" -> new int[]{6, 6};
            default -> new int[]{4, 4};
        };
    }

    private void updateStatusLabels() {
        attemptsLabel.setText(String.valueOf(attempts));
        pairsFoundLabel.setText(pairsFound + "/" + totalPairs);

        if (pairsFound == totalPairs) {
            statusLabel.setText("🎉 Congratulations! Game Complete!");
            statusLabel.setTextFill(Color.web("#2ecc71"));
            gameActive = false;
            
            // Add to leaderboard and refresh leaderboard view
            if (leaderboardConsumer != null) {
                leaderboardConsumer.accept(Data.username, 10000 - attempts * 100, difficultyBox.getValue());
                // Ensure leaderboard is refreshed when shown
                sceneChanger.accept("leaderboardScene");
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

    @Override
    public void updateWindow() {
        usernameLabel.setText(Data.username);
        initializeGame();
    }

    // Add this interface at the end of the file (or in a shared location):
    @FunctionalInterface
    interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }
}