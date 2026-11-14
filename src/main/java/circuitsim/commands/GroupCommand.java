package circuitsim.commands;

import circuitsim.exceptions.InvalidShapeException;
import circuitsim.model.Circuit;
import circuitsim.model.CircuitElement;
import circuitsim.model.ElementStyle;
import circuitsim.model.GroupShape;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete Command that groups multiple CircuitElements into a Composite GroupShape
 * (or ungroups an existing GroupShape back into individual elements).
 */
public class GroupCommand implements Command {
    private final Circuit circuit;
    private final List<CircuitElement> members;
    private final GroupShape groupShape;
    private final boolean ungroupMode;

    public GroupCommand(Circuit circuit, List<CircuitElement> members) throws InvalidShapeException {
        this.circuit = circuit;
        this.members = new ArrayList<>(members);
        this.groupShape = new GroupShape(this.members, ElementStyle.createDefault());
        this.ungroupMode = false;
    }

    public GroupCommand(Circuit circuit, GroupShape existingGroup) {
        this.circuit = circuit;
        this.groupShape = existingGroup;
        this.members = new ArrayList<>(existingGroup.getChildren());
        this.ungroupMode = true;
    }

    @Override
    public void execute() {
        if (!ungroupMode) {
            for (CircuitElement m : members) {
                m.setSelected(false);
                circuit.removeElement(m);
            }
            groupShape.setSelected(true);
            circuit.addElement(groupShape);
        } else {
            circuit.removeElement(groupShape);
            for (CircuitElement m : members) {
                m.setSelected(true);
                circuit.addElement(m);
            }
        }
    }

    @Override
    public void undo() {
        if (!ungroupMode) {
            circuit.removeElement(groupShape);
            for (CircuitElement m : members) {
                m.setSelected(true);
                circuit.addElement(m);
            }
        } else {
            for (CircuitElement m : members) {
                m.setSelected(false);
                circuit.removeElement(m);
            }
            groupShape.setSelected(true);
            circuit.addElement(groupShape);
        }
    }

    @Override
    public String getName() {
        return ungroupMode ? "Ungroup Subcircuit" : "Group Subcircuit";
    }
}
