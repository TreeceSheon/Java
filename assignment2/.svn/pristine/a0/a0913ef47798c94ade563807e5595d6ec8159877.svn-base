package pacman.game;

import pacman.board.PacmanBoard;
import pacman.ghost.Blinky;
import pacman.ghost.Ghost;
import pacman.hunter.Hunter;
import pacman.hunter.Phil;
import pacman.util.Position;

import java.io.*;

public class GameWriter {

    public static void write(Writer writer, PacmanGame game) throws IOException {
        writer.write("[Board]");
        writer.append(System.lineSeparator());
        writer.write(game.getBoard().getWidth()+","+game.getBoard().getHeight());
        writer.append(System.lineSeparator());
        for (int i = 0; i < game.getBoard().getHeight(); i++) {
            for (int j = 0; j < game.getBoard().getWidth(); j++) {
                writer.append(game.getBoard().getEntry(new Position(i,j)).getChar());
            }
            writer.append(System.lineSeparator());
        }
        writer.write("\n[Game]\r");
        writer.write("title = " + game.getTitle() + "\r");
        writer.write("author = " + game.getAuthor() + "\r");
        writer.write("ives = " + game.getLives() + "\r");
        writer.write("level = " + game.getLevel() + "\r");
        writer.write("score = " + game.getScores().getScore() + "\r");
        Hunter hunter = game.getHunter();
        writer.write("hunter = " + ((Hunter)hunter).toString() + ",SPECIAL:" +
                hunter.getSpecialDurationRemaining() + "," + hunter.getClass().getSimpleName());
        writer.append(System.lineSeparator());
        for (int i = 0; i < game.getGhosts().size(); i++) {
            Ghost currentGohst = game.getGhosts().get(i);
            writer.write(currentGohst.getType() + " = " + currentGohst.getPosition().getX() + "," +
                    currentGohst.getPosition().getY() + currentGohst.getPhase() + currentGohst.getPhase().getDuration());
            writer.append(System.lineSeparator());
        }
        writer.write("\n[scores]\r");
        for (String e : game.getScores().getEntriesByName()) {
            writer.write(e);
            writer.append(System.lineSeparator());
        }
        writer.write(System.lineSeparator());
        writer.close();
    }

    public static void main(String[] args) throws IOException {
        Hunter tree = new Phil();
        PacmanGame game = new PacmanGame("aa","treece",tree, new PacmanBoard(4,4));
        game.getScores().setScore("ivy",0);
        write(new PrintWriter("C:\\Users\\Trrrr\\Desktop\\semester2\\CSSE7023\\out.txt"),game);
    }
}
