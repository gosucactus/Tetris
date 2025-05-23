import java.awt.Color;

public class Renderer {
    private GameEngine ge; // To access drawing methods from GameEngine
    private Board boardP1;
    private Board boardP2;
    private Piece pieceP1;
    private Piece pieceP2;
    private ScoreManager scoreManagerP1;
    private ScoreManager scoreManagerP2;
    private GameState gameState;

    // Tile palette (should be initialized in TetrisGame or passed in)
    private Color[] tileColors;

    // Board offsets for 2-player mode
    private final int BOARD_P1_OFFSET_X = 20;
    private final int BOARD_P2_OFFSET_X = 340;
    //private final int BOARD_WIDTH = 200; // removed in the 2p implementation

    public Renderer(GameEngine gameEngine, Board boardP1, Board boardP2, 
                   Piece pieceP1, Piece pieceP2,
                   ScoreManager scoreManagerP1, ScoreManager scoreManagerP2, 
                   GameState gameState, Color[] tileColors) {
        this.ge = gameEngine;
        this.boardP1 = boardP1;
        this.boardP2 = boardP2;
        this.pieceP1 = pieceP1;
        this.pieceP2 = pieceP2;
        this.scoreManagerP1 = scoreManagerP1;
        this.scoreManagerP2 = scoreManagerP2;
        this.gameState = gameState;
        this.tileColors = tileColors;
    }

    public void render() {
        ge.changeBackgroundColor(ge.black);
        ge.clearBackground(ge.mWidth, ge.mHeight);

        if (gameState.isShowModeSelect()) {
            drawModeSelect();
            return;
        }

        // Draw boards based on game mode
        drawBoard(1);
        if (!gameState.isSinglePlayerMode()) {
            drawBoard(2);
        }

        if (gameState.isShowCountdown()) {
            drawCountdown();
            return;
        }

        if (gameState.isGameOver()) {
            drawGameOverScreen();
            return;
        }

        if (gameState.isShowHelp()) {
            drawHelpScreen();
            return;
        }

        if (gameState.isPaused()) {
            drawPauseMenu();
            return;
        }
    }

    private void drawBoard(int player) {
        int offsetX = (player == 1) ? BOARD_P1_OFFSET_X : BOARD_P2_OFFSET_X;
        Board board = (player == 1) ? boardP1 : boardP2;
        Piece piece = (player == 1) ? pieceP1 : pieceP2;
        ScoreManager scoreManager = (player == 1) ? scoreManagerP1 : scoreManagerP2;

        drawGridLines(offsetX, board);
        drawBorderWalls(offsetX, board);
        drawPlacedTiles(offsetX, board);
        
        if (piece != null) {
            drawGhostPiece(offsetX, piece);
            drawCurrentPiece(offsetX, piece);
        }
        
        drawScoreAndLevel(offsetX, scoreManager, player);
    }

    private void drawGridLines(int offsetX, Board board) {
        ge.changeColor(50, 50, 50); // subtle grey
        for (int x = 0; x <= board.WIDTH; x++) {
            int px = offsetX + x * 20;
            ge.drawLine(px, 0, px, board.HEIGHT * 20);
        }
        for (int y = 0; y <= board.HEIGHT; y++) {
            int py = y * 20;
            ge.drawLine(offsetX, py, offsetX + board.WIDTH * 20, py);
        }
    }

    private void drawBorderWalls(int offsetX, Board board) {
        ge.changeColor(128, 128, 128);
        // Left wall
        for (int y = 0; y <= board.HEIGHT; y++) {
            ge.drawSolidRectangle(offsetX - 19, y * 20 + 1, 18, 18);
        }
        // Right wall
        for (int y = 0; y <= board.HEIGHT; y++) {
            ge.drawSolidRectangle(offsetX + board.WIDTH * 20 + 1, y * 20 + 1, 18, 18);
        }
        // Bottom wall
        for (int x = -1; x <= board.WIDTH; x++) {
            ge.drawSolidRectangle(offsetX + x * 20 + 1, board.HEIGHT * 20 + 1, 18, 18);
        }
    }

    private void drawPlacedTiles(int offsetX, Board board) {
        int[][] grid = board.getGrid();
        for (int y = 0; y < board.HEIGHT; y++) {
            for (int x = 0; x < board.WIDTH; x++) {
                if (grid[x][y] != 0) {
                    ge.changeColor(tileColors[grid[x][y]]);
                    ge.drawSolidRectangle(offsetX + x * 20 + 1, y * 20 + 1, 18, 18);
                }
            }
        }
    }

    private void drawCurrentPiece(int offsetX, Piece piece) {
        if (piece == null) return;
        ge.changeColor(tileColors[piece.getColor()]);
        int[][] shape = piece.getShape();
        for (int[] block : shape) {
            int px = piece.getX() + block[0];
            int py = piece.getY() + block[1];
            if (py >= 0) {
                ge.drawSolidRectangle(offsetX + px * 20 + 1, py * 20 + 1, 18, 18);
            }
        }
    }
    
    private void drawGhostPiece(int offsetX, Piece piece) {
        if (piece == null) return;
        int[][] ghostBlocks = piece.getGhostCoordinates();
        Color base = tileColors[piece.getColor()];
        Color translucent = new Color(base.getRed(), base.getGreen(), base.getBlue(), 88);
        ge.changeColor(translucent);
        for (int[] block : ghostBlocks) {
            int px = block[0];
            int py = block[1];
            if (py >= 0) {
                ge.drawSolidRectangle(offsetX + px * 20 + 1, py * 20 + 1, 18, 18);
            }
        }
    }

    private void drawScoreAndLevel(int offsetX, ScoreManager scoreManager, int player) {
        Board board = (player == 1) ? boardP1 : boardP2;
        // Calculate text position (in the black area to the right of the board)
        int textX = offsetX + (board.WIDTH * 20) + 25;
        int baseY = 45;
        
        // Clear the score area first
        ge.changeColor(ge.black);
        ge.drawSolidRectangle(textX, 0, 100, 150);
        
        ge.changeColor(ge.white);
        ge.drawText(textX, baseY, "P" + player, 14);
        ge.drawText(textX, baseY + 15, "SCORE:", 14);
        ge.drawText(textX, baseY + 30, ge.toString(scoreManager.getScore()), 14);
        ge.drawText(textX, baseY + 50, "LEVEL:", 14);
        ge.drawText(textX, baseY + 65, ge.toString(scoreManager.getLevel()), 14);
    }

    private void drawCountdown() {
        long remaining = gameState.getCountdownRemaining();
        ge.changeColor(ge.white);
        
        int centerX = ge.mWidth / 2;
        if (remaining > 0) {
            if (gameState.isResumingFromPause()) {
                ge.drawBoldText(centerX - 100, 150, "Resuming in", 30);
                ge.drawBoldText(centerX - 25, 200, "" + remaining, 50);
            } else {
                ge.drawBoldText(centerX - 25, 200, "" + remaining, 50);
            }
        } else {
            if (gameState.isResumingFromPause()) {
                ge.drawBoldText(centerX - 45, 200, "Go!", 50);
            } else {
                ge.drawBoldText(centerX - 45, 200, "Go!", 50);
            }
        }
    }

    private void drawModeSelect() {
        ge.changeColor(ge.white);
        int centerX = ge.mWidth / 2;
        int startY = 150;
        
        ge.drawBoldText(centerX - 140, startY, "Select Game Mode", 30);
        
        ge.drawText(centerX - 100, startY + 60, "Press 1: Single Player", 20);
        ge.drawText(centerX - 100, startY + 90, "Press 2: Two Players", 20);
    }

    private void drawGameOverScreen() {
        ge.changeColor(ge.white);
        ge.drawBoldText(ge.mWidth / 2 - 100, 130, "Game Over!", 30);
        
        if (gameState.isSinglePlayerMode()) {
            // Single player score
            ge.drawText(ge.mWidth / 2 - 80, 170, "Score: " + scoreManagerP1.getScore(), 20);
            ge.drawText(ge.mWidth / 2 - 80, 190, "Level: " + scoreManagerP1.getLevel(), 20);
        } else {
            // Two player scores
            ge.drawText(BOARD_P1_OFFSET_X + 20, 170, "P1 Score: " + scoreManagerP1.getScore(), 20);
            ge.drawText(BOARD_P1_OFFSET_X + 20, 190, "P1 Level: " + scoreManagerP1.getLevel(), 20);
            
            ge.drawText(BOARD_P2_OFFSET_X + 20, 170, "P2 Score: " + scoreManagerP2.getScore(), 20);
            ge.drawText(BOARD_P2_OFFSET_X + 20, 190, "P2 Level: " + scoreManagerP2.getLevel(), 20);
            
            // Determine winner
            String winner = "";
            if (scoreManagerP1.getScore() > scoreManagerP2.getScore()) {
                winner = "Player 1 Wins!";
            } else if (scoreManagerP2.getScore() > scoreManagerP1.getScore()) {
                winner = "Player 2 Wins!";
            } else {
                winner = "It's a Tie!";
            }
            ge.drawBoldText(ge.mWidth / 2 - 100, 230, winner, 30);
        }
        
        ge.drawText(ge.mWidth / 2 - 80, 270, "Press R to restart", 20);
    }

    private void drawHelpScreen() {
        ge.changeColor(ge.white);
        int centerX = ge.mWidth / 2 - 150;
        int startY = 100;
        int lineHeight = 25;
        
        ge.drawBoldText(centerX, startY, "Controls:", 24);
        startY += lineHeight * 2;
        
        ge.drawText(centerX, startY, "Player 1:", 20);
        ge.drawText(centerX + 20, startY + lineHeight, "← → : Move", 18);
        ge.drawText(centerX + 20, startY + lineHeight * 2, "↑ : Rotate", 18);
        ge.drawText(centerX + 20, startY + lineHeight * 3, "↓ : Soft Drop", 18);
        ge.drawText(centerX + 20, startY + lineHeight * 4, "Space : Hard Drop", 18);
        
        if (!gameState.isSinglePlayerMode()) {
            ge.drawText(centerX + 200, startY, "Player 2:", 20);
            ge.drawText(centerX + 220, startY + lineHeight, "A D : Move", 18);
            ge.drawText(centerX + 220, startY + lineHeight * 2, "W : Rotate", 18);
            ge.drawText(centerX + 220, startY + lineHeight * 3, "S : Soft Drop", 18);
            ge.drawText(centerX + 220, startY + lineHeight * 4, "Q : Hard Drop", 18);
        }
        
        startY += lineHeight * 6;
        ge.drawText(centerX, startY, "ESC : Pause", 18);
        ge.drawText(centerX, startY + lineHeight, "H : Toggle Help", 18);
        ge.drawText(centerX, startY + lineHeight * 2, "R : Restart Game", 18);
    }

    private void drawPauseMenu() {
        ge.changeColor(ge.white);
        ge.drawBoldText(ge.mWidth / 2 - 60, 200, "PAUSED", 40);
        ge.drawText(ge.mWidth / 2 - 80, 240, "Press ESC to resume", 20);
    }
}