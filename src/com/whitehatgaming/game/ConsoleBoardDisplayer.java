package com.whitehatgaming.game;

import com.whitehatgaming.pieces.Piece;

public class ConsoleBoardDisplayer {

    public void displayBoard(Board board) {
        System.out.println();
        System.out.println("    A  B  C  D  E  F  G  H");
        System.out.println(" |--------------------------|");

        for (int row = 0; row < 8; row++) {
            int rowNum = 8 - row;
            System.out.print(rowNum + "| ");

            for (int col = 0; col < 8; col++) {
                Square square = new Square(row, col);
                Piece piece = board.at(square);

                if (piece != null) {
                    String pieceAbrv = piece.name().substring(6,7);
                    if("KN".equalsIgnoreCase(piece.name().substring(6,8))) { pieceAbrv = "N"; }
                    if(piece.name().startsWith("B")) { pieceAbrv = pieceAbrv.toLowerCase(); }
                    System.out.print("[" + pieceAbrv + "]");
                } else {
                    System.out.print("[ ]");
                }
            }

            System.out.print( " |" + rowNum);
            System.out.println();
        }

        System.out.println(" |--------------------------|");
        System.out.println("    A  B  C  D  E  F  G  H");
        System.out.println();
    }
}
