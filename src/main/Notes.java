package main;

import gamestate.GameState;
import gamestate.GameStateManager;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import static java.nio.file.Files.size;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import main.InventoryManager;

public class Notes {
    private List<Note> notes;
    private int levelWidth;
    private int yPosition;
    private int objectSize;
    private int maxNotes;
    private Random random;
    private InventoryManager inventoryManager;
    
    public BufferedImage getRandomNoteImage() {
    // Get uncollected notes 
    List<Note> uncollectedNotes = notes.stream() 
        .filter(note -> !inventoryManager.hasCollectedNote(note)) 
        .collect(Collectors.toList()); 

    if (uncollectedNotes.isEmpty()) {
        // All notes have been collected, trigger cutscene
        GameStateManager.setState(GameState.CUTSCENE);
        return null;
    }

    // Randomly select an uncollected note 
    int randomIndex = random.nextInt(uncollectedNotes.size()); 
    Note selectedNote = uncollectedNotes.get(randomIndex); 

    return selectedNote.getImage(); 
    }

    public Notes(int levelWidth, int yPosition) {
        this.levelWidth = levelWidth;
        this.yPosition = yPosition;
        this.objectSize = 30;
        this.maxNotes = 60;
        this.notes = new ArrayList<>();
        this.random = new Random();
        
        // Initialize InventoryManager 
this.inventoryManager = InventoryManager.getInstance();         
        generateNotes();
    
    }
    
    

    private void generateNotes() {
    notes.clear();
    maxNotes = 60; // Increase to 60 notes total

    // Create a list of note image paths to track used notes
    List<String> availableNotePaths = new ArrayList<>(Arrays.asList(
        "/notes/1.png", "/notes/2.png", "/notes/3.png", 
        "/notes/4.png", "/notes/5.png", "/notes/6.png", 
        "/notes/7.png", "/notes/8.png", "/notes/9.png", 
        "/notes/10.png", "/notes/11.png", "/notes/12.png", 
        "/notes/13.png"
    ));

    for (int i = 0; i < maxNotes; i++) {
        // Randomly choose left or right side 
        int side = random.nextBoolean() ? 1 : -1; 

        // Wider spread of notes across the level
        int distance = random.nextInt(levelWidth * 2); 

        // Calculate x position 
        int x = side * distance; 

        // Ensure y position is exactly at the ground level 
        int y = yPosition - objectSize; 

        // If no available note paths left, cycle back through them
        if (availableNotePaths.isEmpty()) {
            availableNotePaths = new ArrayList<>(Arrays.asList(
                "/notes/1.png", "/notes/2.png", "/notes/3.png", 
                "/notes/4.png", "/notes/5.png", "/notes/6.png", 
                "/notes/7.png", "/notes/8.png", "/notes/9.png", 
                "/notes/10.png", "/notes/11.png", "/notes/12.png", 
                "/notes/13.png"
            ));
        }

        // Randomly select a note path 
        int randomIndex = random.nextInt(availableNotePaths.size()); 
        String notePath = availableNotePaths.remove(randomIndex); 

        Note note = new Note(x, y, objectSize, notePath); 
        notes.add(note); 
        }
    }

    public void draw(Graphics g, int xOffset, int yOffset) {
        for (Note note : notes) {
            note.draw(g, xOffset, yOffset);
        }
    }

    public List<Note> getNotes() {
        return notes;
    }

    public static class Note {
    private int x, y, size;
    private Rectangle hitbox;
    private boolean isPickedUp = false;
    private boolean isVisible = true;
    private String interactionText = "Note found! ";
    private BufferedImage noteImage; // New field to store note image
    private int uniqueId;
    private String notePath; // Store the specific note path
    
        public Note(int x, int y, int size, String notePath) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.hitbox = new Rectangle(x, y, size, size);
        this.uniqueId = hashCode();
        this.notePath = notePath;
        loadNoteImage();
        }
        
        @Override
        public int hashCode() {
            // Ensure a unique hash for each note
            return Objects.hash(x, y, size, uniqueId);
        }
        
        private void loadNoteImage() {
        try {
            var is = getClass().getResourceAsStream(notePath);
            if (is != null) {
                noteImage = ImageIO.read(is);
                System.out.println("Loaded note image: " + notePath);
            } else {
                System.err.println("Note image not found: " + notePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
        
        // Optional method to get the note image
        public BufferedImage getImage() {
            return noteImage;
        }
        
        public void draw(Graphics g, int xOffset, int yOffset) {
            if (!isVisible) return;

            int drawX = x - xOffset;
            int drawY = y - yOffset;

            // Draw red square for note
            g.setColor(Color.RED);
            g.fillRect(drawX, drawY, size, size);
        }

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

        public void interact(Player player) {
            if (isInteractable(player)) {
                player.startPickup();

                player.setPickupCompletionListener(() -> {
                    // Placeholder for future note interaction logic
                    isVisible = false;
                    isPickedUp = true;
                    
                    // Optional: Add some debug or temporary interaction feedback
                    System.out.println(interactionText);
                });
            }
        }

        // Getters for potential future use
        public int getX() {
            return x;
        }

        public int getY() { 
           return y;
        }

        public boolean isVisible() {
            return isVisible;
        }
    }
    
    // Method to get the InventoryManager
    public InventoryManager getInventoryManager() {
        return inventoryManager;
        }
}

