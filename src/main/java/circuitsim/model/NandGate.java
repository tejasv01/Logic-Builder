package circuitsim.model;

import circuitsim.exceptions.InvalidShapeException;

/**
 * Concrete 2-input NAND Logic Gate. Output is LOW iff all inputs are HIGH.
 */
public class NandGate extends LogicGate {
    public NandGate(int x, int y, int width, int height, ElementStyle style)
            throws InvalidShapeException {
        super(x, y, width, height, style, 2);
    }

    @Override
    public ElementType getType() {
        return ElementType.NAND;
    }

    @Override
    public String getGateSymbol() {
        return "NAND";
    }

    @Override
    public void evaluate() {
        boolean andResult = true;
        for (Pin pin : inputPins) {
            andResult = andResult && pin.getValue();
        }
        outputPin.setValue(!andResult);
    }

    @Override
    public NandGate clone() {
        try {
            return new NandGate(x, y, width, height, style.clone());
        } catch (InvalidShapeException e) {
            throw new AssertionError("Cloning valid NandGate should not fail", e);
        }
    }
}
