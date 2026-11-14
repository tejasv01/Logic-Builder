package circuitsim.model;

import circuitsim.exceptions.InvalidShapeException;

/**
 * Concrete 1-input NOT (Inverter) Logic Gate. Output is the logical negation of its input.
 */
public class NotGate extends LogicGate {
    public NotGate(int x, int y, int width, int height, ElementStyle style)
            throws InvalidShapeException {
        super(x, y, width, height, style, 1);
    }

    @Override
    public ElementType getType() {
        return ElementType.NOT;
    }

    @Override
    public String getGateSymbol() {
        return "NOT (!)";
    }

    @Override
    public void evaluate() {
        outputPin.setValue(!inputPins.get(0).getValue());
    }

    @Override
    public NotGate clone() {
        try {
            return new NotGate(x, y, width, height, style.clone());
        } catch (InvalidShapeException e) {
            throw new AssertionError("Cloning valid NotGate should not fail", e);
        }
    }
}
