package com.whitehatgaming.game;

/**
 * A board coordinate. The raw constructor allows off-board values so offsets can be
 * computed; the factory methods return {@code null} when a coordinate leaves the board.
 */
public record Square(int row, int col) {

    public static Square at(int row, int col) {
        Square newSquare = new Square(row, col);
        return newSquare.isValid() ? newSquare : null;
    }

    public static Square atOffset(Square square, int rowOffset, int colOffset) {
        return at(square.row + rowOffset, square.col + colOffset);
    }

    public boolean isValid() {
        return col >= 0 && col < Board.SIZE && row >= 0 && row < Board.SIZE;
    }
}
