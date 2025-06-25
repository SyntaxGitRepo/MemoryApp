package gui.memory;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.function.Consumer;

public abstract class gameController {
    protected double xOffset = 0;
    protected double yOffset = 0;

    protected Runnable onExit;
    protected Consumer<String> sceneChanger;

    public void setWindowDragHandlers(Stage stage, Pane rootPane) {
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

    public void setOnExit(Runnable onExit) {
        this.onExit = onExit;
    }
    public void setSceneChanger(Consumer<String> callback) {
        this.sceneChanger = callback;
    }

    public abstract void updateWindow();
}
