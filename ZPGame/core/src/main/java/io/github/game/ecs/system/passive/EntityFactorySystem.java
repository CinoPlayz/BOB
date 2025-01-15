package io.github.game.ecs.system.passive;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import io.github.game.assets.AssetDescriptors;
import io.github.game.assets.RegionNames;
import io.github.game.config.GameConfig;
import io.github.game.ecs.component.BoundsComponent;
import io.github.game.ecs.component.DimensionComponent;
import io.github.game.ecs.component.MovementComponentXYR;
import io.github.game.ecs.component.PositionComponent;
import io.github.game.ecs.component.TextureComponent;
import io.github.game.ecs.component.ZOrderComponent;


public class EntityFactorySystem extends EntitySystem {

    private static final int BACKGROUND_Z_ORDER = 0;


    private final AssetManager assetManager;


    private PooledEngine engine;
    private TextureAtlas gamePlayAtlas;
    private TextureAtlas spritesAtlas;

    public EntityFactorySystem(AssetManager assetManager) {
        this.assetManager = assetManager;
        setProcessing(false);   // passive system

        init();
    }

    private void init() {
        gamePlayAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);
        spritesAtlas = assetManager.get(AssetDescriptors.SPRITES);

    }

    @Override
    public void addedToEngine(Engine engine) {
        this.engine = (PooledEngine) engine;
    }


}
