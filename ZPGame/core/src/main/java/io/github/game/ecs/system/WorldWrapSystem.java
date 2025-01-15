package io.github.game.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import io.github.game.common.Mappers;
import io.github.game.config.GameConfig;
import io.github.game.ecs.component.DimensionComponent;
import io.github.game.ecs.component.PositionComponent;
import io.github.game.ecs.component.WorldWrapComponent;


public class WorldWrapSystem extends IteratingSystem {
    private static final Family FAMILY = Family.all(
        PositionComponent.class,
        DimensionComponent.class,
        WorldWrapComponent.class
    ).get();

    public WorldWrapSystem() {
        super(FAMILY);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PositionComponent position = Mappers.POSITION.get(entity);
        DimensionComponent dimension = Mappers.DIMENSION.get(entity);


    }
}
