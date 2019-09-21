package pacman.ghost;

import pacman.game.PacmanGame;
import pacman.util.Position;

/**
 * Clyde is a ghost that behaves in a very scared manner
 * when close to a hunter. When not chasing the hunter down, clyde
 * likes to hang out in the bottom left corner of the board in a
 * orange glow.
 *
 * @ass1
 */
public class Clyde extends Ghost {

    /**
     * Get Clydes colour.
     * @return "#e78c45"
     * @ass1
     */
    @Override
    public String getColour() {
        return "#e78c45";
    }

    /**
     * Get Clydes type/name.
     * @return CLYDE;
     * @ass1
     */
    @Override
    public GhostType getType() {
        return GhostType.CLYDE;
    }

    @Override
    public Position chaseTarget(PacmanGame game) {
        var distance = Math.sqrt(Math.pow(this.getPosition().getX() - game.getHunter().getPosition().getX(),2) +
                                 Math.pow(this.getPosition().getY() - game.getHunter().getPosition().getY(),2));
        return (distance > 8)?game.getHunter().getPosition():home(game);
    }

    @Override
    public Position home(PacmanGame game) {
        return (new Position(-1,game.getBoard().getHeight()));
    }
}
