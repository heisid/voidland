package com.kucing_oren.voidland.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kucing_oren.voidland.VoidLand;
import com.kucing_oren.voidland.emulator.Chip8Constants;
import com.kucing_oren.voidland.settings.AppSettings;

public class SettingsScreen extends AbstractMenuScreen {
    private static final int KEYS_PER_ROW = 4;

    private final boolean returnToMainScreen;
    private final AppSettings editedSettings;
    private final TextButton[] keyBindingButtons = new TextButton[Chip8Constants.REGISTER_COUNT];
    private final TextButton[] quirkButtons = new TextButton[6];
    private final Label keyBindingMessage;
    private int capturingKey = -1;

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

        addMessage("CHIP-8 compatibility quirks:");
        quirkButtons[0] = addButton("", () -> {
            editedSettings.resetVfOnLogic = !editedSettings.resetVfOnLogic;
            updateQuirkLabels();
        });
        quirkButtons[1] = addButton("", () -> {
            editedSettings.incrementIndexOnLoadStore = !editedSettings.incrementIndexOnLoadStore;
            updateQuirkLabels();
        });
        quirkButtons[2] = addButton("", () -> {
            editedSettings.displayWait = !editedSettings.displayWait;
            updateQuirkLabels();
        });
        quirkButtons[3] = addButton("", () -> {
            editedSettings.clipSprites = !editedSettings.clipSprites;
            updateQuirkLabels();
        });
        quirkButtons[4] = addButton("", () -> {
            editedSettings.shiftUsesVx = !editedSettings.shiftUsesVx;
            updateQuirkLabels();
        });
        quirkButtons[5] = addButton("", () -> {
            editedSettings.jumpUsesVx = !editedSettings.jumpUsesVx;
            updateQuirkLabels();
        });

        addMessage("CHIP-8 key bindings (select a key, then press a keyboard key):");
        Table keyBindingGrid = new Table();
        content.add(keyBindingGrid).pad(4f * uiScale).row();
        for (int key = 0; key < keyBindingButtons.length; key++) {
            final int chip8Key = key;
            TextButton button = new TextButton("", application.getSkin());
            button.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
                @Override
                public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                    beginKeyCapture(chip8Key);
                }
            });
            keyBindingButtons[key] = button;
            keyBindingGrid.add(button)
                .width(100f * uiScale)
                .height(44f * uiScale)
                .pad(3f * uiScale);
            if ((key + 1) % KEYS_PER_ROW == 0) {
                keyBindingGrid.row();
            }
        }
        keyBindingMessage = addMessage("");

        addGap();
        addButton("Save settings", () -> {
            application.saveSettings(editedSettings);
            returnToPreviousScreen();
        });
        addButton("Cancel", this::returnToPreviousScreen);
        updateLabels(soundButton, scaleButton);
        updateQuirkLabels();
        updateKeyBindingLabels();
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

    private void updateQuirkLabels() {
        quirkButtons[0].setText(toggleLabel("Reset VF on logic ops", editedSettings.resetVfOnLogic));
        quirkButtons[1].setText(toggleLabel("Fx55/Fx65 increment I", editedSettings.incrementIndexOnLoadStore));
        quirkButtons[2].setText(toggleLabel("Wait after drawing", editedSettings.displayWait));
        quirkButtons[3].setText(toggleLabel("Clip at screen edges", editedSettings.clipSprites));
        quirkButtons[4].setText(toggleLabel("Shift source: Vx", editedSettings.shiftUsesVx));
        quirkButtons[5].setText(toggleLabel("Jump offset: Vx", editedSettings.jumpUsesVx));
    }

    private String toggleLabel(String name, boolean enabled) {
        return name + ": " + (enabled ? "On" : "Off");
    }

    private void beginKeyCapture(int chip8Key) {
        capturingKey = chip8Key;
        keyBindingMessage.setText("Press a key for CHIP-8 " + Integer.toHexString(chip8Key).toUpperCase()
            + " (Escape cancels).");
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    keyBindingMessage.setText("Key binding unchanged.");
                } else {
                    assignKey(capturingKey, keycode);
                }
                capturingKey = -1;
                restoreInputProcessor();
                return true;
            }
        });
    }

    private void assignKey(int chip8Key, int keyCode) {
        for (int otherKey = 0; otherKey < editedSettings.chip8KeyBindings.length; otherKey++) {
            if (otherKey != chip8Key && editedSettings.chip8KeyBindings[otherKey] == keyCode) {
                editedSettings.chip8KeyBindings[otherKey] = editedSettings.chip8KeyBindings[chip8Key];
                break;
            }
        }
        editedSettings.chip8KeyBindings[chip8Key] = keyCode;
        updateKeyBindingLabels();
        keyBindingMessage.setText("Key binding updated.");
    }

    private void updateKeyBindingLabels() {
        for (int chip8Key = 0; chip8Key < keyBindingButtons.length; chip8Key++) {
            String keyName = Input.Keys.toString(editedSettings.chip8KeyBindings[chip8Key]);
            keyBindingButtons[chip8Key].setText(
                Integer.toHexString(chip8Key).toUpperCase() + ": " + keyName
            );
        }
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
