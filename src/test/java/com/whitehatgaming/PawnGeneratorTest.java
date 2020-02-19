package com.whitehatgaming;

import com.whitehatgaming.game.Board;
import com.whitehatgaming.game.Square;
import com.whitehatgaming.generators.PawnMoveGenerator;
import com.whitehatgaming.initializer.BlackPawnAttackInitializer;
import com.whitehatgaming.initializer.BlackPawnOnlyInitializer;
import com.whitehatgaming.moves.Move;
import com.whitehatgaming.pieces.Color;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

public class PawnGeneratorTest {
    private static Board board;
    private static PawnMoveGenerator pmg;

    @BeforeAll
    public static void setUp() {
        pmg = new PawnMoveGenerator();
        board = new Board();
    }

    @Test
    public void pawnInitMove() {
        new BlackPawnOnlyInitializer().init(board);

        List<Move> moves = pmg.generateMoves(Square.at(Color.BLACK.pawnRow(), 0), Color.BLACK, board);

        Assertions.assertEquals(2, moves.size());
    }

    @Test
    public void pawnAttack() {
        new BlackPawnAttackInitializer().init(board);

        List<Move> rightAttack = pmg.generateMoves(Square.at(Color.BLACK.pawnRow(), 0), Color.BLACK, board);
        List<Move> leftAttack = pmg.generateMoves(Square.at(Color.BLACK.pawnRow(), 2), Color.BLACK, board);
        List<Move> frontPawn = pmg.generateMoves(Square.at(Color.BLACK.pawnRow(), 1), Color.BLACK, board);

        Assertions.assertEquals(3, rightAttack.size());
        Assertions.assertEquals(3, leftAttack.size());
        Assertions.assertEquals(0, frontPawn.size());
    }
}
