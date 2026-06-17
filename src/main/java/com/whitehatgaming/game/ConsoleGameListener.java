package com.whitehatgaming.game;

import com.whitehatgaming.pieces.Color;

/**
 * Renders a game to the console. This is the only place in the game flow that
 * writes to {@code System.out}; the engine itself is silent.
 */
public class ConsoleGameListener implements GameListener {

    private final ConsoleBoardDisplayer displayer = new ConsoleBoardDisplayer();

    @Override
    public void gameStarted(Board board) {
        System.out.println("        -=- START -=-");
        displayer.displayBoard(board);
    }

    @Override
    public void movePlayed(int moveNumber, Color mover, BoardState resultingState, Board board) {
        System.out.println("-====-====- Move:" + moveNumber + " (" + mover + ") -====-====-");
        if (resultingState == BoardState.CHECK) {
            System.out.println("        -=- CHECK -=-");
        }
        displayer.displayBoard(board);
    }

    @Override
    public void gameEnded(GameResult result) {
        switch (result.finalState()) {
            case CHECKMATE -> System.out.println("        -=- CHECKMATE - " + result.winner() + " wins -=-");
            case STALEMATE -> System.out.println("        -=- STALEMATE - draw -=-");
            default -> { /* game did not reach a terminal state */ }
        }
    }
}
