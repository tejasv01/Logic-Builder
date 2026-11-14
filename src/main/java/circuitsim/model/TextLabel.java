package circuitsim.model;

import circuitsim.exceptions.InvalidShapeException;
import circuitsim.export.CircuitVisitor;
import circuitsim.ui.UITheme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * Annotative text label element for circuit schematics (e.g., "Half Adder", "Carry Out").
 */
public class TextLabel extends CircuitElement {
    private String text;

    public TextLabel(int x, int y, int width, int height, ElementStyle style, String text)
            throws InvalidShapeException {
        super(x, y, width, height, style);
        if (text == null || text.trim().isEmpty()) {
            throw new InvalidShapeException("TextLabel text cannot be null or empty.");
        }
        this.text = text.trim();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) throws InvalidShapeException {
        if (text == null || text.trim().isEmpty()) {
            throw new InvalidShapeException("TextLabel text cannot be null or empty.");
        }
        this.text = text.trim();
    }

    @Override
    public ElementType getType() {
        return ElementType.TEXT;
    }

    @Override
    public void evaluate() {
        // Passive annotation; no signal evaluation needed
    }

    @Override
    public void draw(Graphics2D g2d) {
        Color bg = UITheme.isDarkMode() ? new Color(30, 41, 59, 180) : new Color(241, 245, 249, 200);
        Color border = UITheme.isDarkMode() ? new Color(51, 65, 85) : new Color(203, 213, 225);

        g2d.setColor(bg);
        g2d.fillRoundRect(x, y, width, height, 8, 8);

        g2d.setColor(selected ? UITheme.SELECTION_COLOR : border);
        g2d.setStroke(new BasicStroke(selected ? 1.5f : 1.0f));
        g2d.drawRoundRect(x, y, width, height, 8, 8);

        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2d.setColor(UITheme.getTextColor());
        FontMetrics fm = g2d.getFontMetrics();
        int tx = x + 10;
        int ty = y + ((height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(text, tx, ty);

        drawSelectionHandles(g2d);
    }

    @Override
    public void accept(CircuitVisitor visitor) {
        visitor.visitText(this);
    }

    @Override
    public TextLabel clone() {
        try {
            return new TextLabel(x, y, width, height, style.clone(), text);
        } catch (InvalidShapeException e) {
            throw new AssertionError(e);
        }
    }
}
