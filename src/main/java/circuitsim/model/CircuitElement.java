package circuitsim.model;

import circuitsim.exceptions.InvalidShapeException;
import circuitsim.export.CircuitVisitor;
import circuitsim.ui.UITheme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Abstract base class representing any visual and logical element on the circuit canvas.
 *
 * OOM Concepts:
 * - Abstraction & Inheritance: Defines the common contract for gates, switches, LEDs, wires, labels, and groups.
 * - Polymorphism: Subclasses override draw(), evaluate(), accept(), and clone().
 * - Composition: Owns an ElementStyle instance (strong lifecycle ownership).
 */
public abstract class CircuitElement implements Cloneable {
    protected String id;
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected ElementStyle style; // COMPOSITION: CircuitElement owns its ElementStyle
    protected boolean selected;

    protected CircuitElement(int x, int y, int width, int height, ElementStyle style)
            throws InvalidShapeException {
        if (width <= 0 || height <= 0) {
            throw new InvalidShapeException("Element dimensions must be positive (width=" + width + ", height=" + height + ").");
        }
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.style = (style != null) ? style.clone() : ElementStyle.createDefault();
        this.selected = false;
    }

    public abstract ElementType getType();

    public abstract void draw(Graphics2D g2d);

    public abstract void evaluate();

    public abstract void accept(CircuitVisitor visitor);

    @Override
    public abstract CircuitElement clone();

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public boolean contains(Point p) {
        return getBounds().contains(p);
    }

    public void move(int dx, int dy) {
        this.x += dx;
        this.y += dy;
        updatePinOffsets();
    }

    public List<Pin> getPins() {
        return Collections.emptyList();
    }

    public void resize(int newWidth, int newHeight) throws InvalidShapeException {
        if (newWidth < 20 || newHeight < 20) {
            throw new InvalidShapeException("Minimum resized dimensions are 20x20 pixels.");
        }
        this.width = newWidth;
        this.height = newHeight;
        updatePinOffsets();
    }

    protected void updatePinOffsets() {
        // Subclasses with pins override this to recalculate pin positions on resize
    }

    protected void drawPins(Graphics2D g2d) {
        for (Pin pin : getPins()) {
            Point pos = pin.getAbsolutePosition();
            boolean high = pin.getValue();

            // Outer subtle terminal ring
            if (high) {
                // Neon glow halo
                g2d.setColor(new Color(34, 197, 94, 90));
                g2d.fillOval(pos.x - 7, pos.y - 7, 14, 14);

                g2d.setColor(new Color(34, 197, 94));
                g2d.fillOval(pos.x - 4, pos.y - 4, 8, 8);

                g2d.setColor(new Color(255, 255, 255, 200));
                g2d.fillOval(pos.x - 1, pos.y - 1, 3, 3);
            } else {
                g2d.setColor(UITheme.isDarkMode() ? new Color(51, 65, 85) : new Color(203, 213, 225));
                g2d.fillOval(pos.x - 5, pos.y - 5, 10, 10);

                g2d.setColor(UITheme.isDarkMode() ? new Color(100, 116, 139) : new Color(71, 85, 105));
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawOval(pos.x - 5, pos.y - 5, 10, 10);
            }
        }
    }

    protected void drawSelectionHandles(Graphics2D g2d) {
        if (!selected) {
            return;
        }
        // Modern CAD-style bounding box
        g2d.setColor(UITheme.SELECTION_HALO);
        g2d.fillRoundRect(x - 3, y - 3, width + 6, height + 6, 8, 8);

        float[] dash = {4.0f, 4.0f};
        g2d.setColor(UITheme.SELECTION_COLOR);
        g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
        g2d.drawRoundRect(x - 3, y - 3, width + 6, height + 6, 8, 8);

        // Circular resize handle at bottom-right
        int rx = x + width;
        int ry = y + height;
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.setColor(Color.WHITE);
        g2d.fillOval(rx - 5, ry - 5, 10, 10);
        g2d.setColor(UITheme.SELECTION_COLOR);
        g2d.drawOval(rx - 5, ry - 5, 10, 10);
    }

    public boolean isOnResizeHandle(Point p) {
        if (!selected) {
            return false;
        }
        Rectangle handle = new Rectangle(x + width - 7, y + height - 7, 14, 14);
        return handle.contains(p);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ElementStyle getStyle() {
        return style;
    }

    public void setStyle(ElementStyle style) {
        if (style != null) {
            this.style = style.clone();
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
