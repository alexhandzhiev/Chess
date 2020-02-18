package com.whitehatgaming.game;

import static com.whitehatgaming.game.BoardEvaluator.*;

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

    private GameState(Color playerColor, BoardState state) {
        this.playerColor = playerColor;
        this.boardState = state;
    }

    public Color getPlayerColor() {
        return playerColor;
    }

    public GameState copy() {
        return new GameState(playerColor, boardState);
    }

    public void notifyMove(Move move, Board board) {
        playerColor = playerColor.opponent();

        evaluateBoardState(board);
    }

    private void evaluateBoardState(Board board) {
        if (isCheck(playerColor, board)) {
            boardState = BoardState.CHECK;
        } else {
            boardState = BoardState.STANDARD;
        }
    }
}
