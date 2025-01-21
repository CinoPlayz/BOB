package io.github.game;

abstract public class GameObjectDynamic extends  GameObject{
    public float speed;

    public GameObjectDynamic(float x, float y, float width, float height, float speed) {
        super(x, y, width, height);
        this.speed = speed;
    }
    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

}
