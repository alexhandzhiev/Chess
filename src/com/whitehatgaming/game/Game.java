package com.whitehatgaming.game;

import com.whitehatgaming.exceptions.InvalidMovementException;
import com.whitehatgaming.moves.Move;
import com.whitehatgaming.pieces.Color;
import com.whitehatgaming.pieces.Piece;

import java.util.*;

public class Game {

    private GameState state;
    private final Board board;

    private final Stack<Move> moveHistory;
    private final Stack<GameState> stateHistory;
    private final BoardInitializer initializer;
    private final ConsoleBoardDisplayer boardDisplayer;

    public Game() {
        this.board = new Board();
        this.state = new GameState(Color.WHITE);
        this.initializer = new BoardInitializer();
        boardDisplayer = new ConsoleBoardDisplayer();
        moveHistory = new Stack<>();
        stateHistory = new Stack<>();

        initializer.init(board);
    }

    public void start(LinkedHashMap<Square, Square> moves) throws InvalidMovementException {
        int currentMove = 1;
        System.out.println("        -=- START -=-");

        for(Map.Entry<Square, Square> entry : moves.entrySet()) {
            System.out.println("-====-====- Move:" + currentMove++ + " -====-====-");

            String unicodeMessage =
                    "\u2654 " + // white king
                            "\u2655 " + // white queen
                            "\u2656 " + // white rook
                            "\u2657 " + // white bishop
                            "\u2658 " + // white knight
                            "\u2659 " + // white pawn
                            "\n" +
                            "\u265A " + // black king
                            "\u265B " + // black queen
                            "\u265C " + // black rook
                            "\u265D " + // black bishop
                            "\u265E " + // black knight
                            "\u265F " + // black pawn
                            "\n" ;
            System.out.println(unicodeMessage);
            Piece piece = board.at(entry.getKey());
            List<Move> legalMoves = piece.availableMoves(entry.getKey(), board);

            if(!legalMoves.isEmpty()) {
                for (Move move : legalMoves) {
                    if (move.equals(entry.getKey(), entry.getValue())) {
                        executeMove(move);
                        break;
                    }
                }
            } else {
                String msg = board.at(entry.getKey()) +
                        " [" + entry.getKey().getCol() +","+ entry.getKey().getRow() +
                        "] -> [" + entry.getValue().getCol() + "," + entry.getValue().getRow() + "]";
                throw new InvalidMovementException(msg);
            }

            //TODO: less displaying in the engine - more in the displayer
            if(BoardState.CHECK.name().equalsIgnoreCase(state.getBoardState().name())) {
                System.out.println( "        -=- " + state.getBoardState().name() + " -=-");
            }

            boardDisplayer.displayBoard(board);
        }
    }

    public void executeMove(Move move) {
        move.execute();

        moveHistory.add(move);
        stateHistory.add(state.copy());

        state.notifyMove(move, board);
    }
}
