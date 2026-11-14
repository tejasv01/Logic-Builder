package circuitsim.model;

import circuitsim.exceptions.InvalidShapeException;

/**
 * Concrete 2-input XOR Logic Gate. Output is HIGH iff inputs differ.
 */
public class XorGate extends LogicGate {
    public XorGate(int x, int y, int width, int height, ElementStyle style)
            throws InvalidShapeException {
        super(x, y, width, height, style, 2);
    }

    @Override
    public ElementType getType() {
        return ElementType.XOR;
    }

    @Override
    public String getGateSymbol() {
        return "XOR (^)";
    }

    @Override
    public void evaluate() {
        boolean a = inputPins.get(0).getValue();
        boolean b = inputPins.get(1).getValue();
        outputPin.setValue(a ^ b);
    }

    @Override
    public XorGate clone() {
        try {
            return new XorGate(x, y, width, height, style.clone());
        } catch (InvalidShapeException e) {
            throw new AssertionError("Cloning valid XorGate should not fail", e);
        }
    }
}
