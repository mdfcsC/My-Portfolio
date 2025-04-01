package edu.uob.executor;

import edu.uob.GameState;

public abstract class CommandExecutor {
    protected GameState gameState;

    public CommandExecutor(GameState gameState) {
        this.gameState = gameState;
    }
}
