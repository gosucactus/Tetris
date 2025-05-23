public class GameState {
    private boolean isGameOver;
    private boolean isPaused;
    private boolean showHelp;
    private boolean showCountdown;
    private boolean showModeSelect = true; // Start with mode selection
    private boolean isSinglePlayerMode = true; // Default to single player
    private boolean isResumingFromPause = false;

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
        showModeSelect = true;
        isResumingFromPause = false;
        pauseMenuSelection = 0;
    }

    // Getters and Setters
    public boolean isGameOver() { return isGameOver; }
    public void setGameOver(boolean gameOver) { isGameOver = gameOver; }

    public boolean isPaused() { return isPaused; }
    public void setPaused(boolean paused) { 
        if (isPaused && !paused) { // If we're unpausing
            isResumingFromPause = true;
            showCountdown = true;
            countdownStartTime = System.currentTimeMillis();
        }
        isPaused = paused; 
    }

    public boolean isResumingFromPause() { return isResumingFromPause; }
    public void setResumingFromPause(boolean resuming) { isResumingFromPause = resuming; }

    public boolean isShowHelp() { return showHelp; }
    public void setShowHelp(boolean showHelp) { this.showHelp = showHelp; }

    public boolean isShowModeSelect() { return showModeSelect; }
    public void setShowModeSelect(boolean showModeSelect) { this.showModeSelect = showModeSelect; }

    public boolean isSinglePlayerMode() { return isSinglePlayerMode; }
    public void setSinglePlayerMode(boolean singlePlayerMode) { 
        this.isSinglePlayerMode = singlePlayerMode;
    }

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
                isResumingFromPause = false; // Reset the resuming flag when countdown ends
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