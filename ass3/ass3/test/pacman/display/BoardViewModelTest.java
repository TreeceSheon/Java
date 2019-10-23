package pacman.display;


import javafx.geometry.Pos;
import javafx.util.Pair;
import org.junit.Test;
import pacman.board.BoardItem;
import pacman.board.PacmanBoard;
import pacman.game.GameReader;
import pacman.game.PacmanGame;
import pacman.ghost.*;
import pacman.hunter.Hunter;
import pacman.hunter.Phil;
import pacman.hunter.Speedy;
import pacman.util.Direction;
import pacman.util.Position;
import pacman.util.UnpackableException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class BoardViewModelTest {

    private Hunter testHunter = new Speedy();
    private PacmanBoard board = new PacmanBoard(10,10);
    private PacmanGame gamePrototype = new PacmanGame("Map for testing","Treece",testHunter,board);
    private PacmanGame gameFromArchive = GameReader.read(new FileReader("maps/default.map"));
    private BoardViewModel boardModelArchived = new BoardViewModel(gameFromArchive);
    private BoardViewModel boardModelPrototype = new BoardViewModel(gamePrototype);
    public BoardViewModelTest() throws UnpackableException, IOException {
        for (Ghost ghost : gamePrototype.getGhosts()) {
            ghost.setPosition(new Position(0,0));
        }
    }

    @Test
    public void getLives() {
        assertEquals(4,boardModelPrototype.getLives());
        assertEquals(5,boardModelArchived.getLives());

    }

    @Test
    public void getLevel() {
        assertEquals(2,boardModelArchived.getLevel());
        assertEquals(0,boardModelPrototype.getLevel());
    }

    @Test
    public void getPacmanColour() {
        assertEquals("#FFE709",boardModelArchived.getPacmanColour());
        gameFromArchive.getHunter().activateSpecial(Hunter.SPECIAL_DURATION);
        assertEquals("#FFE709",boardModelArchived.getPacmanColour());

        assertEquals("#FFE709",boardModelPrototype.getPacmanColour());
        gamePrototype.getHunter().activateSpecial(Hunter.SPECIAL_DURATION);
        assertEquals("#CDC3FF",boardModelPrototype.getPacmanColour());
    }

    @Test
    public void getPacmanMouthAngle() {
        for (Direction direction : Direction.values()) {
            gameFromArchive.getHunter().setDirection(direction);
            switch (direction.toString()) {
                case "RIGHT":
                    assertEquals(30,boardModelArchived.getPacmanMouthAngle());
                    break;
                case "UP":
                    assertEquals(120,boardModelArchived.getPacmanMouthAngle());
                    break;
                case "LEFT":
                    assertEquals(210,boardModelArchived.getPacmanMouthAngle());
                    break;
                case "DOWN":
                    assertEquals(300,boardModelArchived.getPacmanMouthAngle());
                    break;
            }
        }
    }

    @Test
    public void getPacmanPosition() {
        assertEquals(new Position(1,1),boardModelArchived.getPacmanPosition());
        assertEquals(new Position(0,0),boardModelPrototype.getPacmanPosition());
    }

    @Test
    public void getBoard() {
        PacmanBoard boardArchived = new PacmanBoard(25,9);
        boardArchived.setEntry(new Position(1,1), BoardItem.DOT);
        boardArchived.setEntry(new Position(6,2), BoardItem.BIG_DOT);
        boardArchived.setEntry(new Position(6,6), BoardItem.BIG_DOT);
        boardArchived.setEntry(new Position(19,2), BoardItem.BIG_DOT);
        boardArchived.setEntry(new Position(19,6), BoardItem.BIG_DOT);
        boardArchived.setEntry(new Position(23,7), BoardItem.GHOST_SPAWN);
        boardArchived.setEntry(new Position(1,7), BoardItem.PACMAN_SPAWN);
        assertEquals(boardArchived,gameFromArchive.getBoard());

        assertEquals(board,gamePrototype.getBoard());
    }

    @Test
    public void getGhosts() {
        List<Pair<Position,String>> l1 = new ArrayList<>();
        l1.addAll(List.of((new Pair<>(new Position(3,6), "#0000FF")),
                new Pair<>(new Position(1,6),new Inky().getColour()),
                new Pair<>(new Position(8,6),"#0000FF"),
                new Pair<>(new Position(6,4),new Clyde().getColour())));
        assertEquals(l1.size(), boardModelArchived.getGhosts().size());
        assertTrue(l1.containsAll(boardModelArchived.getGhosts()));

        List<Pair<Position, String>> l2 = new ArrayList<>();
        l2.addAll(List.of(new Pair<>(new Inky().getPosition(), new Inky().getColour()),
                    new Pair<>(new Blinky().getPosition(), new Blinky().getColour()),
                    new Pair<>(new Clyde().getPosition(), new Clyde().getColour()),
                    new Pair<>(new Pinky().getPosition(), new Pinky().getColour())));
        assertEquals(l2.size(), boardModelPrototype.getGhosts().size());
        System.out.println(l2.toString());
        System.out.println(boardModelPrototype.getGhosts().toString());
        assertTrue(l2.containsAll(boardModelPrototype.getGhosts()));
    }
}