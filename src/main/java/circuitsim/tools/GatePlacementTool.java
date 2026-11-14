package circuitsim.tools;

import circuitsim.commands.AddShapeCommand;
import circuitsim.commands.CommandManager;
import circuitsim.exceptions.InvalidShapeException;
import circuitsim.factory.ShapeFactory;
import circuitsim.model.Circuit;
import circuitsim.model.CircuitElement;
import circuitsim.model.ElementStyle;
import circuitsim.model.ElementType;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Concrete Tool strategy for placing logic gates, switches, LEDs, and text labels
 * onto the canvas by clicking or dragging a bounding box, with automatic 20px CAD grid snapping.
 */
public class GatePlacementTool implements Tool {
    private final Circuit circuit;
    private final CommandManager commandManager;
    private final ElementType elementType;
    private final Supplier<ElementStyle> styleSupplier;
    private final Supplier<String> textPromptSupplier;
    private final Consumer<String> statusConsumer;

    private Point startPoint;
    private Point currentPoint;

    public GatePlacementTool(
            Circuit circuit,
            CommandManager commandManager,
            ElementType elementType,
            Supplier<ElementStyle> styleSupplier,
            Supplier<String> textPromptSupplier,
            Consumer<String> statusConsumer) {
        this.circuit = circuit;
        this.commandManager = commandManager;
        this.elementType = elementType;
        this.styleSupplier = styleSupplier;
        this.textPromptSupplier = textPromptSupplier;
        this.statusConsumer = statusConsumer;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        startPoint = e.getPoint();
        currentPoint = startPoint;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        currentPoint = e.getPoint();
        circuit.notifyListeners();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (startPoint == null) {
            return;
        }
        Point end = e.getPoint();
        int rawX = Math.min(startPoint.x, end.x);
        int rawY = Math.min(startPoint.y, end.y);

        // Snap to nearest 20px CAD Grid
        int x = Math.round(rawX / 20.0f) * 20;
        int y = Math.round(rawY / 20.0f) * 20;

        int w = Math.abs(end.x - startPoint.x);
        int h = Math.abs(end.y - startPoint.y);

        // Sensible default dimensions
        if (w < 30 || h < 24) {
            w = (elementType == ElementType.LED) ? 46 : (elementType == ElementType.SWITCH ? 70 : 84);
            h = (elementType == ElementType.LED) ? 46 : (elementType == ElementType.SWITCH ? 44 : 52);
        } else {
            w = Math.round(w / 10.0f) * 10;
            h = Math.round(h / 10.0f) * 10;
        }

        String text = "Label";
        if (elementType == ElementType.TEXT && textPromptSupplier != null) {
            text = textPromptSupplier.get();
            if (text == null) {
                startPoint = null;
                currentPoint = null;
                circuit.notifyListeners();
                return;
            }
        }

        try {
            CircuitElement element = ShapeFactory.createElement(
                    elementType, x, y, w, h, styleSupplier.get(), text);
            commandManager.executeCommand(new AddShapeCommand(circuit, element));
            if (statusConsumer != null) {
                statusConsumer.accept("✓ Placed " + elementType + " at (" + x + ", " + y + ")");
            }
        } catch (InvalidShapeException ex) {
            if (statusConsumer != null) {
                statusConsumer.accept("Error: " + ex.getMessage());
            }
        } finally {
            startPoint = null;
            currentPoint = null;
            circuit.notifyListeners();
        }
    }

    @Override
    public void drawPreview(Graphics2D g2d) {
        if (startPoint != null && currentPoint != null) {
            int rawX = Math.min(startPoint.x, currentPoint.x);
            int rawY = Math.min(startPoint.y, currentPoint.y);
            int x = Math.round(rawX / 20.0f) * 20;
            int y = Math.round(rawY / 20.0f) * 20;
            int w = Math.max(40, Math.abs(currentPoint.x - startPoint.x));
            int h = Math.max(30, Math.abs(currentPoint.y - startPoint.y));

            g2d.setColor(new Color(59, 130, 246, 30));
            g2d.fillRoundRect(x, y, w, h, 8, 8);

            float[] dash = {4.0f, 4.0f};
            g2d.setColor(new Color(59, 130, 246));
            g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
            g2d.drawRoundRect(x, y, w, h, 8, 8);
        }
    }
}
