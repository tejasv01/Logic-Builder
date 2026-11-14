package circuitsim.export;

import circuitsim.exceptions.ExportFailedException;
import circuitsim.model.Circuit;
import circuitsim.model.CircuitElement;
import circuitsim.model.ElementStyle;
import circuitsim.model.GroupShape;
import circuitsim.model.InputSwitch;
import circuitsim.model.LogicGate;
import circuitsim.model.OutputLed;
import circuitsim.model.TextLabel;
import circuitsim.model.Wire;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Concrete Visitor that generates a complete, standalone SVG vector graphic document
 * from a Circuit.
 */
public class SvgExportVisitor implements CircuitVisitor {
    private final StringBuilder svg = new StringBuilder();

    public String exportToString(Circuit circuit, int width, int height) {
        svg.setLength(0);
        svg.append(String.format(
                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 %d %d\">%n",
                width, height, width, height));
        svg.append("  <rect width=\"100%\" height=\"100%\" fill=\"#ffffff\"/>\n");
        circuit.accept(this);
        svg.append("</svg>\n");
        return svg.toString();
    }

    public void exportToFile(Circuit circuit, File file, int width, int height)
            throws ExportFailedException {
        if (file == null) {
            throw new ExportFailedException("Target SVG file cannot be null.");
        }
        try {
            String content = exportToString(circuit, width, height);
            Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ExportFailedException("Failed to write SVG file: " + e.getMessage(), e);
        }
    }

    @Override
    public void visitGate(LogicGate gate) {
        ElementStyle s = gate.getStyle();
        svg.append(String.format(
                "  <rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" rx=\"8\" fill=\"%s\" stroke=\"%s\" stroke-width=\"%.1f\"/>%n",
                gate.getX(), gate.getY(), gate.getWidth(), gate.getHeight(),
                ElementStyle.toHex(s.getFillColor()), ElementStyle.toHex(s.getStrokeColor()), s.getStrokeWidth()));
        svg.append(String.format(
                "  <text x=\"%d\" y=\"%d\" font-family=\"sans-serif\" font-size=\"12\" font-weight=\"bold\" text-anchor=\"middle\">%s</text>%n",
                gate.getX() + gate.getWidth() / 2, gate.getY() + gate.getHeight() / 2 + 4,
                escapeXml(gate.getGateSymbol())));
    }

    @Override
    public void visitSwitch(InputSwitch sw) {
        ElementStyle s = sw.getStyle();
        String fill = sw.isState() ? "#bbf7d0" : ElementStyle.toHex(s.getFillColor());
        svg.append(String.format(
                "  <rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" fill=\"%s\" stroke=\"%s\" stroke-width=\"%.1f\"/>%n",
                sw.getX(), sw.getY(), sw.getWidth(), sw.getHeight(),
                fill, ElementStyle.toHex(s.getStrokeColor()), s.getStrokeWidth()));
        svg.append(String.format(
                "  <text x=\"%d\" y=\"%d\" font-family=\"sans-serif\" font-size=\"12\" font-weight=\"bold\" text-anchor=\"middle\">SW: %s</text>%n",
                sw.getX() + sw.getWidth() / 2, sw.getY() + sw.getHeight() / 2 + 4,
                sw.isState() ? "1" : "0"));
    }

    @Override
    public void visitLed(OutputLed led) {
        ElementStyle s = led.getStyle();
        String fill = led.isLit() ? "#4ade80" : ElementStyle.toHex(s.getFillColor());
        int cx = led.getX() + led.getWidth() / 2;
        int cy = led.getY() + led.getHeight() / 2;
        svg.append(String.format(
                "  <ellipse cx=\"%d\" cy=\"%d\" rx=\"%d\" ry=\"%d\" fill=\"%s\" stroke=\"%s\" stroke-width=\"%.1f\"/>%n",
                cx, cy, led.getWidth() / 2, led.getHeight() / 2,
                fill, ElementStyle.toHex(s.getStrokeColor()), s.getStrokeWidth()));
    }

    @Override
    public void visitWire(Wire wire) {
        Point p1 = wire.getSourcePin().getAbsolutePosition();
        Point p2 = wire.getTargetPin().getAbsolutePosition();
        String stroke = wire.getSourcePin().getValue()
                ? "#16a34a"
                : ElementStyle.toHex(wire.getStyle().getStrokeColor());
        svg.append(String.format(
                "  <line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"%s\" stroke-width=\"%.1f\"/>%n",
                p1.x, p1.y, p2.x, p2.y, stroke, wire.getStyle().getStrokeWidth()));
    }

    @Override
    public void visitText(TextLabel label) {
        svg.append(String.format(
                "  <text x=\"%d\" y=\"%d\" font-family=\"sans-serif\" font-size=\"13\" fill=\"%s\">%s</text>%n",
                label.getX() + 6, label.getY() + label.getHeight() / 2 + 4,
                ElementStyle.toHex(label.getStyle().getStrokeColor()), escapeXml(label.getText())));
    }

    @Override
    public void visitGroup(GroupShape group) {
        svg.append("  <g>\n");
        for (CircuitElement child : group.getChildren()) {
            child.accept(this);
        }
        svg.append("  </g>\n");
    }

    private String escapeXml(String input) {
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;");
    }
}
