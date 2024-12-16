package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;

public class InteractableObject {
    private int x, y, size;
    private BufferedImage objectImage;
    private Rectangle hitbox;
    private boolean isPickedUp = false;
    private boolean isVisible = true;
    private int textDisplayTime = 4400; // Default display time (2 seconds at 60 FPS)
    private int textX = 0; // X position of the text
    private int textY = 0; // Y position of the text
    private int textSize = 15; // Default text size
    private Color textColor = Color.YELLOW; // Default text color
    private boolean textDisplayed = false; // Flag to track text display state
    private float batteryPercentageToAdd;
    private Flashlight flashlight; // Add a reference to the flashlight
    private int maxObjects = 30;
    
    // Randomizer-related fields
    private String interactionText = "";
    private Font customFont;
    private Random random = new Random();

    // Constructor to initialize position and size
    public InteractableObject(int x, int y, int size, Flashlight flashlight) {
        this.x = x;
        this.y = y - size;
        this.size = size;
        this.hitbox = new Rectangle(this.x, this.y, size, size);
        this.flashlight = flashlight;
        loadImage();
        loadCustomFont();
    }

    // Load image for the interactable object 
    private void loadImage() {
        try (var is = getClass().getResourceAsStream("/interactable_object.png")) {
            if (is != null) {
                objectImage = ImageIO.read(is);
            } else {
                System.err.println("Interactable object image not found! ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Draw method
    public void draw(Graphics g, int xOffset, int yOffset) {
        if (!isVisible) return; 

        int drawX = x - xOffset; 
        int drawY = y - yOffset; 

        // Draw object 
        if (objectImage != null) { 
            g.drawImage(objectImage, drawX, drawY, size, size, null); 
        } else { 
            g.setColor(Color.ORANGE); 
            g.fillRect(drawX, drawY, size, size); 
        } 

        // Draw interaction text if applicable 
        if (textDisplayed && textDisplayTime > 0 && customFont != null) { 
            g.setFont(customFont.deriveFont((float)textSize)); 
            g.setColor(textColor); 

            // Calculate text width for centering 
            int textWidth = g.getFontMetrics().stringWidth(interactionText); 

            // Determine X position 
            int displayX;
            if (textX == 0) {
                // Center horizontally above the object
                displayX = drawX + (size - textWidth) / 2;
            } else {
                displayX = textX;
            }

            // Determine Y position 
            int displayY;
            if (textY == 0) {
                // Position above the object
                displayY = drawY - 65;  // 20 pixels above the object
            } else {
                displayY = textY;
            }

            g.drawString(interactionText, displayX, displayY); 

            textDisplayTime--; 

            // Reset text display when time is up 
            if (textDisplayTime <= 0) { 
                textDisplayed = false; 
            } 
        } 
    }


    // Check if player is close enough to interact
    public boolean isInteractable(Player player) {
        Rectangle playerHitbox = new Rectangle(
            (int) player.getX(), 
            (int) player.getY(), 
            player.getWidth(), 
            player.getHeight()
        );

        return isVisible && 
               !isPickedUp && 
               playerHitbox.intersects(hitbox);
    }

    // Interaction method
        public void interact(Player player) {
    if (isInteractable(player)) {
        player.startPickup(); // Start the pickup animation

        // Set a listener to be called when the pickup animation is complete
        player.setPickupCompletionListener(() -> {
            // Generate interaction text 
            generateInteractionText();

            // Always add battery percentage if applicable
            if (batteryPercentageToAdd > 0 && player.getFlashlight() != null) {
                // Add the battery charge
                player.getFlashlight().addBatteryCharge(batteryPercentageToAdd);
            }

            // Make the object disappear after pickup
            isVisible = false; 
            isPickedUp = true; 

            // Display the interaction text
            textDisplayed = true; 
            textDisplayTime = 120; // Reset text display time
        });
    }
}


    // Getters and Setters
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
        updateHitbox();
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
        updateHitbox();
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
        updateHitbox();
    }

    public boolean isPickedUp() {
        return isPickedUp;
    }

    public boolean isVisible() {
        return isVisible;
    }

    // Update hitbox when position or size changes
    private void updateHitbox() {
        hitbox.x = x;
        hitbox.y = y;
        hitbox.width = size;
        hitbox.height = size;
    }

    // Reset method if needed
   public void reset() {
        textDisplayed = false;
        textDisplayTime = 120; // Reset to default 
        isPickedUp = false;
        isVisible = true;
        // Add any other reset logic you need
    }
    
            private void loadCustomFont() {
                try {
                    // Replace with your font file path
                    File fontFile = new File("C:\\Users\\Local Admin\\Documents\\NetBeansProjects\\Pillet\\res\\VCR.ttf");
                    customFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(24f);
                } catch (FontFormatException | IOException e) {
                    // Fallback to default font if custom font fails to load
                    customFont = new Font("Arial", Font.BOLD, 24);
                    System.err.println("Could not load custom font: " + e.getMessage());
                }
            }

        private void generateInteractionText() {
            String[] noBatteryTexts = { 
                "nothing much here", 
                "hmm nothing", 
                "just garbage", 
                "empty huh..." 
            };

            String[] batteryTexts = { 
                "oh a battery", 
                "this will be handy", 
                "nice, something useful", 
                "battery, nice" 
            };

            // Weighted randomization to favor battery texts when a battery is picked up
            boolean isBatteryText = batteryPercentageToAdd > 0 || random.nextFloat() < 0.7f;

            if (isBatteryText) {
                // Always choose a battery text when battery is being added
                interactionText = batteryTexts[random.nextInt(batteryTexts.length)];

                // Always add 10% to the battery percentage when a battery is picked up
                if (batteryPercentageToAdd == 0) {
                    batteryPercentageToAdd = 10f;
                }

                // Append battery percentage to the text
                interactionText += String.format(" (+%.1f%%)", batteryPercentageToAdd);
            } else {
                // Choose from no battery texts
                interactionText = noBatteryTexts[random.nextInt(noBatteryTexts.length)];
                batteryPercentageToAdd = 0;
            }
        }
    
        public void setTextDisplay(int x, int y, int size, Color color, int displayTime) {
        // If x or y is 0, it will use default positioning
        this.textX = x;  
        this.textY = y;  
        this.textSize = size; 
        this.textColor = color; 
        this.textDisplayTime = displayTime; 
        }
        
        
}