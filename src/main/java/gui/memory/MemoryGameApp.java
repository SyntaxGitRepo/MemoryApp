package gui.memory;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class MemoryGameApp extends Application {
    private Stage primaryStage;
    private static final String LEADERBOARD_FILE = "/gui/memory/leaderboard.json";
    private static final Map<String, ScoreEntry> leaderboard = new HashMap<>();

    // For window dragging
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.initStyle(StageStyle.UNDECORATED);
        loadLeaderboard();
        showUsernameScreen();
    }

    private void enableWindowDrag(Pane dragPane) {
        dragPane.setOnMousePressed(event -> {
            xOffset = primaryStage.getX() - event.getScreenX();
            yOffset = primaryStage.getY() - event.getScreenY();
        });
        dragPane.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() + xOffset);
            primaryStage.setY(event.getScreenY() + yOffset);
        });
    }

    private void showUsernameScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("UsernameScreen.fxml"));
            AnchorPane root = loader.load();
            UsernameScreenController controller = loader.getController();

            // Set callbacks for continue and leaderboard actions
            controller.setOnContinue(username -> showGameScreen(username));
            controller.setOnLeaderboard(() -> showLeaderboardScreen(this::showUsernameScreen));
            controller.setOnExit(() -> primaryStage.close());
            controller.setWindowDragHandlers(primaryStage);

            Scene scene = new Scene(root, 650, 850);
            scene.setFill(javafx.scene.paint.Color.web("#1a1a1a"));
            primaryStage.setTitle("Memory Game - Dark Mode");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showGameScreen(String username) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("MemoryGame.fxml"));
            Scene scene = new Scene(loader.load(), 650, 850);
            scene.setFill(javafx.scene.paint.Color.web("#1a1a1a"));

            MemoryGameController controller = loader.getController();
            controller.setUsername(username);
            controller.setLeaderboardConsumer(this::addScoreAndShowLeaderboard);
            controller.setLeaderboardButtonAction(() -> showLeaderboardScreen(() -> showGameScreen(username)));

            primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showLeaderboardScreen(Runnable onBack) {
        VBox root = new VBox(30);
        root.setStyle("-fx-background-color: #181818; -fx-padding: 40;");
        root.setAlignment(javafx.geometry.Pos.TOP_CENTER);

        // Exit button (top right)
        HBox topBar = new HBox();
        topBar.setAlignment(javafx.geometry.Pos.TOP_RIGHT);
        Button exitBtn = new Button("X");
        exitBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 22px; -fx-font-family: Roboto; -fx-min-width: 38; -fx-min-height: 38; -fx-max-width: 38; -fx-max-height: 38; -fx-padding: 0; -fx-cursor: hand; -fx-border-color: transparent;");
        exitBtn.setOnAction(e -> primaryStage.close());
        exitBtn.setOnMouseEntered(ev -> exitBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #c0392b; -fx-font-size: 22px; -fx-font-family: Roboto; -fx-min-width: 38; -fx-min-height: 38; -fx-max-width: 38; -fx-max-height: 38; -fx-padding: 0; -fx-cursor: hand; -fx-border-color: transparent;"));
        exitBtn.setOnMouseExited(ev -> exitBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 22px; -fx-font-family: Roboto; -fx-min-width: 38; -fx-min-height: 38; -fx-max-width: 38; -fx-max-height: 38; -fx-padding: 0; -fx-cursor: hand; -fx-border-color: transparent;"));
        topBar.getChildren().add(exitBtn);

        // Enable window drag on topBar and background
        enableWindowDrag(topBar);
        enableWindowDrag(root);

        Label title = new Label("🏅 Leaderboard");
        title.setStyle("-fx-text-fill: #fff; -fx-font-size: 32px; -fx-font-family: Roboto; -fx-padding: 0 0 20 0;");

        VBox board = new VBox(10);
        board.setStyle("-fx-background-color: #232323; -fx-background-radius: 10; -fx-padding: 30 40 30 40;");
        board.setAlignment(javafx.geometry.Pos.CENTER);

        // Always reload leaderboard from file to ensure it's up-to-date
        loadLeaderboard();

        // Sort and show all users by score descending
        List<ScoreEntry> sorted = leaderboard.values().stream()
            .sorted(Comparator.comparingInt(ScoreEntry::score).reversed())
            .collect(Collectors.toList());

        int rank = 1;
        for (ScoreEntry entry : sorted) {
            HBox row = new HBox(20);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label rankLabel = new Label("#" + rank++);
            rankLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 18px; -fx-font-family: Roboto; -fx-min-width: 40;");
            Label nameLabel = new Label(entry.username);
            nameLabel.setStyle("-fx-text-fill: #fff; -fx-font-size: 18px; -fx-font-family: Roboto;");
            Label scoreLabel = new Label("Score: " + entry.score());
            scoreLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 18px; -fx-font-family: Roboto;");
            Label modeLabel = new Label(entry.mode);
            modeLabel.setStyle("-fx-text-fill: #bbbbbb; -fx-font-size: 14px; -fx-font-family: Roboto;");

            row.getChildren().addAll(rankLabel, nameLabel, scoreLabel, modeLabel);
            board.getChildren().add(row);
        }
        if (leaderboard.isEmpty()) {
            Label empty = new Label("No scores yet. Play a game!");
            empty.setStyle("-fx-text-fill: #bbbbbb; -fx-font-size: 16px; -fx-font-family: Roboto;");
            board.getChildren().add(empty);
        }

        Button backBtn = new Button("Back");
        backBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-family: Roboto; -fx-font-size: 15px; -fx-background-radius: 5; -fx-border-radius: 5; -fx-padding: 10 24; -fx-cursor: hand;");
        backBtn.setOnAction(e -> onBack.run());

        root.getChildren().addAll(topBar, title, board, backBtn);

        Scene scene = new Scene(root, 650, 850);
        scene.setFill(javafx.scene.paint.Color.web("#181818"));
        primaryStage.setScene(scene);
    }

    // Called by controller when a score should be added and leaderboard shown
    private void addScoreAndShowLeaderboard(String username, int score, String mode) {
        // Only update if new score is higher
        ScoreEntry existing = leaderboard.get(username);
        if (existing == null || score > existing.score) {
            leaderboard.put(username, new ScoreEntry(username, score, mode));
            saveLeaderboard();
        }
        // Go back to the main screen (not login) after leaderboard
        showLeaderboardScreen(() -> showGameScreen(username));
    }

    private void loadLeaderboard() {
        leaderboard.clear();
        try {
            // Try to load from resources (classpath)
            InputStream in = getClass().getResourceAsStream(LEADERBOARD_FILE);
            if (in == null) {
                // If not found, try to load from file system (for write-back)
                Path path = Paths.get("src/main/resources" + LEADERBOARD_FILE);
                if (!Files.exists(path)) return;
                in = Files.newInputStream(path);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line.trim());
            }
            reader.close();
            // Very simple JSON parsing (no external libs)
            // Format: { "username": {"score":123,"mode":"6x6"}, ... }
            String content = json.toString().replaceAll("[\\{\\}\"]", "");
            if (!content.isEmpty()) {
                // Improved: split on '", "' (comma between entries), not on '},'
                String[] entries = content.split("\",\\s*\"");
                for (String entry : entries) {
                    entry = entry.trim();
                    // Remove possible leading/trailing quotes and braces
                    if (entry.startsWith("\"")) entry = entry.substring(1);
                    if (entry.endsWith("}")) entry = entry.substring(0, entry.length() - 1);
                    // Now entry is like: username: score:8100,mode:4×4 (Easy)
                    String[] parts = entry.split(":", 2);
                    if (parts.length < 2) continue;
                    String user = parts[0].trim();
                    String[] fields = parts[1].replace("{", "").replace("}", "").split(",");
                    int score = 0;
                    String mode = "";
                    for (String field : fields) {
                        String[] kv = field.split(":", 2);
                        if (kv.length < 2) continue;
                        String key = kv[0].trim();
                        String value = kv[1].trim();
                        if (key.equals("score")) score = Integer.parseInt(value);
                        if (key.equals("mode")) mode = value;
                    }
                    leaderboard.put(user, new ScoreEntry(user, score, mode));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveLeaderboard() {
        Path path = Paths.get("src/main/resources" + LEADERBOARD_FILE);
        try {
            // Ensure parent directories exist
            Files.createDirectories(path.getParent());
            BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
            writer.write("{\n");
            int i = 0;
            for (ScoreEntry entry : leaderboard.values()) {
                writer.write("  \"" + entry.username + "\": {\"score\":" + entry.score + ",\"mode\":\"" + entry.mode + "\"}");
                if (i++ < leaderboard.size() - 1) writer.write(",");
                writer.write("\n");
            }
            writer.write("}\n");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}