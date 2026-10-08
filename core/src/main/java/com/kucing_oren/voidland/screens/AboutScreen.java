package com.kucing_oren.voidland.screens;

import com.badlogic.gdx.Gdx;
import com.kucing_oren.voidland.VoidLand;

public class AboutScreen extends AbstractMenuScreen {
    public AboutScreen(VoidLand application) {
        super(application, "About");
        addMessage("Void Land Version " + Gdx.files.internal("version.txt").readString().trim());
        addGap();
        addMessage("https://github.com/heisid/voidland");
        addGap();
        addButton("Back", application::showInitialScreen);
    }
}
