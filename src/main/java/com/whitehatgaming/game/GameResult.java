package com.whitehatgaming.game;

import com.whitehatgaming.pieces.Color;

/**
 * The outcome of a played game: the final board state, the winner (null for an
 * ongoing game or a draw) and how many moves were applied.
 */
public record GameResult(BoardState finalState, Color winner, int movesPlayed) {
}
