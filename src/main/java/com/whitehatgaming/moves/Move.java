package com.whitehatgaming.moves;

import com.whitehatgaming.game.Square;
import com.whitehatgaming.pieces.Piece;

public interface Move {
    void execute();

    Piece getCapturedPiece();

    Square getSource();

    Square getDst();

    default boolean equals(Square src, Square dst) {
        return getSource().equals(src) && getDst().equals(dst);
    }
}
