package main;


import inputs.KeyboardInputs;
import inputs.MouseInputs;
import java.awt.*;
import javax.swing.*;
import sounds.SoundManager;
import java.io.IOException;
import gamestate.GameState;
import gamestate.GameStateManager;
import main.GamePanel;
import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import main.InventoryManager;
import java.util.ArrayList; // Explicit import for ArrayList
import java.util.List;      // Import for List interface
import java.awt.Font;
import java.awt.FontMetrics;


public class GamePanel extends JPanel {
    private MouseInputs mouseInputs;
    private KeyboardInputs keyboardInputs;
    private Player player;
    private Level level;
    private Camera camera;
    private BackgroundLayer parallaxBackground;
    private Flashlight flashlight;
    private InteractableObject interactableObject; // New interactable object
    private boolean isFlashing = false;
    private float flashAlpha = 1f;
    private final float FLASH_DURATION = 30; // Frames
    private float currentFlashFrame = 0;
    private GamePanel gamePanel;
    private JFrame openingSceneFrame; // New frame for opening scene
    private Notes notes; // Add this field
    private BufferedImage currentNoteImage; // To hold the currently displayed note
    private boolean isNoteDisplayed = false; // Flag to check if a note is displayed
    private int currentCutsceneScene = 0;
    private String[] cutsceneScenes = {
        "Something is wrong",
        "Where's Sarah?",
        "You walked for a couple of minutes, and there you stumbled upon Sarah's lifeless corpse",
        "You are doomed, stuck, the choppers will not be coming for you",
        "Survive till daytime"
    };
    private Jumpscare jumpscare;
    
    // Sound manager reference
    private SoundManager soundManager;

    public GamePanel() {
        // Initialize sound manager
        soundManager = new SoundManager();
        // Initialize flashlight 
        flashlight = new Flashlight();
        jumpscare = new Jumpscare(this, flashlight);
        mouseInputs = new MouseInputs(this);
        player = new Player(100, 350, flashlight);
        player.setGamePanel(this); // Add this line
        level = new Level(550, 500);
        interactableObject = new InteractableObject(300, level.getYPosition(), 50, flashlight);
        keyboardInputs = new KeyboardInputs(this);
        camera = new Camera(this);
        notes = new Notes(50000, level.getYPosition()); // Use level width and y position

        
        
        // Initialize parallax background 
        parallaxBackground = new BackgroundLayer(550, 500);

        setPanelSize();
        addKeyListener(keyboardInputs);
        addMouseListener(mouseInputs);
        addMouseMotionListener(mouseInputs);
        setFocusable(true);
        requestFocusInWindow();

        // Foreground level customizations 
        level.setTreeSpacing(200);
        level.setTreeSize(550);
        level.setTreeVerticalOffset(-540);
        level.setRandomnessFactor(0.7f);
        level.setBushSpacing(200);  // Adjust bush spacing
        level.setBushRandomnessFactor(0.6f);  // Adjust randomness of bush placement
        // Set vertical offset for bushes (can be positive or negative)
        level.setBushVerticalOffset(35);  // Moves bushes 20 pixels up or down

        // Set bush size as a factor of original image size
        level.setBushSizeFactor(0.9f);  // Scales bushes to 80% of their original size
        level.setObjectSpacing(400);  // Wider spacing
        level.setObjectRandomnessFactor(0.7f);  // More random placement
        level.setMaxObjects(15);  // More objects
        level.setObjectSize(40);  // Slightly smaller objects
        level.setInteractableObjectVerticalOffset(5);  // Move objects 20 pixels up
        
        // Parallax background customizations 
        parallaxBackground.setParallaxSpeed(0.70f);          
        parallaxBackground.setTreeSpacing(250);            
        parallaxBackground.setTreeSize(650);               
        parallaxBackground.setTreeVerticalOffset(-600);    
        parallaxBackground.setHueAdjust(0.1f);             
        parallaxBackground.setBrightnessAdjust(1.2f);      
        parallaxBackground.setOffsetX(50);                 
        parallaxBackground.setOffsetY(20);                 
    }
    
    public void setCurrentNoteImage(BufferedImage image) {
        this.currentNoteImage = image;
    }
    
    public void setNoteDisplayed(boolean displayed) {
        this.isNoteDisplayed = displayed;
    }
    
    public void triggerFlash() {
    Flashlight flashlight = getFlashlight();

    // Debugging print
    System.out.println("Trigger Flash Called");

    if (flashlight.getBatteryLife() >= 10f) {
        isFlashing = true;
        flashAlpha = 1f;  // Explicitly set to full opacity
        currentFlashFrame = 0;

        flashlight.reduceBatteryLife(10f);

        // Debugging print
        System.out.println("Flash Activated - Alpha: " + flashAlpha);

        soundManager.playFlashSound();
        repaint();
        }
    }


    // Method to toggle flashlight 
    public void toggleFlashlight() {
        flashlight.toggle();
    }

    // Getter for flashlight 
    public Flashlight getFlashlight() {
        return flashlight;
    }

    private void setPanelSize() {
        Dimension size = new Dimension(1380, 800);
        setMinimumSize(size);
        setPreferredSize(size);
        setMaximumSize(size);
    }

    public void update() {
    if (GameStateManager.isState(GameState.PLAYING)) {
        // Existing update logic
        keyboardInputs.update(1.0f / 60);
        player.update(level, interactableObject);
        camera.update(player);
        manageBackgroundMusic();
        flashlight.updateBatteryLife();
        jumpscare.update();
        // Instead of drawing here, just trigger a repaint
        if (isNoteDisplayed && currentNoteImage != null) {
            repaint(); // This will call paintComponent()
        }
        
        // Update flash effect
        if (isFlashing) {
            currentFlashFrame++;
            
            // Debugging print
            System.out.println("Flash Frame: " + currentFlashFrame);

            // Gradually reduce alpha
            flashAlpha = Math.max(0, 1f - (currentFlashFrame / (float)FLASH_DURATION));

            // Debugging print
            System.out.println("Updated Flash Alpha: " + flashAlpha);

            // End flash when duration is complete
            if (currentFlashFrame >= FLASH_DURATION) {
                isFlashing = false;
                flashAlpha = 0f;
                currentFlashFrame = 0;
            }

            // Always repaint during flash
            repaint();
            }
            InventoryManager inventoryManager = InventoryManager.getInstance();
            if (inventoryManager.shouldTriggerCutscene()) {

                GameStateManager.setState(GameState.CUTSCENE);

                inventoryManager.resetCutsceneTimer();

            }
        }
    }
    
    private void manageBackgroundMusic() {

    // Only start background music when in PLAYING state 
    if (GameStateManager.isState(GameState.PLAYING)) { 
        soundManager.startBackgroundMusic(); 

    } else { 
        // Stop music in other states, including MAIN_MENU 
        soundManager.stopBackgroundMusic(); 
        }
    }

        @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Debugging print
        if (isFlashing) {
            System.out.println("Painting Flash - Alpha: " + flashAlpha);
        }

        // Explicitly render white screen
        if (isFlashing) {
            Graphics2D g2d = (Graphics2D) g.create();

            // Use AlphaComposite to control transparency
            Composite originalComposite = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, flashAlpha));

            // Fill entire screen with white
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            // Restore original composite
            g2d.setComposite(originalComposite);
            g2d.dispose();
        }

        if (GameStateManager.isState(GameState.MAIN_MENU)) {
            drawMainMenu(g);
        } else if (GameStateManager.isState(GameState.HOW_TO_PLAY)) {
            drawHowToPlayScreen(g);
        } else if (GameStateManager.isState(GameState.PLAYING)) {
            drawGame(g);
            flashlight.drawBatteryText(g, getWidth(), getHeight());
        } else if (GameStateManager.isState(GameState.OPENING_SCENE)) {
            // The GIF will be added directly to the panel, so no special drawing needed        

        } else if (GameStateManager.isState(GameState.PAUSED)) {
            drawGame(g);
            drawPauseMenu(g);
            flashlight.drawBatteryText(g, getWidth(), getHeight());
        }
        else if (GameStateManager.isState(GameState.CUTSCENE)) {
        drawCutscene(g);
        }
        
        /// Draw the note if it's displayed
    if (isNoteDisplayed && currentNoteImage != null) {
    Graphics2D g2d = (Graphics2D) g.create();

    // Calculate center of the screen
    int x = (getWidth() - currentNoteImage.getWidth()) / 2;
    int y = (getHeight() - currentNoteImage.getHeight()) / 2;

    // Optional: Add rendering hints for better quality
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // Draw the note image
    g2d.drawImage(currentNoteImage, x, y, null);

    g2d.dispose();
    }
        
    }

    private void drawMainMenu(Graphics g) {
    try {
        // Load the main menu background image
        BufferedImage mainMenuBackground = ImageIO.read(getClass().getResourceAsStream("/main_menu.png"));
        // Draw the background image
        g.drawImage(mainMenuBackground, 0, 0, getWidth(), getHeight(), null);
    } catch (IOException e) {
        // Fallback to black background if image fails to load
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        e.printStackTrace();
    }

    // Start button
    int buttonWidth = 200;
    int buttonHeight = 60;
    int startButtonX = 950;
    int startButtonY = 450;

    // How to Play button
    int howToPlayButtonX = 950;
    int howToPlayButtonY = 500;
    
    // Exit button 
    int exitButtonX = 950; 
    int exitButtonY = 550; // Positioned below the How to Play button

    // Check if mouse is hovering over the START button
    Point mousePosition = getMousePosition();
    boolean isStartHovering = mousePosition != null && 
                               mousePosition.x >= startButtonX && 
                               mousePosition.x <= startButtonX + buttonWidth && 
                               mousePosition.y >= startButtonY && 
                               mousePosition.y <= startButtonY + buttonHeight;

    // Check if mouse is hovering over the HOW TO PLAY button
    boolean isHowToPlayHovering = mousePosition != null && 
                                   mousePosition.x >= howToPlayButtonX && 
                                   mousePosition.x <= howToPlayButtonX + buttonWidth && 
                                   mousePosition.y >= howToPlayButtonY && 
                                   mousePosition.y <= howToPlayButtonY + buttonHeight;

    // Check if mouse is hovering over the EXIT button
    boolean isExitHovering = mousePosition != null && 
                         mousePosition.x >= exitButtonX && 
                         mousePosition.x <= exitButtonX + buttonWidth && 
                         mousePosition.y >= exitButtonY && 
                         mousePosition.y <= exitButtonY + buttonHeight;
    
    // Draw START button text
    g.setFont(new Font("Arial", Font.BOLD, 36));
    FontMetrics fm = g.getFontMetrics();
    String startText = "START";
    int startTextWidth = fm.stringWidth(startText);
    int startTextX = startButtonX + (buttonWidth - startTextWidth) / 2;
    int startTextY = startButtonY + (buttonHeight - fm.getHeight()) / 2 + fm.getAscent();

    // Draw HOW TO PLAY button text
    String howToPlayText = "HOW TO PLAY";
    int howToPlayTextWidth = fm.stringWidth(howToPlayText);
    int howToPlayTextX = howToPlayButtonX + (buttonWidth - howToPlayTextWidth) / 2;
    int howToPlayTextY = howToPlayButtonY + (buttonHeight - fm.getHeight()) / 2 + fm.getAscent();

    // Draw EXIT button text 
    String exitText = "EXIT"; 
    int exitTextWidth = fm.stringWidth(exitText); 
    int exitTextX = exitButtonX + (buttonWidth - exitTextWidth) / 2; 
    int exitTextY = exitButtonY + (buttonHeight - fm.getHeight()) / 2 + fm.getAscent(); 
    
    // Set text color based on hover state
    Color startTextColor = isStartHovering ? new Color(64, 58, 58) : Color.WHITE;
    Color howToPlayTextColor = isHowToPlayHovering ? new Color(64, 58, 58) : Color.WHITE;
    Color exitTextColor = isExitHovering ? new Color(64, 58, 58) : Color.WHITE; 
    
    // Draw buttons
    g.setColor(startTextColor);
    g.drawString(startText, startTextX, startTextY);

    g.setColor(howToPlayTextColor);
    g.drawString(howToPlayText, howToPlayTextX, howToPlayTextY);
    
    g.setColor(exitTextColor); 
    g.drawString(exitText, exitTextX, exitTextY); 
    }

    private void drawHowToPlayScreen(Graphics g) {
    try {
        // Load the how to play screen image
        BufferedImage howToPlayBackground = ImageIO.read(getClass().getResourceAsStream("/how_to_play_screen.png"));
        // Draw the background image
        g.drawImage(howToPlayBackground, 0, 0, getWidth(), getHeight(), null);
    } catch (IOException e) {
        // Fallback to black background if image fails to load
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        e.printStackTrace();
    }

    // Back button
    int buttonWidth = 200;
    int buttonHeight = 60;
    int backButtonX = 590; // Centered horizontally
    int backButtonY = 700; // Near bottom of screen

    // Check if mouse is hovering over the BACK button
    Point mousePosition = getMousePosition();
    boolean isBackHovering = mousePosition != null && 
                              mousePosition.x >= backButtonX && 
                              mousePosition.x <= backButtonX + buttonWidth && 
                              mousePosition.y >= backButtonY && 
                              mousePosition.y <= backButtonY + buttonHeight;

    // Draw BACK button text
    g.setFont(new Font("Arial", Font.BOLD, 36));
    FontMetrics fm = g.getFontMetrics();
    String backText = "BACK";
    int backTextWidth = fm.stringWidth(backText);
    int backTextX = backButtonX + (buttonWidth - backTextWidth) / 2;
    int backTextY = backButtonY + (buttonHeight - fm.getHeight()) / 2 + fm.getAscent();

    // Set text color based on hover state
    Color backTextColor = isBackHovering ? new Color(64, 58, 58) : Color.WHITE;

    // Draw BACK button
    g.setColor(backTextColor);
    g.drawString(backText, backTextX, backTextY);
    }
    
    private void drawGame(Graphics g) { 
    // Fill the entire screen with black 
    g.setColor(Color.BLACK); 
    g.fillRect(0, 0, getWidth(), getHeight()); 

    // Apply camera offsets to level and player rendering 
    int xOffset = (int) camera.getXOffset(); 
    int yOffset = (int) camera.getYOffset(); 

    // Draw parallax background 
    parallaxBackground.draw(g, xOffset, yOffset); 

    // Draw the level (including bushes)
    level.draw(g, xOffset, yOffset); 

    // Draw the player 
    player.draw(g, xOffset, yOffset); 

    // Draw the interactable object 
    interactableObject.draw(g, xOffset, yOffset);
    
    notes.draw(g, xOffset, yOffset);

    // Draw flashlight overlay LAST to ensure it's on top of everything
    flashlight.drawFlashlightOverlay(g, player, camera, getWidth(), getHeight()); 
    }

    private void drawPauseMenu(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("PAUSED", 550, 200);
    }

    public Player getPlayer() {
        return player;
    }

    // Cleanup method to stop sounds when the game closes
    public void cleanup() {
        // Existing cleanup...
        if (flashlight != null) {
            flashlight.cleanup();
        }
        if (jumpscare != null) {

        jumpscare.cleanup();

    }
    }
    
    public InteractableObject getInteractableObject() {
    return interactableObject;
    }
    
    public Level getLevel() {
    return level;
    }
    
    public Notes getNotes() {
    return notes;
    }
    
    public void dismissNote() {
    setNoteDisplayed(false);
    setCurrentNoteImage(null);
    repaint(); // Force a repaint to remove the note
    }
    
    public void displayRandomNote() {
    Notes notes = this.getNotes(); // Use 'this' to reference the current GamePanel
    BufferedImage randomNoteImage = notes.getRandomNoteImage();

    if (randomNoteImage != null) {
        setCurrentNoteImage(randomNoteImage);
        setNoteDisplayed(true);
        repaint(); // Force a repaint to show the note
    } else {
        System.err.println("No note image available");
        }
    }
    
    public boolean isNoteDisplayed() {
    return isNoteDisplayed;
    }
    
    private void drawCutscene(Graphics g) {
        // Set background color to black
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Set text color to white
        g.setColor(Color.WHITE);
        
        // Main scene text font
        Font mainFont = new Font("Arial", Font.BOLD, 36);
        g.setFont(mainFont);
        FontMetrics mainFm = g.getFontMetrics();

        // Get current scene text
        String sceneText = cutsceneScenes[currentCutsceneScene];

        // Calculate text wrapping
        List<String> wrappedLines = wrapText(sceneText, getWidth() - 100, mainFont);

        // Calculate total text height
        int lineHeight = mainFm.getHeight();
        int totalTextHeight = lineHeight * wrappedLines.size();

        // Calculate starting Y position to center the text vertically
        int y = (getHeight() - totalTextHeight) / 2;

        // Draw each line of text
        for (String line : wrappedLines) {
            int textWidth = mainFm.stringWidth(line);
            int x = (getWidth() - textWidth) / 2;
            g.drawString(line, x, y);
            y += lineHeight;
        }

        // "Click to continue" subtitle
        Font subtitleFont = new Font("Arial", Font.PLAIN, 24);
        g.setFont(subtitleFont);
        FontMetrics subtitleFm = g.getFontMetrics();
        String subtitleText = "Click to continue";
        int subtitleWidth = subtitleFm.stringWidth(subtitleText);
        int subtitleX = (getWidth() - subtitleWidth) / 2;
        int subtitleY = getHeight() - 50;
        
        // Slightly fade the subtitle
        g.setColor(new Color(255, 255, 255, 180));
        g.drawString(subtitleText, subtitleX, subtitleY);
    }

    // Method to handle scene progression
    public void progressCutscene() {
        currentCutsceneScene++;
        
        // If we've reached the end of scenes, return to main menu
        if (currentCutsceneScene >= cutsceneScenes.length) {
            GameStateManager.setState(GameState.MAIN_MENU);
            currentCutsceneScene = 0; // Reset for potential future use
        }
        
        repaint(); // Redraw the scene
    }

    // Text wrapping utility method
    private List<String> wrapText(String text, int maxWidth, Font font) {
        List<String> wrappedLines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        FontMetrics fm = getFontMetrics(font);

        for (String word : words) {
            if (fm.stringWidth(currentLine + " " + word) < maxWidth) {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            } else {
                wrappedLines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            }
        }

        if (currentLine.length() > 0) {
            wrappedLines.add(currentLine.toString());
        }

        return wrappedLines;
    }
    
}