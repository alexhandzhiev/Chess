package com.whitehatgaming.generators;

import static com.whitehatgaming.generators.MoveGeneratorHelper.addVectorIfEmptyOrOpponent;

import com.whitehatgaming.game.Board;
import com.whitehatgaming.moves.Move;
import com.whitehatgaming.game.Square;
import com.whitehatgaming.pieces.Color;

import java.util.ArrayList;
import java.util.List;

public class RookMoveGenerator implements MoveGenerator {
    @Override
    public List<Move> generateMoves(Square square, Color color, Board board) {
        List<Move> moves = new ArrayList<>();

        moves.addAll(addVectorIfEmptyOrOpponent(square, 1, 0, color, board));
        moves.addAll(addVectorIfEmptyOrOpponent(square, -1, 0, color, board));
        moves.addAll(addVectorIfEmptyOrOpponent(square, 0, 1, color, board));
        moves.addAll(addVectorIfEmptyOrOpponent(square, 0, -1, color, board));

        return moves;
    }
}
