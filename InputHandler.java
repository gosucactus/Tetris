import java.awt.event.KeyEvent;

public class InputHandler {
    private TetrisGame game;
    private int playerNumber;
    private boolean leftHeld = false;
    private boolean rightHeld = false;
    private boolean softDropping = false;
    
    // DAS (Delayed Auto Shift) settings
    private double leftHeldTime = 0;
    private double rightHeldTime = 0;
    private final double DAS_DELAY = 0.167; // 167ms initial delay
    private final double ARR_DELAY = 0.033; // 33ms auto-repeat rate
    
    public InputHandler(TetrisGame game, int playerNumber) {
        this.game = game;
        this.playerNumber = playerNumber;
    }
    
    public void update(double dt) {
        if (game.getGameState().isGameOver() || game.getGameState().isPaused()) return;
        
        // Handle DAS (Delayed Auto Shift)
        if (leftHeld) {
            leftHeldTime += dt;
            if (leftHeldTime >= DAS_DELAY) {
                double repeatTime = leftHeldTime - DAS_DELAY;
                while (repeatTime >= ARR_DELAY) {
                    moveLeft();
                    repeatTime -= ARR_DELAY;
                }
            }
        }
        
        if (rightHeld) {
            rightHeldTime += dt;
            if (rightHeldTime >= DAS_DELAY) {
                double repeatTime = rightHeldTime - DAS_DELAY;
                while (repeatTime >= ARR_DELAY) {
                    moveRight();
                    repeatTime -= ARR_DELAY;
                }
            }
        }
    }
    
    private void moveLeft() {
        Piece piece = (playerNumber == 1) ? game.getCurrentPieceP1() : game.getCurrentPieceP2();
        if (piece != null) piece.moveLeft();
    }
    
    private void moveRight() {
        Piece piece = (playerNumber == 1) ? game.getCurrentPieceP1() : game.getCurrentPieceP2();
        if (piece != null) piece.moveRight();
    }
    
    private void moveDown() {
        Piece piece = (playerNumber == 1) ? game.getCurrentPieceP1() : game.getCurrentPieceP2();
        if (piece != null && !piece.isLanded()) {
            piece.moveDown();
            game.resetFallTimer(playerNumber);
        }
    }
    
    private void rotate() {
        Piece piece = (playerNumber == 1) ? game.getCurrentPieceP1() : game.getCurrentPieceP2();
        if (piece != null) piece.rotate();
    }
    
    private void hardDrop() {
        game.hardDrop(playerNumber);
    }

    public void keyPressed(KeyEvent e) {
        GameState gameState = game.getGameState();
        
        if (gameState.isGameOver() || gameState.isPaused() || gameState.isShowCountdown()) return;

        // Player-specific controls
        if (playerNumber == 1) {
            // Player 1 controls (Arrow keys + Space)
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    leftHeld = true;
                    leftHeldTime = 0;
                    moveLeft();
                    break;
                case KeyEvent.VK_RIGHT:
                    rightHeld = true;
                    rightHeldTime = 0;
                    moveRight();
                    break;
                case KeyEvent.VK_DOWN:
                    softDropping = true;
                    moveDown();
                    break;
                case KeyEvent.VK_SPACE:
                    hardDrop();
                    break;
                case KeyEvent.VK_UP:
                    rotate();
                    break;
            }
        } else {
            // Player 2 controls (WASD + Q)
            switch (e.getKeyCode()) {
                case KeyEvent.VK_A:
                    leftHeld = true;
                    leftHeldTime = 0;
                    moveLeft();
                    break;
                case KeyEvent.VK_D:
                    rightHeld = true;
                    rightHeldTime = 0;
                    moveRight();
                    break;
                case KeyEvent.VK_S:
                    softDropping = true;
                    moveDown();
                    break;
                case KeyEvent.VK_Q:
                    hardDrop();
                    break;
                case KeyEvent.VK_W:
                    rotate();
                    break;
            }
        }
    }

    public void keyReleased(KeyEvent e) {
        if (playerNumber == 1) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    leftHeld = false;
                    leftHeldTime = 0;
                    break;
                case KeyEvent.VK_RIGHT:
                    rightHeld = false;
                    rightHeldTime = 0;
                    break;
                case KeyEvent.VK_DOWN:
                    softDropping = false;
                    break;
            }
        } else {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_A:
                    leftHeld = false;
                    leftHeldTime = 0;
                    break;
                case KeyEvent.VK_D:
                    rightHeld = false;
                    rightHeldTime = 0;
                    break;
                case KeyEvent.VK_S:
                    softDropping = false;
                    break;
            }
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