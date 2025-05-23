public class ScoreManager {
    private int playerScore;
    private int currentLevel;

    public ScoreManager() {
        playerScore = 0;
        currentLevel = 1;
    }

    public void addScoreForLines(int linesCleared, int level) {
        int basePoints = 1500; // Or some other base value
        int pointsEarned = 0;
        switch (linesCleared) {
            case 1: pointsEarned = basePoints * level; break;
            case 2: pointsEarned = (basePoints * 2) * level; break;
            case 3: pointsEarned = (basePoints * 3) * level; break;
            case 4: pointsEarned = (basePoints * 5) * level; break;
        }
        playerScore += pointsEarned;
        updateLevel();
    }

    private void updateLevel() {
        // Example logic: increase level every 5000 points
        int newLevel = (playerScore / 5000) + 1;
        if (newLevel > currentLevel) {
            currentLevel = newLevel;
            // increase game speed here or notify GameState/TetrisGame
        }
    }
    
    public void reset() {
        playerScore = 0;
        currentLevel = 1;
    }

    public int getScore() { return playerScore; }
    public int getLevel() { return currentLevel; }
}