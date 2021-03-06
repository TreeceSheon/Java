package pacman.game;

import pacman.util.Direction;
import pacman.util.Position;

/**
 * Entity
 *
 * A entity is the animated objects in the game that can traverse
 * the game board and interact with other entities.
 * @ass1
 */
public abstract class Entity implements Moveable {

    // position of the entity
    private Position position;
    // direction the entity is facing
    private Direction direction;

    /**
     * Creates an entity that is at position (0, 0) and is facing UP.
     * @ass1
     */
    public Entity() {
        position = new Position(0, 0);
        direction = Direction.UP;
    }

    /**
     * Creates an entity that is at the given position facing in the
     * given direction.
     *
     * If the position is null then the position will be the same
     * as the default position ( 0, 0 ). If the direction is null
     * then the direction will be the same as the default ( UP ).
     *
     * @param position to be set to.
     * @param direction to be facing.
     * @ass1
     */
    public Entity(Position position, Direction direction) {
        this();

        if (position != null) {
            this.position = position;
        }

        if (direction != null) {
            this.direction = direction;
        }
    }

    /**
     * {@inheritDoc}
     * @ass1
     */
    public Position getPosition() {
        return this.position;
    }

    /**
     * {@inheritDoc}
     * @ass1
     */
    public void setPosition(Position position) {
        if (position != null) {
            this.position = position;
        }
    }

    /**
     * {@inheritDoc}
     * @ass1
     */
    public Direction getDirection() {
        return this.direction;
    }

    /**
     * {@inheritDoc}
     * @ass1
     */
    public void setDirection(Direction direction) {
        if (direction != null) {
            this.direction = direction;
        }
    }

    /**
     * Checks if another object instance is equal to this instance.
     * Entities are equal if their positions and directions are equal.
     * @param o object to be compared
     * @return true if same, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Entity)) {
            return false;
        } else {
            var other = (Entity)o;
            return this.direction.equals(other.direction) && this.position.equals(other.position);
        }
    }

    /**
     * For two objects that are equal the hash should also be equal.
     * For two objects that are not equal the hash does not have to be different.
     * @return hashcode of current instance.
     */
    @Override
    public int hashCode() {
        return this.direction.hashCode() + this.position.hashCode();
    }

    /**
     * Represents this entity in a comma-separated string format
     * @return string in "x,y,DIRECTION".
     */
    @Override
    public String toString() {
        return String.format("%d,%d,%s", getPosition().getX(), getPosition().getY(), getDirection());
    }
}
