package pacman.game;

import pacman.board.PacmanBoard;
import pacman.hunter.*;
import pacman.util.*;
import java.io.*;
import java.util.*;

public class GameReader {

    public static PacmanGame read(Reader reader) throws IOException {

        var infoFromReader = getInfo(reader);

        //set board size
        var boardAttributes = infoFromReader.get(0);
        PacmanBoard board =setBoard(boardAttributes);
        //set title
        String title = infoFromReader.get(0).split("=")[1];
        //set author
        String author = infoFromReader.get(1).split("=")[1];
        //set current score
        String score = infoFromReader.get(2).split("=")[1];
        //set the hunter
        var hunterInfo = infoFromReader.get(3).split(",");
        Hunter hunter = setHunter(hunterInfo);
        //game initialization
        PacmanGame game = new PacmanGame(title,author,hunter,board);
        //set scores
        for(int i = 4; i <infoFromReader.size(); i++) {
            game.getScores().setScore(infoFromReader.get(i).split(":")[0],
                    Integer.parseInt(infoFromReader.get(i).split(":")[1]));
        }
        return game;
    }


    private static ArrayList<String> getInfo(Reader reader) throws IOException {
            BufferedReader br = (BufferedReader) reader;
            var info = new ArrayList<String>();
            var line = br.readLine();
            while (line != null) {
                switch (line) {
                    case "[Board]":
                        info.add(line = br.readLine());
                        break;
                    case "[Game]":
                        line = br.readLine();
                        while (!line.equals("")) {
                            info.add(line);
                            line = br.readLine();
                        }
                        break;
                    case "[Scores]":
                        line = br.readLine();
                        while (line != null && !line.equals("")) {
                            info.add(line);
                            line = br.readLine();
                        }
                        break;

                }
                line = br.readLine();
            }
            return info;
    }
    
    private static PacmanBoard setBoard(String boardInfo) {
        
        int width = Integer.parseInt(boardInfo.split(",")[0]);
        int height = Integer.parseInt(boardInfo.split(",")[1]);
        
        return new PacmanBoard(width,height);
    }
    
    private static Hunter setHunter(String[] hunterInfo) {

        Position hunterPosition = new Position(Integer.parseInt(hunterInfo[0]),Integer.parseInt(hunterInfo[1]));
        Direction hunterDirection = Direction.valueOf(hunterInfo[2]);
        int duration = Integer.parseInt(hunterInfo[3].split(":")[1]);

        Hunter hunter;
        switch(hunterInfo[4]) {
            case "Hungry":
                hunter = new Hungry();
                break;
            case "Phasey":
                hunter = new Phasey();
                break;
            case "Phil":
                hunter = new Phil();
                break;
            case "Speedy":
                hunter = new Speedy();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + hunterInfo[4]);
        }
        hunter.setPosition(hunterPosition);
        hunter.setDirection(hunterDirection);
        hunter.activateSpecial(duration);

        return hunter;

    }
}
