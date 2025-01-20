package io.github.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.github.game.assets.RegionNames;
import io.github.game.common.GameManager;

public class Score {

    private BitmapFont scoreFont;
    private GlyphLayout layout;
    private static final float SCORE_PADDING = 40f;
    private static final float RIGHT_SCORE_PADDING = 120f;
    private TextureRegion scoreIcon;

    public Score(TextureAtlas gameplayAtlas) {
        scoreIcon = gameplayAtlas.findRegion(RegionNames.PASSENGERS);
        scoreFont = new BitmapFont(Gdx.files.internal("fonts/arial.fnt"));
        scoreFont.getData().setScale(2f);
        scoreFont.setColor(Color.WHITE);
        layout = new GlyphLayout();


    }

    public void draw(OrthographicCamera camera, SpriteBatch batch, FitViewport viewport) {

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        String scoreText = GameManager.INSTANCE.getScore() + "  ";
        layout.setText(scoreFont, scoreText);

        float iconSize = 50;
        float iconX = viewport.getWorldWidth() - layout.width - SCORE_PADDING - iconSize - RIGHT_SCORE_PADDING;
        float iconY = viewport.getWorldHeight() - SCORE_PADDING - iconSize;

        float scoreX = iconX + iconSize + 10;
        float scoreY = viewport.getWorldHeight() - SCORE_PADDING;


        batch.draw(scoreIcon, iconX, iconY, iconSize, iconSize);

        scoreFont.draw(batch, scoreText, scoreX, scoreY);

        batch.end();
    }


}
