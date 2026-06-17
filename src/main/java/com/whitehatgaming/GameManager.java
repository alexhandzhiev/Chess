package com.whitehatgaming;

import com.whitehatgaming.exceptions.InvalidMovementException;
import com.whitehatgaming.game.ConsoleGameListener;
import com.whitehatgaming.game.Game;
import com.whitehatgaming.game.MoveCommand;
import com.whitehatgaming.game.Square;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class GameManager {

    public static void main(String[] args) {

        if (args == null || args.length < 1) {
            throw new IllegalArgumentException("No input file supplied.");
        }

        try {
            List<MoveCommand> moves = readMoves(args[0]);

            Game gameEngine = new Game(new ConsoleGameListener());
            gameEngine.start(moves);

        } catch (FileNotFoundException e) {
            System.out.println("Input file not found: " + args[0]);
        } catch (IOException e) {
            System.out.println("Could not read input file: " + e.getMessage());
        } catch (InvalidMovementException e) {
            System.out.println(e.getMessage());
        } catch (RuntimeException e) {
            // Last-resort guard so an unexpected internal failure (e.g. a malformed move
            // record or a corrupt position) reports cleanly instead of dumping a stack trace.
            System.out.println("Could not play the game: " + e.getMessage());
        }
    }

    private static List<MoveCommand> readMoves(String path) throws IOException, InvalidMovementException {
        UserInput input = new UserInputFile(path);
        List<MoveCommand> moves = new ArrayList<>();
        int[] moveIn;

        while ((moveIn = input.nextMove()) != null) {

            for (int coordinate : moveIn) {
                if (coordinate > 7 || coordinate < 0) {
                    throw new InvalidMovementException();
                }
            }

            Square source = new Square(moveIn[1], moveIn[0]);
            Square destination = new Square(moveIn[3], moveIn[2]);

            if (source.equals(destination)) {
                throw new InvalidMovementException();
            }

            moves.add(new MoveCommand(source, destination));
        }

        return moves;
    }
}
