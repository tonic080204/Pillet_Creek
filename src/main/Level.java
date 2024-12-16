package main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;

public class Level {
    private int yPosition;
    private int groundHeight;
    private List<Tree> trees;  // List to store all trees
    private Random random;
    private int treeSpacing = 200;  // Average spacing between trees
    private int treeSize = 600;     // Default size of trees
    private int treeVerticalOffset = -590; // Vertical offset for trees from the ground
    private float randomnessFactor = 0.5f; // Adjusts the randomness of tree placement
    private int levelWidth = 50000;  // Width of the level (can be adjusted)
    private List<Bush> bushes;
    private BufferedImage[] bushImages;
    private int bushSpacing = 150;  // Spacing between bushes
    private float bushRandomnessFactor = 0.5f;
    private int bushVerticalOffset = 0; // New field to control vertical offset of bushes
    private List<InteractableObject> interactableObjects;
    private int objectSpacing = 300;  // Default spacing between objects
    private float objectRandomnessFactor = 0.5f;  // Controls object placement randomness
    private int objectSize = 50;  // Default size of interactable objects
    private int maxObjects = 60;  // Maximum number of objects to generate
    private int verticalOffset = 0;
    
    // A flag to check if trees are already generated
    private boolean treesGenerated = false;

    public Level(int yPosition, int groundHeight) {
        this.yPosition = yPosition;
        this.groundHeight = groundHeight;
        this.trees = new ArrayList<>();
        this.bushes = new ArrayList<>();
        this.random = new Random();
        this.interactableObjects = new ArrayList<>();
        loadBushImages();
    }
    
    private void loadBushImages() {
        bushImages = new BufferedImage[4];
        String[] bushPaths = {
            "/bushes/bush1.png",
            "/bushes/bush2.png",
            "/bushes/bush3.png",
            "/bushes/bush4.png"
        };

        try {
            for (int i = 0; i < bushPaths.length; i++) {
                var is = getClass().getResourceAsStream(bushPaths[i]);
                if (is != null) {
                    bushImages[i] = ImageIO.read(is);
                } else {
                    System.err.println("Bush image not found: " + bushPaths[i]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void generateBushes() {
        if (!bushes.isEmpty()) return;  // Only generate once 

        int leftBoundary = -levelWidth; 
        int rightBoundary = levelWidth; 

        for (int x = leftBoundary; x <= rightBoundary; x += bushSpacing) { 
            // Add randomness to bush placement 
            int randomizedX = x + (int) (bushRandomnessFactor * random.nextFloat() * bushSpacing); 

            // Position bush at ground level with vertical offset
            int randomizedY = yPosition + bushVerticalOffset; 

            // Randomly select a bush image 
            BufferedImage bushImage = bushImages[random.nextInt(bushImages.length)]; 

            // Determine bush size 
            int bushSize = bushImage.getHeight(); 

            // Randomly flip the bush 
            boolean flipped = random.nextBoolean(); 

            bushes.add(new Bush(randomizedX, randomizedY, bushSize, bushImage, flipped)); 
        } 
    } 

    // Generate trees only once, and store their fixed positions
    public void generateTrees() {
        if (treesGenerated) {
            return; // Do nothing if trees are already generated
        }

        trees.clear(); // Clear existing trees if needed
        int leftBoundary = -levelWidth; // Starting position for trees
        int rightBoundary = levelWidth; // Ending position for trees

        // Generate trees across the level width
        for (int x = leftBoundary; x <= rightBoundary; x += treeSpacing) {
            // Add randomness to tree placement
            int randomizedX = x + (int) (randomnessFactor * random.nextFloat() * treeSpacing);
            int randomizedY = yPosition + treeVerticalOffset + random.nextInt(10); // Slight vertical variation
            boolean flipped = random.nextBoolean(); // Randomly flip the tree horizontally

            // Create tree and add to list
            trees.add(new Tree(randomizedX, randomizedY, treeSize, treeSize, flipped));
        }

        treesGenerated = true; // Mark trees as generated
    }

    // Draw the ground and trees within the visible area
    public void draw(Graphics g, int xOffset, int yOffset) {
        generateTrees();
        generateBushes();  // Generate bushes when drawing

        // Draw ground 
        g.setColor(new Color(78, 111, 68)); 
        g.fillRect(0 - 23, yPosition - yOffset, 1280000, groundHeight); 

        // Draw trees
        for (Tree tree : trees) { 
            tree.draw(g, xOffset, yOffset); 
        }

        // Draw bushes
        for (Bush bush : bushes) {
            bush.draw(g, xOffset, yOffset);
        }
    }
    
    private class Bush { 
        private int x, y, size; 
        private BufferedImage image; 
        private boolean flipped; 
        private float scaleFactor = 1.0f; // New field for scaling

        public Bush(int x, int y, int size, BufferedImage image, boolean flipped) { 
            this.x = x; 
            // Adjust y to place the bottom of the bush at the ground level 
            this.y = y - size; 
            this.size = size; 
            this.image = image; 
            this.flipped = flipped; 
        } 

        public void draw(Graphics g, int xOffset, int yOffset) {
        int drawX = x - xOffset;
        int drawY = y - yOffset;

        // Calculate scaled width and height while maintaining aspect ratio
        int scaledWidth = (int)(size * scaleFactor);
        int scaledHeight = (int)(size * scaleFactor);

        if (flipped) {
            g.drawImage(image, drawX + scaledWidth, drawY, -scaledWidth, scaledHeight, null);
        } else {
            g.drawImage(image, drawX, drawY, scaledWidth, scaledHeight, null);
        }
        
        for (InteractableObject obj : getInteractableObjects()) {
            obj.draw(g, xOffset, yOffset);
        }
    }

    public void setScaleFactor(float factor) {
            this.scaleFactor = factor;
        }
    // Getter and setter for size 
        public void setSize(int newSize) { 
            this.size = newSize; 
        } 

        public int getSize() { 
            return size; 
        } 
    }
    
    // Method to set a fixed size for all bushes
    public void setBushSize(int size) {
    for (Bush bush : bushes) {
        bush.setSize(size);
        }
    }

    // Method to set bush size as a factor of original image size
    public void setBushSizeFactor(float factor) {
        for (Bush bush : bushes) {
            bush.setScaleFactor(factor);
        }
    }
    
    public void setBushVerticalOffset(int offset) {
        this.bushVerticalOffset = offset;
        // Regenerate bushes with new offset
        bushes.clear();
        generateBushes();
    }
    
    public void setBushSpacing(int spacing) {
        this.bushSpacing = spacing;
    }

    public void setBushRandomnessFactor(float factor) {
        this.bushRandomnessFactor = factor;
    }

    // Setters for tree customization
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

    public int getYPosition() {
        return yPosition;
    }
    
    public void generateInteractableObjects() {
    if (!interactableObjects.isEmpty()) return;  // Only generate once 

    int leftBoundary = -levelWidth;
    int rightBoundary = levelWidth;

    Random random = new Random();

    // Limit the number of objects to maxObjects
    int objectCount = random.nextInt(maxObjects) + 1;

    for (int i = 0; i < objectCount; i++) {
        // Add randomness to object placement
        int randomizedX = leftBoundary + random.nextInt(rightBoundary * 2);

        // Use the vertical offset with optional slight randomness 
        int randomizedY = yPosition + verticalOffset + random.nextInt(20);

        // Create the interactable object with the randomly generated position
        InteractableObject obj = new InteractableObject(
            randomizedX, 
            randomizedY, 
            objectSize, 
            // You'll need to pass the flashlight reference here, 
            // which might require modifying the constructor or method signature
            null  // Placeholder, replace with actual flashlight reference
        );

        interactableObjects.add(obj);
    }
}
    
    // Getter for interactable objects
    public List<InteractableObject> getInteractableObjects() {
        generateInteractableObjects();  // Ensure objects are generated
        return interactableObjects;
    }

    // Setter methods to customize object generation
    public void setObjectSpacing(int spacing) {
        this.objectSpacing = spacing;
    }

    public void setObjectRandomnessFactor(float factor) {
        // Ensure factor is between 0 and 1
        this.objectRandomnessFactor = Math.max(0, Math.min(1, factor));
    }

    public void setMaxObjects(int max) {
        this.maxObjects = max;
    }

    public void setObjectSize(int size) {
        this.objectSize = size;
    }
    
    public void setInteractableObjectVerticalOffset(int offset) {
    this.verticalOffset = offset;

    // Regenerate objects with new offset 
    interactableObjects.clear(); 
    generateInteractableObjects(); 
    }
    
}
