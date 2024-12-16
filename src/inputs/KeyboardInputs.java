package inputs;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

import gamestate.GameState;
import gamestate.GameStateManager;
import main.GamePanel;
import main.InteractableObject;
import main.Player;

public class KeyboardInputs implements KeyListener {
    private GamePanel gamePanel;
    private Set<Integer> pressedKeys;
    private int lastDirection = 0; // Track the last horizontal direction (-1 for left, 1 for right, 0 for none)

    public KeyboardInputs(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        pressedKeys = new HashSet<>();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        
        if (GameStateManager.isState(GameState.PLAYING) && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            GameStateManager.setState(GameState.PAUSED);
        } else if (GameStateManager.isState(GameState.PAUSED) && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            GameStateManager.setState(GameState.PLAYING);
        } else {
            pressedKeys.add(e.getKeyCode());
            handleMovement();
        }
        
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
        handleMovement();
    }

    private void handleMovement() {
    if (GameStateManager.isState(GameState.PLAYING)) {
        Player player = gamePanel.getPlayer();
        InteractableObject interactableObject = gamePanel.getInteractableObject(); // You'll need to add this method to GamePanel
        boolean moving = false;

        if (pressedKeys.contains(KeyEvent.VK_A)) {
            player.move(-1, 0, 1.0f / 60); // Move left
            lastDirection = -1;
            moving = true;
        }

        if (pressedKeys.contains(KeyEvent.VK_D)) {
            player.move(1, 0, 1.0f / 60); // Move right
            lastDirection = 1;
            moving = true;
        }

        if (!moving) {
            player.stop(); // Stop movement if no keys are pressed
        }

        if (pressedKeys.contains(KeyEvent.VK_SPACE)) {
            player.jump();
        }

        // Add pickup interaction with 'F' key
        if (pressedKeys.contains(KeyEvent.VK_F)) {
        player.tryPickup(gamePanel.getLevel().getInteractableObjects());
        player.tryPickup(gamePanel.getNotes().getNotes()); // Add this line
        }
    }
}

    public void update(float delta) {
        handleMovement();
    }
}
