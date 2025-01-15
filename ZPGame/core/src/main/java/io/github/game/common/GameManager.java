package io.github.game.common;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

public class GameManager {

    public static final GameManager INSTANCE = new GameManager();
    private static final String SCORES_FILE = "scores.json";
    private Array<ScoreEntry> highScores;
    private int result;
    private int lives;

    private int score;


    public static class ScoreEntry {
        public String name;
        public int score;
        public long date;

        public ScoreEntry() {
        } //  za JSON

        public ScoreEntry(String name, int score) {
            this.name = name;
            this.score = score;
            this.date = System.currentTimeMillis();
        }
    }

    private GameManager() {
        highScores = new Array<>();
        loadScores();
    }

    public int getResult() {
        return result;
    }

    public void resetResult() {
        result = 0;
        lives = 3;
    }

    public int getScore() {
        return score;
    }

    public boolean isGameOver() {
        return lives <= 0;
    }

    public int getLives() {
        return lives;
    }


    public void addScore(String playerName, int score) {
        highScores.add(new ScoreEntry(playerName, score));
        // sortirano po score-u padajoče
        highScores.sort((o1, o2) -> o2.score - o1.score);
        // Obdrži samo top 10 rezultatov
        if (highScores.size > 10) {
            highScores.truncate(10);
        }
        saveScores();
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
            file.writeString(json.prettyPrint(scoresJson), false);//false: datoteka se prepiše
        } catch (Exception e) {
            Gdx.app.error("GameManager", "Error saving scores", e);
        }
    }

    public Array<ScoreEntry> getHighScores() {
        return new Array<>(highScores);
    }


}
