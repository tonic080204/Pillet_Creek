package sounds;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SoundManager {
    private List<Clip> walkingSounds;
    private Clip backgroundMusicClip;
    private Random random;
    private Clip currentWalkingClip;
    private boolean isWalkingSoundPlaying = false;
    private float walkingSoundVolume = 0.099f;
    private float backgroundMusicVolume = 0.500f;
    private Clip flashlightOnClip;
    private Clip flashlightOffClip;
    private float flashlightSoundVolume = 0.5f;

    public SoundManager() {
        walkingSounds = new ArrayList<>();
        random = new Random();
        loadWalkingSounds();
        loadBackgroundMusic();
        loadFlashlightSounds();
    }

    private void loadFlashlightSounds() {
    try {
        // Load flashlight on sound 
        URL flashlightOnURL = getClass().getResource("/sounds/flashlight_on.wav"); 
        if (flashlightOnURL != null) { 
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(flashlightOnURL); 
            flashlightOnClip = AudioSystem.getClip(); 
            flashlightOnClip.open(audioInputStream); 
            applyFlashlightSoundVolume(flashlightOnClip); 
        } 

        // Load flashlight out sound (replacing flashlight_off.wav)
        URL flashlightOutURL = getClass().getResource("/sounds/flashlight_off.wav"); 
        if (flashlightOutURL != null) { 
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(flashlightOutURL); 
            flashlightOffClip = AudioSystem.getClip(); 
            flashlightOffClip.open(audioInputStream); 
            applyFlashlightSoundVolume(flashlightOffClip); 
        } 
    } catch (Exception e) { 
        e.printStackTrace(); 
        } 
    }
    
    public void playFlashlightOnSound() {
        if (flashlightOnClip != null) {
            flashlightOnClip.setFramePosition(0);
            flashlightOnClip.start();
        }
    }
    
    public void playFlashlightOffSound() {
        if (flashlightOffClip != null) {
            flashlightOffClip.setFramePosition(0);
            flashlightOffClip.start();
        }
    }
    
    public void setFlashlightSoundVolume(float volume) {
        this.flashlightSoundVolume = Math.max(0.0f, Math.min(1.0f, volume));
        applyFlashlightSoundVolume(flashlightOnClip);
        applyFlashlightSoundVolume(flashlightOffClip);
    }

    private void applyFlashlightSoundVolume(Clip clip) {
        if (clip != null) {
            try {
                FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float volumeDB = (float) (20.0 * Math.log10(flashlightSoundVolume));
                volumeControl.setValue(volumeDB);
            } catch (IllegalArgumentException e) {
                System.out.println("Volume control not supported for flashlight sounds");
            }
        }
    }
    
    private void loadWalkingSounds() {
        String[] soundFiles = { 
            "/sounds/Leif_branchfoley_1.wav", 
            "/sounds/Leif_branchfoley_2.wav", 
            "/sounds/Leif_branchfoley_6.wav", 
            "/sounds/Leif_branchfoley_7.wav", 
            "/sounds/Leif_branchfoley_8.wav", 
            "/sounds/Leif_branchfoley_9.wav", 
        };

        for (String soundFile : soundFiles) {
            try {
                URL soundURL = getClass().getResource(soundFile);
                if (soundURL != null) {
                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundURL);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioInputStream);
                    walkingSounds.add(clip);
                } else {
                    System.err.println("Could not load sound file: " + soundFile);
                }
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadBackgroundMusic() {
    try {
        URL backgroundMusicURL = getClass().getResource("/sounds/background.wav"); 
        if (backgroundMusicURL != null) { 
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(backgroundMusicURL); 
            backgroundMusicClip = AudioSystem.getClip(); 
            backgroundMusicClip.open(audioInputStream); 


            // Do NOT start looping automatically
            backgroundMusicClip.stop();


            // Apply volume control 
            applyBackgroundMusicVolume(); 
        }
    } catch (Exception e) {
        e.printStackTrace();
        }
    }

    // Background Music Methods
    public void startBackgroundMusic() {

    if (backgroundMusicClip != null && !backgroundMusicClip.isRunning()) { 
        backgroundMusicClip.setFramePosition(0); 
        backgroundMusicClip.start(); 
        backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY); 
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusicClip != null && backgroundMusicClip.isRunning()) {
            backgroundMusicClip.stop();
        }
    }

    public void setBackgroundMusicVolume(float volume) {
        this.backgroundMusicVolume = Math.max(0.0f, Math.min(1.0f, volume));
        applyBackgroundMusicVolume();
    }

    private void applyBackgroundMusicVolume() {
        if (backgroundMusicClip != null) {
            try {
                FloatControl volumeControl = (FloatControl) backgroundMusicClip.getControl(FloatControl.Type.MASTER_GAIN);
                float volumeDB = (float) (20.0 * Math.log10(backgroundMusicVolume));
                volumeControl.setValue(volumeDB);
            } catch (IllegalArgumentException e) {
                System.out.println("Volume control not supported for background music");
            }
        }
    }

    // Walking Sound Methods
    public void startWalkingSound() {
        if (!isWalkingSoundPlaying && !walkingSounds.isEmpty()) {
            // Select a random walking sound
            currentWalkingClip = walkingSounds.get(random.nextInt(walkingSounds.size()));

            // Apply volume control
            applyWalkingSoundVolume(currentWalkingClip);

            // Reset the clip to the beginning
            currentWalkingClip.setFramePosition(0);

            // Add a listener to reset isWalkingSoundPlaying when the sound finishes
            currentWalkingClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    isWalkingSoundPlaying = false;
                }
            });

            // Play the sound
            currentWalkingClip.start();
            isWalkingSoundPlaying = true;
        }
    }

    public void stopWalkingSound() {
        if (currentWalkingClip != null && isWalkingSoundPlaying) {
            currentWalkingClip.stop();
            isWalkingSoundPlaying = false;
        }
    }

    public void setWalkingSoundVolume(float volume) {
        this.walkingSoundVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }

    private void applyWalkingSoundVolume(Clip clip) {
        try {
            FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float volumeDB = (float) (20.0 * Math.log10(walkingSoundVolume));
            volumeControl.setValue(volumeDB);
        } catch (IllegalArgumentException e) {
            System.out.println("Volume control not supported for walking sounds");
        }
    }
    
    public void playFlashSound() {
    try {
        URL flashSoundURL = getClass().getResource("/sounds/flash.wav");
        if (flashSoundURL != null) {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(flashSoundURL);
            Clip flashClip = AudioSystem.getClip();
            flashClip.open(audioInputStream);
            flashClip.start();
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    // Cleanup method
    public void cleanup() {
        // Existing cleanup...
        if (flashlightOnClip != null) {
            flashlightOnClip.stop();
            flashlightOnClip.close();
        }
        if (flashlightOffClip != null) {
            flashlightOffClip.stop();
            flashlightOffClip.close();
        }
    }
    
    public void playSoundFromPath(String path) throws Exception {
    URL soundURL = getClass().getResource(path);
    if (soundURL != null) {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundURL);
        Clip clip = AudioSystem.getClip();
        clip.open(audioInputStream);
        clip.start();
    }
}
}