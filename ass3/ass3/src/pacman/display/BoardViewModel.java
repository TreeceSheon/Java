package pacman.display;

import javafx.util.Pair;
import pacman.board.PacmanBoard;
import pacman.game.PacmanGame;
import pacman.ghost.Ghost;
import pacman.ghost.Phase;
import pacman.util.Position;
import java.util.LinkedList;
import java.util.List;

public class BoardViewModel {

    private PacmanGame gameModel;
    private PacmanBoard boardModel;

    public BoardViewModel(PacmanGame model) {
        gameModel = model;
        boardModel = gameModel.getBoard();
    }

    public int getLives() {
        return gameModel.getLives();
    }

    public int getLevel() {
        return gameModel.getLevel();
    }

    public String getPacmanColour() {
        return gameModel.getHunter().isSpecialActive() ? "#CDC3FF" : "#FFE709";
    }

    public int getPacmanMouthAngle() {
        switch (gameModel.getHunter().getDirection().toString()){
            case "RIGHT":
                return 30;
            case "UP":
                return 120;
            case "LEFT":
                return 210;
            case "DOWN":
                return 300;
            default:
                return -1;
        }
    }

    public Position getPacmanPosition() {
        return gameModel.getHunter().getPosition();
    }

    public PacmanBoard getBoard() {
        return boardModel;
    }

    public List<Pair<Position,String>> getGhosts() {
        List<Pair<Position,String>> ghosts = new LinkedList<>();
        for (Ghost ghost : gameModel.getGhosts()) {
            var color = (ghost.getPhase() == Phase.FRIGHTENED) ?  "#0000FF" :  ghost.getColour();
            ghosts.add(new Pair<>(ghost.getPosition(),color));
        }
        return ghosts;
    }
}
