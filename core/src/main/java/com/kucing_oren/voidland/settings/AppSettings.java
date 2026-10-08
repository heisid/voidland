package com.kucing_oren.voidland.settings;

import com.kucing_oren.voidland.emulator.KeyboardDriver;

public class AppSettings {
    public boolean soundEnabled = true;
    public float uiScale = 1f;
    public int[] keyBindings = KeyboardDriver.defaultKeyBindings();

    public AppSettings copy() {
        AppSettings copy = new AppSettings();
        copy.soundEnabled = soundEnabled;
        copy.uiScale = uiScale;
        copy.keyBindings = keyBindings.clone();
        return copy;
    }

    public void validate() {
        if (uiScale != 0.8f && uiScale != 1f && uiScale != 1.2f) {
            throw new IllegalStateException("Unsupported UI scale in settings: " + uiScale);
        }
        if (keyBindings == null || keyBindings.length != 16) {
            throw new IllegalStateException("Keyboard mapping must contain exactly 16 keys.");
        }
        for (int i = 0; i < keyBindings.length; i++) {
            for (int j = i + 1; j < keyBindings.length; j++) {
                if (keyBindings[i] == keyBindings[j]) {
                    throw new IllegalStateException("Keyboard mapping contains duplicate keys.");
                }
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
