package pacman.util;
/**
 * Exception thrown when a game file cannot be unpacked.
 */
public class UnpackableException extends Exception {
    /**
     * Standard UnpackableException which takes no parameters,
     */
    public UnpackableException() {};

    /**
     * A UnpackableException that contains a message.
     * @param message to add to the exception.
     */
    public UnpackableException(String message) {
        super(message);
    }
}
