package main;

public class Camera {

    private float xOffset, yOffset;
    private float smoothness = 0.1f; // Lower values for slower panning
    private float targetOffsetX = 0; // Target X offset for smooth panning
    private float targetOffsetY = -150; // Target Y offset for vertical adjustment
    private GamePanel gamePanel;

    public Camera(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void update(Player player) {
    // Calculate center of player 
    float centerX = player.getX() - gamePanel.getWidth() / 2 + player.getWidth() / 2; 
    float centerY = player.getY() - gamePanel.getHeight() / 2 + player.getHeight() / 2; 

    // Adjust target offset based on player movement direction 
    if (player.isMovingRight()) { 
        targetOffsetX = 250;  // Shift camera to the right 
    } else if (player.isMovingLeft()) { 
        targetOffsetX = -250; // Shift camera to the left 
    } else { 
        targetOffsetX = 0;    // Center the camera when idle 
    } 

    // Smoothly interpolate xOffset and yOffset to approach target offsets 
    xOffset += (centerX + targetOffsetX - xOffset) * smoothness; 
    yOffset += (centerY + targetOffsetY - yOffset) * smoothness; 
}

    // Method to set vertical offset for the camera
    public void setVerticalOffset(float offsetY) {
        this.targetOffsetY = offsetY;
    }

    public float getXOffset() {
        return xOffset;
    }

    public float getYOffset() {
        return yOffset;
    }
}
