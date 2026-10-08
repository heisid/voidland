package com.kucing_oren.voidland.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kucing_oren.voidland.VoidLand;

abstract class AbstractMenuScreen extends ScreenAdapter {
    protected final VoidLand application;
    protected final Stage stage;
    protected final Table content;
    protected final float uiScale;
    private final ScrollPane scrollPane;
    private final Label scrollHint;

    AbstractMenuScreen(VoidLand application, String title) {
        this.application = application;
        this.uiScale = application.getSettings().uiScale;
        this.stage = new Stage(new ScreenViewport());

        Table root = new Table();
        root.setFillParent(true);
        root.pad(24f * uiScale);
        stage.addActor(root);

        Label heading = new Label(title, application.getSkin());
        heading.setFontScale(1.6f);
        root.add(heading).padBottom(22f * uiScale).row();

        content = new Table();
        content.top();
        scrollPane = new ScrollPane(content, new ScrollPane.ScrollPaneStyle());
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setOverscroll(false, false);
        scrollPane.setFlickScroll(true);
        scrollPane.setCancelTouchFocus(true);
        root.add(scrollPane).expand().fill();
        root.row();

        scrollHint = new Label("", application.getSkin());
        scrollHint.setColor(Color.LIGHT_GRAY);
        root.add(scrollHint).height(24f * uiScale).padTop(4f * uiScale);
        updateScrollHint();
    }

    protected TextButton addButton(String text, Runnable action) {
        TextButton button = new TextButton(text, application.getSkin());
        button.pad(10f * uiScale);
        button.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                action.run();
            }
        });
        content.add(button).width(320f * uiScale).height(50f * uiScale).pad(5f * uiScale).row();
        return button;
    }

    protected Label addMessage(String text, float fontScale) {
        Label label = new Label(text, application.getSkin());
        label.setColor(Color.LIGHT_GRAY);
        label.setWrap(true);
        label.setFontScale(fontScale);
        content.add(label).width(360f * uiScale).pad(8f * uiScale).row();
        return label;
    }

    protected Label addMessage(String text) {
        return addMessage(text, 1.0f);
    }

    protected void addGap() {
        content.add().height(10f * uiScale).row();
    }

    @Override
    public void show() {
        restoreInputProcessor();
    }

    protected final void restoreInputProcessor() {
        Gdx.input.setInputProcessor(new InputMultiplexer(new InputAdapter() {
            private final Vector2 pointer = new Vector2();

            @Override
            public boolean scrolled(float amountX, float amountY) {
                pointer.set(Gdx.input.getX(), Gdx.input.getY());
                stage.getViewport().unproject(pointer);
                Vector2 panePosition = scrollPane.localToStageCoordinates(new Vector2());
                boolean pointerOverPane = pointer.x >= panePosition.x
                    && pointer.x <= panePosition.x + scrollPane.getWidth()
                    && pointer.y >= panePosition.y
                    && pointer.y <= panePosition.y + scrollPane.getHeight();
                if (!pointerOverPane || scrollPane.getMaxY() <= 0f) {
                    return false;
                }
                scrollPane.setScrollY(scrollPane.getScrollY() + amountY * 40f * uiScale);
                return true;
            }
        }, stage));
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.08f, 0.1f, 0.14f, 1f);
        stage.act(delta);
        updateScrollHint();
        stage.draw();
    }

    private void updateScrollHint() {
        scrollPane.validate();
        float maxScroll = scrollPane.getMaxY();
        if (maxScroll <= 0f) {
            scrollHint.setText("");
            scrollHint.setVisible(false);
        } else {
            scrollHint.setVisible(true);
            if (scrollPane.getScrollY() <= 0f) {
                scrollHint.setText("Scroll down to see more");
            } else if (scrollPane.getScrollY() >= maxScroll) {
                scrollHint.setText("Scroll up to see more");
            } else {
                scrollHint.setText("Scroll up or down to navigate");
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
