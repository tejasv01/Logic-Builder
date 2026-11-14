package circuitsim.model;

import java.awt.Color;

/**
 * Encapsulates visual styling attributes (fill color, stroke color, and line width)
 * for any CircuitElement.
 *
 * OOM Concepts:
 * - Encapsulation: Private fields with controlled getters/setters.
 * - Prototype Pattern: Implements Cloneable for deep copying styles.
 * - SOLID (SRP): Sole responsibility is storing and cloning style configuration.
 */
public class ElementStyle implements Cloneable {
    private Color fillColor;
    private Color strokeColor;
    private float strokeWidth;

    public ElementStyle(Color fillColor, Color strokeColor, float strokeWidth) {
        this.fillColor = fillColor != null ? fillColor : new Color(240, 244, 255);
        this.strokeColor = strokeColor != null ? strokeColor : new Color(30, 41, 59);
        this.strokeWidth = strokeWidth > 0 ? strokeWidth : 2.0f;
    }

    public static ElementStyle createDefault() {
        return new ElementStyle(new Color(240, 244, 255), new Color(30, 41, 59), 2.0f);
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setFillColor(Color fillColor) {
        if (fillColor != null) {
            this.fillColor = fillColor;
        }
    }

    public Color getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(Color strokeColor) {
        if (strokeColor != null) {
            this.strokeColor = strokeColor;
        }
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(float strokeWidth) {
        if (strokeWidth > 0) {
            this.strokeWidth = strokeWidth;
        }
    }

    @Override
    public ElementStyle clone() {
        try {
            ElementStyle copy = (ElementStyle) super.clone();
            copy.fillColor = new Color(fillColor.getRGB(), true);
            copy.strokeColor = new Color(strokeColor.getRGB(), true);
            copy.strokeWidth = this.strokeWidth;
            return copy;
        } catch (CloneNotSupportedException e) {
            return new ElementStyle(fillColor, strokeColor, strokeWidth);
        }
    }

    public static String toHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    public static Color fromHex(String hex) {
        return Color.decode(hex.trim());
    }
}
