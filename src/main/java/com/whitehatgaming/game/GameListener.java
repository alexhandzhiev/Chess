package com.whitehatgaming.game;

import com.whitehatgaming.pieces.Color;

/**
 * Hooks the engine calls as a game progresses, so rendering (or any other
 * side effect) lives outside {@link Game}. All methods are no-ops by default.
 */
public interface GameListener {

    GameListener NONE = new GameListener() {
    };

    default void gameStarted(Board board) {
    }

    default void movePlayed(int moveNumber, Color mover, BoardState resultingState, Board board) {
    }

    default void gameEnded(GameResult result) {
    }
}
