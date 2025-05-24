import java.awt.Color;
import java.util.List;

public class Renderer {
    private GameEngine ge; // To access drawing methods from GameEngine
    private Board board;
    private Piece piece;
    private ScoreManager scoreManager;
    private GameState gameState;
    private TetrisGame game;

    // Tile palette (should be initialized in TetrisGame or passed in)
    private Color[] tileColors;

    private static final int PREVIEW_PIECE_SIZE = 15; // Smaller than main grid
    private static final int PREVIEW_X = 265;  // Adjust based on your layout
    private static final int PREVIEW_Y = 150;  // Start Y position for previews
    private static final int PREVIEW_SPACING = 50; // Vertical space between previews
    private static final int HOLD_X = 270;    // Same X as preview but different Y
    private static final int HOLD_Y = 360;     // Above the next pieces
    
    public Renderer(GameEngine ge, Board board, Piece piece, ScoreManager scoreManager, 
                   GameState gameState, Color[] tileColors, TetrisGame game) {
        this.ge = ge;
        this.board = board;
        this.piece = piece;
        this.scoreManager = scoreManager;
        this.gameState = gameState;
        this.tileColors = tileColors;
        this.game = game;
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
        drawNextPieces(); // Add this line
        drawHoldPiece();  // Add this before or after drawNextPieces()
    }

    private void drawGridLines() {
        ge.changeColor(50, 50, 50);
        for (int x = 0; x <= board.WIDTH; x++) {
            int px = (x + 1) * 20;
            ge.drawLine(px, 0, px, board.VISIBLE_HEIGHT * 20);
        }
        for (int y = 0; y <= board.VISIBLE_HEIGHT; y++) {
            int py = y * 20;
            ge.drawLine(20, py, (board.WIDTH + 1) * 20, py);
        }
    }

    private void drawBorderWalls() {
        ge.changeColor(128, 128, 128);
        // Draw vertical walls for the visible height
        for (int y = 0; y <= board.VISIBLE_HEIGHT; y++) {
            ge.drawSolidRectangle(1, y * 20 + 1, 18, 18);
            ge.drawSolidRectangle((board.WIDTH + 1) * 20 + 1, y * 20 + 1, 18, 18);
        }
        // Draw bottom wall
        for (int x = 0; x < board.WIDTH; x++) {
            ge.drawSolidRectangle((x + 1) * 20 + 1, board.VISIBLE_HEIGHT * 20 + 1, 18, 18);
        }
    }

    private void drawPlacedTiles() {
        int[][] grid = board.getGrid();
        // Only draw the visible portion
        for (int y = board.BUFFER_HEIGHT; y < board.TOTAL_HEIGHT; y++) {
            for (int x = 0; x < board.WIDTH; x++) {
                if (grid[x][y] != 0) {
                    ge.changeColor(tileColors[grid[x][y]]);
                    // Adjust y coordinate to account for buffer
                    int displayY = y - board.BUFFER_HEIGHT;
                    ge.drawSolidRectangle((x + 1) * 20 + 1, displayY * 20 + 1, 18, 18);
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
            // Only draw if in visible area (below buffer zone)
            if (py >= board.BUFFER_HEIGHT) {
                // Adjust y coordinate to account for buffer
                int displayY = py - board.BUFFER_HEIGHT;
                ge.drawSolidRectangle((px + 1) * 20 + 1, displayY * 20 + 1, 18, 18);
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
            int px = block[0];
            int py = block[1];
            // Only draw if in visible area
            if (py >= board.BUFFER_HEIGHT) {
                // Adjust y coordinate to account for buffer
                int displayY = py - board.BUFFER_HEIGHT;
                ge.drawSolidRectangle((px + 1) * 20 + 1, displayY * 20 + 1, 18, 18);
            }
        }
    }

    private void drawScoreAndLevel() {
        // Assuming a fixed position for score/level display
        ge.changeColor(ge.black); // Clear area for text
        ge.drawSolidRectangle(board.WIDTH * 20 + 40, 0, ge.mWidth - (board.WIDTH * 20 + 25) , ge.mHeight);


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

    private void drawNextPieces() {
        List<Integer> nextPieces = piece.getNextPieces();
        
        // Draw "NEXT" text
        ge.changeColor(ge.white);
        ge.drawText(PREVIEW_X, PREVIEW_Y - 20, "NEXT", 18);

        // Draw preview boxes and pieces
        for (int i = 0; i < nextPieces.size(); i++) {
            int y = PREVIEW_Y + (i * PREVIEW_SPACING);
            
            // Draw preview box background
            ge.changeColor(40, 40, 40);
            ge.drawSolidRectangle(PREVIEW_X, y, PREVIEW_PIECE_SIZE * 4, PREVIEW_PIECE_SIZE * 4);
            
            // Draw the piece
            drawPreviewPiece(nextPieces.get(i), PREVIEW_X, y);
        }
    }

    private void drawHoldPiece() {
        // Draw "HOLD" text
        ge.changeColor(ge.white);
        ge.drawText(HOLD_X, HOLD_Y - 20, "HOLD", 18);

        Integer heldType = game.getHeldPieceType();
        if (heldType != null) {
            // Draw hold box background
            ge.changeColor(40, 40, 40);
            ge.drawSolidRectangle(HOLD_X, HOLD_Y, PREVIEW_PIECE_SIZE * 4, PREVIEW_PIECE_SIZE * 4);
            
            // Draw the held piece
            drawPreviewPiece(heldType, HOLD_X, HOLD_Y);
        }
    }

    private void drawPreviewPiece(int pieceType, int x, int y) {
        int[][] shape = Piece.SHAPES[pieceType];
        ge.changeColor(tileColors[pieceType + 1]);

        // Calculate center offset for the piece
        int offsetX = 25;
        int offsetY = 15;
        
        // Adjust offset for I and O pieces
        if (pieceType == 0) {  // I piece
            offsetX = 15;  // Move left
        } else if (pieceType == 3) {  // O piece
            offsetX = 15;  // Move left but not as much as I
        }

        // Draw each block of the piece
        for (int[] block : shape) {
            int blockX = x + offsetX + (block[0] * PREVIEW_PIECE_SIZE);
            int blockY = y + offsetY + (block[1] * PREVIEW_PIECE_SIZE);
            ge.drawSolidRectangle(blockX, blockY, PREVIEW_PIECE_SIZE - 1, PREVIEW_PIECE_SIZE - 1);
        }
    }
}
