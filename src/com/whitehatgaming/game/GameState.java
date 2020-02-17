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

    public GameState copy() {
        return new GameState(playerColor, boardState);
    }

    public boolean isEnded() {
        return boardState == BoardState.CHECKMATE || boardState == BoardState.STALEMATE;
    }

    public void notifyMove(Move move, Board board) {
        playerColor = playerColor.opponent();

        evaluateBoardState(board);
    }

    public Color currentPlayerColor() {
        return playerColor;
    }

    private void evaluateBoardState(Board board) {
        if (isCheck(playerColor, board)) {
            boardState = BoardState.CHECK;
        }

        if (isCheckMate(playerColor, board)) {
            boardState = BoardState.CHECKMATE;
        }

        if (isStaleMate(playerColor, board)) {
            boardState = BoardState.STALEMATE;
        }

        boardState = BoardState.STANDARD;
    }
}
