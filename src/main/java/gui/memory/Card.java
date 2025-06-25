package gui.memory;

import javafx.scene.control.Button;

public class Card {
    private Button button;
    private String symbol;
    private boolean revealed = false;
    private boolean matched = false;
    private MemoryGameController controller;

    public Card(String symbol, MemoryGameController controller) {
        this.symbol = symbol;
        this.controller = controller;
        this.button = new Button();
        setupButton();
    }

    private void setupButton() {
        button.setPrefSize(90, 90);
        button.setFocusTraversable(false);
        button.getStyleClass().add("card-button");
        button.setText(""); // Hidden state: no symbol
        button.setOnAction(e -> controller.onCardClick(this));

        button.setOnMouseEntered(e -> {
            if (!revealed && !matched) {
                button.getStyleClass().removeAll("card-button", "card-flipped", "card-matched", "card-wrong");
                button.getStyleClass().add("card-button");
                // Hover effect is handled by CSS :hover
            }
        });
        button.setOnMouseExited(e -> {
            if (!revealed && !matched) {
                button.getStyleClass().removeAll("card-button", "card-flipped", "card-matched", "card-wrong");
                button.getStyleClass().add("card-button");
            }
        });
    }

    public void reveal() {
        revealed = true;
        button.getStyleClass().removeAll("card-button", "card-flipped", "card-matched", "card-wrong");
        button.getStyleClass().add("card-flipped");
        button.setText(symbol);
    }

    public void hide() {
        revealed = false;
        if (!matched) {
            button.getStyleClass().removeAll("card-flipped", "card-matched", "card-wrong");
            if (!button.getStyleClass().contains("card-button")) {
                button.getStyleClass().add("card-button");
            }
            button.setText(""); // Hide symbol
        }
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
        if (matched) {
            button.getStyleClass().removeAll("card-button", "card-flipped", "card-wrong");
            button.getStyleClass().add("card-matched");
            button.setText(symbol);
        } else {
            hide();
        }
    }

    // Optional: show wrong state for mismatched cards
    public void showWrong() {
        if (matched) return;
        button.getStyleClass().removeAll("card-button", "card-flipped", "card-matched");
        button.getStyleClass().add("card-wrong");
        button.setText(symbol);
    }

    // Getters
    public Button getButton() { return button; }
    public String getSymbol() { return symbol; }
    public boolean isRevealed() { return revealed; }
    public boolean isMatched() { return matched; }
}
