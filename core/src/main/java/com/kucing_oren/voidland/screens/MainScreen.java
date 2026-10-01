package com.kucing_oren.voidland.screens;

import com.kucing_oren.voidland.VoidLand;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class MainScreen extends AbstractMenuScreen {
    public MainScreen(VoidLand application) {
        super(application, "Workspace");
        addMessage("This is the main work area. Replace this content with your project-specific interface.");
        Label status = addMessage("Ready.");

        addButton("Pause / Resume", () -> {
            String current = status.getText().toString();
            status.setText(current.equals("Paused.") ? "Ready." : "Paused.");
        });
        addButton("Create new item", () -> status.setText("New item action is a placeholder."));
        addButton("Save snapshot", () -> status.setText("Snapshot action is a placeholder."));
        addGap();
        addButton("Settings", () -> application.showSettingsScreen(true));
        addButton("Return to start", application::showInitialScreen);
    }
}
