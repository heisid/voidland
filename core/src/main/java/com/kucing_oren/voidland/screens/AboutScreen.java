package com.kucing_oren.voidland.screens;

import com.kucing_oren.voidland.VoidLand;

public class AboutScreen extends AbstractMenuScreen {
    public AboutScreen(VoidLand application) {
        super(application, "About");
        addMessage("Void Land Version 0.1");
        addGap();
        addMessage("https://github.com/heisid/voidland");
        addGap();
        addButton("Back", application::showInitialScreen);
    }
}
