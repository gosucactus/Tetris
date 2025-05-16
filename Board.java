public class Board {
    public final int WIDTH = 10;
    public final int HEIGHT = 20;
    public int[][] grid; // The playfield

    public Board() {
        grid = new int[WIDTH][HEIGHT];
        clearBoard();
    }

    public void clearBoard() {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                grid[x][y] = 0;
            }
        }
    }

    // Method to check if a position is within bounds
    public boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
    }

    // Method to check if a cell is occupied
    public boolean isOccupied(int x, int y) {
        if (!isWithinBounds(x,y)) return true; // Treat out of bounds as occupied for collision
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
        int y = 0;

        while (y < HEIGHT) {
            boolean full = true;
            for (int x = 0; x < WIDTH; x++) {
                if (grid[x][y] == 0) {
                    full = false;
                    break;
                }
            }

            if (full) {
                linesCleared++;
                for (int row = y; row > 0; row--) {
                    for (int x = 0; x < WIDTH; x++) {
                        grid[x][row] = grid[x][row - 1];
                    }
                }
                for (int x = 0; x < WIDTH; x++) {
                    grid[x][0] = 0;
                }
                // Don't increment y, so we check the new row y again
            } else {
                y++;
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
}