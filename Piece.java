public class Piece {
    private int pieceX;
    private int pieceY;
    private int pieceColor;
    private int[][] activeShape;
    private Board board;
    private static PieceGenerator pieceGenerator;

    // Define the shapes of all tetriminoes (0=I, 1=J, 2=L, 3=O, 4=S, 5=T, 6=Z)
    private static final int[][][] SHAPES = {
            {{-1, 0}, {0, 0}, {1, 0}, {2, 0}}, // I
            {{-1, 1}, {0, 1}, {1, 1}, {1, 0}}, // J
            {{-1, 1}, {0, 1}, {1, 1}, {-1, 0}}, // L
            {{0, 0}, {0, 1}, {1, 0}, {1, 1}}, // O
            {{-1, 1}, {0, 1}, {0, 0}, {1, 0}}, // S
            {{-1, 1}, {0, 1}, {1, 1}, {0, 0}}, // T
            {{-1, 0}, {0, 0}, {0, 1}, {1, 1}}  // Z
    };

    // Constructor
    public Piece(Board board) {
        this.board = board;
        this.activeShape = new int[4][2];
        if (pieceGenerator == null) {
            pieceGenerator = new PieceGenerator();
        }
    }

    public void spawnNewPiece() {
        int shapeId = pieceGenerator.getNextPiece();
        // Normal spawn position at top of visible playfield
        pieceY = board.BUFFER_HEIGHT; // First visible row
        pieceX = (board.WIDTH / 2) - 1; // Centered, but 1 block left of center
        pieceColor = shapeId + 1;

        // Copy the shape from SHAPES array
        for (int i = 0; i < 4; i++) {
            activeShape[i][0] = SHAPES[shapeId][i][0];
            activeShape[i][1] = SHAPES[shapeId][i][1];
        }

        // Special case: I piece spawns one row lower
        if (shapeId == 0) { // I piece
            pieceY++;
        }
    }

    // Checks if the piece overlaps with the board or is out of bounds at its current spawn location
    public boolean checkSpawnCollision() {
        return !isValidPosition(activeShape, pieceX, pieceY);
    }

    // Add method to check for lock out condition
    public boolean isLockOut() {
        return board.isPieceInBufferZone(activeShape, pieceX, pieceY);
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
        while (true) {
            boolean willLand = false;
            for (int[] block : activeShape) {
                int x = pieceX + block[0];
                int y = ghostY + block[1] + 1;
                if (!board.isWithinBounds(x, y) || board.isOccupied(x, y)) {
                    willLand = true;
                    break;
                }
            }
            if (willLand) break;
            ghostY++;
        }

        int[][] ghostBlocks = new int[4][2];
        for (int i = 0; i < 4; i++) {
            ghostBlocks[i][0] = pieceX + activeShape[i][0];
            ghostBlocks[i][1] = ghostY + activeShape[i][1];
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
    public PieceGenerator getPieceGenerator() {
        return pieceGenerator;
    }

    public boolean rotateClockwise() {
        int shapeId = pieceColor - 1;
        if (shapeId == 3) return false; // O piece does not rotate

        int[][] candidate = new int[4][2];
        // Use second block as pivot point
        int pivotRelX = activeShape[1][0];
        int pivotRelY = activeShape[1][1];

        for (int i = 0; i < 4; i++) {
            int currentRelX = activeShape[i][0];
            int currentRelY = activeShape[i][1];

            // Translate to pivot's coordinate space
            int dx = currentRelX - pivotRelX;
            int dy = currentRelY - pivotRelY;

            // Rotate clockwise: (x, y) -> (-y, x)
            int newRelDx = -dy;
            int newRelDy = dx;

            // Translate back
            candidate[i][0] = pivotRelX + newRelDx;
            candidate[i][1] = pivotRelY + newRelDy;
        }

        // Check if rotation is valid
        if (isValidPosition(candidate, pieceX, pieceY)) {
            // Copy rotated position to activeShape
            for (int i = 0; i < 4; i++) {
                activeShape[i][0] = candidate[i][0];
                activeShape[i][1] = candidate[i][1];
            }
            return true;
        }
        return false;
    }

    public boolean rotateCounterClockwise() {
        int shapeId = pieceColor - 1;
        if (shapeId == 3) return false; // O piece does not rotate

        int[][] candidate = new int[4][2];
        // Use second block as pivot point
        int pivotRelX = activeShape[1][0];
        int pivotRelY = activeShape[1][1];

        for (int i = 0; i < 4; i++) {
            int currentRelX = activeShape[i][0];
            int currentRelY = activeShape[i][1];

            // Translate to pivot's coordinate space
            int dx = currentRelX - pivotRelX;
            int dy = currentRelY - pivotRelY;

            // Rotate counter-clockwise: (x, y) -> (y, -x)
            int newRelDx = dy;
            int newRelDy = -dx;

            // Translate back
            candidate[i][0] = pivotRelX + newRelDx;
            candidate[i][1] = pivotRelY + newRelDy;
        }

        // Check if rotation is valid
        if (isValidPosition(candidate, pieceX, pieceY)) {
            // Copy rotated position to activeShape
            for (int i = 0; i < 4; i++) {
                activeShape[i][0] = candidate[i][0];
                activeShape[i][1] = candidate[i][1];
            }
            return true;
        }
        return false;
    }
}