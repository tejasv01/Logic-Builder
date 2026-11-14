package circuitsim.model;

import circuitsim.exceptions.InvalidShapeException;

/**
 * Concrete 2-input NOR Logic Gate. Output is HIGH iff all inputs are LOW.
 */
public class NorGate extends LogicGate {
    public NorGate(int x, int y, int width, int height, ElementStyle style)
            throws InvalidShapeException {
        super(x, y, width, height, style, 2);
    }

    @Override
    public ElementType getType() {
        return ElementType.NOR;
    }

    @Override
    public String getGateSymbol() {
        return "NOR";
    }

    @Override
    public void evaluate() {
        boolean orResult = false;
        for (Pin pin : inputPins) {
            orResult = orResult || pin.getValue();
        }
        outputPin.setValue(!orResult);
    }

    @Override
    public NorGate clone() {
        try {
            return new NorGate(x, y, width, height, style.clone());
        } catch (InvalidShapeException e) {
            throw new AssertionError("Cloning valid NorGate should not fail", e);
        }
    }
}
