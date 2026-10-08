package com.kucing_oren.voidland.screens;

import com.kucing_oren.voidland.VoidLand;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.kucing_oren.voidland.emulator.Chip8;

public class InitialScreen extends AbstractMenuScreen {
    private TextButton startEmulatorButton;

    public InitialScreen(VoidLand application, Chip8 chip8) {
        super(application, "Pathetic CHIP-8 Emulator");
        Label loadedProgram = addMessage(getLoadedProgramMessage(chip8));
        addButton("Load Program", () -> chip8.selectProgram(programName -> {
            loadedProgram.setText("Loaded ROM: " + programName);
            updateStartButton(startEmulatorButton, true);
        }));
        startEmulatorButton = addButton("Start Emulator", application::showMainScreen);
        updateStartButton(startEmulatorButton, chip8.hasLoadedProgram());
        addButton("Settings", () -> application.showSettingsScreen(false));
        Label message = addMessage("");
        addButton("Help", () -> message.setText("Help content is a placeholder."));
        addButton("About", () -> message.setText("About information is a placeholder."));
        addButton("Exit", Gdx.app::exit);
    }

    private String getLoadedProgramMessage(Chip8 chip8) {
        return chip8.hasLoadedProgram()
            ? "Loaded ROM: " + chip8.getLoadedProgramName()
            : "No ROM loaded.";
    }

    private void updateStartButton(TextButton button, boolean enabled) {
        button.setDisabled(!enabled);
        button.setColor(enabled ? Color.WHITE : Color.GRAY);
    }
}
