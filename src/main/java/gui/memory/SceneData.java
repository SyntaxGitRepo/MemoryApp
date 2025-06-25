package gui.memory;

import javafx.scene.Scene;

public class SceneData {
    private Scene scene;
    private gameController controller;

    public SceneData(Scene scene, gameController controller) {
        this.scene = scene;
        this.controller = controller;
    }

    public Scene getScene() {
        return scene;
    }
    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public gameController getController() {
        return controller;
    }
    public void setController(gameController controller) {
        this.controller = controller;
    }
}
