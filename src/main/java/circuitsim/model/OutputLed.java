package circuitsim.model;

import circuitsim.exceptions.InvalidShapeException;
import circuitsim.export.CircuitVisitor;
import circuitsim.ui.UITheme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;

/**
 * High-quality Circular Output LED indicator featuring realistic lens shading,
 * metallic bezel rim, luminous radial bloom, and specular reflection highlights.
 */
public class OutputLed extends CircuitElement {
    private final Pin inputPin; // COMPOSITION

    public OutputLed(int x, int y, int width, int height, ElementStyle style)
            throws InvalidShapeException {
        super(x, y, width, height, style);
        this.inputPin = new Pin(id + "_in0", this, false, 0, height / 2);
    }

    @Override
    protected void updatePinOffsets() {
        inputPin.setOffsets(0, height / 2);
    }

    public Pin getInputPin() {
        return inputPin;
    }

    public boolean isLit() {
        return inputPin.getValue();
    }

    @Override
    public List<Pin> getPins() {
        return Collections.singletonList(inputPin);
    }

    @Override
    public ElementType getType() {
        return ElementType.LED;
    }

    @Override
    public void evaluate() {
        // OutputLed reflects the boolean state of its inputPin directly
    }

    @Override
    public boolean contains(Point p) {
        double rx = width / 2.0;
        double ry = height / 2.0;
        double cx = x + rx;
        double cy = y + ry;
        double dx = (p.x - cx) / rx;
        double dy = (p.y - cy) / ry;
        return (dx * dx + dy * dy) <= 1.15;
    }

    @Override
    public void draw(Graphics2D g2d) {
        boolean lit = isLit();
        int cx = x + width / 2;
        int cy = y + height / 2;
        int radius = Math.min(width, height) / 2;

        if (lit) {
            // Stage 1: Soft broad radiant bloom
            g2d.setColor(new Color(34, 197, 94, 35));
            g2d.fillOval(cx - radius - 12, cy - radius - 12, (radius + 12) * 2, (radius + 12) * 2);

            // Stage 2: Inner luminous halo
            g2d.setColor(new Color(74, 222, 128, 90));
            g2d.fillOval(cx - radius - 6, cy - radius - 6, (radius + 6) * 2, (radius + 6) * 2);
        }

        // Metallic Bezel Outer Rim
        Color defaultBezel = UITheme.isDarkMode() ? new Color(51, 65, 85) : new Color(203, 213, 225);
        Color bezelBg = (style != null && style.getFillColor() != null && !style.getFillColor().equals(new Color(240, 244, 255)))
                ? style.getFillColor()
                : defaultBezel;
        g2d.setColor(bezelBg);
        g2d.fillOval(x, y, width, height);

        Color defaultStroke = UITheme.isDarkMode() ? new Color(71, 85, 105) : new Color(148, 163, 184);
        Color rimColor = (style != null && style.getStrokeColor() != null && !style.getStrokeColor().equals(new Color(30, 41, 59)))
                ? style.getStrokeColor()
                : defaultStroke;
        float sw = (style != null && style.getStrokeWidth() > 0) ? style.getStrokeWidth() : 1.5f;

        g2d.setColor(selected ? UITheme.SELECTION_COLOR : rimColor);
        g2d.setStroke(new BasicStroke(selected ? Math.max(2.0f, sw + 0.5f) : sw));
        g2d.drawOval(x, y, width, height);

        // Inner Bulb Lens (slightly inset)
        int inset = Math.max(3, radius / 7);
        int lx = x + inset;
        int ly = y + inset;
        int lw = width - inset * 2;
        int lh = height - inset * 2;

        if (lit) {
            // Luminous Radial Gradient Lens
            Point2D center = new Point2D.Float(lx + lw * 0.4f, ly + lh * 0.35f);
            float gradRadius = Math.max(lw, lh) * 0.7f;
            float[] dist = {0.0f, 0.45f, 1.0f};
            Color[] colors = {
                    new Color(220, 252, 231), // bright white-green center
                    new Color(34, 197, 94),   // vibrant emerald
                    new Color(21, 128, 61)    // dark green edge
            };
            RadialGradientPaint p = new RadialGradientPaint(center, gradRadius, dist, colors);
            g2d.setPaint(p);
            g2d.fillOval(lx, ly, lw, lh);
        } else {
            // Dark Frosted Unlit Lens
            Color unlit = UITheme.isDarkMode() ? new Color(15, 30, 24) : new Color(220, 235, 225);
            g2d.setColor(unlit);
            g2d.fillOval(lx, ly, lw, lh);

            g2d.setColor(UITheme.isDarkMode() ? new Color(20, 50, 35) : new Color(180, 200, 190));
            g2d.setStroke(new BasicStroke(1.0f));
            g2d.drawOval(lx, ly, lw, lh);
        }

        // Glass Specular Highlight (Reflection)
        g2d.setColor(new Color(255, 255, 255, lit ? 190 : 50));
        int hx = lx + lw / 4;
        int hy = ly + lh / 6;
        int hw = Math.max(4, lw / 3);
        int hh = Math.max(2, lh / 5);
        g2d.fillOval(hx, hy, hw, hh);

        // Center State Text ("ON" / "OFF")
        g2d.setFont(new Font("SansSerif", Font.BOLD, Math.max(10, height / 4)));
        FontMetrics fm = g2d.getFontMetrics();
        String label = lit ? "ON" : "OFF";
        int tx = x + (width - fm.stringWidth(label)) / 2;
        int ty = y + ((height - fm.getHeight()) / 2) + fm.getAscent();

        // Shadow under text
        g2d.setColor(new Color(0, 0, 0, lit ? 90 : 40));
        g2d.drawString(label, tx + 1, ty + 1);

        g2d.setColor(lit ? Color.WHITE : (UITheme.isDarkMode() ? new Color(148, 163, 184) : Color.DARK_GRAY));
        g2d.drawString(label, tx, ty);

        drawPins(g2d);
        drawSelectionHandles(g2d);
    }

    @Override
    public void accept(CircuitVisitor visitor) {
        visitor.visitLed(this);
    }

    @Override
    public OutputLed clone() {
        try {
            OutputLed copy = new OutputLed(x, y, width, height, style.clone());
            copy.getInputPin().setValue(this.isLit());
            return copy;
        } catch (InvalidShapeException e) {
            throw new AssertionError(e);
        }
    }
}
