package io.github.game.screens;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.github.game.Main;
import io.github.game.WaypointsOne;

public class GameScreenOne extends GameScreen {

    public GameScreenOne(FitViewport viewport, OrthographicCamera camera, TextureAtlas gameplayAtlas, SpriteBatch batch, Main game, Skin skin) {
        super(viewport, camera, gameplayAtlas, batch, game, skin);


        initializeSpawnConfigs();

    }

    @Override
    public void show() {
        this.waypoints = new WaypointsOne(this.shapeRenderer);


    }


    @Override
    protected void initializeSpawnConfigs() {
        spawnConfigs.add(new TrainSpawnConfig("0-1", false));
        spawnConfigs.add(new TrainSpawnConfig("4-6", true));
        spawnConfigs.add(new TrainSpawnConfig("4-7", true));
        spawnConfigs.add(new TrainSpawnConfig("9-11", true));
        spawnConfigs.add(new TrainSpawnConfig("10-12", true));
        spawnConfigs.add(new TrainSpawnConfig("10-13", true));
        spawnConfigs.add(new TrainSpawnConfig("19-8", false));
        spawnConfigs.add(new TrainSpawnConfig("8-22", true));
        spawnConfigs.add(new TrainSpawnConfig("24-25", true));

    }


}
