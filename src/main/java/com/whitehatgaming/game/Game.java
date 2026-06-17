package com.whitehatgaming.game;

import static com.whitehatgaming.game.BoardEvaluator.legalMoves;

import com.whitehatgaming.exceptions.InvalidMovementException;
import com.whitehatgaming.moves.Move;
import com.whitehatgaming.pieces.Color;
import com.whitehatgaming.pieces.Piece;

import java.util.List;

public class Game {

    private final GameState state;
    private final Board board;
    private final GameListener listener;

    public Game() {
        this(GameListener.NONE);
    }

    public Game(GameListener listener) {
        this.board = new Board();
        this.state = new GameState(Color.WHITE);
        BoardInitializer initializer = new BoardInitializer();
        this.listener = listener;

        initializer.init(board);
    }

    public GameResult start(List<MoveCommand> moves) throws InvalidMovementException {
        listener.gameStarted(board);

        int movesPlayed = 0;
        for (MoveCommand command : moves) {
            if (state.getBoardState().isGameOver()) {
                break;
            }

            Color mover = state.getPlayerColor();
            Move move = findLegalMove(command);
            if (move == null) {
                throw new InvalidMovementException(describeIllegal(command));
            }

            executeMove(move);
            movesPlayed++;
            listener.movePlayed(movesPlayed, mover, state.getBoardState(), board);
        }

        GameResult result = new GameResult(state.getBoardState(), winner(), movesPlayed);
        listener.gameEnded(result);
        return result;
    }

    public BoardState getBoardState() {
        return state.getBoardState();
    }

    public Piece pieceAt(Square square) {
        return board.at(square);
    }

    private Color winner() {
        // The side to move is the one that is mated, so the winner is its opponent.
        return state.getBoardState() == BoardState.CHECKMATE ? state.getPlayerColor().opponent() : null;
    }

    private Move findLegalMove(MoveCommand command) {
        for (Move move : legalMoves(state.getPlayerColor(), board)) {
            if (move.matches(command.source(), command.destination())) {
                return move;
            }
        }

        return null;
    }

    private void executeMove(Move move) {
        move.execute();
        state.notifyMove(move, board);
    }

    private String describeIllegal(MoveCommand command) {
        Piece piece = board.at(command.source());
        String mover = piece == null ? "empty square" : piece.name();

        return "Invalid move: " + mover + " " + algebraic(command.source()) + " -> " + algebraic(command.destination());
    }

    private static String algebraic(Square square) {
        char file = (char) ('a' + square.col());
        int rank = Board.SIZE - square.row();
        return String.valueOf(file) + rank;
    }
}
