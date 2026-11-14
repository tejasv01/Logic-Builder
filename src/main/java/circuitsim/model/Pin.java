package circuitsim.model;

import java.awt.Point;

/**
 * Represents an electrical input or output terminal (Pin) owned by a CircuitElement.
 *
 * OOM Relationship:
 * - COMPOSITION: A Pin is strictly composed inside its owning CircuitElement
 *   (LogicGate, InputSwitch, or OutputLed). Its coordinate is relative to its owner,
 *   and its lifecycle is bound to the owner element.
 */
public class Pin {
    private final String id;
    private final CircuitElement owner; // Composition back-reference to owning element
    private final boolean output;
    private int offsetX;
    private int offsetY;
    private boolean value;

    public Pin(String id, CircuitElement owner, boolean output, int offsetX, int offsetY) {
        this.id = id;
        this.owner = owner;
        this.output = output;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.value = false;
    }

    public String getId() {
        return id;
    }

    public CircuitElement getOwner() {
        return owner;
    }

    public boolean isOutput() {
        return output;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsets(int offsetX, int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public Point getAbsolutePosition() {
        return new Point(owner.getX() + offsetX, owner.getY() + offsetY);
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public boolean hitTest(Point p, int tolerance) {
        Point abs = getAbsolutePosition();
        return abs.distance(p) <= tolerance;
    }
}
