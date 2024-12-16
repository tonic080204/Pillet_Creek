package gamestate;

public class GameStateManager {
    private static GameState currentState = GameState.MAIN_MENU;
    private static boolean allNotesCollectedTriggered = false;
    
    public static GameState getCurrentState() {
        return currentState;
    }

    public static void setState(GameState state) {
        currentState = state;
    }

    public static boolean isState(GameState state) {
        return currentState == state;
    }
    
    public static void setAllNotesCollectedTriggered(boolean triggered) {
        allNotesCollectedTriggered = triggered;
    }

    public static boolean isAllNotesCollectedTriggered() {
        return allNotesCollectedTriggered;
    }
    
}
