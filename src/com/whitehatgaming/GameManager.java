package com.whitehatgaming;

import com.whitehatgaming.game.Game;
import com.whitehatgaming.game.Square;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;


public class GameManager {

    public static void main(String[] args) {
        UserInput input;
        try {
            input = new UserInputFile("src/sample-moves.txt");
            LinkedHashMap<Square, Square> moves = new LinkedHashMap<>();

            int[] moveIn;
            while ((moveIn = input.nextMove()) != null) {
                System.out.println(moveIn[0] + "" + moveIn[1] + "" + moveIn[2] + "" + moveIn[3]);
                Square squareSrc = new Square(moveIn[0], moveIn[1]);
                Square squareDst = new Square(moveIn[2], moveIn[3]);
                moves.put(squareSrc, squareDst);
            }

            Game game = new Game();
            game.start(moves);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


//        Player player1 = new ConsolePlayer(Color.WHITE);
//        Player player2 = new ConsolePlayer(Color.BLACK);
//
//        NotationConverter converter = new StandardConverter();
//        BoardDisplayer displayer = new ConsoleBoardDisplayer();

//        Game game = new Game(player1, player2, displayer, converter);

//        game.start()
    }
}
