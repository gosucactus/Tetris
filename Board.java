public class Board {
    public final int WIDTH = 10;
    public final int VISIBLE_HEIGHT = 20; // Standard visible playfield height
    public final int BUFFER_HEIGHT = 20; // Buffer zone above visible area
    public final int TOTAL_HEIGHT = VISIBLE_HEIGHT + BUFFER_HEIGHT;
    private int[][] grid;

    public Board() {
        grid = new int[WIDTH][TOTAL_HEIGHT];
        clearBoard();
    }

    public void clearBoard() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < TOTAL_HEIGHT; y++) {
                grid[x][y] = 0;
            }
        }
    }

    // Method to check if a position is within bounds
    public boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < TOTAL_HEIGHT;
    }

    // Method to check if a cell is occupied
    public boolean isOccupied(int x, int y) {
        if (!isWithinBounds(x, y)) return true; // Treat out of bounds as occupied for collision
        return grid[x][y] != 0;
    }

    // Method to place a piece on the board (when it locks)
    public void placePiece(int x, int y, int color) {
        if (isWithinBounds(x, y)) {
            grid[x][y] = color;
        }
    }

    public int checkAndClearCompletedRows(ScoreManager scoreManager, int currentLevel) {
        int linesCleared = 0;
        // Start checking from the bottom of the TOTAL board (including buffer)
        int y = TOTAL_HEIGHT - 1;

        while (y >= 0) {
            boolean full = true;
            for (int x = 0; x < WIDTH; x++) {
                if (grid[x][y] == 0) {
                    full = false;
                    break;
                }
            }

            if (full) {
                linesCleared++;
                // Move everything down, including buffer zone
                for (int row = y; row > 0; row--) {
                    for (int x = 0; x < WIDTH; x++) {
                        grid[x][row] = grid[x][row - 1];
                    }
                }
                // Clear top row
                for (int x = 0; x < WIDTH; x++) {
                    grid[x][0] = 0;
                }
                // Don't decrement y, check same position again
            } else {
                y--;
            }
        }

        if (linesCleared > 0) {
            scoreManager.addScoreForLines(linesCleared, currentLevel);
        }
        return linesCleared;
    }

    public int[][] getGrid() {
        return grid;
    }

    // Add method to check if a piece is entirely in buffer zone
    public boolean isPieceInBufferZone(int[][] shape, int pieceX, int pieceY) {
        for (int[] block : shape) {
            int y = pieceY + block[1];
            if (y >= BUFFER_HEIGHT) {
                return false;
            }
        }
        return true;
    }
}