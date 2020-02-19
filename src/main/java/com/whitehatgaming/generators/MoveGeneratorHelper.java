package com.whitehatgaming.generators;

import com.whitehatgaming.game.Board;
import com.whitehatgaming.moves.Move;
import com.whitehatgaming.game.Square;
import com.whitehatgaming.moves.StandardMove;
import com.whitehatgaming.pieces.Color;

import java.util.ArrayList;
import java.util.List;

public class MoveGeneratorHelper {

    public static void addMoveIfEmptyOrOpponent(final Square src, final Square dst, final Color color,
                                                final List<Move> moves, final Board board) {
        if (src == null || dst == null) {
            return;
        }

        if (board.isFree(dst) || board.isColor(dst, color.opponent())) {
            moves.add(new StandardMove(src, dst, board));
        }
    }

    public static List<Move> addVectorIfEmptyOrOpponent(final Square src, final int rowOffset, final int colOffset,
                                                        final Color color, final Board board) {
        List<Move> moves = new ArrayList<>();

        Square square = Square.atOffset(src, rowOffset, colOffset);

        while (board.isFree(square) || board.isColor(square, color.opponent())) {
            moves.add(new StandardMove(src, square, board));
            square = Square.atOffset(square, rowOffset, colOffset);
        }

        return moves;
    }
}
