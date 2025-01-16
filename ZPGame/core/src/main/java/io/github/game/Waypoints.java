package io.github.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class Waypoints {

    private com.badlogic.gdx.utils.Array<Vector2> pathJeseniceLjubljana;
    private ShapeRenderer shapeRenderer;
    public Waypoints(ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
        initializePathJeseniceLjubljana();
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

        //pathJeseniceLjubljana.add(new Vector2(809.12006f, 1079.4534f));

    }

    public void drawPath(OrthographicCamera camera) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        float lineThickness = 5f;

        for (int i = 0; i < pathJeseniceLjubljana.size - 1; i++) {
            Vector2 start = pathJeseniceLjubljana.get(i);
            Vector2 end = pathJeseniceLjubljana.get(i + 1);


            float angle = (float) Math.atan2(end.y - start.y, end.x - start.x);
            float dx = (float) Math.cos(angle) * lineThickness / 2;
            float dy = (float) Math.sin(angle) * lineThickness / 2;

            shapeRenderer.rectLine(start.x, start.y, end.x, end.y, lineThickness);
        }

        shapeRenderer.end();
    }



}
