package pacman.game;

import java.util.*;
import pacman.board.PacmanBoard;
import pacman.ghost.Blinky;
import pacman.ghost.Clyde;
import pacman.ghost.Ghost;
import pacman.ghost.Phase;
import pacman.hunter.Hunter;
import pacman.score.ScoreBoard;

public class PacmanGame {

    private String author;

    private PacmanBoard board;

    private List<Ghost> ghosts = new ArrayList<>();

    private Hunter hunter;

    private int level;

    private int lives;

    private ScoreBoard scores;

    private int tick;

    private String title;

    public PacmanGame(String title, String author, Hunter hunter, PacmanBoard board) {
        this.tick = 0;
        this.level = 0;
        lives = 4;
        this.board = board;
        this.title = title;
        this.author = author;
        this.hunter = hunter;
        ghosts.add(new Blinky());
        ghosts.add(new Clyde());
        scores = new ScoreBoard();
    }

    public int getTick() {
        return tick;
    }

    public String getTitle(){
        return this.title;
    }

    public List<Ghost> getGhosts() {
        return ghosts;
    }

    public Hunter getHunter() {
        return hunter;
    }

    public String getAuthor() {
        return author;
    }

    public int getLevel() {
        return level;
    }

    public PacmanBoard getBoard() {
        return board;
    }

    public int getLives() {
        return lives;
    }

    public ScoreBoard getScores() {
        return scores;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public void reset() {
        this.lives = 4;
        this.level = 0;
        scores.reset();
        board.reset();
        hunter.reset();
        for (Ghost g : ghosts) {
            g.reset();
            g.setPosition(board.getGhostSpawn());
        }
    }

    public void setGhostsFrightened() {
        for (Ghost g : ghosts)
            g.setPhase(Phase.FRIGHTENED,Phase.FRIGHTENED.getDuration());
    }

    public void tick() {
        while (getLives() != 0) {
            hunter.move(this);
            for (Ghost g : ghosts) {
                hunter.hit(g);
            }
            for (Ghost g : ghosts) {
                if (!g.isDead() && (getTick()/2 == 0)) {
                    g.move(this);
                }
            }
            for (Ghost g : ghosts) {
                hunter.hit(g);
            }
            for (Ghost g : ghosts) {
                if (g.isDead()) {
                    g.reset();
                    g.setPosition(board.getGhostSpawn());
                    scores.increaseScore(200);
                }
            }
            if (hunter.isDead()) {
                lives--;
                //reset the entities.
                entitiesReset();
            }
            if (getBoard().isEmpty()) {
                level++;
                tick = 0;
                board.reset();
                entitiesReset();
            }
            tick++;
        }
    }

    private void entitiesReset() {
        hunter.reset();
        hunter.setPosition(board.getPacmanSpawn());
        for (Ghost g : ghosts) {
            g.reset();
            g.setPosition(board.getGhostSpawn());
        }
    }
}
