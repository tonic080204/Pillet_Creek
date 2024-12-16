package main;

public class Game implements Runnable {

    private GameWindow gameWindow;
    private GamePanel gamePanel;
    private Thread gameThread;
    private final int TARGET_FPS = 120;
    private final int TARGET_UPS = 60;

    public Game() {
        gamePanel = new GamePanel();  // Initialize the GamePanel
        gameWindow = new GameWindow(gamePanel);  // Pass GamePanel to GameWindow
        gamePanel.requestFocus();  // Focus the panel to receive input
        startGameLoop();  // Start the game loop
    }

    private void startGameLoop() {
        gameThread = new Thread(this);  // Create a new thread for the game loop
        gameThread.start();  // Start the game loop thread
    }

    @Override
    public void run() {
        double timePerUpdate = 1000000000.0 / TARGET_UPS;
        double timePerFrame = 1000000000.0 / TARGET_FPS;

        long lastUpdateTime = System.nanoTime();
        long lastRenderTime = System.nanoTime();
        long lastSecondTime = System.currentTimeMillis();

        int frames = 0;
        int updates = 0;

        while (true) {
            long now = System.nanoTime();

            if (now - lastUpdateTime >= timePerUpdate) {
                updateGame();
                lastUpdateTime += timePerUpdate;
                updates++;
            }

            if (now - lastRenderTime >= timePerFrame) {
                renderGame();
                lastRenderTime = now;
                frames++;
            }

            if (System.currentTimeMillis() - lastSecondTime >= 1000) {
                lastSecondTime = System.currentTimeMillis();
                System.out.println("FPS: " + frames + " | UPS: " + updates);
                frames = 0;
                updates = 0;
            }
        }
    }

    private void updateGame() {
        gamePanel.update();  // Update the game panel (and everything inside it)
    }

    private void renderGame() {
        gamePanel.repaint();  // Repaint the game panel to refresh the display
    }
    
    public void shutdown() {
        if (gamePanel != null) {
            gamePanel.cleanup();
        }
    }
}
