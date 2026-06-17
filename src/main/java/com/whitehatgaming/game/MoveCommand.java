package com.whitehatgaming.game;

/**
 * A single move requested from the input file: a source and destination square.
 * The promotion piece is not part of the input format, so promotions default to a queen.
 */
public record MoveCommand(Square source, Square destination) {
}
