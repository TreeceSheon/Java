package pacman.hunter;


import pacman.game.PacmanGame;

/**
 * A Speedy hunter that has a special ability that allows the hunter
 * to travel twice as fast.
 *
 * @ass1
 */
public class Speedy extends Hunter {

    /**
     * Creates a Speedy Hunter with its special ability.
     *
     * see {@link Hunter#Hunter()}
     * @ass1
     */
    public Speedy() {
        super();
    }

    /**
     * Creates a Speedy Hunter by copying the internal state of
     * another hunter.
     *
     * see {@link pacman.hunter.Hunter#Hunter(Hunter)}
     *
     * @param original hunter to copy from
     * @ass1
     */
    public Speedy(Hunter original) {
        super(original);
    }

    public void move(PacmanGame game) {
        if (isSpecialActive()) {
            super.move(game);
            super.move(game);
            activateSpecial(getSpecialDurationRemaining() - 2);
        }
    }

    public String toString() {
        return getPosition().getX() + "," + getPosition().getY() +
                "," + getDirection() +  getSpecialDurationRemaining() + "," + "SPEEDY";
    }
}
