package com.whitehatgaming.game;

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
    private final List<Color> players;

    public Game() {
        this.board = new Board();
        this.state = new GameState(Color.WHITE);
        this.initializer = new BoardInitializer();
        boardDisplayer = new ConsoleBoardDisplayer();
        moveHistory = new Stack<>();
        stateHistory = new Stack<>();

        players = new ArrayList<>();
        players.add(Color.WHITE);
        players.add(Color.BLACK);

        initializer.init(board);
    }

    public void start(LinkedHashMap<Square, Square> moves) {
        for(Map.Entry<Square, Square> entry : moves.entrySet()) {
//            System.out.println("Src: " + entry.getKey().getCol() + " " + entry.getKey().getRow());
//            System.out.println("Dst: " + entry.getValue().getCol() + " " + entry.getValue().getRow());

            Piece piece = board.at(entry.getKey());
            System.out.println("Coords: " + entry.getKey().getRow() + " " + entry.getKey().getCol());
            System.out.println("Pc: " + board.at(entry.getKey()));

//            List<Move> legalMoves = piece.availableMoves(entry.getKey(), board);
//            System.out.println("Mv: " + legalMoves.get(0));
//            for (Move move : legalMoves) {
//                if (move.equals(entry.getKey(), entry.getValue())) {
//                    executeMove(move);
//                    break;
//                }
//            }
        }
//        System.out.println(legalMoves);
//        for(Map.Entry<Square, Square> entry : moves.entrySet()) {

//            executeMove();
//        }
        for (Square sq : board.allSquares()) {
            List<Move> legalMoves = board.at(sq).availableMoves(sq, board);
            System.out.println("Pc: " + board.at(sq) + " " + sq.getRow() + " " + sq.getCol() + " " + legalMoves);
        }

        boardDisplayer.displayBoard(board);

    }

    public void undo() {
        moveHistory.peek().undo();
        moveHistory.pop();

        state = stateHistory.pop();
    }

    public void executeMove(Move move) {
        move.execute();

        moveHistory.add(move);
        stateHistory.add(state.copy());

        state.notifyMove(move, board);
    }
}
