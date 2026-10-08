package com.kucing_oren.voidland.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

public class SettingsRepository {
    private static final String SETTINGS_FILE = "settings.json";
    private final Json json = new Json();

    public SettingsRepository() {
        json.setOutputType(JsonWriter.OutputType.json);
    }

    public AppSettings load() {
        FileHandle file = Gdx.files.local(SETTINGS_FILE);
        if (!file.exists()) {
            return new AppSettings();
        }

        AppSettings settings = json.fromJson(AppSettings.class, file);
        if (settings == null) {
            throw new IllegalStateException("Settings file contains no settings: " + file.path());
        }
        settings.validate();
        return settings;
    }

    public void save(AppSettings settings) {
        settings.validate();
        Gdx.files.local(SETTINGS_FILE).writeString(json.prettyPrint(settings), false);
    }
}
