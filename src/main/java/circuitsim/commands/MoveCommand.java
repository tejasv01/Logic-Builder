package circuitsim.commands;

import circuitsim.model.Circuit;
import circuitsim.model.CircuitElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Concrete Command that moves one or more selected CircuitElements by (dx, dy)
 * and reverses the translation on undo().
 */
public class MoveCommand implements Command {
    private final Circuit circuit;
    private final List<CircuitElement> targets;
    private final int dx;
    private final int dy;

    public MoveCommand(Circuit circuit, CircuitElement target, int dx, int dy) {
        this(circuit, Collections.singletonList(target), dx, dy);
    }

    public MoveCommand(Circuit circuit, List<CircuitElement> targets, int dx, int dy) {
        this.circuit = circuit;
        this.targets = new ArrayList<>(targets);
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    public void execute() {
        for (CircuitElement el : targets) {
            el.move(dx, dy);
        }
        circuit.notifyListeners();
    }

    @Override
    public void undo() {
        for (CircuitElement el : targets) {
            el.move(-dx, -dy);
        }
        circuit.notifyListeners();
    }

    @Override
    public String getName() {
        return "Move Element(s)";
    }
}
