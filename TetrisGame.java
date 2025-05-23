import java.awt.Color;
import java.awt.event.KeyEvent;

public class TetrisGame extends GameEngine {

    // Player 1 components
    private Board boardP1;
    private Piece currentPieceP1;
    private ScoreManager scoreManagerP1;
    private InputHandler inputHandlerP1;

    // Player 2 components
    private Board boardP2;
    private Piece currentPieceP2;
    private ScoreManager scoreManagerP2;
    private InputHandler inputHandlerP2;

    // Shared components
    private GameState gameState;
    private Renderer renderer;

    // Tile palette
    private Color[] tileColors = {black, cyan, blue, orange, yellow, green, pink, red}; // From GameEngine

    // Fall & lock timers for both players
    private double fallIntervalP1 = 1.0;
    private double fallTimerP1 = 0.0;
    private double fallIntervalP2 = 1.0;
    private double fallTimerP2 = 0.0;
    private final double lockDelay = 0.5;
    private double lockTimerP1 = 0.0;
    private double lockTimerP2 = 0.0;

    public static void main(String[] args) {
        createGame(new TetrisGame(), 30); // Framerate
    }

    @Override
    public void init() {
        setWindowSize(350, 420); // Start with single player width
        
        // Initialize Player 1 components
        boardP1 = new Board();
        scoreManagerP1 = new ScoreManager();
        inputHandlerP1 = new InputHandler(this, 1);
        currentPieceP1 = new Piece(boardP1);

        // Initialize Player 2 components
        boardP2 = new Board();
        scoreManagerP2 = new ScoreManager();
        inputHandlerP2 = new InputHandler(this, 2);
        currentPieceP2 = new Piece(boardP2);

        // Initialize shared components
        gameState = new GameState();
        renderer = new Renderer(this, boardP1, boardP2, currentPieceP1, currentPieceP2, 
                              scoreManagerP1, scoreManagerP2, gameState, tileColors);
    }
    
    public void resetFallTimer(int player) {
        if (player == 1) {
            this.fallTimerP1 = 0;
        } else {
            this.fallTimerP2 = 0;
        }
    }

    public void restartGame() {
        if (gameState.isSinglePlayerMode()) {
            startGame(true);
        } else {
            startGame(false);
        }
    }
    
    private void spawnNewPiece(int player) {
        if (player == 1) {
            currentPieceP1.spawnNewPiece();
            if (currentPieceP1.checkSpawnCollision()) {
                gameState.setGameOver(true);
            }
            fallTimerP1 = 0;
            lockTimerP1 = 0;
        } else {
            currentPieceP2.spawnNewPiece();
            if (currentPieceP2.checkSpawnCollision()) {
                gameState.setGameOver(true);
            }
            fallTimerP2 = 0;
            lockTimerP2 = 0;
        }
    }

    private void updateFallInterval(int player) {
        double baseInterval = 1.0;
        if (player == 1) {
            int level = scoreManagerP1.getLevel();
            fallIntervalP1 = Math.pow(0.8 - ((level - 1) * 0.007), level - 1) * baseInterval;
            fallIntervalP1 = Math.max(0.05, fallIntervalP1);
        } else {
            int level = scoreManagerP2.getLevel();
            fallIntervalP2 = Math.pow(0.8 - ((level - 1) * 0.007), level - 1) * baseInterval;
            fallIntervalP2 = Math.max(0.05, fallIntervalP2);
        }
    }

    @Override
    public void update(double dt) {
        if (gameState.isShowModeSelect()) {
            return;
        }

        double maxDt = 0.1;
        if(dt > maxDt) dt = maxDt;

        gameState.updateCountdown();

        // Don't update game logic during pause, countdown, or help screen
        if (gameState.isPaused() || gameState.isShowCountdown() || gameState.isShowHelp()) {
            return;
        }

        // Update DAS (Delayed Auto Shift) even during countdown
        inputHandlerP1.update(dt);
        if (!gameState.isSinglePlayerMode()) {
            inputHandlerP2.update(dt);
        }

        if (gameState.isGameOver()) {
            return;
        }

        // Update Player 1
        updatePlayer(dt, 1, currentPieceP1, inputHandlerP1.isSoftDropping(), fallIntervalP1, fallTimerP1, lockTimerP1);
        
        // Update Player 2 only in two-player mode
        if (!gameState.isSinglePlayerMode()) {
            updatePlayer(dt, 2, currentPieceP2, inputHandlerP2.isSoftDropping(), fallIntervalP2, fallTimerP2, lockTimerP2);
        }
    }
    
    private void updatePlayer(double dt, int player, Piece piece, boolean isSoftDropping, 
                            double fallInterval, double fallTimer, double lockTimer) {
        double currentFallSpeed = isSoftDropping ? (fallInterval / 20.0) : fallInterval;
        
        if (player == 1) {
            fallTimerP1 += dt;
            while (fallTimerP1 >= currentFallSpeed) {
                fallTimerP1 -= currentFallSpeed;
                if (piece.isLanded()) {
                    lockTimerP1 += currentFallSpeed;
                    if (lockTimerP1 >= lockDelay) {
                        lockPiece(player);
                    }
                } else {
                    piece.moveDown();
                    lockTimerP1 = 0;
                }
            }
        } else {
            fallTimerP2 += dt;
            while (fallTimerP2 >= currentFallSpeed) {
                fallTimerP2 -= currentFallSpeed;
                if (piece.isLanded()) {
                    lockTimerP2 += currentFallSpeed;
                    if (lockTimerP2 >= lockDelay) {
                        lockPiece(player);
                    }
                } else {
                    piece.moveDown();
                    lockTimerP2 = 0;
                }
            }
        }
    }
    
    private void lockPiece(int player) {
        if (player == 1) {
            currentPieceP1.lockPiece();
            boardP1.checkAndClearCompletedRows(scoreManagerP1, scoreManagerP1.getLevel());
            updateFallInterval(1);
            spawnNewPiece(1);
            lockTimerP1 = 0;
            fallTimerP1 = 0;
        } else {
            currentPieceP2.lockPiece();
            boardP2.checkAndClearCompletedRows(scoreManagerP2, scoreManagerP2.getLevel());
            updateFallInterval(2);
            spawnNewPiece(2);
            lockTimerP2 = 0;
            fallTimerP2 = 0;
        }
    }

    public void hardDrop(int player) {
        Piece piece = (player == 1) ? currentPieceP1 : currentPieceP2;
        if (piece == null || gameState.isGameOver() || gameState.isPaused()) return;
        while (!piece.isLanded()) {
            piece.moveDown();
        }
        lockPiece(player);
    }

    @Override
    public void paintComponent() {
        renderer.render();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Handle mode selection
        if (gameState.isShowModeSelect()) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_1:
                    startGame(true);  // Single player
                    return;
                case KeyEvent.VK_2:
                    startGame(false); // Two players
                    return;
            }
            return;
        }

        // Handle global controls
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                if (!gameState.isShowCountdown()) { // Don't allow pause during countdown
                    gameState.setPaused(!gameState.isPaused());
                }
                return;
            case KeyEvent.VK_R:
                if (gameState.isGameOver()) {
                    gameState.setShowModeSelect(true);
                }
                return;
            case KeyEvent.VK_H:
                if (!gameState.isShowCountdown()) { // Don't allow help during countdown
                    gameState.setShowHelp(!gameState.isShowHelp());
                }
                return;
        }

        // Handle player controls
        inputHandlerP1.keyPressed(e);
        if (!gameState.isSinglePlayerMode()) {
            inputHandlerP2.keyPressed(e);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (!gameState.isShowModeSelect()) {
            inputHandlerP1.keyReleased(e);
            if (!gameState.isSinglePlayerMode()) {
                inputHandlerP2.keyReleased(e);
            }
        }
    }

    // Getters for other classes to access necessary game components
    public Board getBoardP1() { return boardP1; }
    public Board getBoardP2() { return boardP2; }
    public Piece getCurrentPieceP1() { return currentPieceP1; }
    public Piece getCurrentPieceP2() { return currentPieceP2; }
    public ScoreManager getScoreManagerP1() { return scoreManagerP1; }
    public ScoreManager getScoreManagerP2() { return scoreManagerP2; }
    public GameState getGameState() { return gameState; }
    public Renderer getRenderer() { return renderer; }

    private void startGame(boolean singlePlayer) {
        // Reset all game components first
        gameState.reset();
        gameState.setSinglePlayerMode(singlePlayer);
        gameState.setShowModeSelect(false);
        
        // Reset player 1 components
        scoreManagerP1.reset();
        boardP1.clearBoard();
        inputHandlerP1.resetDAS();
        fallTimerP1 = 0;
        lockTimerP1 = 0;
        currentPieceP1.spawnNewPiece();
        
        // Reset player 2 components if in two-player mode
        if (!singlePlayer) {
            scoreManagerP2.reset();
            boardP2.clearBoard();
            inputHandlerP2.resetDAS();
            fallTimerP2 = 0;
            lockTimerP2 = 0;
            currentPieceP2.spawnNewPiece();
        }
        
        // Update window size
        setWindowSize(singlePlayer ? 350 : 650, 420);
        
        // Reset fall intervals
        fallIntervalP1 = 1.0;
        if (!singlePlayer) {
            fallIntervalP2 = 1.0;
        }
        
        // Start countdown
        gameState.setShowCountdown(true);
    }
}