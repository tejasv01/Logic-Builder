package circuitsim.model;

import circuitsim.exceptions.InvalidShapeException;
import circuitsim.export.CircuitVisitor;
import circuitsim.ui.UITheme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.util.Collections;
import java.util.List;

/**
 * Interactive boolean signal source (Input Switch) that users can toggle between
 * HIGH (1 / true) and LOW (0 / false).
 * Styled as a premium physical rocker/toggle switch with tactile slider knob and status LED.
 */
public class InputSwitch extends CircuitElement {
    private final Pin outputPin; // COMPOSITION
    private boolean state;

    public InputSwitch(int x, int y, int width, int height, ElementStyle style)
            throws InvalidShapeException {
        super(x, y, width, height, style);
        this.state = false;
        this.outputPin = new Pin(id + "_out", this, true, width, height / 2);
    }

    @Override
    protected void updatePinOffsets() {
        outputPin.setOffsets(width, height / 2);
    }

    public void toggle() {
        this.state = !this.state;
        evaluate();
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
        evaluate();
    }

    public Pin getOutputPin() {
        return outputPin;
    }

    @Override
    public List<Pin> getPins() {
        return Collections.singletonList(outputPin);
    }

    @Override
    public ElementType getType() {
        return ElementType.SWITCH;
    }

    @Override
    public void evaluate() {
        outputPin.setValue(state);
    }

    @Override
    public void draw(Graphics2D g2d) {
        // Drop shadow
        g2d.setColor(new Color(0, 0, 0, 30));
        g2d.fillRoundRect(x + 2, y + 2, width, height, 10, 10);

        // Switch Base Enclosure
        Color defaultFill = UITheme.isDarkMode() ? new Color(30, 41, 59) : new Color(248, 250, 252);
        Color chassisBg = (style != null && style.getFillColor() != null && !style.getFillColor().equals(new Color(240, 244, 255)))
                ? style.getFillColor()
                : defaultFill;

        Color defaultBorder = UITheme.isDarkMode() ? new Color(71, 85, 105) : new Color(203, 213, 225);
        Color border = state
                ? new Color(34, 197, 94)
                : ((style != null && style.getStrokeColor() != null && !style.getStrokeColor().equals(new Color(30, 41, 59)))
                    ? style.getStrokeColor()
                    : defaultBorder);

        float sw = (style != null && style.getStrokeWidth() > 0) ? style.getStrokeWidth() : 1.2f;

        g2d.setColor(chassisBg);
        g2d.fillRoundRect(x, y, width, height, 10, 10);

        g2d.setColor(selected ? UITheme.SELECTION_COLOR : border);
        g2d.setStroke(new BasicStroke(selected ? Math.max(2.0f, sw + 0.5f) : (state ? 1.8f : sw)));
        g2d.drawRoundRect(x, y, width, height, 10, 10);

        // Toggle Pill Track
        int pad = 5;
        int trackH = Math.max(14, height / 3);
        int trackW = Math.max(30, width - pad * 2 - 20);
        int trackX = x + pad;
        int trackY = y + (height - trackH) / 2;

        Color trackColor = state
                ? (UITheme.isDarkMode() ? new Color(22, 101, 52) : new Color(187, 247, 208))
                : (UITheme.isDarkMode() ? new Color(51, 65, 85) : new Color(226, 232, 240));
        g2d.setColor(trackColor);
        g2d.fillRoundRect(trackX, trackY, trackW, trackH, trackH, trackH);

        // Sliding Knob
        int knobDiameter = trackH;
        int knobX = state ? (trackX + trackW - knobDiameter) : trackX;
        int knobY = trackY;

        // Knob Shadow
        g2d.setColor(new Color(0, 0, 0, 40));
        g2d.fillOval(knobX + 1, knobY + 1, knobDiameter, knobDiameter);

        // Knob metallic gradient
        GradientPaint knobGrad = new GradientPaint(
                knobX, knobY,
                state ? new Color(74, 222, 128) : Color.WHITE,
                knobX, knobY + knobDiameter,
                state ? new Color(22, 163, 74) : new Color(203, 213, 225)
        );
        g2d.setPaint(knobGrad);
        g2d.fillOval(knobX, knobY, knobDiameter, knobDiameter);

        g2d.setColor(state ? new Color(21, 128, 61) : new Color(148, 163, 184));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawOval(knobX, knobY, knobDiameter, knobDiameter);

        // State Text ("1" or "0")
        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        String valStr = state ? "1" : "0";
        int tx = x + width - pad - fm.stringWidth(valStr) - 3;
        int ty = y + ((height - fm.getHeight()) / 2) + fm.getAscent();

        g2d.setColor(state ? new Color(34, 197, 94) : UITheme.getTextMutedColor());
        g2d.drawString(valStr, tx, ty);

        drawPins(g2d);
        drawSelectionHandles(g2d);
    }

    @Override
    public void accept(CircuitVisitor visitor) {
        visitor.visitSwitch(this);
    }

    @Override
    public InputSwitch clone() {
        try {
            InputSwitch copy = new InputSwitch(x, y, width, height, style.clone());
            copy.setState(this.state);
            return copy;
        } catch (InvalidShapeException e) {
            throw new AssertionError(e);
        }
    }
}
