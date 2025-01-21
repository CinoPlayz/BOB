package io.github.game.assets;


import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class AssetDescriptors {

    public static final AssetDescriptor<TextureAtlas> GAMEPLAY =
        new AssetDescriptor<TextureAtlas>(AssetPaths.GAMEPLAY, TextureAtlas.class);
    public static final AssetDescriptor<BitmapFont> DUMMY_UI_FONT =
        new AssetDescriptor<BitmapFont>(AssetPaths.DUMMY_UI_FONT, BitmapFont.class);
    public static final AssetDescriptor<BitmapFont> DUMMY_UI_FONT_TITLE =
        new AssetDescriptor<BitmapFont>(AssetPaths.DUMMY_UI_FONT_TITLE, BitmapFont.class);
    public static final AssetDescriptor<Skin> UI_SKIN =
        new AssetDescriptor<Skin>(AssetPaths.UI_SKIN, Skin.class);

    public static final AssetDescriptor<FreeTypeFontGenerator> FONT_REGULAR =
        new AssetDescriptor<>(AssetPaths.FONT_REGULAR, FreeTypeFontGenerator.class);

    public static final AssetDescriptor<FreeTypeFontGenerator> FONT_BOLD =
        new AssetDescriptor<>(AssetPaths.FONT_BOLD, FreeTypeFontGenerator.class);

}
