package circuitsim.commands;

import circuitsim.model.Circuit;
import circuitsim.model.CircuitElement;
import circuitsim.model.Wire;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete Command that removes selected CircuitElement(s) along with any attached Wires,
 * and cleanly restores both the elements and their wires on undo().
 */
public class DeleteCommand implements Command {
    private final Circuit circuit;
    private final List<CircuitElement> deletedElements = new ArrayList<>();
    private final List<Wire> attachedWires = new ArrayList<>();

    public DeleteCommand(Circuit circuit, List<CircuitElement> targets) {
        this.circuit = circuit;
        this.deletedElements.addAll(targets);
        for (CircuitElement el : targets) {
            for (Wire w : circuit.getWiresConnectedTo(el)) {
                if (!deletedElements.contains(w) && !attachedWires.contains(w)) {
                    attachedWires.add(w);
                }
            }
        }
    }

    @Override
    public void execute() {
        for (Wire w : attachedWires) {
            circuit.removeElement(w);
        }
        for (CircuitElement el : deletedElements) {
            circuit.removeElement(el);
        }
    }

    @Override
    public void undo() {
        for (CircuitElement el : deletedElements) {
            circuit.addElement(el);
        }
        for (Wire w : attachedWires) {
            circuit.addElement(w);
        }
    }

    @Override
    public String getName() {
        return "Delete Element(s)";
    }
}
