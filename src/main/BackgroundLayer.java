package main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BackgroundLayer {
    private int yPosition;
    private int groundHeight;
    private List<Tree> trees; // List to store all trees
    private Random random;
    private int treeSpacing = 200;
    private int treeSize = 650; // Slightly larger trees for the background
    private int treeVerticalOffset = -620;
    private float randomnessFactor = 0.5f;
    private float parallaxSpeed = 0.1f; // Speed factor for the background layer
    private int offsetX = 0; // Horizontal offset for the background layer
    private int offsetY = 0; // Vertical offset for the background layer
    private float hueAdjust = 0f; // Hue adjustment (0f - 1f)
    private float brightnessAdjust = 1f; // Brightness multiplier

    private int levelWidth = 50000; // Width of the level
    private boolean treesGenerated = false;

    public BackgroundLayer(int yPosition, int groundHeight) {
        this.yPosition = yPosition;
        this.groundHeight = groundHeight;
        this.trees = new ArrayList<>();
        this.random = new Random();
    }

    // Generate trees for the background layer
    public void generateTrees() {
        if (treesGenerated) {
            return; // Prevent re-generating trees
        }

        trees.clear();
        for (int x = -levelWidth; x <= levelWidth; x += treeSpacing) {
            int randomizedX = x + (int) (randomnessFactor * random.nextFloat() * treeSpacing);
            int randomizedY = yPosition + treeVerticalOffset + random.nextInt(10);
            boolean flipped = random.nextBoolean();

            trees.add(new Tree(randomizedX, randomizedY, treeSize, treeSize, flipped));
        }

        treesGenerated = true;
    }

    // Draw the parallax background
        public void draw(Graphics g, int xOffset, int yOffset) { 
        generateTrees(); 

        // Calculate parallax effect 
        int bgXOffset = (int) (xOffset * parallaxSpeed) + offsetX; 
        int bgYOffset = yOffset + offsetY; 

        // Apply hue and brightness adjustments 
        Graphics2D g2d = (Graphics2D) g.create(); 
        if (hueAdjust != 0f || brightnessAdjust != 1f) { 
            g2d.setComposite(AlphaComposite.SrcOver); 
            g2d.setColor(Color.getHSBColor(hueAdjust, 1.0f, brightnessAdjust)); 
        } 

        // Draw ground for the background 
        g2d.setColor(new Color(78, 111, 68).darker()); // Slightly darker ground 
        g2d.fillRect(-23, yPosition - bgYOffset, 1280000, groundHeight); 

        // Draw trees 
        for (Tree tree : trees) { 
            tree.draw(g2d, bgXOffset, bgYOffset); 
        } 

        g2d.dispose(); // Dispose graphics context 
    }

    // Customization methods
    public void setParallaxSpeed(float speed) {
        this.parallaxSpeed = speed;
    }

    public void setTreeSpacing(int spacing) {
        this.treeSpacing = spacing;
    }

    public void setTreeSize(int size) {
        this.treeSize = size;
    }

    public void setTreeVerticalOffset(int offset) {
        this.treeVerticalOffset = offset;
    }

    public void setRandomnessFactor(float factor) {
        this.randomnessFactor = factor;
    }

    public void setHueAdjust(float hue) {
        this.hueAdjust = hue;
    }

    public void setBrightnessAdjust(float brightness) {
        this.brightnessAdjust = brightness;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }
}
