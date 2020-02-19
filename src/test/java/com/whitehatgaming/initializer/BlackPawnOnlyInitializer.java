package com.whitehatgaming.initializer;

import com.whitehatgaming.game.Board;
import com.whitehatgaming.game.BoardInitializer;
import com.whitehatgaming.game.Square;
import com.whitehatgaming.pieces.Piece;

public class BlackPawnOnlyInitializer extends BoardInitializer {

    @Override
    public void init(Board board) {
        for (int col = 0; col < 8; col++) {
            board.setPieceAt(Square.at(1, col), Piece.BLACK_PAWN);
        }

        board.setPieceAt(Square.at(0, 4), Piece.BLACK_KING);
    }
}
