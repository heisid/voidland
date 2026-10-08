package com.kucing_oren.voidland.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
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
import com.kucing_oren.voidland.emulator.Chip8Constants;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmulatorScreen extends ScreenAdapter {
    private final ShapeRenderer shapeRenderer;
    private final float uiScale;
    private final Stage stage;
    private final ExecutorService audioExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "emulator-audio");
        thread.setDaemon(true);
        return thread;
    });

    private final TextButton pauseButton;
    private boolean paused;

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
                        float y = gridY + (Chip8Constants.DISPLAY_HEIGHT - 1 - j) * pixelSize;
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

    private void updateGridLayout(int width, int height) {
        float padding = 24f * uiScale;
        float controlHeight = 50f * uiScale;
        float controlGap = CONTROL_GAP * uiScale;
        float availableWidth = Math.max(0f, width - 2f * padding);
        float availableHeight = Math.max(0f, height - 2f * padding - controlHeight - controlGap);

        pixelSize = Math.min(
            availableWidth / Chip8Constants.DISPLAY_WIDTH,
            availableHeight / Chip8Constants.DISPLAY_HEIGHT
        );
        float gridWidth = Chip8Constants.DISPLAY_WIDTH * pixelSize;
        float gridHeight = Chip8Constants.DISPLAY_HEIGHT * pixelSize;
        gridX = (width - gridWidth) / 2f;
        gridY = padding + controlHeight + controlGap + (availableHeight - gridHeight) / 2f;
    }
}
