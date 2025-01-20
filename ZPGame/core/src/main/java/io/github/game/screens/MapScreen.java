package io.github.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;


import org.bson.Document;
import org.bson.conversions.Bson;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.github.game.Main;
import io.github.game.assets.RegionNames;
import io.github.game.utils.Constants;
import io.github.game.utils.Geolocation;
import io.github.game.utils.MapRasterTiles;
import io.github.game.utils.MongoClientConnect;
import io.github.game.utils.RequestTiles;
import io.github.game.utils.Requests;
import io.github.game.utils.TrainLocHistory;
import io.github.game.utils.ZoomXY;

public class MapScreen implements Screen {
    private OrthographicCamera camera;
    private FitViewport viewport;
    private TextureAtlas gameplayAtlas;
    private TiledMap tiledMap;
    private TiledMapRenderer tiledMapRenderer;
    private Texture[] mapTiles;
    private ZoomXY beginTile;
    private float previousZoomLevel = 0;
    private ShapeRenderer shapeRenderer;
    private Stage stage;
    private Skin skin;
    private Main game;

    private List<TrainLocHistory> listOfTrainLocHistory;

    private final Geolocation MARKER_GEOLOCATION = new Geolocation(46.559070, 15.638100);

    public MapScreen(FitViewport viewport, OrthographicCamera camera, TextureAtlas gameplayAtlas, Skin skin, Main game) {
        this.viewport = viewport;
        this.camera = camera;
        this.gameplayAtlas = gameplayAtlas;
        this.skin = skin;
        this.game = game;
    }

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);

        camera.setToOrtho(false, Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
        camera.position.set(Constants.MAP_WIDTH / 2f, Constants.MAP_HEIGHT / 2f, 0);
        camera.viewportWidth = Constants.MAP_WIDTH / 2f;
        camera.viewportHeight = Constants.MAP_HEIGHT / 2f;
        camera.zoom = 1f;
        camera.update();

        listOfTrainLocHistory = null;

        RequestTiles requestTiles = Requests.requestTiles(camera);
        beginTile = requestTiles.beginTile;
        mapTiles = requestTiles.tiles;

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

        drawTrainLocOnce("2024-05-24T00:00:00.432+00:00", "2024-05-24T23:59:59.999+00:00");
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        handleInput();

        viewport.apply();
        camera.update();

        //Update Tiles based on zoom level
        /*if (camera.zoom != previousZoomLevel){
            previousZoomLevel = camera.zoom;
            RequestTiles requestTiles = Requests.requestTiles(camera);
            beginTile = requestTiles.beginTile;
            mapTiles = requestTiles.tiles;
        }*/

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        drawMarkers();
        stage.act(delta);
        stage.draw();

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
        stage.getViewport().update(width, height, true);
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
        stage.dispose();
        tiledMap.dispose();
        for (Texture texture : mapTiles) {
            if (texture != null) {
                texture.dispose();
            }
        }
    }

    private void drawMarkers() {
        Vector2 marker = MapRasterTiles.getPixelPosition(MARKER_GEOLOCATION.lat, MARKER_GEOLOCATION.lng, beginTile.x, beginTile.y);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.circle(marker.x, marker.y, 10);
        shapeRenderer.end();
    }

    private void drawTrainLocOnce(String startDate, String endDate){
        Bson filter = null;
        @SuppressWarnings("NewApi") DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ENGLISH);
        try {
            filter = new Document("$gte", format.parse(startDate)).append("$lt", format.parse(endDate));
        }
        catch (ParseException e) {
            System.out.println(e.getMessage());
        }

        if(filter != null){
            MongoCollection<Document> collection = MongoClientConnect.database.getCollection("trainlochistories");
            FindIterable<Document> findIter = collection.find(new Document("timeOfRequest", filter));
            List<Document> docs = new ArrayList<>();
            findIter.into(docs);

            listOfTrainLocHistory.clear();
            for(int i = 0; i < docs.size(); i++){
                Document doc = docs.get(i);
                listOfTrainLocHistory.add(new TrainLocHistory(doc));
            }
        }

        if (listOfTrainLocHistory != null){
            TextureRegion textureTrain = gameplayAtlas.findRegion(RegionNames.TRAIN_ICON);

            for(int i = 0; i < listOfTrainLocHistory.size(); i++){
                TrainLocHistory trainLocHistory = listOfTrainLocHistory.get(i);
                double lat = trainLocHistory.getCoordinates().getLat();
                double lng = trainLocHistory.getCoordinates().getLng();

                Vector2 marker = MapRasterTiles.getPixelPosition(lat, lng, beginTile.x, beginTile.y);

                Image trainIcon = new Image(textureTrain);
                float trainIconWidth = trainIcon.getWidth() / 10;
                float trainIconHeight = trainIcon.getHeight() / 10;

                Window.WindowStyle windowStyle = skin.get("default", Window.WindowStyle.class);
                windowStyle.titleFont = game.getFontMediumBold();

                Label.LabelStyle labelStyle = skin.get("default", Label.LabelStyle.class);
                labelStyle.font = game.getFontMedium();

                Window windowAboveTrain = new Window("", windowStyle);
                Label labelName = new Label(trainLocHistory.getTrainType() + " " + trainLocHistory.getTrainNumber(), labelStyle);
                Label labelNextStop = new Label("Next station: " + trainLocHistory.getNextStation(), labelStyle);
                Label labelDelay = new Label("Delay: " + trainLocHistory.getDelay() + "min", labelStyle);
                windowAboveTrain.add(labelName).row();
                windowAboveTrain.add(labelNextStop).row();
                windowAboveTrain.add(labelDelay);
                windowAboveTrain.sizeBy(300, 0);
                windowAboveTrain.setPosition(marker.x - windowAboveTrain.getWidth()/2, marker.y + trainIconHeight);
                windowAboveTrain.setVisible(false);

                trainIcon.addListener(new ClickListener(){
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        windowAboveTrain.setVisible(!windowAboveTrain.isVisible());
                    }
                });


                trainIcon.setWidth(trainIconWidth);
                trainIcon.setHeight(trainIconHeight);
                trainIcon.setPosition(marker.x - trainIconWidth / 2, marker.y);

                stage.addActor(trainIcon);
                stage.addActor(windowAboveTrain);
            }
        }


    }
}
