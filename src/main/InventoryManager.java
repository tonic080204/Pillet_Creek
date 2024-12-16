package main;

import gamestate.GameState;
import gamestate.GameStateManager;
import java.util.HashSet;
import java.util.Set;
import main.Notes;

public class InventoryManager {
    // Singleton instance
    private static InventoryManager instance;

    // Collections to track inventory items
    private Set<Integer> collectedNotes;
    private Set<String> collectedItems;
    private long noteCollectionTimestamp = 0;
    private final long CUTSCENE_DELAY = 10000; // 10 seconds in milliseconds
    private boolean cutsceneTimerStarted = false;

    // Maximum number of notes
    public static final int MAX_NOTES = 13;

    // Private constructor for singleton pattern
    private InventoryManager(int maxNotes) {
        collectedNotes = new HashSet<>();
        collectedItems = new HashSet<>();
    }

    // Singleton getInstance method
    public static InventoryManager getInstance() {
        if (instance == null) {
            instance = new InventoryManager(MAX_NOTES);
        }
        return instance;
    }

    // Note collection methods
    public boolean collectNote(Notes.Note note) {
        if (collectedNotes.contains(note.hashCode()) || collectedNotes.size() >= MAX_NOTES) {
            return false;
        }

        collectedNotes.add(note.hashCode());
        System.out.println("Notes Collected: " + collectedNotes.size() + "/" + MAX_NOTES);

        // Check if all notes have been collected
        if (collectedNotes.size() == MAX_NOTES) {
            // Start the timer for cutscene
            noteCollectionTimestamp = System.currentTimeMillis();
            cutsceneTimerStarted = true;
        }

        return true;
    }


    // Method to check if a specific note has been collected
    public boolean hasCollectedNote(Notes.Note note) {
        return collectedNotes.contains(note.hashCode());
    }

    // Get total number of collected notes
    public int getCollectedNotesCount() {
        return collectedNotes.size();
    }

    // Item collection methods
    public boolean collectItem(String itemName) {
        if (collectedItems.contains(itemName)) {
            return false;
        }
        
        collectedItems.add(itemName);
        System.out.println("Item Collected: " + itemName);
        return true;
    }

    // Method to check if a specific item has been collected
    public boolean hasCollectedItem(String itemName) {
        return collectedItems.contains(itemName);
    }

    // Get total number of collected items
    public int getCollectedItemsCount() {
        return collectedItems.size();
    }

    // Method to trigger scene when all notes are collected
    private void triggerAllNotesCollectedScene() {
    System.out.println("ALL 13 NOTES COLLECTED! CUTSCENE TRIGGERED! ");
    // Instead of immediately changing the state, set a flag
    GameStateManager.setAllNotesCollectedTriggered(true);
    }

    // Method to reset inventory (useful for new game or debugging)
    public void resetInventory() {
        collectedNotes.clear();
        collectedItems.clear();
    }

    // Debug method to print current inventory
    public void printInventory() {
        System.out.println("--- Current Inventory ---");
        System.out.println("Notes Collected: " + collectedNotes.size() + "/" + MAX_NOTES);
        System.out.println("Items Collected: " + collectedItems);
    }
    
    // Method to check if all notes have been collected
    public boolean areAllNotesCollected() {
        return collectedNotes.size() == MAX_NOTES;
    }
    
    // New method to check if cutscene should be triggered
    public boolean shouldTriggerCutscene() {
        if (cutsceneTimerStarted && 
            System.currentTimeMillis() - noteCollectionTimestamp >= CUTSCENE_DELAY) {
            return true;
        }
        return false;
    }

    // Reset method to clear the timer
    public void resetCutsceneTimer() {
        cutsceneTimerStarted = false;
        noteCollectionTimestamp = 0;
    }

}