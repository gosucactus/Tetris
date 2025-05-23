public class GameState {
    private boolean isGameOver;
    private boolean isPaused;
    private boolean showHelp;
    private boolean showCountdown;
    // private boolean isResuming; // This might be part of showCountdown logic

    private int pauseMenuSelection;
    private String[] pauseMenuOptions = {"Resume", "Help", "Quit"};

    private long countdownStartTime;
    private int countdownSeconds = 3;

    public GameState() {
        reset();
    }

    public void reset() {
        isGameOver = false;
        isPaused = false;
        showHelp = false;
        showCountdown = false;
        pauseMenuSelection = 0;
    }

    // Getters and Setters
    public boolean isGameOver() { return isGameOver; }
    public void setGameOver(boolean gameOver) { isGameOver = gameOver; }

    public boolean isPaused() { return isPaused; }
    public void setPaused(boolean paused) { isPaused = paused; }

    public boolean isShowHelp() { return showHelp; }
    public void setShowHelp(boolean showHelp) { this.showHelp = showHelp; }

    public boolean isShowCountdown() { return showCountdown; }
    public void setShowCountdown(boolean showCountdown) {
        this.showCountdown = showCountdown;
        if (showCountdown) {
            countdownStartTime = System.currentTimeMillis();
        }
    }

    public void updateCountdown() {
        if (showCountdown) {
            long elapsed = (long) ((System.currentTimeMillis() - countdownStartTime) / (1000 / 1.5));
            if (elapsed >= countdownSeconds) {
                showCountdown = false;
            }
        }
    }

    public long getCountdownRemaining() {
        if (!showCountdown) return 0;
        long elapsed = (long)((System.currentTimeMillis() - countdownStartTime) / (1000 / 1.5));
        return Math.max(0, countdownSeconds - elapsed);
    }


    public int getPauseMenuSelection() { return pauseMenuSelection; }
    public String[] getPauseMenuOptions() { return pauseMenuOptions; }

    public void nextPauseOption() {
        pauseMenuSelection = (pauseMenuSelection + 1) % pauseMenuOptions.length;
    }

    public void previousPauseOption() {
        pauseMenuSelection = (pauseMenuSelection + pauseMenuOptions.length - 1) % pauseMenuOptions.length;
    }

    public String getSelectedPauseOption() {
        return pauseMenuOptions[pauseMenuSelection];
    }
}