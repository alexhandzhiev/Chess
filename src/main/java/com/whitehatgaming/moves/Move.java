package com.whitehatgaming.moves;

import com.whitehatgaming.game.Square;
import com.whitehatgaming.pieces.Piece;

public interface Move {
    void execute();

    /**
     * Reverts the board to the state it had before {@link #execute()} was called.
     * Together they allow a move to be tried out (to test king safety) and rolled back.
     */
    void undo();

    Piece getCapturedPiece();

    Square getSource();

    Square getDst();

    /** Whether this move goes from {@code src} to {@code dst}. Named to avoid clashing with {@link Object#equals}. */
    default boolean matches(Square src, Square dst) {
        return getSource().equals(src) && getDst().equals(dst);
    }
}
