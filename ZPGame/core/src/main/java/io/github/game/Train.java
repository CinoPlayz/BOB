package io.github.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import io.github.game.screens.GameScreen;

public class Train extends GameObjectDynamic implements Pool.Poolable {

    public int currentWaypoint = 0;
    public TextureRegion texture;
    public float rotation = 0;
    public Array<Vector2> path;

    private boolean isAtJunction = false;

    private Junction currentJunction = null;

    public boolean nextDirection = false;

    private String currentPathId;
    private RailwayPath currentPath;
    private boolean isReversed;

    public Train() {
        super(0, 0, 0, 0, 0);
        this.texture = null;
    }

    public Train(float x, float y, float width, float height, float speed, TextureRegion texture) {
        super(x, y, width, height, speed);
        this.texture = texture;
    }

    public void setPath(RailwayPath path, boolean reversed) {
        this.currentPath = path;
        this.currentPathId = path.getId();
        this.isReversed = reversed;
        if (isReversed) {
            currentWaypoint = path.getSize() - 1;

            Vector2 lastPoint = path.getPoint(currentWaypoint);
            position.x = lastPoint.x;
            position.y = lastPoint.y;
        } else {
            currentWaypoint = 0;
            Vector2 firstPoint = path.getPoint(currentWaypoint);
            position.x = firstPoint.x;
            position.y = firstPoint.y;
        }
    }

    private void handleJunction(Junction junction, Waypoints waypoints) {
        Array<PathConnection> availableConnections =
            junction.getAvailableConnections(currentPathId, isReversed);


        if (availableConnections.size > 0) {
            // Izbere naslednjo pot glede na nextDirection
            PathConnection chosenPath = nextDirection ?
                availableConnections.get(0) : availableConnections.get(1);
            //System.out.println("Chosen path: " + chosenPath.fromPathId + " -> " + chosenPath.toPathId);
            //System.out.println("New direction reversed: " + chosenPath.toReversed);
            // Nastavi novo pot
            RailwayPath newPath = waypoints.getPathById(chosenPath.toPathId);
            setPath(newPath, chosenPath.toReversed);
        }
    }

    private void updateMovement(Vector2 targetPoint, float deltaTime) {
        float dx = targetPoint.x - position.x;
        float dy = targetPoint.y - position.y;
        rotation = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;

        float xSpeed = MathUtils.cosDeg(rotation) * speed;
        float ySpeed = MathUtils.sinDeg(rotation) * speed;

        position.x += xSpeed * deltaTime;
        position.y += ySpeed * deltaTime;
    }

    private void updateBounds() {
        if (texture == null) return;

        float width = texture.getRegionWidth() / 13f;
        float height = texture.getRegionHeight() / 13f;

        float centerX = position.x;
        float centerY = position.y;

        bounds.width = width;
        bounds.height = height;

        bounds.x = centerX - width / 2;
        bounds.y = centerY - height / 2;
    }

    public void update(float deltaTime, Waypoints waypoints) {
        if (currentPath == null) {
            return;
        }

        //Vector2 targetPoint = path.get(currentWaypoint);
        Vector2 targetPoint = currentPath.getPoint(currentWaypoint);

        float distance = Vector2.dst(position.x, position.y, targetPoint.x, targetPoint.y);

        if (distance < 5f) {
            // Preveri za križišče samo če smo na koncu/začetku poti
            if ((isReversed && currentWaypoint == 0) ||
                (!isReversed && currentWaypoint == currentPath.getSize() - 1)) {

                Junction junction = waypoints.getJunctionNearPoint(position, 5f);
                if (junction != null) {
                    handleJunction(junction, waypoints);
                    return;
                }


            }

            // Premik na naslednjo točko
            if (isReversed) {
                currentWaypoint--;
                if (currentWaypoint < 0) {
                    currentWaypoint = 0;
                }
            } else {
                currentWaypoint++;
                if (currentWaypoint >= currentPath.getSize()) {
                    currentWaypoint = 0;
                }
            }
        }

        updateMovement(targetPoint, deltaTime);


        float angle = (float) Math.atan2(
            targetPoint.y - position.y,
            targetPoint.x - position.x
        );

        float dx = targetPoint.x - position.x;
        float dy = targetPoint.y - position.y;
        rotation = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;

        float xSpeed = MathUtils.cosDeg(rotation) * speed;
        float ySpeed = MathUtils.sinDeg(rotation) * speed;

        position.x += xSpeed * deltaTime;
        position.y += ySpeed * deltaTime;

        //updateRotatedBounds();
        updateBounds();
    }

    public boolean shouldRemoveTrain(String pathId, boolean reversed, Array<GameScreen.TrainSpawnConfig> spawnConfigs) {
        for (GameScreen.TrainSpawnConfig config : spawnConfigs) {
            if (config.pathId.equals(pathId) && config.isReversed != reversed) {
                if (!config.isReversed) {
                    if (currentWaypoint == 0) {
                        return true;
                    }
                } else {
                    if (currentWaypoint == currentPath.getSize() - 1) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void drawArrow(ShapeRenderer shapeRenderer) {
        float arrowLength = bounds.width * 0.8f;
        float startX = position.x;
        float startY = position.y;


        float directionAngle = rotation;


        if (nextDirection) {
            directionAngle -= 45;
        } else {
            directionAngle += 45;
        }

        //  končna točka puščice
        float endX = startX + arrowLength * MathUtils.cosDeg(directionAngle);
        float endY = startY + arrowLength * MathUtils.sinDeg(directionAngle);


        float arrowHeadLength = arrowLength * 0.3f;
        float arrowHeadAngle = 25f;

        float angle1 = directionAngle + 180 + arrowHeadAngle;
        float angle2 = directionAngle + 180 - arrowHeadAngle;

        float arrowHead1X = endX + arrowHeadLength * MathUtils.cosDeg(angle1);
        float arrowHead1Y = endY + arrowHeadLength * MathUtils.sinDeg(angle1);

        float arrowHead2X = endX + arrowHeadLength * MathUtils.cosDeg(angle2);
        float arrowHead2Y = endY + arrowHeadLength * MathUtils.sinDeg(angle2);

        float lineThickness = 3f;
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rectLine(startX, startY, endX, endY, lineThickness);

        shapeRenderer.rectLine(endX, endY, arrowHead1X, arrowHead1Y, lineThickness);
        shapeRenderer.rectLine(endX, endY, arrowHead2X, arrowHead2Y, lineThickness);
    }

    public void draw(SpriteBatch batch) {
        //System.out.println("pozicija " + position.x + " " + position.y);
        //batch.draw(texture, position.x, position.y, bounds.width, bounds.height, rotation);
        batch.draw(texture,
            position.x - bounds.width / 2,
            position.y - bounds.height / 2,
            bounds.width / 2,
            bounds.height / 2,
            bounds.width,
            bounds.height,
            1, 1,
            rotation);


    }

    @Override
    public void reset() {
    }

    public boolean isReversed() {
        return isReversed;
    }

    public String getCurrentPathId() {
        return currentPathId;
    }
}
