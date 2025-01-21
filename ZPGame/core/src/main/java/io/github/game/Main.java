package io.github.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.github.game.assets.AssetDescriptors;
import io.github.game.screens.MapScreen;
import io.github.game.utils.Constants;


/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    private SpriteBatch batch;
    private AssetManager assetManager;


    private OrthographicCamera camera;
    private FitViewport viewport;

    private TextureAtlas gameplayAtlas;
    private Skin skin;

    private BitmapFont fontMedium;
    private BitmapFont fontMediumBold;
    private BitmapFont fontSmall;
    private Sound clickSound;


    @Override
    public void create() {
        batch = new SpriteBatch();


        assetManager = new AssetManager();
        FileHandleResolver resolver = new InternalFileHandleResolver();
        assetManager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));

        assetManager.load(AssetDescriptors.GAMEPLAY);
        assetManager.load(AssetDescriptors.UI_SKIN);
        assetManager.load(AssetDescriptors.DUMMY_UI_FONT);
        assetManager.load(AssetDescriptors.DUMMY_UI_FONT_TITLE);
        assetManager.load(AssetDescriptors.FONT_REGULAR);
        assetManager.load(AssetDescriptors.FONT_BOLD);
        assetManager.finishLoading();

        gameplayAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);
        skin = assetManager.get(AssetDescriptors.UI_SKIN);
        fontMedium = createFont(assetManager.get(AssetDescriptors.FONT_REGULAR), 64);
        fontMediumBold = createFont(assetManager.get(AssetDescriptors.FONT_BOLD), 24);
        fontSmall = createFont(assetManager.get(AssetDescriptors.FONT_REGULAR), 24);

        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.MAP_WIDTH, Constants.MAP_HEIGHT, camera);
        viewport.apply(true);
        setScreen(new MapScreen(viewport, camera, gameplayAtlas, skin, this, batch));
    }

    @Override
    public void render() {
        super.render();

    }
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        super.resize(width, height);
    }
    @Override
    public void dispose() {
        getScreen().dispose();
        batch.dispose();
        assetManager.dispose();
    }

    BitmapFont createFont(FreeTypeFontGenerator ftfg, float dp)
    {
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = (int)(dp * Gdx.graphics.getDensity());
        return ftfg.generateFont(parameter);
    }

    public BitmapFont getFontMedium(){
        return fontMedium;
    }

    public BitmapFont getFontSmall(){
        return fontSmall;
    }

    public BitmapFont getFontMediumBold(){
        return fontMediumBold;
    }
}
