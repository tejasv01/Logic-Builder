package circuitsim.model;

import circuitsim.exceptions.InvalidShapeException;
import circuitsim.export.CircuitVisitor;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Composite Subcircuit / Group element that treats a collection of CircuitElements
 * uniformly as a single selectable, movable, stylable, and clonable CircuitElement.
 *
 * Design Pattern:
 * - COMPOSITE PATTERN: GroupShape is the Composite; CircuitElement is the Component.
 *
 * OOM Relationship:
 * - AGGREGATION: GroupShape holds child CircuitElement objects that can exist
 *   independently before grouping and after ungrouping.
 */
public class GroupShape extends CircuitElement {
    // AGGREGATION: Children can survive independently when ungrouped
    private final List<CircuitElement> children = new ArrayList<>();

    public GroupShape(List<CircuitElement> elements, ElementStyle style)
            throws InvalidShapeException {
        super(0, 0, 40, 40, style);
        if (elements == null || elements.isEmpty()) {
            throw new InvalidShapeException("GroupShape must contain at least one child element.");
        }
        this.children.addAll(elements);
        recalculateBounds();
    }

    public final void recalculateBounds() {
        if (children.isEmpty()) {
            return;
        }
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (CircuitElement child : children) {
            Rectangle b = child.getBounds();
            minX = Math.min(minX, b.x);
            minY = Math.min(minY, b.y);
            maxX = Math.max(maxX, b.x + b.width);
            maxY = Math.max(maxY, b.y + b.height);
        }
        this.x = minX - 6;
        this.y = minY - 6;
        this.width = Math.max(20, (maxX - minX) + 12);
        this.height = Math.max(20, (maxY - minY) + 12);
    }

    public List<CircuitElement> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public ElementType getType() {
        return ElementType.GROUP;
    }

    @Override
    public void move(int dx, int dy) {
        for (CircuitElement child : children) {
            if (child.getType() != ElementType.WIRE) {
                child.move(dx, dy);
            }
        }
        recalculateBounds();
    }

    @Override
    public void setStyle(ElementStyle newStyle) {
        super.setStyle(newStyle);
        for (CircuitElement child : children) {
            child.setStyle(newStyle);
        }
    }

    @Override
    public void evaluate() {
        for (CircuitElement child : children) {
            child.evaluate();
        }
    }

    @Override
    public boolean contains(Point p) {
        for (CircuitElement child : children) {
            if (child.contains(p)) {
                return true;
            }
        }
        return getBounds().contains(p);
    }

    @Override
    public void draw(Graphics2D g2d) {
        recalculateBounds();
        for (CircuitElement child : children) {
            child.draw(g2d);
        }
        float[] dash = {5.0f, 5.0f};
        g2d.setColor(selected ? new Color(37, 99, 235) : new Color(100, 116, 139));
        g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
        g2d.drawRoundRect(x, y, width, height, 10, 10);
    }

    @Override
    public void accept(CircuitVisitor visitor) {
        visitor.visitGroup(this);
    }

    @Override
    public GroupShape clone() {
        try {
            List<CircuitElement> clonedChildren = new ArrayList<>();
            for (CircuitElement child : children) {
                if (child.getType() != ElementType.WIRE) {
                    clonedChildren.add(child.clone());
                }
            }
            return new GroupShape(clonedChildren, style.clone());
        } catch (InvalidShapeException e) {
            throw new AssertionError(e);
        }
    }
}
