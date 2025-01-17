package io.github.game.screens;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.github.game.Train;
import io.github.game.Waypoints;
import io.github.game.assets.RegionNames;
import io.github.game.utils.Constants;
import io.github.game.utils.Geolocation;
import io.github.game.utils.MapRasterTiles;
import io.github.game.utils.ZoomXY;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class TestScreen implements Screen {

    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;

    private FitViewport viewport;
    private TiledMap tiledMap;
    private TiledMapRenderer tiledMapRenderer;
    private Texture[] mapTiles;
    private ZoomXY beginTile;
    private final Geolocation CENTER_GEOLOCATION = new Geolocation(46.55, 14.96);
    private final Geolocation MARKER_GEOLOCATION = new Geolocation(46.559070, 15.638100);


    private Waypoints waypoints;
    private TextureAtlas gameplayAtlas;

    private Array<Train> trainArray;
    Pool<Train> trainPool = Pools.get(Train.class, 10);

    private SpriteBatch batch;

    public TestScreen( FitViewport viewport, OrthographicCamera camera, TextureAtlas gameplayAtlas, SpriteBatch batch) {
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
         newTrain.path = waypoints.getPathJeseniceLjubljana();
        if (newTrain.texture != null) {
            newTrain.bounds.width = newTrain.texture.getRegionWidth()/13f;
            newTrain.bounds.height = newTrain.texture.getRegionHeight()/13f;
        } else {
            System.err.println("Error: Train texture not found!");
        }
        newTrain.position.x = waypoints.getPathJeseniceLjubljana().first().x;
        newTrain.position.y = waypoints.getPathJeseniceLjubljana().first().y;
        System.out.println("Path first " + waypoints.getPathJeseniceLjubljana().first().x + " " + waypoints.getPathJeseniceLjubljana().first().y);
        newTrain.speed = 50f;
        trainArray.add(newTrain);
    }

    private boolean isClickNearTrain(Train train, int screenX, int screenY) {

        Vector3 clickWorldCoords = viewport.unproject(new Vector3(screenX, screenY, 0));


        float trainCenterX = train.position.x + train.bounds.width/2;
        float trainCenterY = train.position.y + train.bounds.height/2;


        float distance = Vector2.dst(clickWorldCoords.x, clickWorldCoords.y, trainCenterX, trainCenterY);


        float clickRadius = train.bounds.width;


        System.out.println("==== Click Debug ====");
        System.out.println("Screen click: " + screenX + ", " + screenY);
        System.out.println("World click coords: " + clickWorldCoords.x + ", " + clickWorldCoords.y);
        System.out.println("Train center: " + trainCenterX + ", " + trainCenterY);
        System.out.println("Distance in world: " + distance);
        System.out.println("Click radius: " + clickRadius);
        System.out.println("Camera zoom: " + camera.zoom);
        System.out.println("Is clicked: " + (distance < clickRadius));

        return distance < clickRadius;
    }

    @Override
    public void show() {

        shapeRenderer = new ShapeRenderer();
        waypoints = new Waypoints(shapeRenderer);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
        camera.position.set(Constants.MAP_WIDTH / 2f, Constants.MAP_HEIGHT / 2f, 0);
        camera.viewportWidth = Constants.MAP_WIDTH / 2f;
        camera.viewportHeight = Constants.MAP_HEIGHT / 2f;
        camera.zoom = 2f;
        camera.update();

        try {
            ZoomXY centerTile = MapRasterTiles.getTileNumber(CENTER_GEOLOCATION.lat, CENTER_GEOLOCATION.lng, Constants.ZOOM);
            mapTiles = MapRasterTiles.getRasterTileZone(centerTile, Constants.NUM_TILES);
            beginTile = new ZoomXY(Constants.ZOOM, centerTile.x - ((Constants.NUM_TILES - 1) / 2), centerTile.y - ((Constants.NUM_TILES - 1) / 2));
        } catch (IOException e) {
            e.printStackTrace();
        }

        tiledMap = new TiledMap();
        MapLayers layers = tiledMap.getLayers();

        TiledMapTileLayer layer = new TiledMapTileLayer(Constants.NUM_TILES, Constants.NUM_TILES, MapRasterTiles.TILE_SIZE, MapRasterTiles.TILE_SIZE);
        int index = 0;
        for (int j = Constants.NUM_TILES - 1; j >= 0; j--) {
            for (int i = 0; i < Constants.NUM_TILES; i++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(new StaticTiledMapTile(new TextureRegion(mapTiles[index], MapRasterTiles.TILE_SIZE, MapRasterTiles.TILE_SIZE)));
                layer.setCell(i, j, cell);
                index++;
            }
        }
        layers.add(layer);
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
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


            Vector3 worldCoordinates = camera.unproject(new Vector3(screenX, screenY, 0));
            float correctedX = worldCoordinates.x - 115.5371f;
            float correctedY = worldCoordinates.y - 2.0992f;
           // System.out.println("Kliknjeno na: " + correctedX + ", " + correctedY);
//            waypoints.addWaypoint(new Vector2(worldCoordinates.x, worldCoordinates.y));
//            System.out.println("Dodano: " + worldCoordinates.x + ", " + worldCoordinates.y);
//
//            System.out.println("Screen coordinates: " + screenX + ", " + screenY);
//            System.out.println("World coordinates: " + worldCoordinates.x + ", " + worldCoordinates.y);
//            System.out.println("Camera position: " + camera.position);
//            System.out.println("Viewport: " + viewport.getWorldWidth() + "x" + viewport.getWorldHeight());
             }

//        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
//            com.badlogic.gdx.math.Vector3 worldCoords = viewport.unproject(
//                new com.badlogic.gdx.math.Vector3(
//                    Gdx.input.getX(),
//                    Gdx.input.getY(),
//                    0
//                )
//            );
//            System.out.println(String.format(
//                "Point: (x: %.2f, y: %.2f)",
//                worldCoords.x,
//                worldCoords.y
//            ));
//        }
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            int screenX = Gdx.input.getX();
            int screenY = Gdx.input.getY();

            for (Train train : trainArray) {
                if (isClickNearTrain(train, screenX, screenY)) {
                    System.out.println("Klik na vlak!");

                }
            }
        }


        for (Train train : trainArray) {
            train.update(delta, waypoints);
        }

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();
        drawMarkers();
        waypoints.drawPath(camera);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Train train : trainArray) {
            train.draw(batch);
        }
        batch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.YELLOW);
        for (Train train : trainArray) {
            float centerX = train.position.x + train.bounds.width/2;
            float centerY = train.position.y + train.bounds.height/2;
            Vector3 screenPos = camera.project(new Vector3(centerX, centerY, 0));
            shapeRenderer.circle(centerX, centerY, train.bounds.width);
        }
        shapeRenderer.end();
    }

    private void drawMarkers() {
        Vector2 marker = MapRasterTiles.getPixelPosition(MARKER_GEOLOCATION.lat, MARKER_GEOLOCATION.lng, beginTile.x, beginTile.y);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.circle(marker.x, marker.y, 10);
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

