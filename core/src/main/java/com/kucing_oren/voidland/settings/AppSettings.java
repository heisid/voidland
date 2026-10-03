package com.kucing_oren.voidland.settings;

public class AppSettings {
    public boolean soundEnabled = true;
    public float uiScale = 1f;

    public AppSettings copy() {
        AppSettings copy = new AppSettings();
        copy.soundEnabled = soundEnabled;
        copy.uiScale = uiScale;
        return copy;
    }

    public void validate() {
        if (uiScale != 0.8f && uiScale != 1f && uiScale != 1.2f) {
            throw new IllegalStateException("Unsupported UI scale in settings: " + uiScale);
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
