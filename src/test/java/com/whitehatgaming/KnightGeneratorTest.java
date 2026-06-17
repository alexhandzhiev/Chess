package com.whitehatgaming;

import com.whitehatgaming.game.Board;
import com.whitehatgaming.game.BoardInitializer;
import com.whitehatgaming.game.Square;
import com.whitehatgaming.generators.KnightMoveGenerator;
import com.whitehatgaming.moves.Move;
import com.whitehatgaming.moves.StandardMove;
import com.whitehatgaming.pieces.Color;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class KnightGeneratorTest {
    private Board board;
    private KnightMoveGenerator kng;

    @BeforeEach
    public void setUp() {
        board = new Board();
        kng = new KnightMoveGenerator();

        new BoardInitializer().init(board);
    }

    @Test
    public void knightInitMove() {
        // White knight on b1 in the starting position: only a3 and c3 are open (d2 is its own pawn).
        List<Move> moves = kng.generateMoves(Square.at(7, 1), Color.WHITE, board);
        Assertions.assertEquals(2, moves.size());
    }

    @Test
    public void knightAttackMove() {
        // Black knight on b8: a6 and c6 are open, d7 is blocked by its own pawn.
        List<Move> moves = kng.generateMoves(Square.at(0, 1), Color.BLACK, board);
        Assertions.assertEquals(2, moves.size());
    }

    @Test
    public void knightAllMoves() {
        // A knight on a central square reaches all eight targets.
        new StandardMove(Square.at(0, 1), Square.at(4, 4), board).execute();

        List<Move> moves = kng.generateMoves(Square.at(4, 4), Color.BLACK, board);

        Assertions.assertEquals(8, moves.size());
    }
}
