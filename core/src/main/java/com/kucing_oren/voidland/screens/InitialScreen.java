package com.kucing_oren.voidland.screens;

import com.kucing_oren.voidland.VoidLand;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class InitialScreen extends AbstractMenuScreen {
    public InitialScreen(VoidLand application) {
        super(application, "Welcome");
        addMessage("Choose an option to get started.");
        addButton("Open workspace", application::showMainScreen);
        addButton("Settings", () -> application.showSettingsScreen(false));
        Label message = addMessage("");
        addButton("Help", () -> message.setText("Help content is a placeholder."));
        addButton("About", () -> message.setText("About information is a placeholder."));
        addButton("Exit", Gdx.app::exit);
    }
}
