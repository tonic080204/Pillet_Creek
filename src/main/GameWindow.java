package main;

import javax.swing.JFrame;

public class GameWindow {
    private JFrame jframe;

    public GameWindow(GamePanel gamePanel) {
        // Initialize JFrame and set up properties
        jframe = new JFrame();

        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.add(gamePanel);  // Add the GamePanel to the window
        jframe.setLocationRelativeTo(null);  // Center the window on the screen
        jframe.setResizable(false);
        jframe.pack();  // Make sure the window size is set properly
        jframe.setVisible(true);  // Show the window
    }
}
