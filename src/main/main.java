package main;

import gamestate.GameState;
import gamestate.GameStateManager;

public class main {
    public static void main(String[] args) {
        // Initialize the game state
        GameStateManager.setState(GameState.MAIN_MENU);

        // Create the game instance
        Game game = new Game();
    }
}