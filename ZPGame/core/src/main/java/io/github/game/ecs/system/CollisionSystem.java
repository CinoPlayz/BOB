package io.github.game.ecs.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;

import io.github.game.common.GameManager;
import io.github.game.ecs.system.passive.EntityFactorySystem;
import io.github.game.ecs.system.passive.SoundSystem;

public class CollisionSystem extends EntitySystem {



    private final EntityFactorySystem factorySystem;
    private SoundSystem soundSystem;


    public CollisionSystem(EntityFactorySystem factorySystem) {
        this.factorySystem = factorySystem;
    }

    @Override
    public void addedToEngine(Engine engine) {
        soundSystem = engine.getSystem(SoundSystem.class);
    }

    @Override
    public void update(float deltaTime) {
    }

}
