package io.github.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.game.screens.TestScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    private SpriteBatch batch;
    private AssetManager assetManager;
    private TestScreen testScreen;

    private Waypoints waypoints;

    @Override
    public void create() {
        batch = new SpriteBatch();
        assetManager = new AssetManager();
        waypoints = new Waypoints();
        setScreen(new TestScreen());
    }

    @Override
    public void render() {
        super.render();

    }

    @Override
    public void dispose() {
        getScreen().dispose();
        batch.dispose();
        assetManager.dispose();
    }
}
