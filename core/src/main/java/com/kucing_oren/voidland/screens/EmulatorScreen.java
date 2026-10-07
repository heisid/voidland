package com.kucing_oren.voidland.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kucing_oren.voidland.VoidLand;
import com.kucing_oren.voidland.emulator.Chip8;
import com.kucing_oren.voidland.emulator.DisplayDriver;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmulatorScreen extends ScreenAdapter {
    private static final int BEEP_SAMPLE_RATE = 22050;
    private static final int BEEP_DURATION_MILLIS = 120;
    private final ShapeRenderer shapeRenderer;
    private final float uiScale;
    private final Stage stage;
    private final ExecutorService audioExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "emulator-audio");
        thread.setDaemon(true);
        return thread;
    });
    private final short[] beepSamples = createBeepSamples();

    private final TextButton pauseButton;
    private boolean paused;

    private static final int GRID_ROWS = 32;
    private static final int GRID_COLUMNS = 64;
    private static final float CONTROL_GAP = 16f;
    private float pixelSize;
    private float gridX;
    private float gridY;

    private final Chip8 chip8;

    public EmulatorScreen(VoidLand application, Chip8 chip8) {
        this.uiScale = application.getSettings().uiScale;
        this.stage = new Stage(new ScreenViewport());
        this.shapeRenderer = new ShapeRenderer();

        Table root = new Table();
        root.setFillParent(true);
        root.pad(24f * uiScale);
        stage.addActor(root);

        root.add().expand().fill().row();
        root.add().height(CONTROL_GAP * uiScale).row();

        Table controls = new Table();
        root.add(controls).fillX().height(50f * uiScale);

        pauseButton = new TextButton("Pause", application.getSkin());
        pauseButton.pad(10f * uiScale);
        pauseButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                paused = !paused;
                pauseButton.setText(paused ? "Resume" : "Pause");
            }
        });
        controls.add(pauseButton).expandX().fillX().height(50f * uiScale).padRight(8f * uiScale);
        TextButton exitButton = new TextButton("Exit to main menu", application.getSkin());
        exitButton.pad(10f * uiScale);
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                application.showInitialScreen();
            }
        });
        controls.add(exitButton).expandX().fillX().height(50f * uiScale).padLeft(8f * uiScale);
        TextButton beepButton = new TextButton("Beep", application.getSkin());
        beepButton.pad(10f * uiScale);
        beepButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                playBeep();
            }
        });
        controls.add(beepButton).expandX().fillX().height(50f * uiScale).padLeft(8f * uiScale);

        this.chip8 = chip8;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        if (!paused) {
            chip8.cpuTick();
        }
        boolean[][] displayBuffer = chip8.getDisplayBuffer();
        stage.act(delta);

        if (pixelSize > 0f) {
            shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.WHITE);
            for (int i = 0; i < displayBuffer.length; i++) {
                for (int j = 0; j < displayBuffer[i].length; j++) {
                    if (displayBuffer[i][j]) {
                        float x = gridX + i * pixelSize;
                        float y = gridY + (GRID_ROWS - 1 - j) * pixelSize;
                        shapeRenderer.rect(x, y, pixelSize, pixelSize);
                    }
                }
            }
            shapeRenderer.end();
        }
        stage.draw();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        updateGridLayout(width, height);
    }

    @Override
    public void dispose() {
        stage.dispose();
        shapeRenderer.dispose();
        audioExecutor.shutdown();
    }

    private void playBeep() {
        audioExecutor.execute(() -> {
            AudioDevice audioDevice = Gdx.audio.newAudioDevice(BEEP_SAMPLE_RATE, true);
            try {
                audioDevice.writeSamples(beepSamples, 0, beepSamples.length);
            } finally {
                audioDevice.dispose();
            }
        });
    }

    private static short[] createBeepSamples() {
        int sampleCount = BEEP_SAMPLE_RATE * BEEP_DURATION_MILLIS / 1000;
        short[] samples = new short[sampleCount];
        for (int i = 0; i < sampleCount; i++) {
            double progress = (double) i / sampleCount;
            double envelope = Math.min(1.0, Math.min(progress * 20.0, (1.0 - progress) * 20.0));
            double tone = Math.sin(2.0 * Math.PI * 660.0 * i / BEEP_SAMPLE_RATE);
            samples[i] = (short) (tone * envelope * Short.MAX_VALUE * 0.3);
        }
        return samples;
    }

    private void updateGridLayout(int width, int height) {
        float padding = 24f * uiScale;
        float controlHeight = 50f * uiScale;
        float controlGap = CONTROL_GAP * uiScale;
        float availableWidth = Math.max(0f, width - 2f * padding);
        float availableHeight = Math.max(0f, height - 2f * padding - controlHeight - controlGap);

        pixelSize = Math.min(availableWidth / GRID_COLUMNS, availableHeight / GRID_ROWS);
        float gridWidth = GRID_COLUMNS * pixelSize;
        float gridHeight = GRID_ROWS * pixelSize;
        gridX = (width - gridWidth) / 2f;
        gridY = padding + controlHeight + controlGap + (availableHeight - gridHeight) / 2f;
    }
}
