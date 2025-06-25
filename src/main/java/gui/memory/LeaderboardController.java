package gui.memory;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardController extends gameController {
    @FXML private AnchorPane rootPane;
    @FXML private HBox topBar;
    @FXML private Button exitBtn;
    @FXML private VBox board;
    @FXML private Button backBtn;
    @FXML private Label title;
    @FXML private GridPane leaderboardGrid;

    private static final Map<String, ScoreEntry> leaderboard = new HashMap<>();

    public void setLeaderboardData(List<ScoreEntry> entries) {
        // Remove all rows except the header (row 0)
        leaderboardGrid.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);

        if (entries == null) return;

        List<ScoreEntry> sorted = entries.stream()
            .sorted(Comparator.comparingInt(ScoreEntry::score).reversed())
            .collect(Collectors.toList());

        int row = 1;
        int rank = 1;
        for (ScoreEntry entry : sorted) {
            Label rankLabel = new Label("#" + rank);
            rankLabel.getStyleClass().addAll("leaderboard-row-label", "leaderboard-row-rank");
            if (rank == 1) rankLabel.getStyleClass().add("top1");
            else if (rank == 2) rankLabel.getStyleClass().add("top2");
            else if (rank == 3) rankLabel.getStyleClass().add("top3");

            Label nameLabel = new Label(entry.username);
            nameLabel.getStyleClass().addAll("leaderboard-row-label", "leaderboard-row-name");
            Label scoreLabel = new Label(String.valueOf(entry.score()));
            scoreLabel.getStyleClass().addAll("leaderboard-row-label", "leaderboard-row-score");
            Label modeLabel = new Label(entry.mode);
            modeLabel.getStyleClass().addAll("leaderboard-row-label", "leaderboard-row-mode");

            // Zebra striping
            String parity = (row % 2 == 0) ? "even" : "odd";
            rankLabel.getStyleClass().add(parity);
            nameLabel.getStyleClass().add(parity);
            scoreLabel.getStyleClass().add(parity);
            modeLabel.getStyleClass().add(parity);

            leaderboardGrid.add(rankLabel, 0, row);
            leaderboardGrid.add(nameLabel, 1, row);
            leaderboardGrid.add(scoreLabel, 2, row);
            leaderboardGrid.add(modeLabel, 3, row);
            row++;
            rank++;
        }
        if (sorted.isEmpty()) {
            Label empty = new Label("No scores yet. Play a game!");
            empty.setStyle("-fx-text-fill: #bbbbbb; -fx-font-size: 16px; -fx-font-family: Roboto;");
            board.getChildren().setAll(empty);
        } else {
            board.getChildren().setAll(leaderboardGrid);
        }
    }

    @FXML
    private void initialize() {
        exitBtn.setOnAction(e -> onExit.run());
        backBtn.setOnAction(e -> sceneChanger.accept(Data.fromScene));
        loadLeaderboard();
    }

    @Override
    public void updateWindow() {
        loadLeaderboard();
    }

    private void loadLeaderboard() {
        leaderboard.clear();
        try {
            InputStream in = getClass().getResourceAsStream(Data.LEADERBOARD_FILE);
            if (in == null) {
                Path path = Paths.get("src/main/resources" + Data.LEADERBOARD_FILE);
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

            JSONObject obj = new JSONObject(json.toString());
            for (String user : obj.keySet()) {
                JSONObject entry = obj.getJSONObject(user);
                int score = entry.getInt("score");
                String mode = entry.getString("mode");
                leaderboard.put(user, new ScoreEntry(user, score, mode));
            }

            leaderboardGrid.getChildren().clear();
            // Add header row (already in FXML)
            int i = 1;
            for (Map.Entry<String, ScoreEntry> entry : leaderboard.entrySet().stream()
                    .sorted((a, b) -> Integer.compare(b.getValue().score, a.getValue().score))
                    .collect(Collectors.toList())) {
                ScoreEntry scoreEntry = entry.getValue();
                Label rankLabel = new Label("#" + i);
                rankLabel.getStyleClass().addAll("leaderboard-row-label", "leaderboard-row-rank");
                if (i == 1) rankLabel.getStyleClass().add("top1");
                else if (i == 2) rankLabel.getStyleClass().add("top2");
                else if (i == 3) rankLabel.getStyleClass().add("top3");

                Label nameLabel = new Label(scoreEntry.username);
                nameLabel.getStyleClass().addAll("leaderboard-row-label", "leaderboard-row-name");
                Label scoreLabel = new Label(String.valueOf(scoreEntry.score));
                scoreLabel.getStyleClass().addAll("leaderboard-row-label", "leaderboard-row-score");
                Label modeLabel = new Label(scoreEntry.mode);
                modeLabel.getStyleClass().addAll("leaderboard-row-label", "leaderboard-row-mode");

                // Zebra striping
                String parity = (i % 2 == 0) ? "even" : "odd";
                rankLabel.getStyleClass().add(parity);
                nameLabel.getStyleClass().add(parity);
                scoreLabel.getStyleClass().add(parity);
                modeLabel.getStyleClass().add(parity);

                leaderboardGrid.add(rankLabel, 0, i);
                leaderboardGrid.add(nameLabel, 1, i);
                leaderboardGrid.add(scoreLabel, 2, i);
                leaderboardGrid.add(modeLabel, 3, i);
                i++;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void addScoreAndShowLeaderboard(String username, int score, String mode) {
        // Only update if new score is higher
        ScoreEntry existing = leaderboard.get(username);
        if (existing == null || score > existing.score) {
            leaderboard.put(username, new ScoreEntry(username, score, mode));
            saveLeaderboard();
        }
    }

    private void saveLeaderboard() {
        Path path = Paths.get("src/main/resources" + Data.LEADERBOARD_FILE);
        try {
            Files.createDirectories(path.getParent());
            JSONObject obj = new JSONObject();
            for (ScoreEntry entry : leaderboard.values()) {
                JSONObject scoreObj = new JSONObject();
                scoreObj.put("score", entry.score);
                scoreObj.put("mode", entry.mode);
                obj.put(entry.username, scoreObj);
            }
            BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
            writer.write(obj.toString(2));
            writer.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    // Add this static method:
    public static void addScoreStatic(String username, int score, String mode) {
        ScoreEntry existing = leaderboard.get(username);
        if (existing == null || score > existing.score) {
            leaderboard.put(username, new ScoreEntry(username, score, mode));
            // Save immediately
            Path path = Paths.get("src/main/resources" + Data.LEADERBOARD_FILE);
            try {
                Files.createDirectories(path.getParent());
                JSONObject obj = new JSONObject();
                for (ScoreEntry entry : leaderboard.values()) {
                    JSONObject scoreObj = new JSONObject();
                    scoreObj.put("score", entry.score);
                    scoreObj.put("mode", entry.mode);
                    obj.put(entry.username, scoreObj);
                }
                BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
                writer.write(obj.toString(2));
                writer.close();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }
}