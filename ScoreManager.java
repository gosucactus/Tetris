public class ScoreManager {
    private int playerScore;
    private int currentLevel;
    private int totalLinesCleared;
    private static final int MAX_LEVEL = 15;
    private static final int LINES_PER_LEVEL = 10;

    // Points for different line clears based on guidelines
    private static final int[] LINE_CLEAR_POINTS = {
        0,    // 0 lines - no points
        100,  // Single - 100 points
        300,  // Double - 300 points
        500,  // Triple - 500 points
        800   // Tetris - 800 points
    };

    public ScoreManager() {
        reset();
    }

    public void reset() {
        playerScore = 0;
        currentLevel = 1;
        totalLinesCleared = 0;
    }

    public void addScoreForLines(int linesCleared, int currentLevel) {
        if (linesCleared < 0 || linesCleared > 4) return;
        
        // Add score based on number of lines and current level
        playerScore += LINE_CLEAR_POINTS[linesCleared] * currentLevel;
        
        // Update total lines and check for level up
        totalLinesCleared += linesCleared;
        updateLevel();
    }

    private void updateLevel() {
        // Level increases every 10 lines (can also be adjusted based on starting level)
        int newLevel = (totalLinesCleared / LINES_PER_LEVEL) + 1;
        currentLevel = Math.min(newLevel, MAX_LEVEL);
    }

    // Getters
    public int getScore() { return playerScore; }
    public int getLevel() { return currentLevel; }
    public int getLinesCleared() { return totalLinesCleared; }
}
