package gui.memory;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class MemoryGameApp extends Application {
    private Stage primaryStage;

    private HashMap<String, SceneData> scenes = new HashMap<>();

    // For window dragging
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.initStyle(StageStyle.UNDECORATED);

        scenes.put("usernameScene", loadScene("usernameScene.fxml"));
        scenes.put("leaderboardScene", loadScene("leaderboardScene.fxml"));
        scenes.put("gameScene", loadScene("gameScene.fxml"));

        primaryStage.setScene(scenes.get("usernameScene").getScene());
        primaryStage.setTitle("Memory Game");
        primaryStage.show();

    }

    private SceneData loadScene(String fxmlFile) {
        try {
            URL url = getClass().getResource(fxmlFile);
            if (url == null) {
                throw new FileNotFoundException("FXML file not found: MemoryGameApp.fxml");
            }

            FXMLLoader loader = new FXMLLoader(url);
            System.out.println("Loading FXML file: " + fxmlFile);
            AnchorPane root =  loader.load();

            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.web("#1a1a1a"));

            gameController controller = loader.getController();
            controller.setWindowDragHandlers(primaryStage, loader.getRoot());
            controller.setSceneChanger(this::setScene);
            controller.setOnExit(primaryStage::close);

            return new SceneData(scene, controller);
        } catch (IOException e) {
            System.out.println("Error loading FXML file: " + fxmlFile);
        }
        return null;
    }

    public void setScene(String sceneName) {
        Scene scene = scenes.get(sceneName).getScene();
        gameController controller = scenes.get(sceneName).getController();
        if (scene != null) {
            primaryStage.setScene(scene);
            controller.updateWindow();
        } else {
            System.out.println("Scene not found: " + sceneName);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}