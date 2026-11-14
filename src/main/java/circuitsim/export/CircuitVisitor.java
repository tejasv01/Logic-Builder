package circuitsim.export;

import circuitsim.model.GroupShape;
import circuitsim.model.InputSwitch;
import circuitsim.model.LogicGate;
import circuitsim.model.OutputLed;
import circuitsim.model.TextLabel;
import circuitsim.model.Wire;

/**
 * Visitor Pattern interface for traversing concrete CircuitElement instances
 * without polluting domain classes with format-specific serialization logic.
 *
 * Design Pattern: VISITOR PATTERN
 * SOLID (OCP & SRP): Keeps SVG and JSON export logic decoupled from model classes.
 */
public interface CircuitVisitor {
    void visitGate(LogicGate gate);
    void visitSwitch(InputSwitch sw);
    void visitLed(OutputLed led);
    void visitWire(Wire wire);
    void visitText(TextLabel label);
    void visitGroup(GroupShape group);
}
