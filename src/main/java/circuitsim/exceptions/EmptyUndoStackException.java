package circuitsim.exceptions;

/**
 * Custom exception thrown when an undo() or redo() operation is requested
 * while the corresponding history stack in CommandManager is empty.
 *
 * OOM Concept: Exception Handling (Custom Checked Exception).
 */
public class EmptyUndoStackException extends Exception {
    public EmptyUndoStackException(String message) {
        super(message);
    }
}
