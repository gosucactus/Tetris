import java.util.Random;

public class Piece {
    private int pieceX;
    private int pieceY;
    private int pieceColor;
    private int[][] activeShape;
    private Board board; // Reference to the game board for collision detection

    // All seven tetriminos
    private static final int[][][] SHAPES = {
            {{-1, 0}, {0, 0}, {1, 0}, {2, 0}}, // I
            {{-1, 0}, {0, 0}, {1, 0}, {1, 1}}, // J
            {{-1, 1}, {-1, 0}, {0, 0}, {1, 0}}, // L
            {{0, 0}, {0, 1}, {1, 0}, {1, 1}}, // O
            {{-1, 1}, {0, 1}, {0, 0}, {1, 0}}, // S
            {{-1, 0}, {0, 0}, {1, 0}, {0, 1}}, // T
            {{-1, 0}, {0, 0}, {0, 1}, {1, 1}}  // Z
    };
    private static Random random = new Random();

    public Piece(Board board) {
        this.board = board;
        this.activeShape = new int[4][2];
        spawnNewPiece();
    }

    public void spawnNewPiece() {
        int shapeId = random.nextInt(SHAPES.length);
        pieceX = board.WIDTH / 2;
        pieceY = 0; // Start at the very top or one row above visible
        pieceColor = shapeId + 1; // Color based on shape index (+1 because 0 is empty)

        for (int i = 0; i < 4; i++) {
            activeShape[i][0] = SHAPES[shapeId][i][0];
            activeShape[i][1] = SHAPES[shapeId][i][1];
        }
    }
    
    // Checks if the piece overlaps with the board or is out of bounds at its current spawn location
    public boolean checkSpawnCollision() {
        for (int[] block : activeShape) {
            int x = pieceX + block[0];
            int y = pieceY + block[1];
            if (board.isOccupied(x, y)) {
                return true; // Collision
            }
        }
        return false; // No collision
    }


    public boolean moveLeft() {
        if (canMove(-1, 0)) {
            pieceX--;
            return true;
        }
        return false;
    }

    public boolean moveRight() {
        if (canMove(1, 0)) {
            pieceX++;
            return true;
        }
        return false;
    }

    public boolean moveDown() {
        if (canMove(0, 1)) {
            pieceY++;
            return true;
        }
        return false;
    }

    public boolean isLanded() {
        return !canMove(0, 1);
    }

    private boolean canMove(int dx, int dy) {
        for (int[] block : activeShape) {
            int newX = pieceX + block[0] + dx;
            int newY = pieceY + block[1] + dy;

            if (!board.isWithinBounds(newX, newY) || board.isOccupied(newX, newY)) {
                return false;
            }
        }
        return true;
    }
    
    public void rotate() {
        int shapeId = pieceColor - 1; // Assuming color is shapeId + 1
        if (shapeId == 3) return; // O piece does not rotate

        int[][] candidate = new int[4][2];
        // Pivot is the second block of the shape definition (SHAPES[shapeId][1])
        // relative to the piece's current activeShape
        // For simplicity, we'll use the current activeShape[1] as the reference for pivot *calculation*
        // The actual pivot point is absolute: (pieceX + activeShape[1][0], pieceY + activeShape[1][1])

        int pivotRelX = activeShape[1][0];
        int pivotRelY = activeShape[1][1];

        for (int i = 0; i < 4; i++) {
            int currentRelX = activeShape[i][0];
            int currentRelY = activeShape[i][1];

            // Translate to be relative to the pivot block
            int dx = currentRelX - pivotRelX;
            int dy = currentRelY - pivotRelY;

            // Rotate: (x, y) -> (-y, x) for 90 deg clockwise
            int newRelDx = -dy;
            int newRelDy = dx;

            // Translate back from pivot block's perspective
            candidate[i][0] = pivotRelX + newRelDx; // New relative X
            candidate[i][1] = pivotRelY + newRelDy; // New relative Y
        }

        // Collision check for the rotated shape
        if (isValidPosition(candidate, pieceX, pieceY)) {
            activeShape = candidate; // Commit rotation
        }
    }


    private boolean isValidPosition(int[][] shape, int newPieceX, int newPieceY) {
        for (int[] block : shape) {
            int x = newPieceX + block[0];
            int y = newPieceY + block[1];
            if (!board.isWithinBounds(x, y) || board.isOccupied(x,y)) {
                return false;
            }
        }
        return true;
    }

    public void lockPiece() {
        for (int[] block : activeShape) {
            board.placePiece(pieceX + block[0], pieceY + block[1], pieceColor);
        }
    }
    
    public int[][] getGhostCoordinates() {
        int ghostY = pieceY;
        int[][] currentShape = getShape();
        int currentX = getX();

        while (true) {
            boolean willLand = false;
            for (int[] block : currentShape) {
                int x = currentX + block[0];
                int y = ghostY + block[1] + 1; // Check one step below
                if (y >= board.HEIGHT || (board.isWithinBounds(x,y) && board.grid[x][y] != 0)) {
                    willLand = true;
                    break;
                }
            }
            if (willLand) break;
            ghostY++;
        }

        int[][] ghostBlocks = new int[4][2];
        for (int i = 0; i < 4; i++) {
            ghostBlocks[i][0] = currentX + currentShape[i][0];
            ghostBlocks[i][1] = ghostY + currentShape[i][1];
        }
        return ghostBlocks;
    }


    // Getters
    public int getX() { return pieceX; }
    public int getY() { return pieceY; }
    public void setY(int y) { this.pieceY = y; }
    public void setX(int x) { this.pieceX = x; }
    public int getColor() { return pieceColor; }
    public int[][] getShape() { return activeShape; }
}