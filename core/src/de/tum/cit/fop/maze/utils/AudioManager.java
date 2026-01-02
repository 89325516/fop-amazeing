package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;
import java.util.HashMap;
import java.util.Map;

public class AudioManager implements Disposable {
    private static AudioManager instance;
    private Music backgroundMusic;
    private Map<String, Sound> soundEffects;
    private boolean musicEnabled = true;

    private AudioManager() {
        soundEffects = new HashMap<>();
    }

    public static AudioManager getInstance() {
        if (instance == null)
            instance = new AudioManager();
        return instance;
    }

    public void load() {
        // Load Music
        if (Gdx.files.internal("audio/music.mp3").exists()) {
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/music.mp3"));
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(0.3f);
        }

        // Load Sounds (WAV is safer than OGG sometimes, but OGG is standard)
        // Check both ext just in case
        loadSound("walk", "audio/walk.ogg"); // Or mp3/wav
        loadSound("hit", "audio/hit.ogg");
        loadSound("attack", "audio/attack.ogg");
        loadSound("kill", "audio/kill.ogg");
        loadSound("collect", "audio/collect.ogg");
        loadSound("victory", "audio/victory.ogg");
        loadSound("gameover", "audio/gameover.ogg");
    }

    private void loadSound(String name, String path) {
        if (Gdx.files.internal(path).exists()) {
            soundEffects.put(name, Gdx.audio.newSound(Gdx.files.internal(path)));
        } else {
            // Try .wav
            String wav = path.replace(".ogg", ".wav");
            if (Gdx.files.internal(wav).exists()) {
                soundEffects.put(name, Gdx.audio.newSound(Gdx.files.internal(wav)));
            }
            // Try .mp3
            String mp3 = path.replace(".ogg", ".mp3");
            if (Gdx.files.internal(mp3).exists()) {
                soundEffects.put(name, Gdx.audio.newSound(Gdx.files.internal(mp3)));
            }
        }
    }

    public void playMusic() {
        if (musicEnabled && backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.play();
        }
    }

    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (enabled)
            playMusic();
        else
            stopMusic();
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public boolean isMusicPlaying() {
        return backgroundMusic != null && backgroundMusic.isPlaying();
    }

    public void stopMusic() {
        if (backgroundMusic != null)
            backgroundMusic.stop();
    }

    public void playSound(String name) {
        Sound sound = soundEffects.get(name);
        if (sound != null) {
            sound.play(1.0f); // Volume 1.0
        }
    }

    // For adjusting sound volume logic in settings, we can start here
    public void setVolume(float v) {
        if (backgroundMusic != null)
            backgroundMusic.setVolume(v);
    }

    @Override
    public void dispose() {
        if (backgroundMusic != null)
            backgroundMusic.dispose();
        for (Sound s : soundEffects.values()) {
            s.dispose();
        }
        soundEffects.clear();
    }
}
