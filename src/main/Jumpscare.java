package main;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import sounds.SoundManager;
import gamestate.GameState;
import gamestate.GameStateManager;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Jumpscare {
    private GamePanel gamePanel;
    private Flashlight flashlight;
    private SoundManager soundManager;
    private boolean jumpscareTriggered = false;
    private long batteryDepletedTime = 0;
    private static final long JUMPSCARE_DELAY = 5000;     // 5 seconds before jumpscare
    private static final long JUMPSCARE_DURATION = 5000;  // 5 seconds jumpscare duration
    private BufferedImage jumpscareImage;
    private JFrame jumpscareFrame;

    public Jumpscare(GamePanel gamePanel, Flashlight flashlight) {
        this.gamePanel = gamePanel;
        this.flashlight = flashlight;
        this.soundManager = new SoundManager();
    }

    public void update() {
        // Check if battery is depleted
        if (flashlight.getBatteryLife() <= 0 && !jumpscareTriggered) {
            if (batteryDepletedTime == 0) {
                batteryDepletedTime = System.currentTimeMillis();
            }

            // Check if 5 seconds have passed since battery depletion
            if (System.currentTimeMillis() - batteryDepletedTime >= JUMPSCARE_DELAY) {
                triggerJumpscare();
            }
        }
    }

    private void triggerJumpscare() {
    jumpscareTriggered = true;

    // Load random jumpscare image 
    try { 
        String[] jumpscareImages = { 
            "/jumpscare3.png" 
        }; 
        Random random = new Random(); 
        String selectedImage = jumpscareImages[random.nextInt(jumpscareImages.length)]; 

        // Add null check and error handling
        var is = getClass().getResourceAsStream(selectedImage);
        if (is == null) {
            System.err.println("Jumpscare image not found: " + selectedImage);
            // Fallback to a default image or skip jumpscare
            return;
        }

        jumpscareImage = ImageIO.read(is);
        is.close(); // Close the input stream
    } catch (Exception e) { 
        System.err.println("Error loading jumpscare image: " + e.getMessage());
        e.printStackTrace();
        return; 
    } 

        // Create full-screen jumpscare frame
        jumpscareFrame = new JFrame();
        jumpscareFrame.setUndecorated(true);
        jumpscareFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        JLabel imageLabel = new JLabel(new ImageIcon(jumpscareImage)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.SrcOver.derive(1.0f)); // Full opacity
                super.paintComponent(g2d);
                g2d.dispose();
            }
        };
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setVerticalAlignment(JLabel.CENTER);

        jumpscareFrame.add(imageLabel);
        jumpscareFrame.setVisible(true);

        // Play jumpscare sound
        try {
            soundManager.playSoundFromPath("/jumpscare.wav");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Schedule jumpscare end
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    // Close jumpscare frame
                    if (jumpscareFrame != null) {
                        jumpscareFrame.dispose();
                    }

                    // Reset game state
                    GameStateManager.setState(GameState.MAIN_MENU);

                    // Reset flashlight
                    flashlight.addBatteryCharge(100f);

                    // Reset jumpscare trigger
                    jumpscareTriggered = false;
                    batteryDepletedTime = 0;
                });
            }
        }, JUMPSCARE_DURATION);
    }

    public void cleanup() {
        if (soundManager != null) {
            soundManager.cleanup();
        }
    }
}