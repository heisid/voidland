package com.kucing_oren.voidland.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kucing_oren.voidland.VoidLand;

abstract class AbstractMenuScreen extends ScreenAdapter {
    protected final VoidLand application;
    protected final Stage stage;
    protected final Table content;
    protected final float uiScale;

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
        ScrollPane scrollPane = new ScrollPane(content, new ScrollPane.ScrollPaneStyle());
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setOverscroll(false, false);
        root.add(scrollPane).expand().fill();
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
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.08f, 0.1f, 0.14f, 1f);
        stage.act(delta);
        stage.draw();
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
