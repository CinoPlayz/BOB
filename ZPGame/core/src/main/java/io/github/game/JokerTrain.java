package io.github.game;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class JokerTrain extends Train {

    public JokerTrain() {
        super();
    }

    public JokerTrain(float x, float y, float width, float height, float speed, TextureRegion texture) {
        super(x, y, width, height, speed, texture);
    }

    @Override
    public void setPath(RailwayPath path, boolean reversed) {
        super.setPath(path, reversed);
        this.speed = 100f;
        this.bounds.width *= 1.5;
        this.bounds.height *= 1.5;
    }
}
