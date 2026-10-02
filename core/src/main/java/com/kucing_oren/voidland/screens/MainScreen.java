package com.kucing_oren.voidland.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
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

public class MainScreen extends ScreenAdapter {
    private static final float RECTANGLE_SIZE = 48f;
    private static final float MOVEMENT_SPEED = 200f;

    private final Stage stage;
    private final ShapeRenderer shapeRenderer;
    private final TextButton pauseButton;
    private final float uiScale;
    private final InputAdapter movementInput = new InputAdapter() {
        @Override
        public boolean keyDown(int keycode) {
            return setMovementKey(keycode, true);
        }

        @Override
        public boolean keyUp(int keycode) {
            return setMovementKey(keycode, false);
        }
    };
    private float rectangleX;
    private float rectangleY;
    private boolean movingLeft;
    private boolean movingRight;
    private boolean movingUp;
    private boolean movingDown;
    private boolean paused;

    public MainScreen(VoidLand application) {
        this.uiScale = application.getSettings().uiScale;
        this.stage = new Stage(new ScreenViewport());
        this.shapeRenderer = new ShapeRenderer();
        rectangleX = (Gdx.graphics.getWidth() - RECTANGLE_SIZE) / 2f;
        rectangleY = (Gdx.graphics.getHeight() - RECTANGLE_SIZE) / 2f;

        Table root = new Table();
        root.setFillParent(true);
        root.pad(24f * uiScale);
        stage.addActor(root);

        Table controls = new Table();
        root.add(controls).expand().fillX().bottom();

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
    public void show() {
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, movementInput));
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        updateRectangle(delta);
        stage.act(delta);

        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(rectangleX, rectangleY, RECTANGLE_SIZE, RECTANGLE_SIZE);
        shapeRenderer.end();

        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        clampRectangleToScreen();
    }

    @Override
    public void dispose() {
        stage.dispose();
        shapeRenderer.dispose();
    }

    private boolean setMovementKey(int keycode, boolean pressed) {
        switch (keycode) {
            case Input.Keys.W:
                movingUp = pressed;
                return true;
            case Input.Keys.A:
                movingLeft = pressed;
                return true;
            case Input.Keys.S:
                movingDown = pressed;
                return true;
            case Input.Keys.D:
                movingRight = pressed;
                return true;
            default:
                return false;
        }
    }

    private void updateRectangle(float delta) {
        if (paused) {
            return;
        }

        float horizontal = (movingRight ? 1f : 0f) - (movingLeft ? 1f : 0f);
        float vertical = (movingUp ? 1f : 0f) - (movingDown ? 1f : 0f);
        if (horizontal != 0f && vertical != 0f) {
            horizontal *= 0.7071f;
            vertical *= 0.7071f;
        }

        rectangleX += horizontal * MOVEMENT_SPEED * delta;
        rectangleY += vertical * MOVEMENT_SPEED * delta;
        clampRectangleToScreen();
    }

    private void clampRectangleToScreen() {
        rectangleX = Math.max(0f, Math.min(rectangleX, Gdx.graphics.getWidth() - RECTANGLE_SIZE));
        rectangleY = Math.max(0f, Math.min(rectangleY, Gdx.graphics.getHeight() - RECTANGLE_SIZE));
    }
}
