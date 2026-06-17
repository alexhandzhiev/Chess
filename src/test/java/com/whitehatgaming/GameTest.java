package com.whitehatgaming;

import com.whitehatgaming.exceptions.InvalidMovementException;
import com.whitehatgaming.game.BoardState;
import com.whitehatgaming.game.Game;
import com.whitehatgaming.game.MoveCommand;
import com.whitehatgaming.game.Square;
import com.whitehatgaming.pieces.Piece;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Drives the full engine through {@link Game#start} the same way the file replayer does,
 * using algebraic move strings (e.g. "e2e4") so the sequences read like a game.
 */
public class GameTest {

    @Test
    public void scholarsMateEndsInCheckmate() throws InvalidMovementException {
        // The contents of resources/checkmate.txt: 1.e4 e5 2.Bc4 Nc6 3.Qf3 d6 4.Qxf7#
        Game game = new Game();

        game.start(moves("e2e4", "e7e5", "f1c4", "b8c6", "d1f3", "d7d6", "f3f7"));

        Assertions.assertEquals(BoardState.CHECKMATE, game.getBoardState());
    }

    @Test
    public void illegalMoveIsRejected() {
        // b1b3 is not a knight move; matches the spirit of resources/sample-moves-invalid.txt.
        Game game = new Game();

        Assertions.assertThrows(InvalidMovementException.class,
                () -> game.start(moves("e2e4", "e7e5", "b1b3")));
    }

    @Test
    public void movesFromTheSameSourceSquareAreNotDropped() throws InvalidMovementException {
        // Regression test for the old LinkedHashMap<Square,Square> storage, which collapsed
        // any two moves sharing a source square. Both knights shuffle out, back, and out again;
        // move 5 (b1->c3) shares its source with move 1 and must still be played.
        Game game = new Game();

        game.start(moves("b1c3", "b8c6", "c3b1", "c6b8", "b1c3"));

        Assertions.assertEquals(Piece.WHITE_KNIGHT, game.pieceAt(square("c3")),
                "The repeated b1->c3 move must be replayed, leaving the knight on c3");
        Assertions.assertNull(game.pieceAt(square("b1")),
                "If a move had been dropped, the knight would still be sitting on b1");
    }

    private static List<MoveCommand> moves(String... algebraicMoves) {
        return Arrays.stream(algebraicMoves)
                .map(GameTest::move)
                .collect(Collectors.toList());
    }

    private static MoveCommand move(String fromTo) {
        return new MoveCommand(square(fromTo.substring(0, 2)), square(fromTo.substring(2, 4)));
    }

    /** Converts algebraic notation ("e2") into a board square (row 0 = rank 8, col 0 = file a). */
    private static Square square(String algebraic) {
        int col = algebraic.charAt(0) - 'a';
        int row = 8 - Character.getNumericValue(algebraic.charAt(1));
        return Square.at(row, col);
    }
}
