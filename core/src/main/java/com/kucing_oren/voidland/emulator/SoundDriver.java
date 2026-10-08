package com.kucing_oren.voidland.emulator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

public class SoundDriver {
    private final Music sine;
    private boolean isPlaying = false;

    public SoundDriver() {
        sine = Gdx.audio.newMusic(Gdx.files.internal("sound/sine-200hz.mp3"));
        sine.setLooping(true);
    }

    public void play() {
        if (!isPlaying) sine.play();
        isPlaying = true;
    }

    public void stop() {
        if (isPlaying) sine.stop();
        isPlaying = false;
    }
}
