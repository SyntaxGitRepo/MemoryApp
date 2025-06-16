package gui.memory;

public class ScoreEntry {
    public final String username;
    public final int score;
    public final String mode;
    public ScoreEntry(String username, int score, String mode) {
        this.username = username;
        this.score = score;
        this.mode = mode;
    }
    public int score() { return score; }
}