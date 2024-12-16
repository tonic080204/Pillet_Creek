package main;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import sounds.SoundManager;

public class Flashlight {
    private boolean isOn;
    private float radius;
    private float maxRadius;
    private float minRadius;
    private Color overlayColor;
    private float offsetX;  // Horizontal offset from player center
    private float offsetY;  // Vertical offset from player center
    private float currentRadius;
    private SoundManager soundManager;
    
    // New directional offsets
    private float rightFacingOffset = 160;   // Offset when facing right
    private float leftFacingOffset = -50;   // Offset when facing left
    private float idleOffset = 0;           // Offset when idle
    // Battery-related variables
    private float batteryLife = 100f;  // Start at 100%
    private final float MAX_BATTERY_LIFE = 100f;
    private final float BATTERY_DRAIN_RATE = 0.005f;  // Percentage drained per update
    private boolean batteryDepleted = false;
    // Font-related variables
    private Font customFont;
    private Color batteryTextColor = Color.WHITE;

    public Flashlight() {
        this.isOn = true;
        this.maxRadius = 300;
        this.minRadius = 100;
        this.radius = 150;
        this.currentRadius = radius;
        this.offsetX = 0;
        this.offsetY = 50;
        // Very dark, almost pure black overlay
        this.overlayColor = new Color(0, 0, 0, 255);
        this.soundManager = new SoundManager();
        
        loadCustomFont();
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
    
    // Update battery life
    public void updateBatteryLife() {
    if (isOn && !batteryDepleted) {
        batteryLife -= BATTERY_DRAIN_RATE;

        // Check if battery is depleted 
        if (batteryLife <= 0) {
            batteryLife = 0;
            batteryDepleted = true;
            turnOff(); // This will play the flashlight_out.wav sound
            }
        }
    }
    
    // Method to add battery charge
    public void addBatteryCharge(float amount) {
    batteryLife = Math.min(MAX_BATTERY_LIFE, batteryLife + amount); 
    batteryDepleted = false; 
    }
    
    // Comprehensive setter methods 
    public void setPosition(float offsetX, float offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public void setRadius(float radius) {
        this.currentRadius = Math.max(minRadius, Math.min(maxRadius, radius));
    }

    public void setMaxRadius(float maxRadius) {
        this.maxRadius = maxRadius;
    }

    public void setMinRadius(float minRadius) {
        this.minRadius = minRadius;
    }

    // New method to set custom directional offsets
    public void setDirectionalOffsets(float rightOffset, float leftOffset, float idleOffset) {
        this.rightFacingOffset = rightOffset;
        this.leftFacingOffset = leftOffset;
        this.idleOffset = idleOffset;
    }

    public void toggle() {
        if (batteryDepleted) {
            return;  // Can't toggle if battery is depleted
        }

        if (isOn) {
            turnOff();
        } else {
            turnOn();
        }
    }
    
    private void turnOff() {
        if (isOn) {
            soundManager.playFlashlightOffSound();
            isOn = false;
        }
    }

    private void turnOn() {
        if (!isOn && !batteryDepleted) {
            soundManager.playFlashlightOnSound();
            isOn = true;
        }
    }
    
    // Method to draw battery percentage text
    public void drawBatteryText(Graphics g, int screenWidth, int screenHeight) {
    if (customFont != null) {
        Graphics2D g2d = (Graphics2D) g;

        // Set font and color based on battery level
        g2d.setFont(customFont);
        if (batteryLife > 50) {
            g2d.setColor(Color.GREEN);
        } else if (batteryLife > 20) {
            g2d.setColor(Color.YELLOW);
        } else {
            g2d.setColor(Color.RED);
        }

        // Format battery percentage string
        String batteryText = String.format("Battery Percentage: %.1f%%", batteryLife);

        // Get font metrics for centering
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(batteryText);

        // Position text in the middle, slightly above center
        int x = (screenWidth - textWidth) / 2;
        int y = screenHeight / 2 - 250;  // Adjust this value to position higher

        // Draw the text
        g2d.drawString(batteryText, x, y);
        }
    }
    
    // Setter for battery text color
    public void setBatteryTextColor(Color color) {
        this.batteryTextColor = color;
    }

    // Existing methods remain the same...
    // (drawFlashlightOverlay, other getters/setters)

    // In the GamePanel's paintComponent or update method, you'll need to call:
    // flashlight.updateBatteryLife();
    // And in the drawing method:
    // flashlight.drawBatteryText(g, getWidth(), getHeight());

    public boolean isOn() {
        return isOn;
    }

    public void drawFlashlightOverlay(Graphics g, Player player, Camera camera, int screenWidth, int screenHeight) {
        // Create a full screen dark overlay 
        Area darkOverlay = new Area(new Rectangle(0, 0, screenWidth, screenHeight)); 
 
        Graphics2D g2d = (Graphics2D) g.create(); 
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f)); 
 
        // Set color to dark overlay 
        g2d.setColor(overlayColor); 
 
        // Calculate player's screen position with custom offsets 
        int playerScreenX = (int)(player.getX() - camera.getXOffset() + offsetX); 
        int playerScreenY = (int)(player.getY() - camera.getYOffset() + offsetY); 
 
        // Adjust light direction based on player's facing direction 
        float lightDirectionOffset = player.isFacingRight() ? rightFacingOffset 
                                   : !player.isFacingRight() ? leftFacingOffset 
                                   : idleOffset;
        playerScreenX += lightDirectionOffset; 
 
        // If the flashlight is on, create a circular light area 
        if (isOn) { 
            // Create a circular light area with custom position and radius 
            Ellipse2D lightArea = new Ellipse2D.Double( 
                playerScreenX - currentRadius, 
                playerScreenY - currentRadius, 
                currentRadius * 2, 
                currentRadius * 2 
            ); 
 
            // Subtract the light area from the dark overlay 
            darkOverlay.subtract(new Area(lightArea)); 
        } 
 
        // Fill the dark area 
        g2d.fill(darkOverlay); 
 
        // Optional: Add a soft glow effect if the flashlight is on 
        if (isOn) { 
            RadialGradientPaint softEdge = new RadialGradientPaint( 
                playerScreenX, playerScreenY, currentRadius, 
                new float[]{0.0f, 0.7f, 1.0f}, 
                new Color[]{ 
                    new Color(0, 0, 0, 0),     // Transparent at center 
                    new Color(0, 0, 0, 50),    // Very slight transparency 
                    new Color(0, 0, 0, 255)     // Darker at edges 
                } 
            ); 
 
            g2d.setPaint(softEdge); 
            g2d.fill(new Ellipse2D.Double( 
                playerScreenX - currentRadius, 
                playerScreenY - currentRadius, 
                currentRadius * 2, 
                currentRadius * 2 
            )); 
        } 
 
        // Dispose of the graphics context 
        g2d.dispose(); 
    }

    // Getter methods for current configuration 
    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public float getCurrentRadius() {
        return currentRadius;
    }
    
    public void setFlashlightSoundVolume(float volume) {
        soundManager.setFlashlightSoundVolume(volume);
    }
    
    public void cleanup() {
        if (soundManager != null) {
            soundManager.cleanup();
        }
    }
    
    public float getBatteryLife() {
    return batteryLife;
    }
    
    public void reduceBatteryLife(float amount) {
    batteryLife = Math.max(0, batteryLife - amount);
    
    // Check if battery is depleted
    if (batteryLife <= 0) {
        batteryLife = 0;
        batteryDepleted = true;
        turnOff();
        }
    }
    
}