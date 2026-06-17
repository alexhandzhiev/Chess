package com.whitehatgaming.moves;

import com.whitehatgaming.game.Board;
import com.whitehatgaming.game.Square;
import com.whitehatgaming.pieces.Piece;

public class StandardMove implements Move {
    protected final Board board;
    protected final Square srcSquare;
    protected final Square dstSquare;
    protected Piece movedPiece;
    protected Piece capturedPiece;

    public StandardMove(Square sourceSquare, Square targetSquare, Board board) {
        this.srcSquare = sourceSquare;
        this.board = board;
        this.dstSquare = targetSquare;
    }

    @Override
    public void execute() {
        movedPiece = board.at(srcSquare);
        capturedPiece = board.at(dstSquare);

        board.removePieceAt(srcSquare);
        board.setPieceAt(dstSquare, placedPiece());
    }

    @Override
    public void undo() {
        board.removePieceAt(dstSquare);
        board.setPieceAt(srcSquare, movedPiece);

        if (capturedPiece != null) {
            board.setPieceAt(dstSquare, capturedPiece);
        }
    }

    /**
     * The piece that ends up on the destination square. Overridden by promotions,
     * where a pawn is replaced by the promoted piece.
     */
    protected Piece placedPiece() {
        return movedPiece;
    }

    @Override
    public Square getSource() {
        return srcSquare;
    }

    @Override
    public Square getDst() {
        return dstSquare;
    }

    @Override
    public Piece getCapturedPiece() {
        return capturedPiece;
    }
}
