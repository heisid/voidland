package com.kucing_oren.voidland.settings;

import com.badlogic.gdx.Input;
import com.kucing_oren.voidland.emulator.Chip8Constants;
import com.kucing_oren.voidland.emulator.KeyboardDriver;

import java.util.HashSet;
import java.util.Set;

public class AppSettings {
    public boolean soundEnabled = true;
    public float uiScale = 1f;
    public int[] chip8KeyBindings = KeyboardDriver.defaultKeyBindings();

    public AppSettings copy() {
        AppSettings copy = new AppSettings();
        copy.soundEnabled = soundEnabled;
        copy.uiScale = uiScale;
        copy.chip8KeyBindings = chip8KeyBindings.clone();
        return copy;
    }

    public void validate() {
        if (uiScale != 0.8f && uiScale != 1f && uiScale != 1.2f) {
            throw new IllegalStateException("Unsupported UI scale in settings: " + uiScale);
        }
        if (chip8KeyBindings == null || chip8KeyBindings.length != Chip8Constants.REGISTER_COUNT) {
            throw new IllegalStateException("Settings must define a key binding for each CHIP-8 key.");
        }
        Set<Integer> assignedKeys = new HashSet<>();
        for (int keyCode : chip8KeyBindings) {
            if (keyCode <= Input.Keys.UNKNOWN || !assignedKeys.add(keyCode)) {
                throw new IllegalStateException("CHIP-8 key bindings must be valid and unique.");
            }
        }
    }

    public void cycleUiScale() {
        if (uiScale == 0.8f) {
            uiScale = 1f;
        } else if (uiScale == 1f) {
            uiScale = 1.2f;
        } else {
            uiScale = 0.8f;
        }
    }
}
