import java.awt.Color;
import java.awt.event.KeyEvent;

public class TetrisGame extends GameEngine {

	private Board board;
	private Piece currentPiece;
	private ScoreManager scoreManager;
	private GameState gameState;
	private InputHandler inputHandler;
	private Renderer renderer;

	// Tile palette - order matters!
	private Color[] tileColors = {
        black,  // 0 = empty cell
        cyan,   // 1 = I piece
        blue,   // 2 = J piece
        orange, // 3 = L piece
        yellow, // 4 = O piece
        green,  // 5 = S piece
        purple, // 6 = T piece
        red     // 7 = Z piece
    };

	// Fall & lock timers
	private double fallInterval = 1.0; // Base fall interval
	private double fallTimer = 0.0;
	private final double lockDelay = 0.5;
	private double lockTimer = 0.0;


	public static void main(String[] args) {
		createGame(new TetrisGame(), 30); // Framerate
	}

	@Override
	public void init() {
		setWindowSize(350, 420); // Adjust as needed, considering score panel

		board = new Board();
		scoreManager = new ScoreManager();
		gameState = new GameState();
		// currentPiece is initialized by calling spawnNewPiece which itself is called by restartGame->resetGame
		// However, Piece needs board, so we can instantiate it after board.
		// The actual active piece is created via spawnNewPiece.
		// We can pass a null piece to renderer initially or handle it.

		inputHandler = new InputHandler(this);

		// Initialize currentPiece *after* board
		// Piece needs board for its dimensions and collision checks
		currentPiece = new Piece(board); // Create an initial piece instance
		// The renderer will be updated with new pieces as they spawn

		renderer = new Renderer(this, board, currentPiece, scoreManager, gameState, tileColors);

		restartGame(); // This will set up the initial game state including spawning the first piece
	}

	public void resetFallTimer() {
		this.fallTimer = 0;
	}

	public void restartGame() {
		gameState.reset();
		scoreManager.reset();
		board.clearBoard();
		inputHandler.resetDAS(); // Reset DAS/ARR state
		fallTimer = 0;
		lockTimer = 0;
		spawnNewPiece(); // This will create and assign a new piece to currentPiece
		renderer.setPiece(currentPiece); // Ensure renderer has the new piece
		updateFallInterval(); // Set fall speed based on level 1
	}

	private void spawnNewPiece() {
		currentPiece.spawnNewPiece();
		renderer.setPiece(currentPiece);
		if (currentPiece.checkSpawnCollision()) {
			gameState.setGameOver(true);
		}
		fallTimer = 0;
		lockTimer = 0;
	}

	private void updateFallInterval() {
		// From Tetris Guidelines - frames per gridcell converted to seconds
		// Original values are in frames (at 60fps), so divide by 60 to get seconds
		int level = scoreManager.getLevel();
		switch (level) {
			case 1:  fallInterval = 48.0/60.0; break;  // 0.800s
			case 2:  fallInterval = 43.0/60.0; break;  // 0.717s
			case 3:  fallInterval = 38.0/60.0; break;  // 0.633s
			case 4:  fallInterval = 33.0/60.0; break;  // 0.550s
			case 5:  fallInterval = 28.0/60.0; break;  // 0.467s
			case 6:  fallInterval = 23.0/60.0; break;  // 0.383s
			case 7:  fallInterval = 18.0/60.0; break;  // 0.300s
			case 8:  fallInterval = 13.0/60.0; break;  // 0.217s
			case 9:  fallInterval = 8.0/60.0;  break;  // 0.133s
			case 10: fallInterval = 6.0/60.0;  break;  // 0.100s
			case 11: fallInterval = 5.0/60.0;  break;  // 0.083s
			case 12: fallInterval = 4.0/60.0;  break;  // 0.067s
			case 13: fallInterval = 3.0/60.0;  break;  // 0.050s
			case 14: fallInterval = 2.0/60.0;  break;  // 0.033s
			case 15: fallInterval = 1.0/60.0;  break;  // 0.017s
			default: fallInterval = Math.max(1.0/60.0, 48.0/60.0 - ((level-1) * 5.0/60.0));
		}
	}


	@Override
	public void update(double dt) {
		double maxDt = 0.1; // Clamp dt to avoid large jumps
		if(dt > maxDt) dt = maxDt;

		gameState.updateCountdown(); // Handles countdown state
		inputHandler.update(dt);     // Handles continuous input like DAS

		if (gameState.isGameOver() || gameState.isPaused() || gameState.isShowCountdown() || gameState.isShowHelp()) {
			return; // Game logic is paused
		}

		// Piece falling and locking logic
		double currentFallSpeed = inputHandler.isSoftDropping() ? (fallInterval / 20.0) : fallInterval;
		fallTimer += dt;

		while (fallTimer >= currentFallSpeed) {
			fallTimer -= currentFallSpeed;
			if (currentPiece.isLanded()) {
				lockTimer += currentFallSpeed; // Increment by the interval amount
				if (lockTimer >= lockDelay) {
					lockPiece();
					// lockPiece already calls spawnNewPiece and checks for game over
					// No need to return immediately unless lockPiece does everything
				}
			} else {
				currentPiece.moveDown();
				lockTimer = 0; // Reset lock timer if piece moves down
			}
		}
	}

	private void lockPiece() {
		currentPiece.lockPiece(); // Piece places itself on the board
		board.checkAndClearCompletedRows(scoreManager, scoreManager.getLevel());
		updateFallInterval(); // Update speed for next piece based on new score/level
		spawnNewPiece(); // Spawns a new piece and checks for game over
		lockTimer = 0; // Reset lock timer for the new piece
		fallTimer = 0; // Reset fall timer for the new piece
	}

	public void hardDrop() {
		if (currentPiece == null || gameState.isGameOver() || gameState.isPaused()) return;
		while (!currentPiece.isLanded()) {
			currentPiece.moveDown();
		}
		lockPiece(); // Use the unified lockPiece logic
	}

	public void rotatePiece(boolean clockwise) {
        if (currentPiece == null || gameState.isGameOver() || gameState.isPaused()) return;
        if (clockwise) {
            currentPiece.rotateClockwise();
        } else {
            currentPiece.rotateCounterClockwise();
        }
    }

	@Override
	public void paintComponent() {
		renderer.render();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		inputHandler.keyPressed(e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		inputHandler.keyReleased(e);
	}

	// Getters for other classes to access necessary game components
	public Board getBoard() { return board; }
	public Piece getCurrentPiece() { return currentPiece; }
	public ScoreManager getScoreManager() { return scoreManager; }
	public GameState getGameState() { return gameState; }
	public Renderer getRenderer() { return renderer; }
}