package pacman.ghost;

import pacman.game.Entity;
import pacman.game.PacmanGame;
import pacman.util.Direction;
import pacman.util.Position;
import java.lang.Math;
import java.util.*;
import java.util.zip.DeflaterInputStream;

/**
 * An Abstract Ghost which is a game entity.
 *
 * @ass1
 */
public abstract class Ghost extends Entity {

    // whether the ghost is dead
    private boolean dead;
    // current phase of this ghost
    private Phase phase;
    // duration of current phase
    private int phaseDuration;

    private Position target;

    /**
     * Creates a ghost which is alive and starts in the SCATTER phase
     * with a duration of Phase.SCATTER.duration(). This ghost also has
     * a default position of (0, 0) and a default direction of facing
     * up.
     *
     * @ass1
     */
    public Ghost() {
        super();
        dead = false;
        phase = Phase.SCATTER;
        phaseDuration = Phase.SCATTER.getDuration();
    }

    /**
     * Sets the Ghost Phase and its duration overriding any current
     * phase information.
     *
     * if Phase is null then no changes are made. If the duration is
     * less than zero then the duration is set to 0.
     *
     * @param newPhase to set the ghost to.
     * @param duration of ticks for the phase to last for.
     * @ass1
     */
    public void setPhase(Phase newPhase, int duration) {
        if (newPhase != null) {
            phase = newPhase;
            phaseDuration = Integer.max(0, duration);
        }
    }

    /**
     * Get the phase that the ghost currently is in.
     * @return the set phase.
     * @ass1
     */
    public Phase getPhase() {
        return phase;
    }

    /*
     * NextPhase decreases our phase duration and moves us to the
     * next phase if it is 0.
     *
     * - CHASE goes to SCATTER.
     * - FRIGHTENED && SCATTER go to CHASE.
     */
    private void nextPhase() {
        phaseDuration = Integer.max(0, phaseDuration - 1);
        if (phaseDuration == 0) {
            switch (getPhase()) {
                case CHASE:
                    setPhase(Phase.SCATTER, Phase.SCATTER.getDuration());
                    break;
                case FRIGHTENED:
                case SCATTER:
                    setPhase(Phase.CHASE, Phase.CHASE.getDuration());
                    break;
            }
        }
    }

    /**
     * Gets the phase info of the ghost.
     * @return the phase and duration formatted as such: "PHASE:DURATION".
     * @ass1
     */
    public String phaseInfo() {
        return String.format("%s:%d", phase, phaseDuration);
    }

    /**
     * Gets the ghosts colour.
     * @return hex version of the ghosts colour, e.g. #FFFFFF for white.
     * @ass1
     */
    public abstract String getColour();

    /**
     * Gets the ghosts type.
     * @return this ghosts type.
     * @ass1
     */
    public abstract GhostType getType();

    /**
     * Kills this ghost by setting its status to isDead.
     * @ass1
     */
    public void kill() {
        this.dead = true;
    }

    /**
     * Checks if this ghost is dead.
     * @return true if dead, false otherwise.
     * @ass1
     */
    public boolean isDead() {
        return dead;
    }

    /**
     * Resets the ghost back to an initial state where:
     *
     * <ul>
     *     <li>It is alive</li>
     *     <li>With a Phase of SCATTER with duration SCATTER.getDuration()</li>
     *     <li>Facing in the Direction.UP</li>
     *     <li>With a Position of ( 0, 0 )</li>
     * </ul>
     * @ass1
     */
    public void reset() {
        dead = false;
        this.phase = Phase.SCATTER;
        this.phaseDuration = Phase.SCATTER.getDuration();
        this.setDirection(Direction.UP);
        this.setPosition(new Position(0, 0));
    }

    /**
     * Gets the target block that we should be heading towards when in the chase phase.
     * @param game to read the position from
     * @return the ghost target position.
     */
    public abstract Position chaseTarget(PacmanGame game);

    /**
     * Gets the home block that we should be heading towards when in the scatter phase.
     * @param game to read the board from.
     * @return the ghosts home position.
     */
    public abstract Position home(PacmanGame game);

    /**
     * Move advances the ghost in a direction by one point on the board.
     * The direction this move is made is done as follows:
     * Decrease the phase duration by 1, and if the duration is now zero, then move to the next phase.
     * Get the target position. If the phase is CHASE, then get the chaseTarget.
     * If the phase is SCATTER, then the position is the ghost's home position.
     * If the phase is FRIGHTENED, then choose a target position with coordinates given by:
     * targetPositionX = (x*24 mod (2 * board width )) - board width,
     * targetPositionY = (y*36 mod (2 * board height)) - board height
     * where x and y are the current coordinates of the ghost.
     * Choose the direction that the current Ghost position
     * when moved 1 step has the smallest euclidean distance to the target position.
     * The board item in the move position must be pathable for it to be chosen.
     * The chosen direction cannot be opposite to the current direction.
     * If multiple directions have the same shortest distance,
     * then choose the direction in the order UP, LEFT, DOWN, RIGHT
     * Set the direction of the Ghost to the chosen direction.
     * Set the position of this Ghost to be one forward step in the chosen direction.
     * @param game the game where entity is moving.
     */
    public void move(PacmanGame game) {
        this.phaseDuration--;
        if (this.phaseDuration == 0) {
           nextPhase();
        }
        if (this.phase == Phase.CHASE) {
            target = chaseTarget(game);
        } else if (this.phase == Phase.SCATTER) {
            target = home(game);
        } else if (this.phase == Phase.FRIGHTENED) {
            var x = this.getPosition().getX()*24%(2*game.getBoard().getWidth()) - game.getBoard().getWidth();
            var y = this.getPosition().getY()*36%(2*game.getBoard().getHeight()) - game.getBoard().getHeight();
            target = new Position(x,y);
        }
        //chose next direction to go.
        changeDirection(game);
    }
    /*
    find the pathable direction with minimum distance to the target.
     */
    private void changeDirection(PacmanGame game) {
        // have map initialized which will be storing pairs of direction and distance.
        HashMap<Direction, Double> distances = new HashMap<>();
        // calculate distance for directions that are pathable.
        for (Direction dir : Direction.values()) {
            //check the item stored on the next position is pathable.
            var item = game.getBoard().getEntry(getPosition().add(dir.offset()));
            if (item.getPathable()) {
                var nextPosition = getPosition().add(dir.offset());
                //distance calculation.
                distances.put(dir,nextPosition.distance(chaseTarget(game)));
            }
        }
        //sort pairs by values in descending order.
        var items = new ArrayList<>(distances.entrySet());
        items.sort(new Comparator<>() {
            @Override
            public int compare(Map.Entry<Direction, Double> v1, Map.Entry<Direction, Double> v2) {
                return v1.getValue().compareTo(v2.getValue());
            }
        });
        // set alternative order for values that might be same.
        Direction[] Order = { Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT };
        var minDistance = items.get(0).getValue();
        //do ordered traversal to find the prior direction.
        for(Direction d : Order) {
            if (distances.get(d).equals(minDistance)) {
                //break when find it.
                setDirection(d);
                break;
            }
        }
    }

    /**
     * Checks if another object instance is equal to this Ghost.
     * Ghosts are equal if they have the same alive/dead status,
     * phase duration ,current phase, direction and position.
     * @param o object to be compared
     * @return true if equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Ghost)) {
            return false;
        } else {
            return (isDead() == ((Ghost) o).isDead() && phase == ((Ghost) o).getPhase() &&
                    phaseDuration == ((Ghost) o).phaseDuration && getDirection() == ((Ghost) o).getDirection()
                    && getPosition() == ((Ghost) o).getPosition());
        }
    }

    /**
     * For two objects that are equal the hash should also be equal.
     * For two objects that are not equal the hash does not have to be different.
     * @return hashcode of ghost.
     */
    @Override
    public int hashCode() {
        var a = (isDead())?1:-1;
        return a + phaseDuration + getPhase().hashCode() + getDirection().hashCode() + getPosition().hashCode();
    }

    /**
     * Represents this Ghost in a comma-seperated string format.
     * Format is: "x,y,DIRECTION,PHASE:phaseDuration".
     * @return "x,y,DIRECTION,PHASE:phaseDuration".
     */
    @Override
    public String toString() {
        return getPosition().getX() + "," + getPosition().getY()+ "," + getDirection()
                +"," + getPhase() + "," + phaseDuration;
    }

    public static void main(String[] args) {
        var a = new HashMap<Direction,Double>();
        a.put(Direction.UP,123.5);
        a.put(Direction.DOWN,445.5);
        a.put(Direction.LEFT,123.5);
        a.put(Direction.RIGHT,421.2);
        var temp = new LinkedList<>(a.entrySet());
        temp.sort(new Comparator<>() {
            @Override
            public int compare(Map.Entry<Direction, Double> ele1, Map.Entry<Direction, Double> ele2) {
                return ele1.getValue().compareTo(ele2.getValue());
            }
        });
        for (Iterator it = a.entrySet().iterator();it.hasNext();) {
            var tempp = (Map.Entry)it.next();
            System.out.println(tempp.getValue());
        }

    }
}
