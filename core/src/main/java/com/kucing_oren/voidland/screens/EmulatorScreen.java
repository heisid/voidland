package com.kucing_oren.voidland.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kucing_oren.voidland.VoidLand;

public class EmulatorScreen extends ScreenAdapter {
    private final ShapeRenderer shapeRenderer;
    private final float uiScale;
    private final Stage stage;

    private final TextButton pauseButton;
    private boolean paused;

    private final boolean[][] pixels;
    private static final int GRID_ROWS = 32;
    private static final int GRID_COLUMNS = 64;
    private static final float CONTROL_GAP = 16f;
    private float pixelSize;
    private float gridX;
    private float gridY;

    public EmulatorScreen(VoidLand application) {
        this.uiScale = application.getSettings().uiScale;
        this.stage = new Stage(new ScreenViewport());
        this.shapeRenderer = new ShapeRenderer();

        this.pixels = new boolean[GRID_ROWS][GRID_COLUMNS];

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
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        updatePixels();
        stage.act(delta);

        if (pixelSize > 0f) {
            shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.WHITE);
            for (int i = 0; i < pixels.length; i++) {
                for (int j = 0; j < pixels[i].length; j++) {
                    if (pixels[i][j]) {
                        float x = gridX + j * pixelSize;
                        float y = gridY + (GRID_ROWS - 1 - i) * pixelSize;
                        shapeRenderer.rect(x, y, pixelSize, pixelSize);
                    }
                }
            }
            shapeRenderer.end();
        }
        stage.draw();
    }

    private void updatePixels() {
        if (paused) {
            return;
        }

        for (int i = 0; i < pixels.length; i++) {
            for (int j = 0; j < pixels[i].length; j++) {
                double random = Math.random();
                pixels[i][j] = random < 0.5;
            }
        }
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
