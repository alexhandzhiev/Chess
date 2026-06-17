package com.whitehatgaming.game;

import com.whitehatgaming.moves.Move;
import com.whitehatgaming.pieces.Color;

public class GameState {

    private Color playerColor;

    public BoardState getBoardState() {
        return boardState;
    }

    private BoardState boardState;

    public GameState(Color playerColor) {
        boardState = BoardState.STANDARD;
        this.playerColor = playerColor;
    }

    public Color getPlayerColor() {
        return playerColor;
    }

    public void notifyMove(Move move, Board board) {
        playerColor = playerColor.opponent();

        evaluateBoardState(board);
    }

    private void evaluateBoardState(Board board) {
        boardState = BoardEvaluator.evaluate(playerColor, board);
    }
}
