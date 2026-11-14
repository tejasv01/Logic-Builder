package circuitsim.commands;

import circuitsim.model.Circuit;
import circuitsim.model.CircuitElement;

/**
 * Concrete Command to add a CircuitElement (gate, switch, LED, wire, label) to the Circuit.
 */
public class AddShapeCommand implements Command {
    private final Circuit circuit;
    private final CircuitElement element;

    public AddShapeCommand(Circuit circuit, CircuitElement element) {
        this.circuit = circuit;
        this.element = element;
    }

    @Override
    public void execute() {
        circuit.addElement(element);
    }

    @Override
    public void undo() {
        circuit.removeElement(element);
    }

    @Override
    public String getName() {
        return "Add " + element.getType();
    }
}
