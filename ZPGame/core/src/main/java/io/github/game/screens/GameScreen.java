package io.github.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
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

import java.util.Iterator;

import io.github.game.PendingTrain;
import io.github.game.RailwayPath;
import io.github.game.Score;
import io.github.game.Train;
import io.github.game.Waypoints;
import io.github.game.assets.RegionNames;
import io.github.game.common.GameManager;


public class GameScreen implements Screen {

    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;

    private FitViewport viewport;

    private Waypoints waypoints;
    private TextureAtlas gameplayAtlas;

    private Array<Train> trainArray;
    Pool<Train> trainPool = Pools.get(Train.class, 10);
    private final Array<PendingTrain> pendingTrains = new Array<>();

    private SpriteBatch batch;
    private Sound trainParked;
    private Sound explosion;
    private Sound trainCollected;
    private Score score;
    private float spawnTimer = 0;
    private final Array<TrainSpawnConfig> spawnConfigs = new Array<>();
    private float currentSpawnInterval = 3f;
    private DifficultyLevel currentDifficulty = DifficultyLevel.EASY;


    private boolean debug = true;

    public enum DifficultyLevel {
        EASY(3f),
        NORMAL(1.5f),
        HARD(1.5f);

        final float baseSpawnInterval;

        DifficultyLevel(float baseSpawnInterval) {
            this.baseSpawnInterval = baseSpawnInterval;
        }
    }

    public static class TrainSpawnConfig {
        public final String pathId;
        public final boolean isReversed;

        TrainSpawnConfig(String pathId, boolean isReversed) {
            this.pathId = pathId;
            this.isReversed = isReversed;
        }

    }

    public GameScreen(FitViewport viewport, OrthographicCamera camera, TextureAtlas gameplayAtlas, SpriteBatch batch) {
        this.viewport = viewport;
        this.camera = camera;
        this.batch = batch;
        shapeRenderer = new ShapeRenderer();
        waypoints = new Waypoints(shapeRenderer);

        score = new Score(gameplayAtlas);
        trainArray = new Array<>();
        trainPool.fill(2);
        this.gameplayAtlas = gameplayAtlas;
        initializeSpawnConfigs();
        // spawnPendingTrain();
        spawnTestTrain();
        trainCollected = Gdx.audio.newSound(Gdx.files.internal("sounds/collected.mp3"));
        explosion = Gdx.audio.newSound(Gdx.files.internal("sounds/explosion.mp3"));
        setDifficulty(DifficultyLevel.EASY);

    }

    private void spawnTestTrain() {
//        Train trainReverse = trainPool.obtain();
//        trainReverse.texture = gameplayAtlas.findRegion(RegionNames.TRAIN);
//        RailwayPath pathReverse = waypoints.getPathById("24-25");
//        trainReverse.setPath(pathReverse, true);
//
//        if (trainReverse.texture != null) {
//            trainReverse.bounds.width = trainReverse.texture.getRegionWidth() / 13f;
//            trainReverse.bounds.height = trainReverse.texture.getRegionHeight() / 13f;
//        }
//        //Vector2 startPosReverse = pathReverse.getStart();
//        //trainReverse.position.x = startPosReverse.x;
//        //trainReverse.position.y = startPosReverse.y;
//        trainReverse.speed = 50f;
//        trainArray.add(trainReverse);
    }


    private void initializeSpawnConfigs() {
        spawnConfigs.add(new TrainSpawnConfig("0-1", false));
        spawnConfigs.add(new TrainSpawnConfig("4-6", true));
        spawnConfigs.add(new TrainSpawnConfig("4-7", true));
        spawnConfigs.add(new TrainSpawnConfig("8-22", true));
        spawnConfigs.add(new TrainSpawnConfig("10-12", true));
        spawnConfigs.add(new TrainSpawnConfig("10-13", true));
        spawnConfigs.add(new TrainSpawnConfig("15-16", true));
        spawnConfigs.add(new TrainSpawnConfig("20-23", true));
        spawnConfigs.add(new TrainSpawnConfig("24-25", true));
    }

    public void setDifficulty(DifficultyLevel difficulty) {
        this.currentDifficulty = difficulty;
        this.currentSpawnInterval = difficulty.baseSpawnInterval;
    }

    private void spawnPendingTrain() {
        if (currentDifficulty == DifficultyLevel.HARD) {
            if (trainArray.size >= 30) return;
        } else {
            if (trainArray.size >= 20) return;
        }


        TrainSpawnConfig config = spawnConfigs.get(MathUtils.random(spawnConfigs.size - 1));
        float cooldown = MathUtils.random(3f, 5f);
        //float spawnTime = spawnTimer + cooldown;
        float spawnTime = System.currentTimeMillis() / 1000f + cooldown;


        pendingTrains.add(new PendingTrain(config, cooldown));
        System.out.println("Scheduled train for path " + config.pathId + " with cooldown: " + cooldown + " seconds.");
    }


    private void spawnTrain(PendingTrain pending) {
        Train newTrain = trainPool.obtain();
        newTrain.texture = gameplayAtlas.findRegion(RegionNames.TRAIN);

        RailwayPath initialPath = waypoints.getPathById(pending.config.pathId);
        newTrain.setPath(initialPath, pending.config.isReversed);

        if (newTrain.texture != null) {
            newTrain.bounds.width = newTrain.texture.getRegionWidth() / 13f;
            newTrain.bounds.height = newTrain.texture.getRegionHeight() / 13f;
        }

        float baseSpeed = 50f;
        float speedMultiplier;
        switch (currentDifficulty) {
            case EASY:
                speedMultiplier = MathUtils.random(0.8f, 1.2f);
                break;
            case HARD:
                speedMultiplier = MathUtils.random(1.3f, 2.0f);
                break;
            case NORMAL:
            default:
                speedMultiplier = MathUtils.random(1.0f, 1.5f);
                break;
        }

        newTrain.speed = baseSpeed * speedMultiplier;
        trainArray.add(newTrain);
        System.out.println("Train spawned on path " + pending.config.pathId);
    }


    private boolean isClickNearTrain(Train train, int screenX, int screenY) {
        Vector3 worldClick = new Vector3(screenX, screenY, 0);
        camera.unproject(worldClick,
                viewport.getScreenX(),
                viewport.getScreenY(),
                viewport.getScreenWidth(),
                viewport.getScreenHeight()
        );

        float trainCenterX = train.position.x + train.bounds.width / 2;
        float trainCenterY = train.position.y + train.bounds.height / 2;

        Vector3 trainScreenPos = new Vector3(trainCenterX, trainCenterY, 0);
        camera.project(trainScreenPos,
                viewport.getScreenX(),
                viewport.getScreenY(),
                viewport.getScreenWidth(),
                viewport.getScreenHeight()
        );

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

        return distance <= scaledRadius;
    }


    @Override
    public void show() {

        shapeRenderer = new ShapeRenderer();
        waypoints = new Waypoints(shapeRenderer);

    }


    private void checkTrainCollisions() {
        for (int i = 0; i < trainArray.size; i++) {
            Train trainA = trainArray.get(i);


            for (int j = i + 1; j < trainArray.size; j++) {
                Train trainB = trainArray.get(j);


                if (trainA.bounds.overlaps(trainB.bounds)) {

                    trainArray.removeValue(trainA, true);
                    trainArray.removeValue(trainB, true);
                    trainPool.free(trainA);
                    trainPool.free(trainB);
                    explosion.play();
                    GameManager.INSTANCE.trainCrash(currentDifficulty);
                    System.out.println(currentDifficulty);
                    break;
                }
            }
        }
    }


    @Override
    public void render(float delta) {
        if (GameManager.INSTANCE.getScore() > 2000 && currentDifficulty == DifficultyLevel.EASY) {
            setDifficulty(DifficultyLevel.NORMAL);
        } else if (GameManager.INSTANCE.getScore() > 6000 && currentDifficulty == DifficultyLevel.NORMAL) {
            setDifficulty(DifficultyLevel.NORMAL);
        }
        ScreenUtils.clear(0, 0, 0, 1);
        handleInput();
        viewport.apply();
        camera.update();

        GameManager.INSTANCE.updateScore(delta, currentDifficulty);


        spawnTimer += delta;

        if (spawnTimer >= currentDifficulty.baseSpawnInterval) {
            spawnPendingTrain();
            float randomFactor = MathUtils.random(0.8f, 1.2f);
            currentSpawnInterval = currentDifficulty.baseSpawnInterval * randomFactor;

            spawnTimer = 0;
        }


        for (Iterator<PendingTrain> iterator = pendingTrains.iterator(); iterator.hasNext(); ) {
            PendingTrain pending = iterator.next();
            pending.remainingTime -= delta;

            if (pending.remainingTime <= 0) {
                spawnTrain(pending);
                iterator.remove();
            }
        }

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

        checkTrainCollisions();

        for (Train train : trainArray) {
            train.update(delta, waypoints);


            if (train.shouldRemoveTrain(train.getCurrentPathId(), train.isReversed(), spawnConfigs)) {

                trainArray.removeValue(train, true);
                trainPool.free(train);
                GameManager.INSTANCE.trainDelivered(currentDifficulty);
                trainCollected.play();
            }
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

        Array<GameScreen.TrainSpawnConfig> confs = new Array<>();
        for (PendingTrain train : pendingTrains) {
            if (train.remainingTime > 0) {
                confs.add(new TrainSpawnConfig(train.config.pathId, train.config.isReversed));
            }
        }

        for (GameScreen.TrainSpawnConfig config : spawnConfigs) {
            drawSemaphore(config, confs);
        }


        score.draw(camera, batch, viewport);

    }

    public void drawSemaphore(TrainSpawnConfig config, Array<TrainSpawnConfig> confs) {

        RailwayPath path = waypoints.getPathById(config.pathId);
        if (path == null || path.getSize() < 2) {
            System.out.println("Path not valid or too short to draw semaphore.");
            return;
        }

        Vector2 startPoint = config.isReversed
                ? path.getPoint(path.getSize() - 2)
                : path.getPoint(1);
        Vector2 nextPoint = config.isReversed
                ? path.getPoint(path.getSize() - 3)
                : path.getPoint(2);


        float dx = nextPoint.x - startPoint.x;
        float dy = nextPoint.y - startPoint.y;


        float length = (float) Math.sqrt(dx * dx + dy * dy);
        dx /= length;
        dy /= length;


        float offsetX = -dy * 30;
        float offsetY = dx * 30;


        float semaphoreX = startPoint.x + offsetX;
        float semaphoreY = startPoint.y + offsetY;

        boolean isInConfs = false;
        for (TrainSpawnConfig conf : confs) {
            if (conf.pathId.equals(config.pathId) && conf.isReversed == config.isReversed) {
                isInConfs = true;
                break;
            }
        }

        float radius = 10;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(isInConfs ? Color.RED : Color.GREEN);
        shapeRenderer.circle(semaphoreX, semaphoreY, radius);
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

