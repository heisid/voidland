package com.kucing_oren.voidland.emulator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

public class SoundDriver {
    private Music sine;
    private boolean isPlaying;

    public void loadSound() {
        if (sine != null) return;
        Music music = Gdx.audio.newMusic(Gdx.files.internal("sounds/sine-200hz.mp3"));
        music.setLooping(true);
        sine = music;
    }

    public void play() {
        if (sine == null) {
            throw new IllegalStateException("Sound must be loaded before playback.");
        }
        if (!isPlaying) sine.play();
        isPlaying = true;
    }

    public void stop() {
        if (sine != null && isPlaying) sine.stop();
        isPlaying = false;
    }

    public void dispose() {
        if (sine != null) {
            stop();
            sine.dispose();
            sine = null;
        }
    }
}
