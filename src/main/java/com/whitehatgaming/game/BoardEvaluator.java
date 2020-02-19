package com.whitehatgaming.game;

import com.whitehatgaming.moves.Move;
import com.whitehatgaming.pieces.Color;
import com.whitehatgaming.pieces.Piece;
import com.whitehatgaming.pieces.PieceType;

import java.util.ArrayList;
import java.util.List;

public class BoardEvaluator {
    public static boolean isCheck(Color color, Board board) {
        return isThreatenedBy(color.opponent(), findKing(color, board), board);
    }

    public static boolean isThreatenedBy(Color color, Square square, Board board) {
        return semiLegalMoves(color, board).stream().anyMatch((move) -> (move.getDst().equals(square)));
    }

    public static List<Move> legalMoves(Color color, Board board) {
        List<Move> legalMoves = new ArrayList<>();
        List<Move> semiLegalMoves = semiLegalMoves(color, board);

        semiLegalMoves.stream().filter((move) -> {
            return isThreatenedBy(color.opponent(), move.getDst(), board);
        }).forEach((move) -> {
            legalMoves.add(move);
        });

        return legalMoves;
    }

    public static Square findKing(Color color, Board board) {
        for (Square square : board.allSquares()) {
            Piece piece = board.at(square);

            if (piece.isType(PieceType.KING) && piece.isColor(color)) {
                return square;
            }
        }

        throw new IllegalStateException("The king is missing!");
    }

    private static List<Move> semiLegalMoves(Color color, Board board) {
        List<Move> moveList = new ArrayList<>();

        board.allSquares().stream().forEach((square) -> {
            Piece piece = board.at(square);
            if (piece.isColor(color)) {
                moveList.addAll(piece.availableMoves(square, board));
            }
        });

        return moveList;
    }
}
