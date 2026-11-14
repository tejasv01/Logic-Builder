package circuitsim.model;

import circuitsim.exceptions.InvalidShapeException;
import circuitsim.export.CircuitVisitor;
import circuitsim.ui.UITheme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract superclass for all boolean logic gates (AND, OR, NOT, XOR, NAND, NOR).
 * Renders with a sleek modern IC chassis, pin connector leads, crisp IEEE/ANSI
 * logic gate silhouettes, and customizable fill/stroke styles.
 */
public abstract class LogicGate extends CircuitElement {
    protected final List<Pin> inputPins = new ArrayList<>();
    protected Pin outputPin;
    protected final int inputCount;

    protected LogicGate(int x, int y, int width, int height, ElementStyle style, int inputCount)
            throws InvalidShapeException {
        super(x, y, width, height, style);
        if (inputCount < 1 || inputCount > 4) {
            throw new InvalidShapeException("LogicGate input count must be between 1 and 4.");
        }
        this.inputCount = inputCount;
        initPins();
    }

    private void initPins() {
        inputPins.clear();
        for (int i = 0; i < inputCount; i++) {
            int py = (height * (i + 1)) / (inputCount + 1);
            inputPins.add(new Pin(id + "_in" + i, this, false, 0, py));
        }
        outputPin = new Pin(id + "_out", this, true, width, height / 2);
    }

    @Override
    protected void updatePinOffsets() {
        for (int i = 0; i < inputPins.size(); i++) {
            int py = (height * (i + 1)) / (inputCount + 1);
            inputPins.get(i).setOffsets(0, py);
        }
        if (outputPin != null) {
            outputPin.setOffsets(width, height / 2);
        }
    }

    public List<Pin> getInputPins() {
        return Collections.unmodifiableList(inputPins);
    }

    public Pin getOutputPin() {
        return outputPin;
    }

    @Override
    public List<Pin> getPins() {
        List<Pin> all = new ArrayList<>(inputPins);
        all.add(outputPin);
        return all;
    }

    public abstract String getGateSymbol();

    @Override
    public void draw(Graphics2D g2d) {
        // Drop Shadow
        g2d.setColor(new Color(0, 0, 0, 35));
        g2d.fillRoundRect(x + 2, y + 3, width, height, 12, 12);

        // Determine chassis background (respects user custom fill color if set)
        Color defaultFill = UITheme.isDarkMode() ? new Color(30, 41, 59) : new Color(255, 255, 255);
        Color cardBg = (style != null && style.getFillColor() != null && !style.getFillColor().equals(new Color(240, 244, 255)))
                ? style.getFillColor()
                : defaultFill;

        Color defaultBorder = UITheme.isDarkMode() ? new Color(71, 85, 105) : new Color(203, 213, 225);
        Color cardBorder = (style != null && style.getStrokeColor() != null && !style.getStrokeColor().equals(new Color(30, 41, 59)))
                ? style.getStrokeColor()
                : defaultBorder;

        float sw = (style != null && style.getStrokeWidth() > 0) ? style.getStrokeWidth() : 1.5f;

        g2d.setColor(cardBg);
        g2d.fillRoundRect(x, y, width, height, 12, 12);

        // Header Accent Stripe
        Color accent = UITheme.getAccentColor();
        g2d.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 35));
        g2d.fillRoundRect(x, y, width, Math.max(16, height / 3), 12, 12);
        g2d.fillRect(x, y + Math.max(10, height / 4), width, 6);

        // Chassis Border
        g2d.setColor(selected ? UITheme.SELECTION_COLOR : cardBorder);
        g2d.setStroke(new BasicStroke(selected ? Math.max(2.0f, sw + 0.5f) : sw));
        g2d.drawRoundRect(x, y, width, height, 12, 12);

        // Pin Lead Traces (from chip edge to center)
        g2d.setColor(UITheme.isDarkMode() ? new Color(100, 116, 139) : new Color(148, 163, 184));
        g2d.setStroke(new BasicStroke(1.2f));
        for (Pin inPin : inputPins) {
            int py = y + inPin.getOffsetY();
            g2d.drawLine(x, py, x + 14, py);
        }
        if (outputPin != null) {
            int opy = y + outputPin.getOffsetY();
            g2d.drawLine(x + width - 14, opy, x + width, opy);
        }

        // Gate Silhouette
        drawGateSilhouette(g2d);

        drawPins(g2d);
        drawSelectionHandles(g2d);
    }

    protected void drawGateSilhouette(Graphics2D g2d) {
        String sym = getGateSymbol();
        String simpleName = sym.split(" ")[0]; // "AND", "OR", "NOT", etc.

        // Draw Gate Type Badge at top
        g2d.setFont(new Font("SansSerif", Font.BOLD, 10));
        FontMetrics fm = g2d.getFontMetrics();
        int tx = x + (width - fm.stringWidth(simpleName)) / 2;
        int ty = y + 13;

        g2d.setColor(UITheme.isDarkMode() ? new Color(148, 163, 184) : new Color(71, 85, 105));
        g2d.drawString(simpleName, tx, ty);

        // Draw IEEE logic gate shape in the center area
        int iconX = x + 18;
        int iconY = y + 16;
        int iconW = Math.max(20, width - 36);
        int iconH = Math.max(16, height - 22);

        Color glyphColor = (style != null && style.getStrokeColor() != null && !style.getStrokeColor().equals(new Color(30, 41, 59)))
                ? style.getStrokeColor()
                : (UITheme.isDarkMode() ? new Color(241, 245, 249) : new Color(30, 41, 59));
        g2d.setColor(glyphColor);
        g2d.setStroke(new BasicStroke(1.5f));

        switch (simpleName) {
            case "AND":
            case "NAND": {
                int bubbleW = simpleName.equals("NAND") ? 6 : 0;
                int rightX = iconX + iconW - bubbleW;
                GeneralPath path = new GeneralPath();
                path.moveTo(iconX, iconY);
                path.lineTo(iconX + iconW * 0.45, iconY);
                path.quadTo(rightX, iconY, rightX, iconY + iconH / 2.0);
                path.quadTo(rightX, iconY + iconH, iconX + iconW * 0.45, iconY + iconH);
                path.lineTo(iconX, iconY + iconH);
                path.closePath();
                g2d.draw(path);
                if (simpleName.equals("NAND")) {
                    g2d.drawOval(rightX, iconY + iconH / 2 - 3, 6, 6);
                }
                break;
            }
            case "OR":
            case "NOR": {
                int bubbleW = simpleName.equals("NOR") ? 6 : 0;
                int rightX = iconX + iconW - bubbleW;
                GeneralPath path = new GeneralPath();
                path.moveTo(iconX, iconY);
                path.quadTo(iconX + iconW * 0.5, iconY + 2, rightX, iconY + iconH / 2.0);
                path.quadTo(iconX + iconW * 0.5, iconY + iconH - 2, iconX, iconY + iconH);
                path.quadTo(iconX + iconW * 0.25, iconY + iconH / 2.0, iconX, iconY);
                path.closePath();
                g2d.draw(path);
                if (simpleName.equals("NOR")) {
                    g2d.drawOval(rightX, iconY + iconH / 2 - 3, 6, 6);
                }
                break;
            }
            case "XOR": {
                GeneralPath arc = new GeneralPath();
                arc.moveTo(iconX - 4, iconY);
                arc.quadTo(iconX + iconW * 0.2, iconY + iconH / 2.0, iconX - 4, iconY + iconH);
                g2d.draw(arc);

                GeneralPath path = new GeneralPath();
                path.moveTo(iconX, iconY);
                path.quadTo(iconX + iconW * 0.5, iconY + 2, iconX + iconW, iconY + iconH / 2.0);
                path.quadTo(iconX + iconW * 0.5, iconY + iconH - 2, iconX, iconY + iconH);
                path.quadTo(iconX + iconW * 0.25, iconY + iconH / 2.0, iconX, iconY);
                path.closePath();
                g2d.draw(path);
                break;
            }
            case "NOT": {
                GeneralPath path = new GeneralPath();
                int rightX = iconX + iconW - 6;
                path.moveTo(iconX + 2, iconY + 2);
                path.lineTo(rightX, iconY + iconH / 2.0);
                path.lineTo(iconX + 2, iconY + iconH - 2);
                path.closePath();
                g2d.draw(path);
                g2d.drawOval(rightX, iconY + iconH / 2 - 3, 6, 6);
                break;
            }
            default: {
                g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
                FontMetrics fm2 = g2d.getFontMetrics();
                int lx = x + (width - fm2.stringWidth(sym)) / 2;
                int ly = y + ((height - fm2.getHeight()) / 2) + fm2.getAscent();
                g2d.drawString(sym, lx, ly);
                break;
            }
        }
    }

    @Override
    public void accept(CircuitVisitor visitor) {
        visitor.visitGate(this);
    }
}
