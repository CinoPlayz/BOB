package io.github.game.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.game.common.Mappers;
import io.github.game.ecs.component.DimensionComponent;
import io.github.game.ecs.component.PositionComponent;
import io.github.game.ecs.component.TextureComponent;
import io.github.game.ecs.component.ZOrderComparator;
import io.github.game.ecs.component.ZOrderComponent;


public class RenderSystem extends SortedIteratingSystem {

    private static final Family FAMILY = Family.all(
        PositionComponent.class,
        DimensionComponent.class,
        TextureComponent.class,
        ZOrderComponent.class
    ).get();

    private final SpriteBatch batch;
    private final Viewport viewport;
    private ShapeRenderer shapeRenderer;

    public RenderSystem(SpriteBatch batch, Viewport viewport) {
        super(FAMILY, ZOrderComparator.INSTANCE);
        this.batch = batch;
        this.viewport = viewport;
        this.shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void update(float deltaTime) {   // override to avoid calling batch.begin/end for each entity
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        super.update(deltaTime);    // calls processEntity method, which is wrapped with begin/end

        batch.end();
        if(Gdx.app.getLogLevel() == Application.LOG_DEBUG) {  // samo v debug načinu
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

            shapeRenderer.end();
        }
    }


    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PositionComponent position = Mappers.POSITION.get(entity);
        DimensionComponent dimension = Mappers.DIMENSION.get(entity);
        TextureComponent texture = Mappers.TEXTURE.get(entity);


    }


}
