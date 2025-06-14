package gui.memory;

import javafx.animation.*;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class Card {
    private Button button;
    private String symbol;
    private boolean revealed = false;
    private boolean matched = false;
    private MemoryGameController controller;

    // Dark mode color palette - simple solid colors
    private static final String HIDDEN_COLOR = "#333333";
    private static final String REVEALED_COLOR = "#3498db";
    private static final String MATCHED_COLOR = "#27ae60";
    private static final String HOVER_COLOR = "#555555";

    public Card(String symbol, MemoryGameController controller) {
        this.symbol = symbol;
        this.controller = controller;
        this.button = new Button();
        setupButton();
    }

    private void setupButton() {
        button.setPrefSize(90, 90);
        button.setFont(Font.font("Arial", 28));
        button.setText("?");

        applyHiddenStyle();
        setupHoverEffects();
        button.setOnAction(e -> controller.onCardClick(this));
    }

    private void applyHiddenStyle() {
        button.setStyle(
                "-fx-background-color: " + HIDDEN_COLOR + "; " +
                "-fx-text-fill: #FFFFFF; " +
                "-fx-background-radius: 5; " +
                "-fx-border-radius: 5; " +
                "-fx-border-color: #555555; " +
                "-fx-border-width: 1; " +
                "-fx-font-family: Arial; " +
                "-fx-cursor: hand;"
        );
    }

    private void setupHoverEffects() {
        button.setOnMouseEntered(e -> {
            if (!revealed && !matched) {
                button.setStyle(
                        "-fx-background-color: " + HOVER_COLOR + "; " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-background-radius: 5; " +
                        "-fx-border-radius: 5; " +
                        "-fx-border-color: #777777; " +
                        "-fx-border-width: 1; " +
                        "-fx-font-family: Arial; " +
                        "-fx-cursor: hand;"
                );
            }
        });

        button.setOnMouseExited(e -> {
            if (!revealed && !matched) {
                applyHiddenStyle();
            }
        });
    }

    public void reveal() {
        revealed = true;
        button.setText(symbol);
        button.setStyle(
                "-fx-background-color: " + REVEALED_COLOR + "; " +
                "-fx-text-fill: #FFFFFF; " +
                "-fx-background-radius: 5; " +
                "-fx-border-radius: 5; " +
                "-fx-border-color: #555555; " +
                "-fx-border-width: 1; " +
                "-fx-font-family: Arial;"
        );
    }

    public void hide() {
        revealed = false;
        button.setText("?");
        applyHiddenStyle();
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
        if (matched) {
            button.setStyle(
                    "-fx-background-color: " + MATCHED_COLOR + "; " +
                    "-fx-text-fill: #FFFFFF; " +
                    "-fx-background-radius: 5; " +
                    "-fx-border-radius: 5; " +
                    "-fx-border-color: #555555; " +
                    "-fx-border-width: 1; " +
                    "-fx-font-family: Arial;"
            );
        }
    }

    // Getters
    public Button getButton() { return button; }
    public String getSymbol() { return symbol; }
    public boolean isRevealed() { return revealed; }
    public boolean isMatched() { return matched; }
}