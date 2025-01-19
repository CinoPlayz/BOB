package io.github.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.github.game.RailwayPath;
import io.github.game.Train;
import io.github.game.Waypoints;
import io.github.game.assets.RegionNames;

public class GameScreen implements Screen {

    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;

    private FitViewport viewport;

    private Waypoints waypoints;
    private TextureAtlas gameplayAtlas;

    private Array<Train> trainArray;
    Pool<Train> trainPool = Pools.get(Train.class, 10);

    private SpriteBatch batch;

    public GameScreen(FitViewport viewport, OrthographicCamera camera, TextureAtlas gameplayAtlas, SpriteBatch batch) {
        this.viewport = viewport;
        this.camera = camera;
        this.batch = batch;
        shapeRenderer = new ShapeRenderer();
        waypoints = new Waypoints(shapeRenderer);


        trainArray = new Array<>();
        trainPool.fill(2);
        this.gameplayAtlas = gameplayAtlas;

        spawnTrain();
    }

    private void spawnTrain() {
        Train newTrain = trainPool.obtain();
        newTrain.texture = gameplayAtlas.findRegion(RegionNames.TRAIN);

        RailwayPath initialPath = waypoints.getPathById("0-1");
        newTrain.setPath(initialPath, false);

        if (newTrain.texture != null) {
            newTrain.bounds.width = newTrain.texture.getRegionWidth() / 13f;
            newTrain.bounds.height = newTrain.texture.getRegionHeight() / 13f;
        } else {
            System.err.println("Error: Train texture not found!");
        }

        newTrain.speed = 50f;
        trainArray.add(newTrain);

        Train trainReverse = trainPool.obtain();
        trainReverse.texture = gameplayAtlas.findRegion(RegionNames.TRAIN);

        trainReverse.setPath(initialPath, true); // true = obratna smer

        if (trainReverse.texture != null) {
            trainReverse.bounds.width = trainReverse.texture.getRegionWidth() / 13f;
            trainReverse.bounds.height = trainReverse.texture.getRegionHeight() / 13f;
        }

        trainReverse.speed = 50f;
        trainArray.add(trainReverse);

        Train trainReverseOne = trainPool.obtain();
        trainReverseOne.texture = gameplayAtlas.findRegion(RegionNames.TRAIN);
        RailwayPath pathReverseOne = waypoints.getPathById("1-2");
        trainReverseOne.setPath(pathReverseOne, true);

        if (trainReverseOne.texture != null) {
            trainReverseOne.bounds.width = trainReverseOne.texture.getRegionWidth() / 13f;
            trainReverseOne.bounds.height = trainReverseOne.texture.getRegionHeight() / 13f;
        }

        trainReverseOne.speed = 30f;
        trainArray.add(trainReverseOne);

        Train trainReverseTwo = trainPool.obtain();
        trainReverseTwo.texture = gameplayAtlas.findRegion(RegionNames.TRAIN);
        RailwayPath pathReverseTwo = waypoints.getPathById("1-3");
        trainReverseTwo.setPath(pathReverseTwo, true);

        if (trainReverseTwo.texture != null) {
            trainReverseTwo.bounds.width = trainReverseTwo.texture.getRegionWidth() / 13f;
            trainReverseTwo.bounds.height = trainReverseTwo.texture.getRegionHeight() / 13f;
        }
        trainReverseTwo.speed = 30f;
        trainArray.add(trainReverseTwo);


        Train train1 = trainPool.obtain();
        train1.texture = gameplayAtlas.findRegion(RegionNames.TRAIN);
        RailwayPath path = waypoints.getPathById("20-23");
        train1.setPath(path, true);

        if (trainReverseTwo.texture != null) {
            train1.bounds.width = train1.texture.getRegionWidth() / 13f;
            train1.bounds.height = train1.texture.getRegionHeight() / 13f;
        }
        train1.speed = 60f;
        trainArray.add(train1);
    }

    private boolean isClickNearTrain(Train train, int screenX, int screenY) {
        // 1. Pridobi world koordinate klika, upoštevaje vse transformacije kamere
        Vector3 worldClick = new Vector3(screenX, screenY, 0);
        camera.unproject(worldClick,
            viewport.getScreenX(),
            viewport.getScreenY(),
            viewport.getScreenWidth(),
            viewport.getScreenHeight()
        );

        // 2. Izračuna center vlaka
        float trainCenterX = train.position.x + train.bounds.width / 2;
        float trainCenterY = train.position.y + train.bounds.height / 2;

        // 3. Pretvori pozicijo vlaka v screen koordinate za primerjavo
        Vector3 trainScreenPos = new Vector3(trainCenterX, trainCenterY, 0);
        camera.project(trainScreenPos,
            viewport.getScreenX(),
            viewport.getScreenY(),
            viewport.getScreenWidth(),
            viewport.getScreenHeight()
        );

        // 4. Izračuna razdaljo v world space
        float distance = Vector2.dst(
            worldClick.x,
            worldClick.y,
            trainCenterX,
            trainCenterY
        );


        float baseRadius = train.bounds.width;
        float scaledRadius = baseRadius * (1f / camera.zoom);


        System.out.println("=== Click Detection Debug ===");
        System.out.println("Screen Click: " + screenX + ", " + screenY);
        System.out.println("World Click: " + worldClick.x + ", " + worldClick.y);
        //System.out.println("Train Center World: " + trainCenterX + ", " + trainCenterY);
        //System.out.println("Train Center Screen: " + trainScreenPos.x + ", " + trainScreenPos.y);
        //System.out.println("Distance: " + distance);
        //System.out.println("Detection Radius: " + scaledRadius);
        //System.out.println("Camera Properties:");
        //System.out.println("  Position: " + camera.position.x + ", " + camera.position.y);
        //System.out.println("  Zoom: " + camera.zoom);
        //System.out.println("  Viewport: " + viewport.getScreenWidth() + "x" + viewport.getScreenHeight());

        return distance <= scaledRadius;
    }


    @Override
    public void show() {

        shapeRenderer = new ShapeRenderer();
        waypoints = new Waypoints(shapeRenderer);

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        handleInput();
        viewport.apply();
        camera.update();


        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            int screenX = Gdx.input.getX();
            int screenY = Gdx.input.getY();

            for (Train train : trainArray) {
                if (isClickNearTrain(train, screenX, screenY)) {
                    System.out.println("Klik na vlak!");
                    train.nextDirection = !train.nextDirection;
                    System.out.println("Direction! " + train.nextDirection);
                }
            }
        }


        for (Train train : trainArray) {
            train.update(delta, waypoints);
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(gameplayAtlas.findRegion(RegionNames.BACKGROUND_MAP_LAND), 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        batch.end();
        waypoints.drawPath(camera);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Train train : trainArray) {
            train.draw(batch);

        }
        batch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Train train : trainArray) {
            train.drawArrow(shapeRenderer);
        }
        shapeRenderer.end();


    }


    private void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            camera.zoom += 0.02;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            camera.zoom -= 0.02;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            camera.translate(-3, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            camera.translate(3, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            camera.translate(0, -3, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            camera.translate(0, 3, 0);
        }

        camera.zoom = MathUtils.clamp(camera.zoom, 0.5f, 2f);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}

