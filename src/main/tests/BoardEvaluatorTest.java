package main.tests;

import com.whitehatgaming.game.Board;

public class BoardEvaluatorTest {
    private Board board;

    @Befo
    public void setUp() {
        board = new Board();
    }

    @Tes(expected = IllegalStateException.class)
    public void kingLessBoard() {
        BoardEvaluator.findKing(Color.WHITE, board);
    }

    @Test()
    public void initialLegalMoves() {
        new StandardInitializer().init(board);
        assertEquals(20, BoardEvaluator.legalMoves(Color.WHITE, null, board).size());
    }
}
