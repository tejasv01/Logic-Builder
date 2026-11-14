package circuitsim.model;

import circuitsim.exceptions.InvalidShapeException;
import circuitsim.export.CircuitVisitor;
import circuitsim.ui.UITheme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;

/**
 * Directed electrical connection (Line primitive) linking an output Pin to an input Pin.
 *
 * OOM Relationship:
 * - ASSOCIATION: Wire holds references to sourcePin and targetPin owned by other
 *   CircuitElements. Wire does not own those pins.
 */
public class Wire extends CircuitElement {
    private final Pin sourcePin; // ASSOCIATION: references output Pin of source element
    private final Pin targetPin; // ASSOCIATION: references input Pin of target element

    public Wire(Pin sourcePin, Pin targetPin, ElementStyle style) throws InvalidShapeException {
        super(0, 0, 10, 10, style);
        if (sourcePin == null || targetPin == null) {
            throw new InvalidShapeException("Wire requires non-null source and target pins.");
        }
        if (sourcePin == targetPin || sourcePin.getOwner() == targetPin.getOwner()) {
            throw new InvalidShapeException("Cannot connect a component pin to itself.");
        }
        if (!sourcePin.isOutput() || targetPin.isOutput()) {
            throw new InvalidShapeException("Wire must connect an OUTPUT pin to an INPUT pin.");
        }
        this.sourcePin = sourcePin;
        this.targetPin = targetPin;
        syncGeometryFromPins();
    }

    private void syncGeometryFromPins() {
        Point p1 = sourcePin.getAbsolutePosition();
        Point p2 = targetPin.getAbsolutePosition();
        this.x = Math.min(p1.x, p2.x);
        this.y = Math.min(p1.y, p2.y);
        this.width = Math.max(10, Math.abs(p2.x - p1.x));
        this.height = Math.max(10, Math.abs(p2.y - p1.y));
    }

    public Pin getSourcePin() {
        return sourcePin;
    }

    public Pin getTargetPin() {
        return targetPin;
    }

    @Override
    public ElementType getType() {
        return ElementType.WIRE;
    }

    @Override
    public void evaluate() {
        targetPin.setValue(sourcePin.getValue());
    }

    @Override
    public boolean contains(Point p) {
        Point p1 = sourcePin.getAbsolutePosition();
        Point p2 = targetPin.getAbsolutePosition();
        double dist = Line2D.ptSegDist(p1.x, p1.y, p2.x, p2.y, p.x, p.y);
        return dist <= 10.0;
    }

    @Override
    public void draw(Graphics2D g2d) {
        syncGeometryFromPins();
        Point p1 = sourcePin.getAbsolutePosition();
        Point p2 = targetPin.getAbsolutePosition();
        boolean isHigh = sourcePin.getValue();

        float sw = Math.max(1.5f, style.getStrokeWidth());

        if (selected) {
            // Radiant selection halo
            g2d.setColor(new Color(56, 189, 248, 70));
            g2d.setStroke(new BasicStroke(sw + 6.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);

            g2d.setColor(UITheme.SELECTION_COLOR);
            g2d.setStroke(new BasicStroke(sw + 1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        } else if (isHigh) {
            // Luminous glowing neon logic signal
            g2d.setColor(new Color(34, 197, 94, 50));
            g2d.setStroke(new BasicStroke(sw + 6.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);

            g2d.setColor(new Color(34, 197, 94, 120));
            g2d.setStroke(new BasicStroke(sw + 3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);

            // Bright core
            g2d.setColor(new Color(74, 222, 128));
            g2d.setStroke(new BasicStroke(sw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);

            // Signal node indicator at midpoint
            int mx = (p1.x + p2.x) / 2;
            int my = (p1.y + p2.y) / 2;
            g2d.setColor(new Color(187, 247, 208));
            g2d.fillOval(mx - 3, my - 3, 6, 6);
        } else {
            // Inactive signal line
            Color defaultInactive = UITheme.isDarkMode()
                    ? new Color(71, 85, 105)
                    : new Color(148, 163, 184);
            Color inactive = (style != null && style.getStrokeColor() != null && !style.getStrokeColor().equals(new Color(30, 41, 59)))
                    ? style.getStrokeColor()
                    : defaultInactive;
            g2d.setColor(inactive);
            g2d.setStroke(new BasicStroke(sw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        // Terminal end caps
        g2d.setColor(isHigh ? new Color(34, 197, 94) : (UITheme.isDarkMode() ? new Color(71, 85, 105) : Color.DARK_GRAY));
        g2d.fillOval(p1.x - 3, p1.y - 3, 6, 6);
        g2d.fillOval(p2.x - 3, p2.y - 3, 6, 6);
    }

    @Override
    public void accept(CircuitVisitor visitor) {
        visitor.visitWire(this);
    }

    @Override
    public Wire clone() {
        try {
            return new Wire(sourcePin, targetPin, style.clone());
        } catch (InvalidShapeException e) {
            throw new AssertionError(e);
        }
    }
}
