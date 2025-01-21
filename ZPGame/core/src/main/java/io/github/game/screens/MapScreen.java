package io.github.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
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
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import io.github.game.Main;
import io.github.game.assets.RegionNames;
import io.github.game.utils.Constants;
import io.github.game.utils.Coordinates;
import io.github.game.utils.Geolocation;
import io.github.game.utils.MapRasterTiles;
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
    private int speedOfAnimation = 1;
    private int iterInAnimation = 0;
    private LocalDateTime startDateForAnimation;
    private LocalDateTime endDateForAnimation;
    private String startDateString = null;
    private String endDateString = null;
    private boolean playingAnimation = false;
    private float timeOfLastIter = 0;

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

        DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        startDateForAnimation = LocalDateTime.parse("2024-05-24T00:00:00.432+00:00", ISO_FORMATTER);
        endDateForAnimation = LocalDateTime.parse("2024-05-24T23:59:59.432+00:00", ISO_FORMATTER);

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

        drawAnimationInput();
        drawTrainLoc();
        drawAddData();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(218, 231, 205, 255);
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

        if(playingAnimation){
            float elapsedTime = getCurrentTime() - timeOfLastIter;
            if(elapsedTime > (float) Constants.ANIMATION_TIME / speedOfAnimation){
                timeOfLastIter = getCurrentTime();
                iterInAnimation++;
                stage.clear();
                drawTrainLoc();
                drawAnimationInput();
                drawAddData();
            }
        }

        stage.act(delta);
        stage.draw();
    }

    private void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            camera.zoom += 0.05F;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            camera.zoom -= 0.05F;
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

        camera.zoom = MathUtils.clamp(camera.zoom, 0.2f, 1f);
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

    private boolean displayStartButton = true;
    private boolean displayEndButton = false;

    private void drawAnimationInput(){
        LocalDateTime dateTimeCurrent = LocalDateTime.from(startDateForAnimation);
        dateTimeCurrent = dateTimeCurrent.plusMinutes(iterInAnimation * 10L);

        Window.WindowStyle windowStyle = skin.get("default", Window.WindowStyle.class);
        windowStyle.titleFont = game.getFontMediumBold();

        Label.LabelStyle labelStyle = skin.get("default", Label.LabelStyle.class);
        labelStyle.font = game.getFontMedium();

        TextField.TextFieldStyle textFieldStyle = skin.get("default", TextField.TextFieldStyle.class);
        textFieldStyle.font = game.getFontMedium();

        com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle listStyle = skin.get("default", com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle.class);
        listStyle.font = game.getFontMedium();
        SelectBox.SelectBoxStyle selectBoxStyle = skin.get("default", SelectBox.SelectBoxStyle.class);
        selectBoxStyle.font = game.getFontMedium();
        selectBoxStyle.listStyle.font = game.getFontMedium();

        TextButton.TextButtonStyle textButtonStyle = skin.get("default", TextButton.TextButtonStyle.class);
        textButtonStyle.font = game.getFontMedium();

        Window windowAnimationInput = new Window("", windowStyle);
        Label labelSpeed = new Label("Speed:", labelStyle);
        SelectBox<Integer> selectBoxSpeed = new SelectBox<>(selectBoxStyle);
        selectBoxSpeed.setItems(1, 2, 3, 4, 5);
        selectBoxSpeed.setSelected(1);
        Label labelTime = new Label("Time:", labelStyle);
        Label labelCurrentTime = new Label(dateTimeCurrent.format(DateTimeFormatter.ISO_DATE_TIME), labelStyle);

        if (startDateString == null){
            startDateString = "2024-05-24T00:00:00";
        }

        if (endDateString == null){
            endDateString = "2024-05-24T23:59:59";
        }

        Label labelStartDate = new Label("Start:", labelStyle);
        Label labelEndDate = new Label("End:", labelStyle);
        TextField textFieldStartDate = new TextField(startDateString, textFieldStyle);
        TextField textFieldEndDate = new TextField(endDateString, textFieldStyle);
        TextButton textButton = new TextButton("START", textButtonStyle);
        TextButton textButtonEnd = new TextButton("End", textButtonStyle);
        textButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                displayStartButton = false;
                displayEndButton = true;
                playingAnimation = !playingAnimation;
                speedOfAnimation = selectBoxSpeed.getSelected();
                startDateString = textFieldStartDate.getText();
                endDateString = textFieldEndDate.getText();
                DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                startDateForAnimation = LocalDateTime.parse(startDateString, ISO_FORMATTER);
                endDateForAnimation = LocalDateTime.parse(endDateString, ISO_FORMATTER);
                listOfTrainLocHistory = Requests.getTrainList(startDateForAnimation, endDateForAnimation);
                stage.clear();
                drawAnimationInput();
                drawTrainLoc();
                drawAddData();
            }
        });

        textButtonEnd.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                displayStartButton = true;
                displayEndButton = false;
                playingAnimation = false;
                iterInAnimation = 0;
                stage.clear();
                drawAnimationInput();
                drawAddData();
            }
        });

        if(!displayStartButton){
            if (!playingAnimation){
                textButton.setText("RESUME");
            } else {
                textButton.setText("PAUSE");
            }
        }
        else {
            textButton.setText("START");
        }


        windowAnimationInput.add(labelSpeed).padRight(2f);
        windowAnimationInput.add(selectBoxSpeed).padLeft(4).padRight(4).row();
        windowAnimationInput.add(labelTime).padRight(2f);
        windowAnimationInput.add(labelCurrentTime).row();
        windowAnimationInput.add(labelStartDate).padRight(2f);
        windowAnimationInput.add(textFieldStartDate).expandX().fillX().row();
        windowAnimationInput.add(labelEndDate).padRight(2f);
        windowAnimationInput.add(textFieldEndDate).expandX().fillX().row();
        if (!displayEndButton){
            windowAnimationInput.add(textButton).colspan(2);
        }
        else {
            windowAnimationInput.add(textButton).expandX();
            windowAnimationInput.add(textButtonEnd);
        }
        windowAnimationInput.center();

        windowAnimationInput.sizeBy(650, 250);
        windowAnimationInput.setPosition(100, 2000);


        stage.addActor(windowAnimationInput);
    }

    private void drawTrainLoc(){
        List<TrainLocHistory> listOfCurrentTrains = new ArrayList<>();
        long minutesDiff = ChronoUnit.MINUTES.between(startDateForAnimation, endDateForAnimation);
        long maxIters = minutesDiff / 10;
        LocalDateTime dateTimeGetStart = null;
        LocalDateTime dateTimeGetEnd = null;

        if(iterInAnimation <= maxIters){
            LocalDateTime dateTime = LocalDateTime.from(startDateForAnimation);
            dateTime = dateTime.plusMinutes(iterInAnimation * 10L);
            dateTimeGetStart = dateTime.truncatedTo(ChronoUnit.HOURS).plusMinutes(5 * (dateTime.getMinute() / 5));
            dateTimeGetEnd = dateTimeGetStart.plusMinutes(5);
        }

        if(listOfTrainLocHistory != null){
            for(int i = 0; i < listOfTrainLocHistory.size(); i++) {
                TrainLocHistory trainLocHistory = listOfTrainLocHistory.get(i);
                if(trainLocHistory.getTimeOfRequest().isAfter(dateTimeGetStart) && trainLocHistory.getTimeOfRequest().isBefore(dateTimeGetEnd)){
                    listOfCurrentTrains.add(trainLocHistory);
                }
            }
        }

        TextureRegion textureTrain = gameplayAtlas.findRegion(RegionNames.TRAIN_ICON);

        for(int i = 0; i < listOfCurrentTrains.size(); i++){
            TrainLocHistory trainLocHistory = listOfCurrentTrains.get(i);
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

            TextButton.TextButtonStyle textButtonStyle = skin.get("default", TextButton.TextButtonStyle.class);
            textButtonStyle.font = game.getFontMedium();

            TextButton textButtonUpdate = new TextButton("Update", textButtonStyle);
            textButtonUpdate.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    displayUpdateWindow = !displayUpdateWindow;
                    drawUpdateData(trainLocHistory, marker.x - trainIconWidth / 2, marker.y);
                }
            });

            Window windowAboveTrain = new Window("", windowStyle);
            Label labelName = new Label(trainLocHistory.getTrainType() + " " + trainLocHistory.getTrainNumber(), labelStyle);
            Label labelNextStop = new Label("Next station: " + trainLocHistory.getNextStation(), labelStyle);
            Label labelDelay = new Label("Delay: " + trainLocHistory.getDelay() + "min", labelStyle);
            windowAboveTrain.add(labelName).row();
            windowAboveTrain.add(labelNextStop).row();
            windowAboveTrain.add(labelDelay).row();
            windowAboveTrain.add(textButtonUpdate);
            windowAboveTrain.sizeBy(400, 150);
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

    private boolean displayAddWindow = false;

    private void drawAddData(){
        TextButton.TextButtonStyle textButtonStyle = skin.get("default", TextButton.TextButtonStyle.class);
        textButtonStyle.font = game.getFontMedium();
        TextButton textButtonAdd = new TextButton("Add", textButtonStyle);
        textButtonAdd.setPosition(2400, 2400);
        textButtonAdd.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                displayAddWindow = !displayAddWindow;
                stage.clear();
                drawAnimationInput();
                drawAddData();
            }
        });
        stage.addActor(textButtonAdd);

        if(displayAddWindow){
            Window.WindowStyle windowStyle = skin.get("default", Window.WindowStyle.class);
            windowStyle.titleFont = game.getFontMediumBold();

            Label.LabelStyle labelStyle = skin.get("default", Label.LabelStyle.class);
            labelStyle.font = game.getFontMedium();

            TextField.TextFieldStyle textFieldStyle = skin.get("default", TextField.TextFieldStyle.class);
            textFieldStyle.font = game.getFontMedium();

            com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle listStyle = skin.get("default", com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle.class);
            listStyle.font = game.getFontMedium();
            SelectBox.SelectBoxStyle selectBoxStyle = skin.get("default", SelectBox.SelectBoxStyle.class);
            selectBoxStyle.font = game.getFontMedium();
            selectBoxStyle.listStyle.font = game.getFontMedium();

            Window windowAddData = new Window("", windowStyle);
            Label labelRequestTime = new Label("Time:", labelStyle);
            Label labelTrainType = new Label("Type:", labelStyle);
            Label labelNumber = new Label("Number:", labelStyle);
            Label labelNextStation = new Label("Next:", labelStyle);
            Label labelLat = new Label("Latitude:", labelStyle);
            Label labelLng = new Label("Longitude:", labelStyle);
            Label labelDelay = new Label("Delay:", labelStyle);
            Label labelRouteFrom = new Label("Route From:", labelStyle);
            Label labelRouteTo = new Label("Route To:", labelStyle);
            Label labelRouteStartTime = new Label("Route Time:", labelStyle);

            TextField textFieldRequestTime = new TextField(startDateString, textFieldStyle);
            TextField textFieldNumber = new TextField("", textFieldStyle);
            TextField textFieldNextStation = new TextField("", textFieldStyle);
            TextField textFieldLat = new TextField("", textFieldStyle);
            TextField textFieldLng = new TextField("", textFieldStyle);
            TextField textFieldDelay = new TextField("", textFieldStyle);
            TextField textFieldRouteFrom = new TextField("", textFieldStyle);
            TextField textFieldRouteTo = new TextField("", textFieldStyle);
            TextField textFieldRouteStartTime = new TextField("", textFieldStyle);
            SelectBox<String> selectBoxTrainType = new SelectBox<>(selectBoxStyle);
            selectBoxTrainType.setItems("LP", "LPV", "RG", "IC", "ICS", "MV");
            selectBoxTrainType.setSelected("LP");

            TextButton textButtonAddData = new TextButton("Add Data", textButtonStyle);
            textButtonAddData.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    TrainLocHistory trainLocHistory = new TrainLocHistory();

                    DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                    LocalDateTime reqDateTime = LocalDateTime.parse(textFieldRequestTime.getText(), ISO_FORMATTER);
                    trainLocHistory.setTimeOfRequest(reqDateTime);

                    trainLocHistory.setTrainType(selectBoxTrainType.getSelected());
                    trainLocHistory.setTrainNumber(textFieldNumber.getText());
                    trainLocHistory.setNextStation(textFieldNextStation.getText());
                    trainLocHistory.setDelay(Integer.parseInt(textFieldDelay.getText()));
                    trainLocHistory.setRouteFrom(textFieldRouteFrom.getText());
                    trainLocHistory.setRouteTo(textFieldRouteTo.getText());
                    trainLocHistory.setRouteStartTime(textFieldRouteStartTime.getText());
                    double lat = Double.parseDouble(textFieldLat.getText());
                    double lng = Double.parseDouble(textFieldLng.getText());
                    trainLocHistory.setCoordinates(new Coordinates(lat, lng));
                    Requests.insertTrainLocHistory(trainLocHistory);
                }
            });

            windowAddData.add(labelRequestTime).padRight(2f);
            windowAddData.add(textFieldRequestTime).expandX().fillX().row();

            windowAddData.add(labelTrainType).padRight(2f).padTop(5f);
            windowAddData.add(selectBoxTrainType).row();

            windowAddData.add(labelNumber).padRight(2f).padTop(5f);
            windowAddData.add(textFieldNumber).expandX().fillX().row();

            windowAddData.add(labelNextStation).padRight(2f).padTop(5f);
            windowAddData.add(textFieldNextStation).expandX().fillX().row();

            windowAddData.add(labelLat).padRight(2f).padTop(5f);
            windowAddData.add(textFieldLat).expandX().fillX().row();

            windowAddData.add(labelLng).padRight(2f).padTop(5f);
            windowAddData.add(textFieldLng).expandX().fillX().row();

            windowAddData.add(labelDelay).padRight(2f).padTop(5f);
            windowAddData.add(textFieldDelay).expandX().fillX().row();

            windowAddData.add(labelRouteFrom).padRight(2f).padTop(5f);
            windowAddData.add(textFieldRouteFrom).expandX().fillX().row();

            windowAddData.add(labelRouteTo).padRight(2f).padTop(5f);
            windowAddData.add(textFieldRouteTo).expandX().fillX().row();

            windowAddData.add(labelRouteStartTime).padRight(2f).padTop(5f);
            windowAddData.add(textFieldRouteStartTime).expandX().fillX().row();

            windowAddData.add(textButtonAddData).colspan(2).padTop(10f);

            windowAddData.sizeBy(600, 670);
            windowAddData.setPosition(1600, 1800);
            stage.addActor(windowAddData);
        }
    }

    private boolean displayUpdateWindow = false;

    private void drawUpdateData(TrainLocHistory trainLocHistoryPassed, float x, float y){
        if (displayUpdateWindow){
            Window.WindowStyle windowStyle = skin.get("default", Window.WindowStyle.class);
            windowStyle.titleFont = game.getFontMediumBold();

            Label.LabelStyle labelStyle = skin.get("default", Label.LabelStyle.class);
            labelStyle.font = game.getFontMedium();

            TextField.TextFieldStyle textFieldStyle = skin.get("default", TextField.TextFieldStyle.class);
            textFieldStyle.font = game.getFontMedium();

            TextButton.TextButtonStyle textButtonStyle = skin.get("default", TextButton.TextButtonStyle.class);
            textButtonStyle.font = game.getFontMedium();

            com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle listStyle = skin.get("default", com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle.class);
            listStyle.font = game.getFontMedium();
            SelectBox.SelectBoxStyle selectBoxStyle = skin.get("default", SelectBox.SelectBoxStyle.class);
            selectBoxStyle.font = game.getFontMedium();
            selectBoxStyle.listStyle.font = game.getFontMedium();
            DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            Window windowUpdateData = new Window("", windowStyle);
            Label labelRequestTime = new Label("Time:", labelStyle);
            Label labelTrainType = new Label("Type:", labelStyle);
            Label labelNumber = new Label("Number:", labelStyle);
            Label labelNextStation = new Label("Next:", labelStyle);
            Label labelLat = new Label("Latitude:", labelStyle);
            Label labelLng = new Label("Longitude:", labelStyle);
            Label labelDelay = new Label("Delay:", labelStyle);
            Label labelRouteFrom = new Label("Route From:", labelStyle);
            Label labelRouteTo = new Label("Route To:", labelStyle);
            Label labelRouteStartTime = new Label("Route Time:", labelStyle);

            TextField textFieldRequestTime = new TextField(ISO_FORMATTER.format(trainLocHistoryPassed.getTimeOfRequest()), textFieldStyle);
            TextField textFieldNumber = new TextField(trainLocHistoryPassed.getTrainNumber(), textFieldStyle);
            TextField textFieldNextStation = new TextField(trainLocHistoryPassed.getNextStation(), textFieldStyle);
            TextField textFieldLat = new TextField(String.valueOf(trainLocHistoryPassed.getCoordinates().getLat()), textFieldStyle);
            TextField textFieldLng = new TextField(String.valueOf(trainLocHistoryPassed.getCoordinates().getLng()), textFieldStyle);
            TextField textFieldDelay = new TextField(String.valueOf(trainLocHistoryPassed.getDelay()), textFieldStyle);
            TextField textFieldRouteFrom = new TextField(trainLocHistoryPassed.getRouteFrom(), textFieldStyle);
            TextField textFieldRouteTo = new TextField(trainLocHistoryPassed.getRouteTo(), textFieldStyle);
            TextField textFieldRouteStartTime = new TextField(trainLocHistoryPassed.getRouteStartTime(), textFieldStyle);
            SelectBox<String> selectBoxTrainType = new SelectBox<>(selectBoxStyle);
            selectBoxTrainType.setItems("LP", "LPV", "RG", "IC", "ICS", "MV");
            selectBoxTrainType.setSelected("LP");

            TextButton textButtonAddData = new TextButton("Update Data", textButtonStyle);
            textButtonAddData.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    TrainLocHistory trainLocHistory = new TrainLocHistory();


                    LocalDateTime reqDateTime = LocalDateTime.parse(textFieldRequestTime.getText(), ISO_FORMATTER);
                    trainLocHistory.setTimeOfRequest(reqDateTime);

                    trainLocHistory.setTrainType(selectBoxTrainType.getSelected());
                    trainLocHistory.setTrainNumber(textFieldNumber.getText());
                    trainLocHistory.setNextStation(textFieldNextStation.getText());
                    trainLocHistory.setDelay(Integer.parseInt(textFieldDelay.getText()));
                    trainLocHistory.setRouteFrom(textFieldRouteFrom.getText());
                    trainLocHistory.setRouteTo(textFieldRouteTo.getText());
                    trainLocHistory.setRouteStartTime(textFieldRouteStartTime.getText());
                    double lat = Double.parseDouble(textFieldLat.getText());
                    double lng = Double.parseDouble(textFieldLng.getText());
                    trainLocHistory.setCoordinates(new Coordinates(lat, lng));
                    Requests.updateTrainLocHistory(trainLocHistoryPassed, trainLocHistory);
                    windowUpdateData.remove();
                }
            });

            TextButton textButtonCancel = new TextButton("Cancel", textButtonStyle);
            textButtonCancel.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    windowUpdateData.remove();
                }
            });

            windowUpdateData.add(labelRequestTime).padRight(2f);
            windowUpdateData.add(textFieldRequestTime).expandX().fillX().row();

            windowUpdateData.add(labelTrainType).padRight(2f).padTop(5f);
            windowUpdateData.add(selectBoxTrainType).row();

            windowUpdateData.add(labelNumber).padRight(2f).padTop(5f);
            windowUpdateData.add(textFieldNumber).expandX().fillX().row();

            windowUpdateData.add(labelNextStation).padRight(2f).padTop(5f);
            windowUpdateData.add(textFieldNextStation).expandX().fillX().row();

            windowUpdateData.add(labelLat).padRight(2f).padTop(5f);
            windowUpdateData.add(textFieldLat).expandX().fillX().row();

            windowUpdateData.add(labelLng).padRight(2f).padTop(5f);
            windowUpdateData.add(textFieldLng).expandX().fillX().row();

            windowUpdateData.add(labelDelay).padRight(2f).padTop(5f);
            windowUpdateData.add(textFieldDelay).expandX().fillX().row();

            windowUpdateData.add(labelRouteFrom).padRight(2f).padTop(5f);
            windowUpdateData.add(textFieldRouteFrom).expandX().fillX().row();

            windowUpdateData.add(labelRouteTo).padRight(2f).padTop(5f);
            windowUpdateData.add(textFieldRouteTo).expandX().fillX().row();

            windowUpdateData.add(labelRouteStartTime).padRight(2f).padTop(5f);
            windowUpdateData.add(textFieldRouteStartTime).expandX().fillX().row();

            windowUpdateData.add(textButtonCancel).padTop(10f);
            windowUpdateData.add(textButtonAddData).padTop(10f);

            windowUpdateData.sizeBy(600, 670);
            windowUpdateData.setPosition(x-600, y-600);
            stage.addActor(windowUpdateData);
        }
    }

    private float getCurrentTime(){
        return TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f;
    }
}
