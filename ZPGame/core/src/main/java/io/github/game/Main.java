package io.github.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.github.game.assets.AssetDescriptors;
import io.github.game.screens.GameScreen;
import io.github.game.screens.MapScreen;
import io.github.game.utils.Constants;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class Main extends Game {
    private SpriteBatch batch;
    private AssetManager assetManager;
    private GameScreen testScreen;

    private OrthographicCamera camera;
    private FitViewport viewport;

    private TextureAtlas gameplayAtlas;


    @Override
    public void create() {
        batch = new SpriteBatch();


        assetManager = new AssetManager();
        assetManager.load(AssetDescriptors.GAMEPLAY);
        assetManager.finishLoading();

        gameplayAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);

        camera = new OrthographicCamera();
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
        viewport.apply(true);
        setScreen(new GameScreen(viewport, camera, gameplayAtlas, batch));

//        viewport = new FitViewport(Constants.MAP_WIDTH, Constants.MAP_HEIGHT, camera);
//        viewport.apply(true);
//        setScreen(new MapScreen(viewport, camera));

    }

    @Override
    public void render() {
        super.render();

    }

    @Override
    public void resize(int width, int height) {
        // viewport.update(width, height);
        super.resize(width, height);
    }

    @Override
    public void dispose() {
        getScreen().dispose();
        batch.dispose();
        assetManager.dispose();
    }
}
