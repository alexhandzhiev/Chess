package com.whitehatgaming.generators;

import com.whitehatgaming.game.*;
import com.whitehatgaming.moves.Move;
import com.whitehatgaming.moves.PromotionMove;
import com.whitehatgaming.moves.StandardMove;
import com.whitehatgaming.pieces.Color;

import java.util.ArrayList;
import java.util.List;

public class PawnMoveGenerator implements MoveGenerator {

    @Override
    public List<Move> generateMoves(Square square, Color color, Board board) {
        List<Move> moves = new ArrayList<>();

        Square fwd = Square.atOffset(square, color.dir(), 0);
        Square push = Square.atOffset(square, 2 * color.dir(), 0);
        Square leftDiag = Square.atOffset(square, color.dir(), -1);
        Square rightDiag = Square.atOffset(square, color.dir(), 1);

        if (board.isFree(fwd)) {
            addAdvance(square, fwd, color, moves, board);
        }

        if (square.row() == color.pawnRow() && board.isFree(fwd) && board.isFree(push)) {
            moves.add(new StandardMove(square, push, board));
        }

        if (board.isColor(rightDiag, color.opponent())) {
            addAdvance(square, rightDiag, color, moves, board);
        }

        if (board.isColor(leftDiag, color.opponent())) {
            addAdvance(square, leftDiag, color, moves, board);
        }

        return moves;
    }

    private void addAdvance(Square src, Square dst, Color color, List<Move> moves, Board board) {
        if (dst.row() == color.promotionRow()) {
            moves.add(new PromotionMove(src, dst, board));
        } else {
            moves.add(new StandardMove(src, dst, board));
        }
    }
}
