package circuitsim.model;

import circuitsim.exceptions.InvalidShapeException;

/**
 * Concrete 2-input OR Logic Gate. Output is HIGH (true) if at least one input is HIGH.
 */
public class OrGate extends LogicGate {
    public OrGate(int x, int y, int width, int height, ElementStyle style)
            throws InvalidShapeException {
        super(x, y, width, height, style, 2);
    }

    @Override
    public ElementType getType() {
        return ElementType.OR;
    }

    @Override
    public String getGateSymbol() {
        return "OR (>=1)";
    }

    @Override
    public void evaluate() {
        boolean result = false;
        for (Pin pin : inputPins) {
            result = result || pin.getValue();
        }
        outputPin.setValue(result);
    }

    @Override
    public OrGate clone() {
        try {
            return new OrGate(x, y, width, height, style.clone());
        } catch (InvalidShapeException e) {
            throw new AssertionError("Cloning valid OrGate should not fail", e);
        }
    }
}
