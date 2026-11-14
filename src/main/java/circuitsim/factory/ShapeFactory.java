package circuitsim.factory;

import circuitsim.exceptions.InvalidShapeException;
import circuitsim.model.AndGate;
import circuitsim.model.CircuitElement;
import circuitsim.model.ElementStyle;
import circuitsim.model.ElementType;
import circuitsim.model.InputSwitch;
import circuitsim.model.NandGate;
import circuitsim.model.NorGate;
import circuitsim.model.NotGate;
import circuitsim.model.OrGate;
import circuitsim.model.OutputLed;
import circuitsim.model.Pin;
import circuitsim.model.TextLabel;
import circuitsim.model.Wire;
import circuitsim.model.XorGate;

/**
 * Factory class responsible for instantiating concrete CircuitElement objects
 * and validating creation parameters.
 *
 * Design Pattern:
 * - FACTORY PATTERN: Centralizes object creation so tools and JSON loaders do
 *   not couple directly to concrete gate constructors.
 *
 * SOLID Principles:
 * - SRP: Solely responsible for constructing and validating CircuitElements.
 * - DIP: Clients depend on the abstract CircuitElement product type.
 */
public final class ShapeFactory {

    private ShapeFactory() {
        // Utility factory class
    }

    public static CircuitElement createElement(
            ElementType type,
            int x,
            int y,
            int width,
            int height,
            ElementStyle style,
            String text) throws InvalidShapeException {

        if (type == null) {
            throw new InvalidShapeException("ElementType cannot be null.");
        }

        switch (type) {
            case AND:
                return new AndGate(x, y, width, height, style);
            case OR:
                return new OrGate(x, y, width, height, style);
            case NOT:
                return new NotGate(x, y, width, height, style);
            case XOR:
                return new XorGate(x, y, width, height, style);
            case NAND:
                return new NandGate(x, y, width, height, style);
            case NOR:
                return new NorGate(x, y, width, height, style);
            case SWITCH:
                return new InputSwitch(x, y, width, height, style);
            case LED:
                return new OutputLed(x, y, width, height, style);
            case TEXT:
                return new TextLabel(x, y, width, height, style, text);
            default:
                throw new InvalidShapeException("Unsupported element type for standard factory creation: " + type);
        }
    }

    public static Wire createWire(Pin sourcePin, Pin targetPin, ElementStyle style)
            throws InvalidShapeException {
        return new Wire(sourcePin, targetPin, style);
    }
}
