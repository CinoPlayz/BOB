package io.github.game;

import io.github.game.screens.GameScreen;

public class PendingTrain {
    public GameScreen.TrainSpawnConfig config;
    public float remainingTime;


    public PendingTrain(GameScreen.TrainSpawnConfig config, float cooldown) {
        this.config = config;
        this.remainingTime = cooldown;
    }



}
