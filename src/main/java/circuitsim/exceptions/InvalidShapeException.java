package circuitsim.exceptions;

/**
 * Custom exception thrown when a circuit element, wire connection, or geometric
 * dimension is invalid (e.g., non-positive width/height, null text, invalid pin wiring).
 *
 * OOM Concept: Exception Handling (Custom Checked Exception) & Encapsulation.
 */
public class InvalidShapeException extends Exception {
    public InvalidShapeException(String message) {
        super(message);
    }

    public InvalidShapeException(String message, Throwable cause) {
        super(message, cause);
    }
}
