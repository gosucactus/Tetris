# 🎮 Tetris Game (Java OOP Edition)

Welcome to the Tetris Game project built using Java and object-oriented programming principles. This project demonstrates a modular design of a classic puzzle game using clean architecture, game loop logic, and custom rendering.

## 🗂️ Code Structure

| Class         | Description                                                                 |
|---------------|-----------------------------------------------------------------------------|
| `TetrisGame`  | Main controller class managing game state, input, rendering, and updates.   |
| `Board`       | Represents the Tetris grid. Handles collisions, placement, and row clearing.|
| `Piece`       | Represents and controls the falling Tetris block (Tetromino).               |
| `ScoreManager`| Manages player score and levels based on gameplay.                          |
| `GameState`   | Handles game status like pause, game over, and menu options.                |
| `InputHandler`| Deals with player inputs (keyboard events).                                 |
| `Renderer`    | Renders game objects using custom graphics engine.                          |

## 🖼️ Game Snapshots

Below are some gameplay screenshots showing different game states:

### ▶️ Game Start
![Game Start](assets/Game1.png)

### ▶️ Ghost Pieces
![Game Start](assets/Game2.png)

### 🧱 Help Screen
![Gameplay](assets/Game_help.png)

### 💥 Pause Menu
![Game Over](assets/Game_pause.png)

## 🧩 Class Diagram

![Tetris UML Diagram](assets/Tetris_Game%20_Classes.png)

*This UML diagram illustrates the main architecture of the Tetris game and how different components interact with each other.*
