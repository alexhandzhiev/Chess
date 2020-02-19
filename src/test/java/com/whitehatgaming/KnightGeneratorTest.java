package com.whitehatgaming;

import com.whitehatgaming.game.Board;
import com.whitehatgaming.game.BoardInitializer;
import com.whitehatgaming.game.Square;
import com.whitehatgaming.generators.KnightMoveGenerator;
import com.whitehatgaming.moves.Move;
import com.whitehatgaming.moves.StandardMove;
import com.whitehatgaming.pieces.Color;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

public class KnightGeneratorTest {
    private static Board board;
    private static KnightMoveGenerator kng = new KnightMoveGenerator();

    @BeforeAll
    public static void setUp() {
        board = new Board();
        kng = new KnightMoveGenerator();

        new BoardInitializer().init(board);
    }

    @Test
    public void knightInitMove() {
        List<Move> moves = kng.generateMoves(Square.at(0, 1), Color.WHITE, board);
        Assertions.assertEquals(2, moves.size());
    }

    @Test
    public void knightAttackMove() {
        new StandardMove(Square.at(6, 0), Square.at(2, 0), board).execute();

        List<Move> moves = kng.generateMoves(Square.at(0, 1), Color.WHITE, board);

        Assertions.assertEquals(2, moves.size());
    }

    @Test
    public void knightAllMoves() {
        new StandardMove(Square.at(0, 1), Square.at(4, 4), board).execute();

        List<Move> moves = kng.generateMoves(Square.at(4, 4), Color.BLACK, board);

        Assertions.assertEquals(8, moves.size());
    }
}
