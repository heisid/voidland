package com.kucing_oren.voidland.screens;

import com.kucing_oren.voidland.VoidLand;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.kucing_oren.voidland.emulator.Chip8;

public class InitialScreen extends AbstractMenuScreen {
    public InitialScreen(VoidLand application, Chip8 chip8) {
        super(application, "Welcome");
        addMessage("Choose an option to get started.");
        addButton("Load Program", chip8::selectProgram);
        addButton("Open workspace", application::showMainScreen);
        addButton("Settings", () -> application.showSettingsScreen(false));
        Label message = addMessage("");
        addButton("Help", () -> message.setText("Help content is a placeholder."));
        addButton("About", () -> message.setText("About information is a placeholder."));
        addButton("Exit", Gdx.app::exit);
    }
}
