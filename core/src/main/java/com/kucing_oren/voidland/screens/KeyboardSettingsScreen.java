package com.kucing_oren.voidland.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kucing_oren.voidland.VoidLand;
import com.kucing_oren.voidland.settings.AppSettings;

public class KeyboardSettingsScreen extends AbstractMenuScreen {
    private final boolean returnToMainScreen;
    private final AppSettings editedSettings;
    private final int[] originalKeyBindings;
    private final TextButton[] bindingButtons = new TextButton[16];
    private final Label status;

    public KeyboardSettingsScreen(VoidLand application, boolean returnToMainScreen, AppSettings settingsToEdit) {
        super(application, "Keyboard Mapping");
        this.returnToMainScreen = returnToMainScreen;
        this.editedSettings = settingsToEdit.copy();
        this.originalKeyBindings = editedSettings.keyBindings.clone();

        addMessage("Select a CHIP-8 key, then press a key on your keyboard.");
        status = addMessage("");

        Table grid = new Table();
        for (int key = 0; key < bindingButtons.length; key++) {
            final int chip8Key = key;
            TextButton button = new TextButton("", application.getSkin());
            button.pad(6f * uiScale);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    status.setText("Press a key for CHIP-8 " + Integer.toHexString(chip8Key).toUpperCase() + ".");
                    stage.setKeyboardFocus(button);
                }
            });
            button.addListener(new InputListener() {
                @Override
                public boolean keyDown(InputEvent event, int keycode) {
                    assignKey(chip8Key, keycode);
                    return true;
                }
            });
            bindingButtons[key] = button;
            grid.add(button).width(165f * uiScale).height(42f * uiScale).pad(3f * uiScale);
            if (key % 2 == 1) {
                grid.row();
            }
        }
        content.add(grid).pad(6f * uiScale).row();

        addButton("Done", this::returnToSettings);
        addButton("Cancel", this::cancel);
        updateBindingLabels();
    }

    private void assignKey(int chip8Key, int keycode) {
        for (int i = 0; i < editedSettings.keyBindings.length; i++) {
            if (i != chip8Key && editedSettings.keyBindings[i] == keycode) {
                status.setText("That key is already assigned to CHIP-8 "
                    + Integer.toHexString(i).toUpperCase() + ".");
                return;
            }
        }

        editedSettings.keyBindings[chip8Key] = keycode;
        status.setText("Updated CHIP-8 " + Integer.toHexString(chip8Key).toUpperCase() + ".");
        updateBindingLabels();
        stage.setKeyboardFocus(null);
    }

    private void updateBindingLabels() {
        for (int i = 0; i < bindingButtons.length; i++) {
            String keyName = Input.Keys.toString(editedSettings.keyBindings[i]);
            bindingButtons[i].setText("CHIP-8 " + Integer.toHexString(i).toUpperCase() + ": " + keyName);
        }
    }

    private void returnToSettings() {
        application.showSettingsScreen(returnToMainScreen, editedSettings);
    }

    private void cancel() {
        editedSettings.keyBindings = originalKeyBindings;
        returnToSettings();
    }
}
