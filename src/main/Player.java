package main;

import gamestate.GameState;
import gamestate.GameStateManager;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import sounds.SoundManager;

public class Player {
    private GamePanel gamePanel;
    private float x, y;
    private float yVelocity = 0;
    private final float gravity = 0.5f;
    private final float jumpStrength = -10f;
    private boolean onGround = false;
    private boolean moving = false;
    private boolean facingRight = true;
    private boolean movingLeft = false;
    private boolean movingRight = false;
    private int standOffset = 30;
    private Flashlight flashlight;
    private BufferedImage[][] animations;
    private int aniTick, aniIndex;
    private final int aniSpeed = 15; // Reduced from 25 to make animation smoother

    // Dimensions
    private final int width = 120, height = 110;
    private final float baseSpeed = 80f;

    // Pickup animation variables
    private boolean isPickingUp = false;
    private int pickupAniTick = 0;
    private final int pickupAniSpeed = 10;
    private final int pickupAniLength = 22;

    // Sound manager for walking sounds
    private SoundManager soundManager;
    
    public interface PickupCompletionListener {
        void onPickupComplete();
    }

    private PickupCompletionListener pickupCompletionListener;


    public Player(float startX, float startY, Flashlight flashlight) {
        this.x = startX;
        this.y = startY;
        this.flashlight = flashlight;  // Make sure this line is present
        importImg();
        this.soundManager = new SoundManager();
    }
    
    public void setPickupCompletionListener(PickupCompletionListener listener) {
        this.pickupCompletionListener = listener;
    }

    
    private void importImg() {
        try (var is = getClass().getResourceAsStream("/Player.png")) {
            BufferedImage img = ImageIO.read(is);
            animations = new BufferedImage[3][22]; // 3 rows, 22 frames max
            
            // Walking animation (row 0)
            for (int i = 0; i < 8; i++) {
                animations[0][i] = img.getSubimage(i * 150, 0 * 150, 150, 150);
            }
            
            // Idle animation (row 1)
            for (int i = 0; i < 8; i++) {
                animations[1][i] = img.getSubimage(i * 150, 1 * 150, 150, 150);
            }
            
            // Pickup animation (row 2)
            for (int i = 0; i < 22; i++) {
                animations[2][i] = img.getSubimage(i * 150, 2 * 150, 150, 150);
            }

            // Debug print total frames
            System.out.println("Total Frames Loaded: " + 
                (animations.length * animations[0].length));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update(Level level, InteractableObject interactableObject) {
    if (!isPickingUp) {
        applyGravity(level);
        updateAnimationTick();
    } else {
        updatePickupAnimation();
    }
    
}

    private void updatePickupAnimation() {
    pickupAniTick++;
    if (pickupAniTick >= pickupAniSpeed) {
        pickupAniTick = 0;
        aniIndex++;

        // When pickup animation completes 
        if (aniIndex >= pickupAniLength) {
            isPickingUp = false;
            aniIndex = 0;

            // Trigger the completion listener if set 
            if (pickupCompletionListener != null) {
                pickupCompletionListener.onPickupComplete();
                pickupCompletionListener = null; // Clear the listener 
                }
            }
        }
    }

    private void updateAnimationTick() {
        aniTick++;
        if (aniTick >= aniSpeed) {
            aniTick = 0;
            
            // Determine the correct number of frames based on the animation row
            int maxFrames;
            if (isPickingUp) {
                maxFrames = 22; // Pickup animation
            } else if (moving) {
                maxFrames = 8;  // Walking animation
            } else {
                maxFrames = 8;  // Idle animation
            }
            
            aniIndex = (aniIndex + 1) % maxFrames;

            // Optional: Debug print for animation tracking
            System.out.println("Animation Row: " + (isPickingUp ? 2 : (moving ? 0 : 1)) + 
                               ", Frame: " + aniIndex + 
                               ", Moving: " + moving);
        }
    }

    private void applyGravity(Level level) {
        if (!onGround) {
            yVelocity += gravity;
            y += yVelocity;
        }

        int platformY = level.getYPosition() - height + standOffset;
        if (y >= platformY) {
            y = platformY;
            onGround = true;
            yVelocity = 0;
        } else {
            onGround = false;
        }
    }

    public void draw(Graphics g, int xOffset, int yOffset) {
        int animationRow;
        if (isPickingUp) {
            animationRow = 2; // Pickup animation is the 3rd row (index 2)
        } else {
            animationRow = moving ? 0 : 1; // Walking is row 0, Idle is row 1
        }

        int drawX = (int) (x - xOffset); 
        int drawY = (int) (y - yOffset); 

        if (!facingRight) {
            g.drawImage(animations[animationRow][aniIndex], drawX + width, drawY, -width, height, null); 
        } else { 
            g.drawImage(animations[animationRow][aniIndex], drawX, drawY, width, height, null); 
        } 
    }

    public void move(int xChange, int yChange, float delta) {
        if (!isPickingUp) {
            x += xChange * baseSpeed * delta;
            y += yChange * baseSpeed * delta;

            // Explicitly set moving based on x change
            moving = xChange != 0;

            movingLeft = xChange < 0;
            movingRight = xChange > 0;

            if (xChange != 0) {
                facingRight = xChange > 0;
                soundManager.startWalkingSound();
            } else {
                soundManager.stopWalkingSound();
            }
        }
    }

    public void stop() {
        if (!isPickingUp) {
            moving = false;
            movingLeft = false;
            movingRight = false;
            soundManager.stopWalkingSound();
        }
    }

    public void jump() {
        if (onGround && !isPickingUp) {
            yVelocity = jumpStrength;
            onGround = false;
        }
    }

    // Getters and other methods remain the same
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public boolean isMovingLeft() {
        return movingLeft;
    }

    public boolean isMovingRight() {
        return movingRight;
    }

    // Method to start pickup animation
    public void startPickup() {
        if (!isPickingUp) {
            isPickingUp = true;
            aniIndex = 0;
            pickupAniTick = 0;
            moving = false;
            movingLeft = false;
            movingRight = false;
            soundManager.stopWalkingSound();
        }
    }

    // Method to clean up sound resources
    public void cleanup() {
        soundManager.cleanup();
    }
    
    public void tryPickup(List<?> interactableObjects) {
    if (isPickingUp) {
        return;
    }

    for (Object obj : interactableObjects) {
        if (obj instanceof Notes.Note) {
            Notes.Note note = (Notes.Note) obj;

            // Check if the note can be collected through the InventoryManager
            if (note.isInteractable(this) && 
                gamePanel.getNotes().getInventoryManager().collectNote(note)) {

                startPickup();

                setPickupCompletionListener(() -> {
                    note.interact(this);
                    if (gamePanel != null) {
                        gamePanel.displayRandomNote();
                    }

                    // Don't immediately change state
                    isPickingUp = false;
                    aniIndex = 0;
                    pickupAniTick = 0;
                });
                break;
            }
        } else if (obj instanceof InteractableObject) {
            InteractableObject interactableObject = (InteractableObject) obj;
            if (interactableObject.isInteractable(this)) {
                startPickup();
                interactableObject.interact(this);
                break;
                }
            }
        }
    }
    
    public Flashlight getFlashlight() {
        return flashlight;
    }
    
        public void setGamePanel(GamePanel gamePanel) {
    this.gamePanel = gamePanel;
    }
    
}