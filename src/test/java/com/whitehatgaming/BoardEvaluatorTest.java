package com.whitehatgaming;

import com.whitehatgaming.game.Board;
import com.whitehatgaming.game.BoardEvaluator;
import com.whitehatgaming.game.BoardInitializer;
import com.whitehatgaming.pieces.Color;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BoardEvaluatorTest {
    private static Board board;
    private static BoardInitializer boardInitializer;

    @BeforeAll
    public static void setUp() {
        board = new Board();
        boardInitializer = new BoardInitializer();
    }

    @Test
    public void kingLessBoard() {
        Assertions.assertThrows(IllegalStateException.class, () -> { BoardEvaluator.findKing(Color.WHITE, board); });
    }

    @Test
    public void initialLegalMoves() {
        boardInitializer.init(board);
        System.out.println(BoardEvaluator.legalMoves(Color.WHITE, board));
        Assertions.assertEquals(20, BoardEvaluator.legalMoves(Color.WHITE, board).size());
    }
}
