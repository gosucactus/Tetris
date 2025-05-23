public class ScoreManager {
    private int playerScore;
    private int currentLevel;
    private int totalLinesCleared;
    private static final int MAX_LEVEL = 15;
    private static final int LINES_PER_LEVEL = 10;

    public ScoreManager() {
        reset();
    }

    public void addScoreForLines(int linesCleared, int level) {
        // Base points per line clear type
        int basePoints = 0;
        switch (linesCleared) {
            case 1: basePoints = 100; break;  // Single
            case 2: basePoints = 300; break;  // Double
            case 3: basePoints = 500; break;  // Triple
            case 4: basePoints = 800; break;  // Tetris
        }
        
        // Multiply points by level
        playerScore += basePoints * level;
        
        // Update total lines and check for level up
        totalLinesCleared += linesCleared;
        updateLevel();
    }

    private void updateLevel() {
        // Level increases every 10 lines, max level is 15
        int newLevel = Math.min(MAX_LEVEL, 1 + (totalLinesCleared / LINES_PER_LEVEL));
        if (newLevel > currentLevel) {
            currentLevel = newLevel;
        }
    }

    public void reset() {
        playerScore = 0;
        currentLevel = 1;
        totalLinesCleared = 0;
    }

    public int getScore() { return playerScore; }
    public int getLevel() { return currentLevel; }
    public int getTotalLines() { return totalLinesCleared; }
}