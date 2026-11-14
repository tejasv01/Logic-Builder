package circuitsim.model;

import circuitsim.exceptions.InvalidShapeException;

/**
 * Concrete 2-input AND Logic Gate. Output is HIGH (true) iff all inputs are HIGH.
 */
public class AndGate extends LogicGate {
    public AndGate(int x, int y, int width, int height, ElementStyle style)
            throws InvalidShapeException {
        super(x, y, width, height, style, 2);
    }

    @Override
    public ElementType getType() {
        return ElementType.AND;
    }

    @Override
    public String getGateSymbol() {
        return "AND (&)";
    }

    @Override
    public void evaluate() {
        boolean result = true;
        for (Pin pin : inputPins) {
            result = result && pin.getValue();
        }
        outputPin.setValue(result);
    }

    @Override
    public AndGate clone() {
        try {
            return new AndGate(x, y, width, height, style.clone());
        } catch (InvalidShapeException e) {
            throw new AssertionError("Cloning valid AndGate should not fail", e);
        }
    }
}
