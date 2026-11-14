package circuitsim.exceptions;

/**
 * Custom exception thrown when loading a circuit JSON file fails due to
 * malformed syntax, missing required fields, or unknown element types.
 *
 * OOM Concept: Exception Handling (Custom Checked Exception).
 */
public class FileFormatException extends Exception {
    public FileFormatException(String message) {
        super(message);
    }

    public FileFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
