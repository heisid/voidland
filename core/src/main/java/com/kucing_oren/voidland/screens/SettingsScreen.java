package com.kucing_oren.voidland.screens;

import com.kucing_oren.voidland.VoidLand;
import com.kucing_oren.voidland.settings.AppSettings;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

public class SettingsScreen extends AbstractMenuScreen {
    private final boolean returnToMainScreen;
    private final AppSettings editedSettings;

    public SettingsScreen(VoidLand application, boolean returnToMainScreen) {
        super(application, "Settings");
        this.returnToMainScreen = returnToMainScreen;
        this.editedSettings = application.getSettings().copy();
        addMessage("Changes are applied when you save.");

        TextButton soundButton = addButton("", () -> {
            editedSettings.soundEnabled = !editedSettings.soundEnabled;
            updateLabels();
        });
        TextButton scaleButton = addButton("", () -> {
            editedSettings.cycleUiScale();
            updateLabels();
        });
        addGap();
        addButton("Save settings", () -> {
            application.saveSettings(editedSettings);
            returnToPreviousScreen();
        });
        addButton("Cancel", this::returnToPreviousScreen);
        updateLabels(soundButton, scaleButton);
    }

    private void updateLabels() {
        for (com.badlogic.gdx.scenes.scene2d.Actor actor : content.getChildren()) {
            if (actor instanceof TextButton) {
                TextButton button = (TextButton) actor;
                if (button.getText().toString().startsWith("Sound:")) {
                    button.setText(soundLabel());
                } else if (button.getText().toString().startsWith("UI scale:")) {
                    button.setText(scaleLabel());
                }
            }
        }
    }

    private void updateLabels(TextButton soundButton, TextButton scaleButton) {
        soundButton.setText(soundLabel());
        scaleButton.setText(scaleLabel());
    }

    private String soundLabel() {
        return "Sound: " + (editedSettings.soundEnabled ? "On" : "Off");
    }

    private String scaleLabel() {
        return "UI scale: " + Math.round(editedSettings.uiScale * 100f) + "%";
    }

    private void returnToPreviousScreen() {
        if (returnToMainScreen) {
            application.showMainScreen();
        } else {
            application.showInitialScreen();
        }
    }
}
