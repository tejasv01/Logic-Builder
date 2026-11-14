package circuitsim.commands;

/**
 * Command Pattern interface encapsulating a reversible action on the Circuit model.
 *
 * Design Pattern: COMMAND PATTERN
 * SOLID (OCP): New commands can be implemented without modifying CommandManager.
 */
public interface Command {
    void execute();
    void undo();
    String getName();
}
