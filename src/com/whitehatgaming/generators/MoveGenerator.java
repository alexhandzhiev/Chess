package com.whitehatgaming.generators;

import com.whitehatgaming.game.Board;
import com.whitehatgaming.moves.Move;
import com.whitehatgaming.game.Square;
import com.whitehatgaming.pieces.Color;

import java.util.List;

public interface MoveGenerator {
    public List<Move> generateMoves(Square square, Color color, Board board);
}
