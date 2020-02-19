package com.whitehatgaming;

import com.whitehatgaming.exceptions.InvalidMovementException;
import com.whitehatgaming.game.Game;
import com.whitehatgaming.game.Square;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;


public class GameManager {

    public static void main(String[] args) {

        if(args == null || args.length < 1) {
            throw new IllegalArgumentException("No input file supplied.");
        }

        UserInput input;
        try {
            input = new UserInputFile(args[0]);
            LinkedHashMap<Square, Square> moves = new LinkedHashMap<>();
            int[] moveIn;

            // System.out.println("[Movement Coordinates]");
            // System.out.println("======================");

            while ((moveIn = input.nextMove()) != null) {

                for (int i = 0; i < moveIn.length; i++) {
                    if(moveIn[i] > 7 || moveIn[i] < 0)  {
                        throw new InvalidMovementException();
                    }
                }

                Square squareSrc = new Square(moveIn[1], moveIn[0]);
                Square squareDst = new Square(moveIn[3], moveIn[2]);

                if(squareSrc.equals(squareDst)) {
                    throw new InvalidMovementException();
                }

                moves.put(squareSrc, squareDst);
            }

            System.out.println();

            Game gameEngine = new Game();
            gameEngine.start(moves);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidMovementException e) {
            System.out.println(e);
        }
    }
}
