package io.github.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.io.IOException;

import io.github.game.utils.Constants;
import io.github.game.utils.Geolocation;
import io.github.game.utils.MapRasterTiles;
import io.github.game.utils.ZoomXY;

public class MapScreen implements Screen {
    private OrthographicCamera camera;
    private FitViewport viewport;
    private TiledMap tiledMap;
    private TiledMapRenderer tiledMapRenderer;
    private Texture[] mapTiles;
    private ZoomXY beginTile;
    private final Geolocation CENTER_GEOLOCATION = new Geolocation(46.55, 14.96);

    private final Geolocation MARKER_GEOLOCATION = new Geolocation(46.559070, 15.638100);
    public MapScreen(FitViewport viewport, OrthographicCamera camera) {
        this.viewport = viewport;
        this.camera = camera;
    }

    @Override
    public void show() {

        camera.setToOrtho(false, Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
        camera.position.set(Constants.MAP_WIDTH / 2f, Constants.MAP_HEIGHT / 2f, 0);
        camera.viewportWidth = Constants.MAP_WIDTH / 2f;
        camera.viewportHeight = Constants.MAP_HEIGHT / 2f;
        camera.zoom = 2f;
        camera.update();


        try {
            ZoomXY centerTile = MapRasterTiles.getTileNumber(
                CENTER_GEOLOCATION.lat,
                CENTER_GEOLOCATION.lng,
                Constants.ZOOM
            );
            mapTiles = MapRasterTiles.getRasterTileZone(centerTile, Constants.NUM_TILES);
            beginTile = new ZoomXY(
                Constants.ZOOM,
                centerTile.x - ((Constants.NUM_TILES - 1) / 2),
                centerTile.y - ((Constants.NUM_TILES - 1) / 2)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }


        tiledMap = new TiledMap();
        MapLayers layers = tiledMap.getLayers();

        TiledMapTileLayer layer = new TiledMapTileLayer(
            Constants.NUM_TILES,
            Constants.NUM_TILES,
            MapRasterTiles.TILE_SIZE,
            MapRasterTiles.TILE_SIZE
        );


        int index = 0;
        for (int j = Constants.NUM_TILES - 1; j >= 0; j--) {
            for (int i = 0; i < Constants.NUM_TILES; i++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(new StaticTiledMapTile(
                    new TextureRegion(mapTiles[index],
                        MapRasterTiles.TILE_SIZE,
                        MapRasterTiles.TILE_SIZE)
                ));
                layer.setCell(i, j, cell);
                index++;
            }
        }
        layers.add(layer);
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
    }

    //    private void drawMarkers() {
//        Vector2 marker = MapRasterTiles.getPixelPosition(MARKER_GEOLOCATION.lat, MARKER_GEOLOCATION.lng, beginTile.x, beginTile.y);
//        shapeRenderer.setProjectionMatrix(camera.combined);
//        shapeRenderer.setColor(Color.RED);
//        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//        shapeRenderer.circle(marker.x, marker.y, 10);
//        shapeRenderer.end();
//    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        handleInput();

        viewport.apply();
        camera.update();

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();
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
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        tiledMap.dispose();
        for (Texture texture : mapTiles) {
            if (texture != null) {
                texture.dispose();
            }
        }
    }
}
