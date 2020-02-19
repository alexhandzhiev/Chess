package com.whitehatgaming.pieces;

import com.whitehatgaming.generators.*;

public enum PieceType {
    KING(new KingMoveGenerator()),
    QUEEN(new QueenMoveGenerator()),
    BISHOP(new BishopMoveGenerator()),
    KNIGHT(new KnightMoveGenerator()),
    ROOK(new RookMoveGenerator()),
    PAWN(new PawnMoveGenerator());

    MoveGenerator generator;

    private PieceType(MoveGenerator generator) {
        this.generator = generator;
    }
}
