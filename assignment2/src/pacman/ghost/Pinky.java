package pacman.ghost;

import pacman.game.PacmanGame;
import pacman.util.Direction;
import pacman.util.Position;

/**
 * Pinky is a cunning ghost that tries to ambush the hunter.
 * When not chasing the hunter down, Pinky likes to hang out in
 * the top left corner of the board in a pink glow.
 *
 * @ass1
 */
public class Pinky extends Ghost {

    /**
     * Get Pinky colour.
     * @return "#c397d8"
     * @ass1
     */
    @Override
    public String getColour() {
        return "#c397d8";
    }

    /**
     * Get Pinkys type/name.
     * @return PINKY;
     * @ass1
     */
    @Override
    public GhostType getType() {
        return GhostType.PINKY;
    }

    @Override
    public Position chaseTarget(PacmanGame game) {
        Position behind = game.getHunter().getDirection().opposite().offset();
        return game.getHunter().getPosition().add(behind.multiply(2));
    }

    @Override
    public Position home(PacmanGame game) {
        return new Position(-1,-1);
    }
}
