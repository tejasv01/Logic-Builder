package circuitsim.commands;

import circuitsim.exceptions.EmptyUndoStackException;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Invoker class in the Command Pattern that manages the undo and redo stacks.
 *
 * OOM Relationship:
 * - AGGREGATION: Holds Command instances in undoStack and redoStack.
 */
public class CommandManager {
    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    public void executeCommand(Command command) {
        if (command != null) {
            command.execute();
            undoStack.push(command);
            redoStack.clear();
        }
    }

    public void undo() throws EmptyUndoStackException {
        if (undoStack.isEmpty()) {
            throw new EmptyUndoStackException("Nothing to undo: undo stack is empty.");
        }
        Command cmd = undoStack.pop();
        cmd.undo();
        redoStack.push(cmd);
    }

    public void redo() throws EmptyUndoStackException {
        if (redoStack.isEmpty()) {
            throw new EmptyUndoStackException("Nothing to redo: redo stack is empty.");
        }
        Command cmd = redoStack.pop();
        cmd.execute();
        undoStack.push(cmd);
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}
