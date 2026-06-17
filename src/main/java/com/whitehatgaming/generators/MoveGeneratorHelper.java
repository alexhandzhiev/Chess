package com.whitehatgaming.generators;

import com.whitehatgaming.game.Board;
import com.whitehatgaming.moves.Move;
import com.whitehatgaming.game.Square;
import com.whitehatgaming.moves.StandardMove;
import com.whitehatgaming.pieces.Color;

import java.util.ArrayList;
import java.util.List;

public class MoveGeneratorHelper {

    /**
     * A single step to {@code dst}: a one-element list if the square is empty or holds an
     * opponent, otherwise empty. Used by the king and knight.
     */
    public static List<Move> stepIfEmptyOrOpponent(final Square src, final Square dst, final Color color,
                                                   final Board board) {
        List<Move> moves = new ArrayList<>();

        if (dst != null && (board.isFree(dst) || board.isColor(dst, color.opponent()))) {
            moves.add(new StandardMove(src, dst, board));
        }

        return moves;
    }

    /**
     * Every step along a direction until blocked, including a capture of the first opponent
     * reached. Used by the sliding pieces (rook, bishop, queen).
     */
    public static List<Move> slideWhileEmptyOrOpponent(final Square src, final int rowOffset, final int colOffset,
                                                       final Color color, final Board board) {
        List<Move> moves = new ArrayList<>();

        Square square = Square.atOffset(src, rowOffset, colOffset);
        while (board.isFree(square) || board.isColor(square, color.opponent())) {
            moves.add(new StandardMove(src, square, board));

            if (board.isColor(square, color.opponent())) {
                break;
            }
            square = Square.atOffset(square, rowOffset, colOffset);
        }

        return moves;
    }
}
