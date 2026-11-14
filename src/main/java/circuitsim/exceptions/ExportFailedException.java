package circuitsim.exceptions;

/**
 * Custom exception thrown when exporting the circuit to SVG, PNG, or JSON fails
 * due to I/O errors or rendering issues.
 *
 * OOM Concept: Exception Handling (Custom Checked Exception).
 */
public class ExportFailedException extends Exception {
    public ExportFailedException(String message) {
        super(message);
    }

    public ExportFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
