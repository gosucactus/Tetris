import java.awt.event.KeyEvent;

public class InputHandler {
    // DAS/ARR (Delayed Auto Shift/Auto Repeat, horizontal movement)
    private double dasDelay = 0.15;
    private double arrInterval = 0.05;
    private double leftHeldTime = 0, rightHeldTime = 0;
    private boolean leftHeld = false, rightHeld = false;

    private boolean softDropping = false;

    private TetrisGame game; // Reference to the main game logic

    public InputHandler(TetrisGame game) {
        this.game = game;
    }

    public void update(double dt) {
        if (game.getGameState().isGameOver() || game.getGameState().isPaused() || game.getGameState().isShowCountdown()) {
            return;
        }

        // DAS/ARR for L/R movement
        if (leftHeld) {
            leftHeldTime += dt;
            if (leftHeldTime >= dasDelay) {
                int repeats = (int)((leftHeldTime - dasDelay) / arrInterval);
                for (int i=0; i<repeats; i++) game.getCurrentPiece().moveLeft();
                leftHeldTime = dasDelay + (leftHeldTime - dasDelay) % arrInterval;
            }
        }
        if (rightHeld) {
            rightHeldTime += dt;
            if (rightHeldTime >= dasDelay) {
                int repeats = (int)((rightHeldTime - dasDelay) / arrInterval);
                for (int i=0; i<repeats; i++) game.getCurrentPiece().moveRight();
                rightHeldTime = dasDelay + (rightHeldTime - dasDelay) % arrInterval;
            }
        }
    }

    public void keyPressed(KeyEvent e) {
        GameState gameState = game.getGameState();
        Piece currentPiece = game.getCurrentPiece();

        if (gameState.isGameOver()) {
            if (e.getKeyCode() == KeyEvent.VK_R) {
                game.restartGame();
            }
            return;
        }

        if (gameState.isShowHelp() && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            gameState.setShowHelp(false);
            gameState.setPaused(true); // Return to pause menu
            return;
        }

        if (!gameState.isShowHelp() && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            gameState.setPaused(!gameState.isPaused());
            if (!gameState.isPaused() && !gameState.isShowCountdown()) { // If unpausing directly
                // gameState.setShowCountdown(true); // Optionally trigger countdown on Esc resume
            }
            return;
        }

        if (gameState.isPaused()) {
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                gameState.previousPauseOption();
            } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                gameState.nextPauseOption();
            } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                String selectedOption = gameState.getSelectedPauseOption();
                switch (selectedOption) {
                    case "Resume":
                        gameState.setPaused(false);
                        gameState.setShowCountdown(true);
                        break;
                    case "Help":
                        gameState.setShowHelp(true);
                        // Paused state remains true, help overlays pause menu or replaces it visually
                        break;
                    case "Quit":
                        System.exit(0);
                        break;
                }
            }
            return;
        }

        // Active game key presses
        if (gameState.isShowCountdown()) return; // No game actions during countdown

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                leftHeld = true;
                leftHeldTime = 0;
                if (currentPiece != null) currentPiece.moveLeft();
                break;
            case KeyEvent.VK_RIGHT:
                rightHeld = true;
                rightHeldTime = 0;
                if (currentPiece != null) currentPiece.moveRight();
                break;
            case KeyEvent.VK_DOWN:
                softDropping = true;
                if (currentPiece != null && !currentPiece.isLanded()) {
                    currentPiece.moveDown(); // Move one step immediately
                    game.resetFallTimer(); // Reset fall timer to reflect soft drop
                }
                break;
            case KeyEvent.VK_SPACE:
                if (currentPiece != null) game.hardDrop();
                break;
            case KeyEvent.VK_UP:    // Clockwise rotation
                game.rotatePiece(true);
                break;
            case KeyEvent.VK_Z:     // Counter-clockwise rotation
                game.rotatePiece(false);
                break;
        }
    }

    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                leftHeld = false;
                leftHeldTime = 0; // Reset time on release
                break;
            case KeyEvent.VK_RIGHT:
                rightHeld = false;
                rightHeldTime = 0; // Reset time on release
                break;
            case KeyEvent.VK_DOWN:
                softDropping = false;
                break;
        }
    }

    public boolean isSoftDropping() {
        return softDropping;
    }

    public void resetDAS() {
        leftHeld = false;
        rightHeld = false;
        leftHeldTime = 0;
        rightHeldTime = 0;
        softDropping = false;
    }
}