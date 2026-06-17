package com.whitehatgaming.moves;

import com.whitehatgaming.game.Board;
import com.whitehatgaming.game.Square;
import com.whitehatgaming.pieces.Piece;
import com.whitehatgaming.pieces.PieceType;

/**
 * A pawn move that reaches the last rank. The pawn is replaced on the destination
 * square by a promoted piece. The supplied move format only carries source and
 * destination, so promotion defaults to a queen.
 */
public class PromotionMove extends StandardMove {

    private final PieceType promoteTo;

    public PromotionMove(Square sourceSquare, Square targetSquare, Board board) {
        this(sourceSquare, targetSquare, board, PieceType.QUEEN);
    }

    public PromotionMove(Square sourceSquare, Square targetSquare, Board board, PieceType promoteTo) {
        super(sourceSquare, targetSquare, board);
        this.promoteTo = promoteTo;
    }

    @Override
    protected Piece placedPiece() {
        return Piece.of(promoteTo, movedPiece.color());
    }
}
