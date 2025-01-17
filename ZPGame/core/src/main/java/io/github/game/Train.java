package io.github.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class Train extends GameObjectDynamic implements Pool.Poolable {

    public int currentWaypoint  = 0;
    public TextureRegion texture;
    public float rotation = 0;
    public Array<Vector2> path;

    private boolean isAtJunction = false;

    private Junction currentJunction = null;

    public Train() {
        super(0, 0, 0, 0, 0);
        this.texture = null;
    }
    public Train(float x, float y, float width, float height, float speed, TextureRegion texture) {
        super(x, y, width, height, speed);
        this.texture = texture;
    }



    public void update(float deltaTime, Waypoints waypoints) {
        if (path == null || path.size == 0) {
            return;
        }
        Vector2 targetPoint = path.get(currentWaypoint);


        float distance = Vector2.dst(position.x, position.y, targetPoint.x, targetPoint.y);

        if (currentWaypoint == path.size - 1 && distance < 5f) {

            Junction nearJunction = waypoints.getJunctionNearPoint(position, 5f);
            if (nearJunction != null) {
                currentJunction = nearJunction;
                System.out.println("Prišli na križišče");
                return;
            }
        }


        float threshold = 5f;
        if (distance < threshold) {
            currentWaypoint++;
            if (currentWaypoint >= path.size) {
                currentWaypoint = 0;
            }
            return;
        }

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

        //float xSpeed = (float) Math.cos(angle) * speed;
        //float ySpeed = (float) Math.sin(angle) * speed;


       //position.x += xSpeed * deltaTime;
       //position.y += ySpeed * deltaTime;




    }

    public void draw(SpriteBatch batch) {
        //System.out.println("pozicija " + position.x + " " + position.y);
        //batch.draw(texture, position.x, position.y, bounds.width, bounds.height, rotation);
        batch.draw(texture,
            position.x - bounds.width/2,
            position.y - bounds.height/2,
            bounds.width/2,
            bounds.height/2,
            bounds.width,
            bounds.height,
            1, 1,
            rotation);

    }

    @Override
    public void reset(){
              }
}
