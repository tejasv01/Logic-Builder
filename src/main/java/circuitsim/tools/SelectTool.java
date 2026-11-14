package circuitsim.tools;

import circuitsim.commands.AddShapeCommand;
import circuitsim.commands.CommandManager;
import circuitsim.commands.MoveCommand;
import circuitsim.commands.ResizeCommand;
import circuitsim.exceptions.InvalidShapeException;
import circuitsim.factory.ShapeFactory;
import circuitsim.model.Circuit;
import circuitsim.model.CircuitElement;
import circuitsim.model.ElementStyle;
import circuitsim.model.InputSwitch;
import circuitsim.model.Pin;
import circuitsim.model.Wire;
import circuitsim.ui.UITheme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

/**
 * Modern Multi-purpose Selection Tool:
 * 1. Click/drag empty canvas -> Marquee Box Selection.
 * 2. Click Input Switch -> Instant direct toggle (0 <-> 1).
 * 3. Drag from a Pin -> Direct wire creation with magnetic snapping.
 * 4. Drag bottom-right corner -> Resize component.
 * 5. Drag component -> Move selection (with optional grid snapping).
 */
public class SelectTool implements Tool {
    private final Circuit circuit;
    private final CommandManager commandManager;
    private final Consumer<String> statusConsumer;

    // Interaction Modes
    private enum Mode { NONE, MOVE, RESIZE, MARQUEE, WIRING }
    private Mode currentMode = Mode.NONE;

    private Point startPoint;
    private Point lastPoint;
    private CircuitElement activeElement;
    private int initialWidth;
    private int initialHeight;

    // Wiring Sub-mode
    private Pin wireStartPin;
    private Point wireCurrentMouse;
    private Pin wireSnappedPin;

    // Marquee Sub-mode
    private Point marqueeCurrent;

    public SelectTool(Circuit circuit, CommandManager commandManager) {
        this(circuit, commandManager, null);
    }

    public SelectTool(Circuit circuit, CommandManager commandManager, Consumer<String> statusConsumer) {
        this.circuit = circuit;
        this.commandManager = commandManager;
        this.statusConsumer = statusConsumer;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        startPoint = e.getPoint();
        lastPoint = startPoint;
        currentMode = Mode.NONE;
        activeElement = null;
        wireStartPin = null;
        wireSnappedPin = null;

        // 1. Check if user clicked directly on any Pin terminal to start a wire
        Pin pin = circuit.findPinAt(startPoint, 16);
        if (pin != null) {
            currentMode = Mode.WIRING;
            wireStartPin = pin;
            wireCurrentMouse = startPoint;
            if (statusConsumer != null) {
                statusConsumer.accept("Dragging wire from " + (pin.isOutput() ? "OUTPUT" : "INPUT") + " pin...");
            }
            return;
        }

        // 2. Check if user clicked on a selected element's resize handle
        for (CircuitElement sel : circuit.getSelectedElements()) {
            if (sel.isOnResizeHandle(startPoint)) {
                currentMode = Mode.RESIZE;
                activeElement = sel;
                initialWidth = sel.getWidth();
                initialHeight = sel.getHeight();
                return;
            }
        }

        // 3. Check if user clicked on any Circuit Element
        CircuitElement hit = circuit.findElementAt(startPoint);
        if (hit != null) {
            currentMode = Mode.MOVE;
            activeElement = hit;
            if (!e.isShiftDown() && !hit.isSelected()) {
                circuit.clearSelection();
            }
            hit.setSelected(true);
            circuit.notifyListeners();
        } else {
            // 4. Clicked on empty space -> Start Marquee selection box
            currentMode = Mode.MARQUEE;
            marqueeCurrent = startPoint;
            if (!e.isShiftDown()) {
                circuit.clearSelection();
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Point current = e.getPoint();

        switch (currentMode) {
            case WIRING:
                if (wireStartPin != null) {
                    wireCurrentMouse = current;
                    Pin nearby = circuit.findPinAt(current, 20);
                    if (nearby != null && nearby != wireStartPin) {
                        wireSnappedPin = nearby;
                        wireCurrentMouse = nearby.getAbsolutePosition();
                    } else {
                        wireSnappedPin = null;
                    }
                    circuit.notifyListeners();
                }
                break;

            case MARQUEE:
                marqueeCurrent = current;
                circuit.notifyListeners();
                break;

            case RESIZE:
                if (activeElement != null) {
                    int newW = Math.max(30, initialWidth + (current.x - startPoint.x));
                    int newH = Math.max(24, initialHeight + (current.y - startPoint.y));
                    try {
                        activeElement.resize(newW, newH);
                        circuit.notifyListeners();
                    } catch (InvalidShapeException ignored) {
                    }
                }
                break;

            case MOVE:
                if (activeElement != null && lastPoint != null) {
                    int dx = current.x - lastPoint.x;
                    int dy = current.y - lastPoint.y;
                    for (CircuitElement sel : circuit.getSelectedElements()) {
                        sel.move(dx, dy);
                    }
                    lastPoint = current;
                    circuit.notifyListeners();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (startPoint == null) {
            return;
        }
        Point end = e.getPoint();
        int totalDx = end.x - startPoint.x;
        int totalDy = end.y - startPoint.y;
        boolean wasClick = (Math.abs(totalDx) <= 4 && Math.abs(totalDy) <= 4);

        switch (currentMode) {
            case WIRING:
                if (wireStartPin != null) {
                    Pin endPin = (wireSnappedPin != null) ? wireSnappedPin : circuit.findPinAt(end, 20);
                    if (endPin != null && endPin != wireStartPin) {
                        if (wireStartPin.isOutput() == endPin.isOutput()) {
                            if (statusConsumer != null) {
                                statusConsumer.accept("Connection Error: Wire must connect an OUTPUT pin to an INPUT pin.");
                            }
                        } else if (wireStartPin.getOwner() == endPin.getOwner()) {
                            if (statusConsumer != null) {
                                statusConsumer.accept("Connection Error: Cannot connect a component pin to itself.");
                            }
                        } else {
                            Pin source = wireStartPin.isOutput() ? wireStartPin : endPin;
                            Pin target = wireStartPin.isOutput() ? endPin : wireStartPin;
                            try {
                                if (circuit.isInputPinAlreadyWired(target)) {
                                    throw new InvalidShapeException("Target input pin already has a connected wire.");
                                }
                                Wire wire = ShapeFactory.createWire(source, target, ElementStyle.createDefault());
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
                    }
                }
                break;

            case MARQUEE:
                if (!wasClick && marqueeCurrent != null) {
                    Rectangle box = getMarqueeRectangle(startPoint, marqueeCurrent);
                    int count = 0;
                    for (CircuitElement el : circuit.getElements()) {
                        if (box.intersects(el.getBounds())) {
                            el.setSelected(true);
                            count++;
                        }
                    }
                    if (statusConsumer != null) {
                        statusConsumer.accept("Selected " + count + " element(s) with box select.");
                    }
                }
                break;

            case RESIZE:
                if (activeElement != null) {
                    int finalW = activeElement.getWidth();
                    int finalH = activeElement.getHeight();
                    if (finalW != initialWidth || finalH != initialHeight) {
                        try {
                            activeElement.resize(initialWidth, initialHeight);
                        } catch (InvalidShapeException ignored) {
                        }
                        commandManager.executeCommand(
                                new ResizeCommand(circuit, activeElement, initialWidth, initialHeight, finalW, finalH));
                    }
                }
                break;

            case MOVE:
                if (wasClick && activeElement instanceof InputSwitch) {
                    // Direct Instant Switch Toggle on Click!
                    InputSwitch sw = (InputSwitch) activeElement;
                    sw.toggle();
                    circuit.evaluate();
                    circuit.notifyListeners();
                    if (statusConsumer != null) {
                        statusConsumer.accept("Toggled switch " + sw.getId() + " to " + (sw.isState() ? "HIGH (1)" : "LOW (0)"));
                    }
                } else if (totalDx != 0 || totalDy != 0) {
                    List<CircuitElement> moved = circuit.getSelectedElements();
                    for (CircuitElement sel : moved) {
                        sel.move(-totalDx, -totalDy);
                    }
                    commandManager.executeCommand(new MoveCommand(circuit, moved, totalDx, totalDy));
                }
                break;

            default:
                break;
        }

        currentMode = Mode.NONE;
        activeElement = null;
        wireStartPin = null;
        wireSnappedPin = null;
        marqueeCurrent = null;
        circuit.notifyListeners();
    }

    private Rectangle getMarqueeRectangle(Point p1, Point p2) {
        int x = Math.min(p1.x, p2.x);
        int y = Math.min(p1.y, p2.y);
        int w = Math.abs(p2.x - p1.x);
        int h = Math.abs(p2.y - p1.y);
        return new Rectangle(x, y, w, h);
    }

    @Override
    public void drawPreview(Graphics2D g2d) {
        if (currentMode == Mode.MARQUEE && startPoint != null && marqueeCurrent != null) {
            Rectangle r = getMarqueeRectangle(startPoint, marqueeCurrent);
            g2d.setColor(new Color(56, 189, 248, 35));
            g2d.fillRect(r.x, r.y, r.width, r.height);

            float[] dash = {4.0f, 4.0f};
            g2d.setColor(UITheme.SELECTION_COLOR);
            g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
            g2d.drawRect(r.x, r.y, r.width, r.height);
        } else if (currentMode == Mode.WIRING && wireStartPin != null && wireCurrentMouse != null) {
            Point p1 = wireStartPin.getAbsolutePosition();

            g2d.setColor(new Color(34, 197, 94, 60));
            g2d.setStroke(new BasicStroke(6.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(p1.x, p1.y, wireCurrentMouse.x, wireCurrentMouse.y);

            g2d.setColor(new Color(34, 197, 94));
            g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(p1.x, p1.y, wireCurrentMouse.x, wireCurrentMouse.y);

            g2d.setColor(new Color(34, 197, 94, 120));
            g2d.fillOval(p1.x - 8, p1.y - 8, 16, 16);

            if (wireSnappedPin != null) {
                Point sp = wireSnappedPin.getAbsolutePosition();
                boolean valid = (wireStartPin.isOutput() != wireSnappedPin.isOutput()) && (wireStartPin.getOwner() != wireSnappedPin.getOwner());
                g2d.setColor(valid ? new Color(34, 197, 94, 160) : new Color(239, 68, 68, 160));
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawOval(sp.x - 10, sp.y - 10, 20, 20);
                g2d.fillOval(sp.x - 5, sp.y - 5, 10, 10);
            }
        }
    }
}
