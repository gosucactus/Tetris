import java.awt.Color;

public class Renderer {
    private GameEngine ge; // To access drawing methods from GameEngine
    private Board board;
    private Piece piece;
    private ScoreManager scoreManager;
    private GameState gameState;

    // Tile palette (should be initialized in TetrisGame or passed in)
    private Color[] tileColors;

    public Renderer(GameEngine gameEngine, Board board, Piece piece, ScoreManager scoreManager, GameState gameState, Color[] tileColors) {
        this.ge = gameEngine;
        this.board = board;
        this.piece = piece; // This will be the *current* piece
        this.scoreManager = scoreManager;
        this.gameState = gameState;
        this.tileColors = tileColors;
    }
    
    public void setPiece(Piece currentPiece) {
        this.piece = currentPiece;
    }

    public void render() {
        ge.changeBackgroundColor(ge.black);
        ge.clearBackground(ge.mWidth, ge.mHeight);

        drawGridLines();
        drawBorderWalls();
        drawPlacedTiles();

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
        
        // Only draw piece and ghost if game is active
        if (piece != null) {
            drawGhostPiece();
            drawCurrentPiece();
        }
        
        drawScoreAndLevel();
    }

    private void drawGridLines() {
        ge.changeColor(50, 50, 50); // subtle grey
        for (int x = 0; x <= board.WIDTH; x++) {
            int px = (x + 1) * 20;
            ge.drawLine(px, 0, px, board.HEIGHT * 20);
        }
        for (int y = 0; y <= board.HEIGHT; y++) {
            int py = y * 20;
            ge.drawLine(20, py, (board.WIDTH + 1) * 20, py);
        }
    }

    private void drawBorderWalls() {
        ge.changeColor(128, 128, 128);
        for (int y = 0; y <= board.HEIGHT; y++) {
            ge.drawSolidRectangle(1, y * 20 + 1, 18, 18);
            ge.drawSolidRectangle((board.WIDTH + 1) * 20 + 1, y * 20 + 1, 18, 18);
        }
        for (int x = 0; x < board.WIDTH; x++) {
            ge.drawSolidRectangle((x + 1) * 20 + 1, board.HEIGHT * 20 + 1, 18, 18);
        }
    }

    private void drawPlacedTiles() {
        int[][] grid = board.getGrid();
        for (int y = 0; y < board.HEIGHT; y++) {
            for (int x = 0; x < board.WIDTH; x++) {
                if (grid[x][y] != 0) {
                    ge.changeColor(tileColors[grid[x][y]]);
                    ge.drawSolidRectangle((x + 1) * 20 + 1, y * 20 + 1, 18, 18);
                }
            }
        }
    }

    private void drawCurrentPiece() {
        if (piece == null) return;
        ge.changeColor(tileColors[piece.getColor()]);
        int[][] shape = piece.getShape();
        for (int[] block : shape) {
            int px = piece.getX() + block[0];
            int py = piece.getY() + block[1];
            // Only draw if within visible board area (optional, GameEngine might handle clipping)
            if (py >= 0) { 
                 ge.drawSolidRectangle((px + 1) * 20 + 1, py * 20 + 1, 18, 18);
            }
        }
    }
    
    private void drawGhostPiece() {
        if (piece == null) return;
        int[][] ghostBlocks = piece.getGhostCoordinates();
        Color base = tileColors[piece.getColor()];
        Color translucent = new Color(base.getRed(), base.getGreen(), base.getBlue(), 88);
        ge.changeColor(translucent);
        for (int[] block : ghostBlocks) {
            int px = block[0]; // ghostCoordinates are absolute
            int py = block[1];
             if (py >= 0) {
                ge.drawSolidRectangle((px + 1) * 20 + 1, py * 20 + 1, 18, 18);
            }
        }
    }

    private void drawScoreAndLevel() {
        // Assuming a fixed position for score/level display
        ge.changeColor(ge.black); // Clear area for text
        ge.drawSolidRectangle(board.WIDTH * 20 + 25, 0, ge.mWidth - (board.WIDTH * 20 + 25) , ge.mHeight);


        ge.changeColor(ge.white);
        ge.drawText(265, 45, "SCORE:", 18);
        ge.drawText(265, 65, ge.toString(scoreManager.getScore()), 18);
        ge.drawText(265, 85, "LEVEL:", 18);
        ge.drawText(265, 105, ge.toString(scoreManager.getLevel()), 18);
    }

    private void drawCountdown() {
        long remaining = gameState.getCountdownRemaining();
        ge.changeColor(ge.white);
        if (remaining > 0) {
            ge.drawBoldText(150, 200, "" + remaining, 50);
        } else {
            ge.drawBoldText(130, 200, "Go!", 50);
        }
    }

    private void drawGameOverScreen() {
        ge.changeColor(ge.white);
        ge.drawBoldText(90, 130, "Game Over!", 30);
        ge.drawText(80, 170, "Final Score: " + scoreManager.getScore(), 20);
        ge.drawText(80, 190, "Level: " + scoreManager.getLevel(), 20);
        ge.drawText(75, 230, "Press R to restart", 20);
    }

    private void drawHelpScreen() {
        ge.changeColor(ge.white);
        ge.drawBoldText(90, 80, "How to Play", 24);
        ge.drawText(50, 120, "← → : Move", 20);
        ge.drawText(50, 150, "↑    : Rotate", 20);
        ge.drawText(50, 180, "↓    : Soft Drop", 20);
        ge.drawText(50, 210, "Space: Hard Drop", 20);
        ge.drawText(50, 240, "Esc  : Pause", 20);
        ge.drawText(50, 280, "Enter: Select Menu Option", 20);
        ge.drawText(50, 330, "(Press Esc to go back)", 16);
    }

    private void drawPauseMenu() {
        ge.changeColor(ge.white);
        ge.drawBoldText(100, 100, "Game Paused", 24);
        String[] options = gameState.getPauseMenuOptions();
        for (int i = 0; i < options.length; i++) {
            if (i == gameState.getPauseMenuSelection()) {
                ge.changeColor(ge.yellow);
            } else {
                ge.changeColor(ge.white);
            }
            ge.drawText(100, 150 + i * 30, options[i], 20);
        }
    }
}