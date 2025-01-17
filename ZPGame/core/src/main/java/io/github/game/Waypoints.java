package io.github.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Waypoints {

    private com.badlogic.gdx.utils.Array<Vector2> pathJeseniceLjubljana;
    private com.badlogic.gdx.utils.Array<Vector2> pathLjubljanaKoper;
    private com.badlogic.gdx.utils.Array<Vector2> pathLjubljanaNovomesto;
    private ShapeRenderer shapeRenderer;

    private Array<Junction> junctions;



    public Waypoints(ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
        junctions = new Array<>();
        initializePathJeseniceLjubljana();
        initializePathLjubljanaKoper();
        initializePathLjubljanaNovomesto();
        initializeJunctions();
    }
    private void initializeJunctions() {

        Junction ljubljanaJunction = new Junction(
            new Vector2(798.3568f, 991.8973f),
            pathLjubljanaKoper,                 // leva pot
            pathLjubljanaNovomesto             // desna pot
        );


        junctions.add(ljubljanaJunction);


    }

    public Junction getJunctionNearPoint(Vector2 point, float threshold) {
        for (Junction junction : junctions) {
            if (junction.getPosition().dst(point) < threshold) {
                return junction;
            }
        }
        return null;
    }

    private void initializePathJeseniceLjubljana() {
        pathJeseniceLjubljana = new com.badlogic.gdx.utils.Array<>();
        pathJeseniceLjubljana.add(new Vector2(504.2126f, 1403.8019f));
        pathJeseniceLjubljana.add(new Vector2(561.4543f, 1371.0337f));
        pathJeseniceLjubljana.add(new Vector2(594.2479f, 1317.9905f));
        pathJeseniceLjubljana.add(new Vector2(636.7184f, 1287.8848f));
        pathJeseniceLjubljana.add(new Vector2(668.9744f, 1276.416f));
        pathJeseniceLjubljana.add(new Vector2(679.72644f, 1243.4431f));
        pathJeseniceLjubljana.add(new Vector2(718.9713f, 1226.9568f));
        pathJeseniceLjubljana.add(new Vector2(750.68976f, 1211.9039f));
        pathJeseniceLjubljana.add(new Vector2(777.56976f, 1183.9487f));
        pathJeseniceLjubljana.add(new Vector2(807.6754f, 1155.9934f));
        pathJeseniceLjubljana.add(new Vector2(819.5027f, 1138.0734f));
        pathJeseniceLjubljana.add(new Vector2(813.0514f, 1101.5166f));

        pathJeseniceLjubljana.add(new Vector2(814.86884f, 1079.654f));

        pathJeseniceLjubljana.add(new Vector2(809.1088f, 1052.8253f));
        pathJeseniceLjubljana.add(new Vector2(804.27045f, 1021.2861f));
        pathJeseniceLjubljana.add(new Vector2(798.3568f, 991.8973f));

    }
    private void initializePathLjubljanaKoper() {
        pathLjubljanaKoper = new com.badlogic.gdx.utils.Array<>();

        pathLjubljanaKoper.add(new Vector2(798.3568f, 991.8973f));
        pathLjubljanaKoper.add(new Vector2(758.9588f, 958.00323f));
        pathLjubljanaKoper.add(new Vector2(720.9428f, 899.43036f));
        pathLjubljanaKoper.add(new Vector2(695.59875f, 839.73114f));

    }

    private void initializePathLjubljanaNovomesto() {
        pathLjubljanaNovomesto = new com.badlogic.gdx.utils.Array<>();
        pathLjubljanaNovomesto.add(new Vector2(798.3568f, 991.8973f));
        pathLjubljanaNovomesto.add(new Vector2(850.00507f, 954.93097f));
        pathLjubljanaNovomesto.add(new Vector2(893.7811f, 905.3182f));
        pathLjubljanaNovomesto.add(new Vector2(964.55237f, 884.88934f));


    }

    public void drawPath(OrthographicCamera camera) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setColor(0.68f, 0.85f, 0.90f, 1f);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        float lineThickness = 5f;

        com.badlogic.gdx.utils.Array<com.badlogic.gdx.utils.Array<Vector2>> paths = new com.badlogic.gdx.utils.Array<>();
        paths.add(pathJeseniceLjubljana);
        paths.add(pathLjubljanaKoper);
        paths.add(pathLjubljanaNovomesto);

        for (com.badlogic.gdx.utils.Array<Vector2> path : paths) {
            for (int i = 0; i < path.size - 1; i++) {
                Vector2 start = path.get(i);
                Vector2 end = path.get(i + 1);


                float angle = (float) Math.atan2(end.y - start.y, end.x - start.x);
                float dx = (float) Math.cos(angle) * lineThickness / 2;
                float dy = (float) Math.sin(angle) * lineThickness / 2;

                shapeRenderer.rectLine(start.x, start.y, end.x, end.y, lineThickness);
            }
        }

        shapeRenderer.end();
    }


    public void addWaypoint(Vector2 vector2) {
        pathJeseniceLjubljana.add(vector2);
    }
    public Vector2 getStartWaypoint(com.badlogic.gdx.utils.Array<Vector2> path) {
        if (path == null || path.size == 0) {
            throw new IllegalArgumentException("Path is null or empty");
        }
        return path.get(0);
    }
    public com.badlogic.gdx.utils.Array<Vector2> getPathJeseniceLjubljana() {
        return pathJeseniceLjubljana;
    }
}
