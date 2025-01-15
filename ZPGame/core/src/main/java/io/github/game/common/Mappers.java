package io.github.game.common;

import com.badlogic.ashley.core.ComponentMapper;

import io.github.game.ecs.component.BoundsComponent;
import io.github.game.ecs.component.DimensionComponent;
import io.github.game.ecs.component.MovementComponentXYR;
import io.github.game.ecs.component.PositionComponent;
import io.github.game.ecs.component.TextureComponent;
import io.github.game.ecs.component.ZOrderComponent;


//TODO Explain how Mappers work (see ComponentMapper and Entity implementation)
public final class Mappers {


    public static final ComponentMapper<BoundsComponent> BOUNDS =
        ComponentMapper.getFor(BoundsComponent.class);

    public static final ComponentMapper<DimensionComponent> DIMENSION =
        ComponentMapper.getFor(DimensionComponent.class);

    public static final ComponentMapper<MovementComponentXYR> MOVEMENT =
        ComponentMapper.getFor(MovementComponentXYR.class);

    public static final ComponentMapper<PositionComponent> POSITION =
        ComponentMapper.getFor(PositionComponent.class);


    public static final ComponentMapper<TextureComponent> TEXTURE =
        ComponentMapper.getFor(TextureComponent.class);

    public static final ComponentMapper<ZOrderComponent> Z_ORDER =
        ComponentMapper.getFor(ZOrderComponent.class);


    private Mappers() {
    }
}
