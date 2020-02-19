package com.whitehatgaming.generators;

import static com.whitehatgaming.generators.MoveGeneratorHelper.addMoveIfEmptyOrOpponent;

import com.whitehatgaming.game.Board;
import com.whitehatgaming.moves.Move;
import com.whitehatgaming.game.Square;
import com.whitehatgaming.pieces.Color;

import java.util.ArrayList;
import java.util.List;

public class KnightMoveGenerator implements MoveGenerator {

    @Override
    public List<Move> generateMoves(Square square, Color color, Board board) {
        List<Move> moves = new ArrayList<>();

        addMoveIfEmptyOrOpponent(square, Square.atOffset(square, 2, 1), color, moves, board);
        addMoveIfEmptyOrOpponent(square, Square.atOffset(square, 2, -1), color, moves, board);
        addMoveIfEmptyOrOpponent(square, Square.atOffset(square, -2, 1), color, moves, board);
        addMoveIfEmptyOrOpponent(square, Square.atOffset(square, -2, -1), color, moves, board);
        addMoveIfEmptyOrOpponent(square, Square.atOffset(square, 1, 2), color, moves, board);
        addMoveIfEmptyOrOpponent(square, Square.atOffset(square, 1, -2), color, moves, board);
        addMoveIfEmptyOrOpponent(square, Square.atOffset(square, -1, 2), color, moves, board);
        addMoveIfEmptyOrOpponent(square, Square.atOffset(square, -1, -2), color, moves, board);

        return moves;
    }
}
