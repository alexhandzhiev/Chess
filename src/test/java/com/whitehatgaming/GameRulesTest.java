package com.whitehatgaming;

import com.whitehatgaming.game.Board;
import com.whitehatgaming.game.BoardEvaluator;
import com.whitehatgaming.game.BoardState;
import com.whitehatgaming.game.Square;
import com.whitehatgaming.generators.PawnMoveGenerator;
import com.whitehatgaming.generators.RookMoveGenerator;
import com.whitehatgaming.moves.Move;
import com.whitehatgaming.moves.PromotionMove;
import com.whitehatgaming.pieces.Color;
import com.whitehatgaming.pieces.Piece;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Rules that emerge from move legality: pins, checkmate, stalemate and promotion.
 * Squares are (row, col) with row 0 = rank 8 and col 0 = file a.
 */
public class GameRulesTest {
    private Board board;

    @BeforeEach
    public void setUp() {
        board = new Board();
    }

    @Test
    public void pinnedPieceHasNoLegalMove() {
        board.setPieceAt(Square.at(7, 4), Piece.WHITE_KING);   // e1
        board.setPieceAt(Square.at(6, 4), Piece.WHITE_BISHOP); // e2, pinned along the e-file
        board.setPieceAt(Square.at(0, 4), Piece.BLACK_ROOK);   // e8, pinning rook

        List<Move> legal = BoardEvaluator.legalMoves(Color.WHITE, board);

        boolean bishopCanMove = legal.stream().anyMatch(move -> move.getSource().equals(Square.at(6, 4)));
        Assertions.assertFalse(bishopCanMove, "A pinned bishop must not have any legal move");
        Assertions.assertFalse(legal.isEmpty(), "The king itself should still have legal moves");
    }

    @Test
    public void backRankCheckmate() {
        board.setPieceAt(Square.at(0, 7), Piece.BLACK_KING);  // h8
        board.setPieceAt(Square.at(1, 6), Piece.BLACK_PAWN);  // g7
        board.setPieceAt(Square.at(1, 7), Piece.BLACK_PAWN);  // h7
        board.setPieceAt(Square.at(0, 0), Piece.WHITE_ROOK);  // a8, checks along the 8th rank
        board.setPieceAt(Square.at(7, 4), Piece.WHITE_KING);  // e1

        Assertions.assertEquals(BoardState.CHECKMATE, BoardEvaluator.evaluate(Color.BLACK, board));
    }

    @Test
    public void cornerStalemate() {
        board.setPieceAt(Square.at(0, 7), Piece.BLACK_KING);  // h8, not in check
        board.setPieceAt(Square.at(2, 6), Piece.WHITE_QUEEN); // g6, covers g7/g8/h7
        board.setPieceAt(Square.at(7, 4), Piece.WHITE_KING);  // e1

        Assertions.assertEquals(BoardState.STALEMATE, BoardEvaluator.evaluate(Color.BLACK, board));
    }

    @Test
    public void pawnPromotesToQueen() {
        board.setPieceAt(Square.at(1, 0), Piece.WHITE_PAWN); // a7, one step from promotion

        List<Move> moves = new PawnMoveGenerator().generateMoves(Square.at(1, 0), Color.WHITE, board);

        Assertions.assertEquals(1, moves.size());
        Move promotion = moves.get(0);
        Assertions.assertInstanceOf(PromotionMove.class, promotion);

        promotion.execute();
        Assertions.assertEquals(Piece.WHITE_QUEEN, board.at(Square.at(0, 0)));
    }

    @Test
    public void pawnPromotesOnCapture() {
        board.setPieceAt(Square.at(1, 1), Piece.WHITE_PAWN); // b7
        board.setPieceAt(Square.at(0, 0), Piece.BLACK_ROOK); // a8, capturable with promotion

        List<Move> moves = new PawnMoveGenerator().generateMoves(Square.at(1, 1), Color.WHITE, board);

        Move capturePromotion = moves.stream()
                .filter(move -> move.getDst().equals(Square.at(0, 0)))
                .findFirst()
                .orElseThrow();
        Assertions.assertInstanceOf(PromotionMove.class, capturePromotion);

        capturePromotion.execute();
        Assertions.assertEquals(Piece.WHITE_QUEEN, board.at(Square.at(0, 0)));
        Assertions.assertEquals(Piece.BLACK_ROOK, capturePromotion.getCapturedPiece());
    }

    @Test
    public void slidingPieceCannotJumpOverACapture() {
        board.setPieceAt(Square.at(7, 0), Piece.WHITE_ROOK); // a1
        board.setPieceAt(Square.at(4, 0), Piece.BLACK_PAWN); // a4, first piece up the file

        List<Move> moves = new RookMoveGenerator().generateMoves(Square.at(7, 0), Color.WHITE, board);

        boolean capturesA4 = moves.stream().anyMatch(move -> move.getDst().equals(Square.at(4, 0)));
        boolean reachesA5 = moves.stream().anyMatch(move -> move.getDst().equals(Square.at(3, 0)));
        Assertions.assertTrue(capturesA4, "The rook should be able to capture the pawn on a4");
        Assertions.assertFalse(reachesA5, "The rook must not slide past the pawn to a5");
    }
}
