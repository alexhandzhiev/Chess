package com.whitehatgaming.game;

public enum BoardState {
    STANDARD, CHECK, CHECKMATE, STALEMATE;

    public boolean isGameOver() {
        return this == CHECKMATE || this == STALEMATE;
    }
}
