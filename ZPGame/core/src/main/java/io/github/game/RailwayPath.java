package io.github.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class RailwayPath {
    private String id;
    private Array<Vector2> waypoints;
    private boolean isReversed;

    public RailwayPath(String id, Array<Vector2> waypoints) {
        this.id = id;
        this.waypoints = waypoints;
        this.isReversed = false;
    }


    public Vector2 getPoint(int index) {
        return isReversed ? waypoints.get(waypoints.size - 1 - index) : waypoints.get(index);
    }

    public int getSize() {
        return waypoints.size;
    }

    public String getId() {
        return id;
    }

    public Array<Vector2> getWaypoints() {
        return waypoints;
    }





}
