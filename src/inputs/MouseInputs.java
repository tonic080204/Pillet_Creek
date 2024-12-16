package inputs;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.sound.sampled.*;
import java.net.URL;
import javax.swing.JFrame;  // Add this import
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BorderLayout;

import gamestate.GameState;
import gamestate.GameStateManager;
import java.awt.Graphics;
import main.GamePanel;
import main.InventoryManager;

public class MouseInputs implements MouseListener, MouseMotionListener {
    private GamePanel gamePanel;
    private Timer gifTimer;
    private ImageIcon openingSceneGif;
    private JLabel gifLabel;
    
    public MouseInputs(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    @Override
public void mouseClicked(MouseEvent e) {
    if (GameStateManager.isState(GameState.MAIN_MENU)) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        // Start button dimensions 
        int buttonWidth = 200;
        int buttonHeight = 60;
        int startButtonX = 950;
        int startButtonY = 450;

        // How to Play button dimensions 
        int howToPlayButtonX = 950;
        int howToPlayButtonY = 500;

        // Check if the START button is clicked 
        if (mouseX >= startButtonX && mouseX <= startButtonX + buttonWidth && 
                mouseY >= startButtonY && mouseY <= startButtonY + buttonHeight) {
                
                // Play Opening Scene GIF
                playOpeningScene();
            }
        
        // Check if the HOW TO PLAY button is clicked 
        else if (mouseX >= howToPlayButtonX && mouseX <= howToPlayButtonX + buttonWidth && 
                 mouseY >= howToPlayButtonY && mouseY <= howToPlayButtonY + buttonHeight) {
            GameStateManager.setState(GameState.HOW_TO_PLAY);
        }
    } 
    else if (GameStateManager.isState(GameState.HOW_TO_PLAY)) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        int buttonWidth = 200;
        int buttonHeight = 60;
        int backButtonX = 590;
        int backButtonY = 700;

        // Check if the BACK button is clicked 
        if (mouseX >= backButtonX && mouseX <= backButtonX + buttonWidth && 
            mouseY >= backButtonY && mouseY <= backButtonY + buttonHeight) {
            // Return to main menu 
            GameStateManager.setState(GameState.MAIN_MENU);
        }
    }
    
    if (GameStateManager.isState(GameState.PLAYING)) {
        // Check if a note is currently displayed
        if (gamePanel.isNoteDisplayed()) {
            gamePanel.dismissNote();

            // Check if all notes have been collected
            InventoryManager inventoryManager = InventoryManager.getInstance();
            if (inventoryManager.getCollectedNotesCount() == InventoryManager.MAX_NOTES) {
                // The cutscene will be triggered by the timer in GamePanel
                return;
                }
            }
        }
    }

    // Existing methods remain unchanged

    @Override
public void mousePressed(MouseEvent e) {
    int x = e.getX();
    int y = e.getY();
    // Check if current state is MAIN_MENU
        if (GameStateManager.isState(GameState.MAIN_MENU)) {
            // Exit button coordinates from GamePanel
            int exitButtonX = 950;
            int exitButtonY = 550;
            int buttonWidth = 200;
            int buttonHeight = 60;

            // Check if click is within exit button bounds
            if (x >= exitButtonX && x <= exitButtonX + buttonWidth &&
                y >= exitButtonY && y <= exitButtonY + buttonHeight) {
                // Exit the application
                System.exit(0);
            }
        }
    // Specifically handle cutscene progression
    if (GameStateManager.isState(GameState.CUTSCENE)) {
        // Progress to next scene when left mouse button is clicked
        if (e.getButton() == MouseEvent.BUTTON1) {
            gamePanel.progressCutscene();
            return;  // Important to return and prevent other actions
        }
    }

    // Rest of the existing mouse pressed logic for other game states
    if (GameStateManager.isState(GameState.PLAYING)) {
        // Check if a note is currently displayed
        if (gamePanel.isNoteDisplayed()) {
            // If a note is displayed, left click should only dismiss the note
            if (e.getButton() == MouseEvent.BUTTON1) {
                gamePanel.dismissNote();
                return;  // Exit the method to prevent further actions
            }
        }

        // Left click for toggling flashlight (only if no note is displayed)
        if (e.getButton() == MouseEvent.BUTTON1) {
            gamePanel.toggleFlashlight();
        }

    }
}
    
    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
    if (GameStateManager.isState(GameState.MAIN_MENU) || 
        GameStateManager.isState(GameState.HOW_TO_PLAY)) {
        gamePanel.repaint(); // This will trigger a redraw and update button appearances
        }
    }
    
    private void playOpeningScene() {
    // Load the GIF 
    openingSceneGif = new ImageIcon(getClass().getResource("/scenes/opening_scene.gif")); 

    // Load and prepare the audio
    try {
        URL audioURL = getClass().getResource("/scenes/opening_scene.wav");
        if (audioURL != null) {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioURL);
            Clip audioClip = AudioSystem.getClip();
            audioClip.open(audioInputStream);

            // Create a JLabel with the GIF and set it to fill the entire panel 
            gifLabel = new JLabel(openingSceneGif) { 
                @Override 
                public void paintComponent(Graphics g) { 
                    // Get the panel's size 
                    int panelWidth = gamePanel.getWidth(); 
                    int panelHeight = gamePanel.getHeight(); 

                    // Get the original image size 
                    int imageWidth = openingSceneGif.getIconWidth(); 
                    int imageHeight = openingSceneGif.getIconHeight(); 

                    // Calculate scaling factors 
                    float scaleX = (float)panelWidth / imageWidth; 
                    float scaleY = (float)panelHeight / imageHeight; 

                    // Use the minimum scale factor to maintain aspect ratio 
                    float scale = Math.min(scaleX, scaleY); 

                    // Calculate new dimensions 
                    int scaledWidth = Math.round(imageWidth * scale); 
                    int scaledHeight = Math.round(imageHeight * scale); 

                    // Calculate position to center the image 
                    int x = (panelWidth - scaledWidth) / 2; 
                    int y = (panelHeight - scaledHeight) / 2; 

                    // Draw the scaled and centered image 
                    Graphics2D g2d = (Graphics2D) g; 
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR); 
                    g2d.drawImage(openingSceneGif.getImage(), x, y, scaledWidth, scaledHeight, this); 
                } 
            }; 

            // Set the layout to null to allow precise positioning 
            gamePanel.setLayout(null); 

            // Set the GIF label to fill the entire panel 
            gifLabel.setBounds(0, 0, gamePanel.getWidth(), gamePanel.getHeight()); 

            // Set the game state to OPENING_SCENE 
            GameStateManager.setState(GameState.OPENING_SCENE); 

            // Add the GIF to the game panel 
            gamePanel.add(gifLabel); 

            // Start playing the audio 
            audioClip.start();

            // Create a timer to remove the GIF after 25 seconds 
            gifTimer = new Timer(24012, e -> { 
                // Stop and close the audio 
                audioClip.stop();
                audioClip.close();

                // Remove the GIF label 
                gamePanel.remove(gifLabel); 

                // Restore the default layout 
                gamePanel.setLayout(new BorderLayout()); 

                // Set the game state to PLAYING 
                GameStateManager.setState(GameState.PLAYING); 

                // Repaint the game panel 
                gamePanel.revalidate(); 
                gamePanel.repaint(); 

                // Stop and nullify the timer 
                gifTimer.stop(); 
                gifTimer = null; 
            }); 

            // Start the timer 
            gifTimer.setRepeats(false); 
            gifTimer.start(); 

            // Repaint the game panel to show the GIF 
            gamePanel.revalidate(); 
            gamePanel.repaint(); 
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    
    // Start the timer 
    gifTimer.setRepeats(false); 
    gifTimer.start(); 
    
    // Repaint the game panel to show the GIF 
    gamePanel.revalidate(); 
    gamePanel.repaint(); 
}
}