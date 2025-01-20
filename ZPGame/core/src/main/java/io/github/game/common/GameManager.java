package io.github.game.common;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

import io.github.game.screens.GameScreen;

public class GameManager {

    public static final GameManager INSTANCE = new GameManager();
    private static final String SCORES_FILE = "scores.json";
    private long score;
    Array<ScoreEntry> highScores = new Array<>();
    private float timeMultiplier = 1.0f;
    private static final float BASE_SCORE_RATE = 0.01f; // Points per second
    private static final float MULTIPLIER_INCREASE_RATE = 0.000001f; // How fast multiplier increases
    private static final float MAX_MULTIPLIER = 3.0f;

    private static float SCORE_UPDATE_INTERVAL = 1.0f;
    private float timeSinceLastScoreUpdate = 0.0f;

    public static class ScoreEntry {
        public String name;
        public long score;
        public long date;


        public ScoreEntry(String name, int score) {
            this.name = name;
            this.score = score;
            this.date = System.currentTimeMillis();
        }
    }


    private GameManager() {
        highScores = new Array<>();
        loadScores();
        resetScore();
    }

    public void resetScore() {
        score = 0;
        timeMultiplier = 1.0f;
    }

    public void updateScore(float delta, GameScreen.DifficultyLevel currentDifficulty) {

        if (currentDifficulty == GameScreen.DifficultyLevel.NORMAL)
            SCORE_UPDATE_INTERVAL = 0.1f;
        if (currentDifficulty == GameScreen.DifficultyLevel.HARD)
            SCORE_UPDATE_INTERVAL = 0.01f;

        timeSinceLastScoreUpdate += delta;

        if (timeSinceLastScoreUpdate >= SCORE_UPDATE_INTERVAL) {
            timeMultiplier = Math.min(timeMultiplier + (MULTIPLIER_INCREASE_RATE * delta), MAX_MULTIPLIER);


            // long pointsToAdd = (long)(BASE_SCORE_RATE * timeMultiplier * delta);
            long pointsToAdd = Math.max(1, (long) (BASE_SCORE_RATE * timeMultiplier * delta * 0.0001f));
            score += pointsToAdd;
            timeSinceLastScoreUpdate = 0.0f;
            //System.out.println("Updated score: " + score + ", Multiplier: " + timeMultiplier);
        }
    }

    public void trainDelivered(GameScreen.DifficultyLevel difficultyLevel, boolean isJokerType) {
        int multiplier = 1;
        if(isJokerType){
            multiplier = 2;
        }
        if (difficultyLevel == GameScreen.DifficultyLevel.EASY)
            score += (multiplier * 100);
        if (difficultyLevel == GameScreen.DifficultyLevel.NORMAL)
            score += (multiplier * 500);
        if (difficultyLevel == GameScreen.DifficultyLevel.HARD)
            score += (multiplier * 1000);
    }

    public void trainCrash(GameScreen.DifficultyLevel difficultyLevel) {

        if (difficultyLevel == GameScreen.DifficultyLevel.EASY)
            score -= 100;
        if (difficultyLevel == GameScreen.DifficultyLevel.NORMAL)
            score -= 500;
        if (difficultyLevel == GameScreen.DifficultyLevel.HARD)
            score -= 1000;
    }

    public long getScore() {
        return score;
    }

    public float getMultiplier() {
        return timeMultiplier;
    }


    private void loadScores() {
        try {
            FileHandle file = Gdx.files.local(SCORES_FILE);
            if (file.exists()) {
                Json json = new Json();
                @SuppressWarnings("unchecked")//Ker se podatek o tipu izgubi, bi brez tega prrevajalnik vrnil opozorilo
                Array<ScoreEntry> loadedScores = json.fromJson(Array.class, ScoreEntry.class, file.readString());
                highScores = loadedScores;//ScoreEntry.class zagotovi, da se JSON polja ujemajo s strukturami razreda ScoreEntry
            }
        } catch (Exception e) {
            Gdx.app.error("GameManager", "Error loading scores", e);
        }
    }

    private void saveScores() {
        try {
            FileHandle file = Gdx.files.local(SCORES_FILE);
            Json json = new Json();
            String scoresJson = json.toJson(highScores);
            file.writeString(json.prettyPrint(scoresJson), false);
        } catch (Exception e) {
            Gdx.app.error("GameManager", "Error saving scores", e);
        }
    }

    public Array<ScoreEntry> getHighScores() {
        return new Array<>(highScores);
    }


}
