package circuitsim.tools;

import circuitsim.commands.AddShapeCommand;
import circuitsim.commands.CommandManager;
import circuitsim.exceptions.InvalidShapeException;
import circuitsim.factory.ShapeFactory;
import circuitsim.model.Circuit;
import circuitsim.model.ElementStyle;
import circuitsim.model.Pin;
import circuitsim.model.Wire;
import circuitsim.ui.UITheme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Concrete Tool strategy for wiring an output Pin of one element to an input Pin of another.
 * Features smart magnetic pin snapping, target pin validation halos, and clear status guidance.
 */
public class WireTool implements Tool {
    private final Circuit circuit;
    private final CommandManager commandManager;
    private final Supplier<ElementStyle> styleSupplier;
    private final Consumer<String> statusConsumer;

    private Pin startPin;
    private Point currentMouse;
    private Pin snappedPin;

    public WireTool(
            Circuit circuit,
            CommandManager commandManager,
            Supplier<ElementStyle> styleSupplier,
            Consumer<String> statusConsumer) {
        this.circuit = circuit;
        this.commandManager = commandManager;
        this.styleSupplier = styleSupplier;
        this.statusConsumer = statusConsumer;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Pin pin = circuit.findPinAt(e.getPoint(), 18);
        if (pin != null) {
            startPin = pin;
            currentMouse = e.getPoint();
            snappedPin = null;
            if (statusConsumer != null) {
                statusConsumer.accept("Dragging wire from " + (pin.isOutput() ? "OUTPUT" : "INPUT") +
                        " pin. Release over a matching pin to connect.");
            }
        } else if (statusConsumer != null) {
            statusConsumer.accept("Click directly on a component Pin terminal (small circular connector) to start a wire.");
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (startPin != null) {
            currentMouse = e.getPoint();
            // Check magnetic snapping to other pins within 22px
            Pin nearby = circuit.findPinAt(currentMouse, 22);
            if (nearby != null && nearby != startPin) {
                snappedPin = nearby;
                currentMouse = nearby.getAbsolutePosition();
            } else {
                snappedPin = null;
            }
            circuit.notifyListeners();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (startPin == null) {
            return;
        }

        Point releasePt = (snappedPin != null) ? snappedPin.getAbsolutePosition() : e.getPoint();
        Pin endPin = (snappedPin != null) ? snappedPin : circuit.findPinAt(releasePt, 22);

        if (endPin != null && endPin != startPin) {
            if (startPin.isOutput() == endPin.isOutput()) {
                if (statusConsumer != null) {
                    statusConsumer.accept("Connection Error: Wire must connect an OUTPUT pin to an INPUT pin.");
                }
            } else if (startPin.getOwner() == endPin.getOwner()) {
                if (statusConsumer != null) {
                    statusConsumer.accept("Connection Error: Cannot connect a component pin to itself.");
                }
            } else {
                Pin source = startPin.isOutput() ? startPin : endPin;
                Pin target = startPin.isOutput() ? endPin : startPin;
                try {
                    if (circuit.isInputPinAlreadyWired(target)) {
                        throw new InvalidShapeException("Target input pin already has a connected wire.");
                    }
                    Wire wire = ShapeFactory.createWire(source, target, styleSupplier.get());
                    commandManager.executeCommand(new AddShapeCommand(circuit, wire));
                    if (statusConsumer != null) {
                        statusConsumer.accept("✓ Connected wire: " + source.getOwner().getType() + " → " + target.getOwner().getType());
                    }
                } catch (InvalidShapeException ex) {
                    if (statusConsumer != null) {
                        statusConsumer.accept("Wire Error: " + ex.getMessage());
                    }
                }
            }
        } else if (statusConsumer != null) {
            statusConsumer.accept("Wire cancelled: Release over a valid input/output pin to complete connection.");
        }

        startPin = null;
        currentMouse = null;
        snappedPin = null;
        circuit.notifyListeners();
    }

    @Override
    public void drawPreview(Graphics2D g2d) {
        if (startPin != null && currentMouse != null) {
            Point p1 = startPin.getAbsolutePosition();

            // Glow Halo
            g2d.setColor(new Color(34, 197, 94, 60));
            g2d.setStroke(new BasicStroke(6.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(p1.x, p1.y, currentMouse.x, currentMouse.y);

            // Core Line
            g2d.setColor(new Color(34, 197, 94));
            g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(p1.x, p1.y, currentMouse.x, currentMouse.y);

            // Start Pin Indicator
            g2d.setColor(new Color(34, 197, 94, 120));
            g2d.fillOval(p1.x - 8, p1.y - 8, 16, 16);

            // Snapped Target Pin Halo
            if (snappedPin != null) {
                Point sp = snappedPin.getAbsolutePosition();
                boolean valid = (startPin.isOutput() != snappedPin.isOutput()) && (startPin.getOwner() != snappedPin.getOwner());
                g2d.setColor(valid ? new Color(34, 197, 94, 160) : new Color(239, 68, 68, 160));
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawOval(sp.x - 10, sp.y - 10, 20, 20);
                g2d.fillOval(sp.x - 5, sp.y - 5, 10, 10);
            }
        }
    }
}
