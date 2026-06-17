package com.whitehatgaming.pieces;

public enum Color {

    BLACK(1, 1, 7), WHITE(-1, 6, 0);

    private final int direction;
    private final int pawnRow;
    private final int promotionRow;

    Color(int value, int pawnRow, int promotionRow) {
        this.direction = value;
        this.pawnRow = pawnRow;
        this.promotionRow = promotionRow;
    }

    public int dir() {
        return direction;
    }

    public int pawnRow() {
        return pawnRow;
    }

    public int promotionRow() {
        return promotionRow;
    }

    public Color opponent() {
        if (this == Color.BLACK) {
            return Color.WHITE;
        } else {
            return Color.BLACK;
        }
    }
}
