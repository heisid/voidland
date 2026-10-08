package com.kucing_oren.voidland;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.kucing_oren.voidland.emulator.Chip8;
import com.kucing_oren.voidland.screens.EmulatorScreen;
import com.kucing_oren.voidland.screens.InitialScreen;
import com.kucing_oren.voidland.screens.SettingsScreen;
import com.kucing_oren.voidland.settings.AppSettings;
import com.kucing_oren.voidland.settings.SettingsRepository;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;

public class VoidLand extends ApplicationAdapter {
    private AppSettings settings;
    private SettingsRepository settingsRepository;
    private Skin skin;
    private BitmapFont font;
    private Texture whiteTexture;
    private Screen currentScreen;

    private Chip8 chip8;

    public VoidLand(NativeFileChooser fileChooser) {
        chip8 = new Chip8(fileChooser);
    }

    @Override
    public void create() {
        settingsRepository = new SettingsRepository();
        settings = settingsRepository.load();
        Pixmap pixel = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixel.setColor(Color.WHITE);
        pixel.fill();
        whiteTexture = new Texture(pixel);
        pixel.dispose();
        createSkin();
        chip8.loadSound();
        applyUiScale();
        showInitialScreen();
    }

    public void showInitialScreen() {
        showScreen(new InitialScreen(this, chip8));
    }

    public void showMainScreen() {
        showScreen(new EmulatorScreen(this, chip8));
    }

    public void showSettingsScreen(boolean returnToMainScreen) {
        showScreen(new SettingsScreen(this, returnToMainScreen));
    }

    public void saveSettings(AppSettings updatedSettings) {
        settingsRepository.save(updatedSettings);
        settings = updatedSettings;
        applyUiScale();
    }

    public AppSettings getSettings() {
        return settings;
    }

    public Skin getSkin() {
        return skin;
    }

    private void showScreen(Screen screen) {
        if (currentScreen != null) {
            currentScreen.hide();
            currentScreen.dispose();
        }
        currentScreen = screen;
        currentScreen.show();
        currentScreen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void createSkin() {
        skin = new Skin();
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
            Gdx.files.internal("fonts/NotoSans-Regular.ttf"));
        try {
            FreeTypeFontGenerator.FreeTypeFontParameter parameters =
                new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameters.size = 20;
            parameters.minFilter = Texture.TextureFilter.Linear;
            parameters.magFilter = Texture.TextureFilter.Linear;
            font = generator.generateFont(parameters);
        } finally {
            generator.dispose();
        }
        skin.add("default-font", font);

        skin.add("white", whiteTexture);
        skin.add("white", new TextureRegionDrawable(new TextureRegion(whiteTexture)));

        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        skin.add("default", labelStyle);

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.up = skin.newDrawable("white", new Color(0.16f, 0.2f, 0.27f, 1f));
        buttonStyle.down = skin.newDrawable("white", new Color(0.25f, 0.36f, 0.48f, 1f));
        buttonStyle.over = skin.newDrawable("white", new Color(0.22f, 0.29f, 0.38f, 1f));
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        skin.add("default", buttonStyle);
    }

    private void applyUiScale() {
        font.getData().setScale(settings.uiScale);
    }

    @Override
    public void render() {
        currentScreen.render(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void resize(int width, int height) {
        if (currentScreen != null) {
            currentScreen.resize(width, height);
        }
    }

    @Override
    public void dispose() {
        if (currentScreen != null) {
            currentScreen.hide();
            currentScreen.dispose();
        }
        chip8.dispose();
        skin.dispose();
    }
}
