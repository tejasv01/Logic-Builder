# Stage 4: Complete Compilable Java Source Code (All 7 Packages)

Target Environment: **Java 17 LTS** (`java.awt`, `javax.swing`, `javax.imageio`, `java.nio.file`).
All domain, command, factory, and export packages (`exceptions`, `model`, `factory`, `commands`, `export`) have **zero dependency** on `javax.swing`.

## 1. Package `circuitsim.exceptions`

### `src/main/java/circuitsim/exceptions/InvalidShapeException.java`

```
package circuitsim.exceptions;

/**
 * Custom exception thrown when a circuit element, wire connection, or geometric
 * dimension is invalid (e.g., non-positive width/height, null text, invalid pin wiring).
 *
 * OOM Concept: Exception Handling (Custom Checked Exception) & Encapsulation.
 */
public class InvalidShapeException extends Exception {
    public InvalidShapeException(String message) {
        super(message);
    }

    public InvalidShapeException(String message, Throwable cause) {
        super(message, cause);
    }
}

```

### `src/main/java/circuitsim/exceptions/FileFormatException.java`

```
package circuitsim.exceptions;

/**
 * Custom exception thrown when loading a circuit JSON file fails due to
 * malformed syntax, missing required fields, or unknown element types.
 *
 * OOM Concept: Exception Handling (Custom Checked Exception).
 */
public class FileFormatException extends Exception {
    public FileFormatException(String message) {
        super(message);
    }

    public FileFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}

```

### `src/main/java/circuitsim/exceptions/ExportFailedException.java`

```
package circuitsim.exceptions;

/**
 * Custom exception thrown when exporting the circuit to SVG, PNG, or JSON fails
 * due to I/O errors or rendering issues.
 *
 * OOM Concept: Exception Handling (Custom Checked Exception).
 */
public class ExportFailedException extends Exception {
    public ExportFailedException(String message) {
        super(message);
    }

    public ExportFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}

```

### `src/main/java/circuitsim/exceptions/EmptyUndoStackException.java`

```
package circuitsim.exceptions;

/**
 * Custom exception thrown when an undo() or redo() operation is requested
 * while the corresponding history stack in CommandManager is empty.
 *
 * OOM Concept: Exception Handling (Custom Checked Exception).
 */
public class EmptyUndoStackException extends Exception {
    public EmptyUndoStackException(String message) {
        super(message);
    }
}

```

## 2. Package `circuitsim.model`

### `src/main/java/circuitsim/model/ElementType.java`

```
package circuitsim.model;

/**
 * Enumeration of all supported circuit element types in the Logic Circuit Simulator.
 *
 * SOLID (OCP): New element types can be registered and created via ShapeFactory.
 */
public enum ElementType {
    AND,
    OR,
    NOT,
    XOR,
    NAND,
    NOR,
    SWITCH,
    LED,
    WIRE,
    TEXT,
    GROUP
}

```

### `src/main/java/circuitsim/model/ElementStyle.java`

```
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

```

### `src/main/java/circuitsim/model/Pin.java`

```
package circuitsim.model;

import java.awt.Point;

/**
 * Represents an electrical input or output terminal (Pin) owned by a CircuitElement.
 *
 * OOM Relationship:
 * - COMPOSITION: A Pin is strictly composed inside its owning CircuitElement
 *   (LogicGate, InputSwitch, or OutputLed). Its coordinate is relative to its owner,
 *   and its lifecycle is bound to the owner element.
 */
public class Pin {
    private final String id;
    private final CircuitElement owner; // Composition back-reference to owning element
    private final boolean output;
    private int offsetX;
    private int offsetY;
    private boolean value;

    public Pin(String id, CircuitElement owner, boolean output, int offsetX, int offsetY) {
        this.id = id;
        this.owner = owner;
        this.output = output;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.value = false;
    }

    public String getId() {
        return id;
    }

    public CircuitElement getOwner() {
        return owner;
    }

    public boolean isOutput() {
        return output;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsets(int offsetX, int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public Point getAbsolutePosition() {
        return new Point(owner.getX() + offsetX, owner.getY() + offsetY);
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public boolean hitTest(Point p, int tolerance) {
        Point abs = getAbsolutePosition();
        return abs.distance(p) <= tolerance;
    }
}

```

### `src/main/java/circuitsim/model/CircuitElement.java`

```
package circuitsim.model;

import circuitsim.exceptions.InvalidShapeException;
import circuitsim.export.CircuitVisitor;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Abstract base class representing any visual and logical element on the circuit canvas.
 * Completely independent of javax.swing (uses only java.awt geometry/graphics primitives).
 *
 * OOM Concepts:
 * - Abstraction & Inheritance: Defines the common contract for gates, switches, LEDs, wires, labels, and groups.
 * - Polymorphism: Subclasses override draw(), evaluate(), accept(), and clone().
 * - Composition: Owns an ElementStyle instance (strong lifecycle ownership).
 *
 * Design Patterns:
 * - Prototype Pattern: Implements Cloneable via abstract clone() for Copy/Paste.
 * - Visitor Pattern: Defines accept(CircuitVisitor) for SVG/JSON export.
 *
 * SOLID Principles:
 * - SRP: Manages element geometry, identity, selection state, and style.
 * - OCP & LSP: New circuit elements can extend CircuitElement and substitute cleanly anywhere.
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

    public List<Pin> getPins() {
        return Collections.emptyList();
    }

    public boolean contains(Point p) {
        return getBounds().contains(p);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public void move(int dx, int dy) {
        this.x += dx;
        this.y += dy;
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
            g2d.setColor(pin.getValue() ? new Color(22, 163, 74) : new Color(71, 85, 105));
            g2d.fillOval(pos.x - 5, pos.y - 5, 10, 10);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(pos.x - 5, pos.y - 5, 10, 10);
        }
    }

    protected void drawSelectionHandles(Graphics2D g2d) {
        if (!selected) {
            return;
        }
        g2d.setColor(new Color(37, 99, 235));
        g2d.drawRect(x - 2, y - 2, width + 4, height + 4);
        // Bottom-right resize handle
        g2d.fillRect(x + width - 4, y + height - 4, 8, 8);
    }

    public boolean isOnResizeHandle(Point p) {
        if (!selected) {
            return false;
        }
        Rectangle handle = new Rectangle(x + width - 6, y + height - 6, 12, 12);
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

```

### `src/main/java/circuitsim/model/LogicGate.java`

```
package circuitsim.model;

import circuitsim.exceptions.InvalidShapeException;
import circuitsim.export.CircuitVisitor;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract superclass for all boolean logic gates (AND, OR, NOT, XOR, NAND, NOR).
 *
 * OOM Relationships:
 * - INHERITANCE: Extends CircuitElement.
 * - COMPOSITION: Strictly creates and owns its input Pin list and output Pin.
 *   When a LogicGate is destroyed or cloned, its Pins are destroyed or recreated with it.
 */
public abstract class LogicGate extends CircuitElement {
    // COMPOSITION: LogicGate owns its inputPins and outputPin
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
        g2d.setColor(style.getFillColor());
        g2d.fillRoundRect(x, y, width, height, 12, 12);

        g2d.setColor(style.getStrokeColor());
        g2d.setStroke(new BasicStroke(style.getStrokeWidth()));
        g2d.drawRoundRect(x, y, width, height, 12, 12);

        // Draw gate symbol label centered
        g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
        FontMetrics fm = g2d.getFontMetrics();
        String label = getGateSymbol();
        int tx = x + (width - fm.stringWidth(label)) / 2;
        int ty = y + ((height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(label, tx, ty);

        drawPins(g2d);
        drawSelectionHandles(g2d);
    }

    @Override
    public void accept(CircuitVisitor visitor) {
        visitor.visitGate(this);
    }
}

```

### `src/main/java/circuitsim/model/AndGate.java`

```
package circuitsim.model;

import circuitsim.exceptions.InvalidShapeException;

/**
 * Concrete 2-input AND Logic Gate. Output is HIGH (true) iff all inputs are HIGH.
 */
public class AndGate extends LogicGate {
    public AndGate(int x, int y, int width, int height, ElementStyle style)
            throws InvalidShapeException {
        super(x, y, width, height, style, 2);
    }

    @Override
    public ElementType getType() {
        return ElementType.AND;
    }

    @Override
    public String getGateSymbol() {
        return "AND (&)";
    }

    @Override
    public void evaluate() {
        boolean result = true;
        for (Pin pin : inputPins) {
            result = result && pin.getValue();
        }
        outputPin.setValue(result);
    }

    @Override
    public AndGate clone() {
        try {
            return new AndGate(x, y, width, height, style.clone());
        } catch (InvalidShapeException e) {
            throw new AssertionError("Cloning valid AndGate should not fail", e);
        }
    }
}

```

### `src/main/java/circuitsim/model/OrGate.java`

```
package circuitsim.model;

import circuitsim.exceptions.InvalidShapeException;

/**
 * Concrete 2-input OR Logic Gate. Output is HIGH (true) if at least one input is HIGH.
 */
public class OrGate extends LogicGate {
    public OrGate(int x, int y, int width, int height, ElementStyle style)
            throws InvalidShapeException {
        super(x, y, width, height, style, 2);
    }

    @Override
    public ElementType getType() {
        return ElementType.OR;
    }

    @Override
    public String getGateSymbol() {
        return "OR (>=1)";
    }

    @Override
    public void evaluate() {
        boolean result = false;
        for (Pin pin : inputPins) {
            result = result || pin.getValue();
        }
        outputPin.setValue(result);
    }

    @Override
    public OrGate clone() {
        try {
            return new OrGate(x, y, width, height, style.clone());
        } catch (InvalidShapeException e) {
            throw new AssertionError("Cloning valid OrGate should not fail", e);
        }
    }
}

```

### `src/main/java/circuitsim/model/NotGate.java`

```
package circuitsim.model;

import circuitsim.exceptions.InvalidShapeException;

/**
 * Concrete 1-input NOT (Inverter) Logic Gate. Output is the logical negation of its input.
 */
public class NotGate extends LogicGate {
    public NotGate(int x, int y, int width, int height, ElementStyle style)
            throws InvalidShapeException {
        super(x, y, width, height, style, 1);
    }

    @Override
    public ElementType getType() {
        return ElementType.NOT;
    }

    @Override
    public String getGateSymbol() {
        return "NOT (!)";
    }

    @Override
    public void evaluate() {
        outputPin.setValue(!inputPins.get(0).getValue());
    }

    @Override
    public NotGate clone() {
        try {
            return new NotGate(x, y, width, height, style.clone());
        } catch (InvalidShapeException e) {
            throw new AssertionError("Cloning valid NotGate should not fail", e);
        }
    }
}

```

### `src/main/java/circuitsim/model/XorGate.java`

```
package circuitsim.model;

import circuitsim.exceptions.InvalidShapeException;

/**
 * Concrete 2-input XOR Logic Gate. Output is HIGH iff inputs differ.
 */
public class XorGate extends LogicGate {
    public XorGate(int x, int y, int width, int height, ElementStyle style)
            throws InvalidShapeException {
        super(x, y, width, height, style, 2);
    }

    @Override
    public ElementType getType() {
        return ElementType.XOR;
    }

    @Override
    public String getGateSymbol() {
        return "XOR (^)";
    }

    @Override
    public void evaluate() {
        boolean a = inputPins.get(0).getValue();
        boolean b = inputPins.get(1).getValue();
        outputPin.setValue(a ^ b);
    }

    @Override
    public XorGate clone() {
        try {
            return new XorGate(x, y, width, height, style.clone());
        } catch (InvalidShapeException e) {
            throw new AssertionError("Cloning valid XorGate should not fail", e);
        }
    }
}

```

### `src/main/java/circuitsim/model/NandGate.java`

```
package circuitsim.model;

import circuitsim.exceptions.InvalidShapeException;

/**
 * Concrete 2-input NAND Logic Gate. Output is LOW iff all inputs are HIGH.
 */
public class NandGate extends LogicGate {
    public NandGate(int x, int y, int width, int height, ElementStyle style)
            throws InvalidShapeException {
        super(x, y, width, height, style, 2);
    }

    @Override
    public ElementType getType() {
        return ElementType.NAND;
    }

    @Override
    public String getGateSymbol() {
        return "NAND";
    }

    @Override
    public void evaluate() {
        boolean andResult = true;
        for (Pin pin : inputPins) {
            andResult = andResult && pin.getValue();
        }
        outputPin.setValue(!andResult);
    }

    @Override
    public NandGate clone() {
        try {
            return new NandGate(x, y, width, height, style.clone());
        } catch (InvalidShapeException e) {
            throw new AssertionError("Cloning valid NandGate should not fail", e);
        }
    }
}

```

### `src/main/java/circuitsim/model/NorGate.java`

```
package circuitsim.model;

import circuitsim.exceptions.InvalidShapeException;

/**
 * Concrete 2-input NOR Logic Gate. Output is HIGH iff all inputs are LOW.
 */
public class NorGate extends LogicGate {
    public NorGate(int x, int y, int width, int height, ElementStyle style)
            throws InvalidShapeException {
        super(x, y, width, height, style, 2);
    }

    @Override
    public ElementType getType() {
        return ElementType.NOR;
    }

    @Override
    public String getGateSymbol() {
        return "NOR";
    }

    @Override
    public void evaluate() {
        boolean orResult = false;
        for (Pin pin : inputPins) {
            orResult = orResult || pin.getValue();
        }
        outputPin.setValue(!orResult);
    }

    @Override
    public NorGate clone() {
        try {
            return new NorGate(x, y, width, height, style.clone());
        } catch (InvalidShapeException e) {
            throw new AssertionError("Cloning valid NorGate should not fail", e);
        }
    }
}

```

### `src/main/java/circuitsim/model/InputSwitch.java`

```
package circuitsim.model;

import circuitsim.exceptions.InvalidShapeException;
import circuitsim.export.CircuitVisitor;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.Collections;
import java.util.List;

/**
 * Interactive boolean signal source (Input Switch) that users can toggle between
 * HIGH (1 / true) and LOW (0 / false).
 *
 * OOM Relationship:
 * - COMPOSITION: Strictly owns its single outputPin.
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
        g2d.setColor(state ? new Color(187, 247, 208) : style.getFillColor());
        g2d.fillRect(x, y, width, height);

        g2d.setColor(style.getStrokeColor());
        g2d.setStroke(new BasicStroke(style.getStrokeWidth()));
        g2d.drawRect(x, y, width, height);

        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        String label = "SW: " + (state ? "1" : "0");
        int tx = x + (width - fm.stringWidth(label)) / 2;
        int ty = y + ((height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(label, tx, ty);

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

```

### `src/main/java/circuitsim/model/OutputLed.java`

```
package circuitsim.model;

import circuitsim.exceptions.InvalidShapeException;
import circuitsim.export.CircuitVisitor;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Collections;
import java.util.List;

/**
 * Circular Output LED indicator (equivalent to Circle in vector primitives) that lights up
 * bright green when its input pin receives a HIGH (true) signal.
 *
 * OOM Relationship:
 * - COMPOSITION: Strictly owns its single inputPin.
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
        g2d.setColor(isLit() ? new Color(74, 222, 128) : style.getFillColor());
        g2d.fillOval(x, y, width, height);

        g2d.setColor(style.getStrokeColor());
        g2d.setStroke(new BasicStroke(style.getStrokeWidth()));
        g2d.drawOval(x, y, width, height);

        g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fm = g2d.getFontMetrics();
        String label = isLit() ? "ON" : "OFF";
        int tx = x + (width - fm.stringWidth(label)) / 2;
        int ty = y + ((height - fm.getHeight()) / 2) + fm.getAscent();
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

```

### `src/main/java/circuitsim/model/Wire.java`

```
package circuitsim.model;

import circuitsim.exceptions.InvalidShapeException;
import circuitsim.export.CircuitVisitor;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;

/**
 * Directed electrical connection (Line primitive) linking an output Pin to an input Pin.
 *
 * OOM Relationship:
 * - ASSOCIATION: Wire holds references to sourcePin and targetPin owned by other
 *   CircuitElements. Wire does not own those pins.
 */
public class Wire extends CircuitElement {
    private final Pin sourcePin; // ASSOCIATION: references output Pin of source element
    private final Pin targetPin; // ASSOCIATION: references input Pin of target element

    public Wire(Pin sourcePin, Pin targetPin, ElementStyle style) throws InvalidShapeException {
        super(0, 0, 10, 10, style);
        if (sourcePin == null || targetPin == null) {
            throw new InvalidShapeException("Wire requires non-null source and target pins.");
        }
        if (sourcePin == targetPin || sourcePin.getOwner() == targetPin.getOwner()) {
            throw new InvalidShapeException("Cannot connect a component pin to itself.");
        }
        if (!sourcePin.isOutput() || targetPin.isOutput()) {
            throw new InvalidShapeException("Wire must connect an OUTPUT pin to an INPUT pin.");
        }
        this.sourcePin = sourcePin;
        this.targetPin = targetPin;
        syncGeometryFromPins();
    }

    private void syncGeometryFromPins() {
        Point p1 = sourcePin.getAbsolutePosition();
        Point p2 = targetPin.getAbsolutePosition();
        this.x = Math.min(p1.x, p2.x);
        this.y = Math.min(p1.y, p2.y);
        this.width = Math.max(10, Math.abs(p2.x - p1.x));
        this.height = Math.max(10, Math.abs(p2.y - p1.y));
    }

    public Pin getSourcePin() {
        return sourcePin;
    }

    public Pin getTargetPin() {
        return targetPin;
    }

    @Override
    public ElementType getType() {
        return ElementType.WIRE;
    }

    @Override
    public void evaluate() {
        targetPin.setValue(sourcePin.getValue());
    }

    @Override
    public boolean contains(Point p) {
        Point p1 = sourcePin.getAbsolutePosition();
        Point p2 = targetPin.getAbsolutePosition();
        double dist = Line2D.ptSegDist(p1.x, p1.y, p2.x, p2.y, p.x, p.y);
        return dist <= 6.0;
    }

    @Override
    public void draw(Graphics2D g2d) {
        syncGeometryFromPins();
        Point p1 = sourcePin.getAbsolutePosition();
        Point p2 = targetPin.getAbsolutePosition();

        Color wireColor = sourcePin.getValue()
                ? new Color(22, 163, 74)
                : style.getStrokeColor();
        if (selected) {
            wireColor = new Color(37, 99, 235);
        }

        g2d.setColor(wireColor);
        g2d.setStroke(new BasicStroke(style.getStrokeWidth() + 0.5f));
        g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
    }

    @Override
    public void accept(CircuitVisitor visitor) {
        visitor.visitWire(this);
    }

    @Override
    public Wire clone() {
        try {
            return new Wire(sourcePin, targetPin, style.clone());
        } catch (InvalidShapeException e) {
            throw new AssertionError(e);
        }
    }
}

```

### `src/main/java/circuitsim/model/TextLabel.java`

```
package circuitsim.model;

import circuitsim.exceptions.InvalidShapeException;
import circuitsim.export.CircuitVisitor;
import java.awt.BasicStroke;
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
        g2d.setColor(style.getFillColor());
        g2d.fillRoundRect(x, y, width, height, 6, 6);

        g2d.setColor(style.getStrokeColor());
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRoundRect(x, y, width, height, 6, 6);

        g2d.setFont(new Font("SansSerif", Font.PLAIN, 13));
        FontMetrics fm = g2d.getFontMetrics();
        int tx = x + 8;
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

```

### `src/main/java/circuitsim/model/GroupShape.java`

```
package circuitsim.model;

import circuitsim.exceptions.InvalidShapeException;
import circuitsim.export.CircuitVisitor;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Composite Subcircuit / Group element that treats a collection of CircuitElements
 * uniformly as a single selectable, movable, stylable, and clonable CircuitElement.
 *
 * Design Pattern:
 * - COMPOSITE PATTERN: GroupShape is the Composite; CircuitElement is the Component.
 *
 * OOM Relationship:
 * - AGGREGATION: GroupShape holds child CircuitElement objects that can exist
 *   independently before grouping and after ungrouping.
 */
public class GroupShape extends CircuitElement {
    // AGGREGATION: Children can survive independently when ungrouped
    private final List<CircuitElement> children = new ArrayList<>();

    public GroupShape(List<CircuitElement> elements, ElementStyle style)
            throws InvalidShapeException {
        super(0, 0, 40, 40, style);
        if (elements == null || elements.isEmpty()) {
            throw new InvalidShapeException("GroupShape must contain at least one child element.");
        }
        this.children.addAll(elements);
        recalculateBounds();
    }

    public final void recalculateBounds() {
        if (children.isEmpty()) {
            return;
        }
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (CircuitElement child : children) {
            Rectangle b = child.getBounds();
            minX = Math.min(minX, b.x);
            minY = Math.min(minY, b.y);
            maxX = Math.max(maxX, b.x + b.width);
            maxY = Math.max(maxY, b.y + b.height);
        }
        this.x = minX - 6;
        this.y = minY - 6;
        this.width = Math.max(20, (maxX - minX) + 12);
        this.height = Math.max(20, (maxY - minY) + 12);
    }

    public List<CircuitElement> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public ElementType getType() {
        return ElementType.GROUP;
    }

    @Override
    public void move(int dx, int dy) {
        for (CircuitElement child : children) {
            if (child.getType() != ElementType.WIRE) {
                child.move(dx, dy);
            }
        }
        recalculateBounds();
    }

    @Override
    public void setStyle(ElementStyle newStyle) {
        super.setStyle(newStyle);
        for (CircuitElement child : children) {
            child.setStyle(newStyle);
        }
    }

    @Override
    public void evaluate() {
        for (CircuitElement child : children) {
            child.evaluate();
        }
    }

    @Override
    public boolean contains(Point p) {
        for (CircuitElement child : children) {
            if (child.contains(p)) {
                return true;
            }
        }
        return getBounds().contains(p);
    }

    @Override
    public void draw(Graphics2D g2d) {
        recalculateBounds();
        for (CircuitElement child : children) {
            child.draw(g2d);
        }
        float[] dash = {5.0f, 5.0f};
        g2d.setColor(selected ? new Color(37, 99, 235) : new Color(100, 116, 139));
        g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
        g2d.drawRoundRect(x, y, width, height, 10, 10);
    }

    @Override
    public void accept(CircuitVisitor visitor) {
        visitor.visitGroup(this);
    }

    @Override
    public GroupShape clone() {
        try {
            List<CircuitElement> clonedChildren = new ArrayList<>();
            for (CircuitElement child : children) {
                if (child.getType() != ElementType.WIRE) {
                    clonedChildren.add(child.clone());
                }
            }
            return new GroupShape(clonedChildren, style.clone());
        } catch (InvalidShapeException e) {
            throw new AssertionError(e);
        }
    }
}

```

### `src/main/java/circuitsim/model/CircuitListener.java`

```
package circuitsim.model;

/**
 * Observer Pattern interface notified whenever the Circuit model state changes
 * (elements added/removed/moved/styled or boolean signals propagated).
 *
 * Design Pattern: OBSERVER PATTERN
 * SOLID (DIP): The high-level Circuit model depends on this abstraction rather
 * than concrete Swing UI classes.
 */
public interface CircuitListener {
    void onCircuitChanged();
}

```

### `src/main/java/circuitsim/model/Circuit.java`

```
package circuitsim.model;

import circuitsim.export.CircuitVisitor;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Central domain model (equivalent to Drawing) representing the entire logic circuit.
 * Holds elements, evaluates boolean signal propagation, and notifies registered
 * CircuitListener observers.
 *
 * OOM Relationships:
 * - AGGREGATION: Holds a list of CircuitElement instances.
 * - ASSOCIATION: Maintains a list of CircuitListener observers.
 *
 * Design Pattern:
 * - OBSERVER PATTERN (Subject): Notifies UI listeners via notifyListeners().
 */
public class Circuit {
    // AGGREGATION: Circuit aggregates CircuitElements
    private final List<CircuitElement> elements = new ArrayList<>();
    // ASSOCIATION: Observer listeners
    private final List<CircuitListener> listeners = new ArrayList<>();

    public void addElement(CircuitElement element) {
        if (element != null && !elements.contains(element)) {
            elements.add(element);
            evaluate();
            notifyListeners();
        }
    }

    public void removeElement(CircuitElement element) {
        if (elements.remove(element)) {
            evaluate();
            notifyListeners();
        }
    }

    public void clear() {
        elements.clear();
        notifyListeners();
    }

    public List<CircuitElement> getElements() {
        return Collections.unmodifiableList(elements);
    }

    public List<CircuitElement> getSelectedElements() {
        List<CircuitElement> selected = new ArrayList<>();
        for (CircuitElement el : elements) {
            if (el.isSelected()) {
                selected.add(el);
            }
        }
        return selected;
    }

    public void clearSelection() {
        for (CircuitElement el : elements) {
            el.setSelected(false);
        }
        notifyListeners();
    }

    public CircuitElement findElementAt(Point p) {
        for (int i = elements.size() - 1; i >= 0; i--) {
            CircuitElement el = elements.get(i);
            if (el.contains(p)) {
                return el;
            }
        }
        return null;
    }

    public Pin findPinAt(Point p, int tolerance) {
        for (int i = elements.size() - 1; i >= 0; i--) {
            CircuitElement el = elements.get(i);
            for (Pin pin : el.getPins()) {
                if (pin.hitTest(p, tolerance)) {
                    return pin;
                }
            }
        }
        return null;
    }

    public boolean isInputPinAlreadyWired(Pin targetPin) {
        for (CircuitElement el : elements) {
            if (el instanceof Wire) {
                Wire w = (Wire) el;
                if (w.getTargetPin() == targetPin) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Wire> getWiresConnectedTo(CircuitElement element) {
        List<Wire> connected = new ArrayList<>();
        for (CircuitElement el : elements) {
            if (el instanceof Wire) {
                Wire w = (Wire) el;
                if (w.getSourcePin().getOwner() == element || w.getTargetPin().getOwner() == element) {
                    connected.add(w);
                }
            }
        }
        return connected;
    }

    /**
     * Evaluates boolean logic iteratively across all switches, wires, and gates
     * so multi-stage combinational circuits settle cleanly.
     */
    public void evaluate() {
        // Reset input pins that have no incoming wire to false
        for (CircuitElement el : elements) {
            for (Pin pin : el.getPins()) {
                if (!pin.isOutput() && !isInputPinAlreadyWired(pin)) {
                    pin.setValue(false);
                }
            }
        }
        int passes = Math.max(3, elements.size() + 1);
        for (int pass = 0; pass < passes; pass++) {
            for (CircuitElement el : elements) {
                el.evaluate();
            }
        }
        notifyListeners();
    }

    public void accept(CircuitVisitor visitor) {
        for (CircuitElement el : elements) {
            el.accept(visitor);
        }
    }

    public void addListener(CircuitListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(CircuitListener listener) {
        listeners.remove(listener);
    }

    public void notifyListeners() {
        for (CircuitListener listener : listeners) {
            listener.onCircuitChanged();
        }
    }
}

```

## 3. Package `circuitsim.factory`

### `src/main/java/circuitsim/factory/ShapeFactory.java`

```
package circuitsim.factory;

import circuitsim.exceptions.InvalidShapeException;
import circuitsim.model.AndGate;
import circuitsim.model.CircuitElement;
import circuitsim.model.ElementStyle;
import circuitsim.model.ElementType;
import circuitsim.model.InputSwitch;
import circuitsim.model.NandGate;
import circuitsim.model.NorGate;
import circuitsim.model.NotGate;
import circuitsim.model.OrGate;
import circuitsim.model.OutputLed;
import circuitsim.model.Pin;
import circuitsim.model.TextLabel;
import circuitsim.model.Wire;
import circuitsim.model.XorGate;

/**
 * Factory class responsible for instantiating concrete CircuitElement objects
 * and validating creation parameters.
 *
 * Design Pattern:
 * - FACTORY PATTERN: Centralizes object creation so tools and JSON loaders do
 *   not couple directly to concrete gate constructors.
 *
 * SOLID Principles:
 * - SRP: Solely responsible for constructing and validating CircuitElements.
 * - DIP: Clients depend on the abstract CircuitElement product type.
 */
public final class ShapeFactory {

    private ShapeFactory() {
        // Utility factory class
    }

    public static CircuitElement createElement(
            ElementType type,
            int x,
            int y,
            int width,
            int height,
            ElementStyle style,
            String text) throws InvalidShapeException {

        if (type == null) {
            throw new InvalidShapeException("ElementType cannot be null.");
        }

        switch (type) {
            case AND:
                return new AndGate(x, y, width, height, style);
            case OR:
                return new OrGate(x, y, width, height, style);
            case NOT:
                return new NotGate(x, y, width, height, style);
            case XOR:
                return new XorGate(x, y, width, height, style);
            case NAND:
                return new NandGate(x, y, width, height, style);
            case NOR:
                return new NorGate(x, y, width, height, style);
            case SWITCH:
                return new InputSwitch(x, y, width, height, style);
            case LED:
                return new OutputLed(x, y, width, height, style);
            case TEXT:
                return new TextLabel(x, y, width, height, style, text);
            default:
                throw new InvalidShapeException("Unsupported element type for standard factory creation: " + type);
        }
    }

    public static Wire createWire(Pin sourcePin, Pin targetPin, ElementStyle style)
            throws InvalidShapeException {
        return new Wire(sourcePin, targetPin, style);
    }
}

```

## 4. Package `circuitsim.commands`

### `src/main/java/circuitsim/commands/Command.java`

```
package circuitsim.commands;

/**
 * Command Pattern interface encapsulating a reversible action on the Circuit model.
 *
 * Design Pattern: COMMAND PATTERN
 * SOLID (OCP): New commands can be implemented without modifying CommandManager.
 */
public interface Command {
    void execute();
    void undo();
    String getName();
}

```

### `src/main/java/circuitsim/commands/CommandManager.java`

```
package circuitsim.commands;

import circuitsim.exceptions.EmptyUndoStackException;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Invoker class in the Command Pattern that manages the undo and redo stacks.
 *
 * OOM Relationship:
 * - AGGREGATION: Holds Command instances in undoStack and redoStack.
 */
public class CommandManager {
    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    public void executeCommand(Command command) {
        if (command != null) {
            command.execute();
            undoStack.push(command);
            redoStack.clear();
        }
    }

    public void undo() throws EmptyUndoStackException {
        if (undoStack.isEmpty()) {
            throw new EmptyUndoStackException("Nothing to undo: undo stack is empty.");
        }
        Command cmd = undoStack.pop();
        cmd.undo();
        redoStack.push(cmd);
    }

    public void redo() throws EmptyUndoStackException {
        if (redoStack.isEmpty()) {
            throw new EmptyUndoStackException("Nothing to redo: redo stack is empty.");
        }
        Command cmd = redoStack.pop();
        cmd.execute();
        undoStack.push(cmd);
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}

```

### `src/main/java/circuitsim/commands/AddShapeCommand.java`

```
package circuitsim.commands;

import circuitsim.model.Circuit;
import circuitsim.model.CircuitElement;

/**
 * Concrete Command to add a CircuitElement (gate, switch, LED, wire, label) to the Circuit.
 */
public class AddShapeCommand implements Command {
    private final Circuit circuit;
    private final CircuitElement element;

    public AddShapeCommand(Circuit circuit, CircuitElement element) {
        this.circuit = circuit;
        this.element = element;
    }

    @Override
    public void execute() {
        circuit.addElement(element);
    }

    @Override
    public void undo() {
        circuit.removeElement(element);
    }

    @Override
    public String getName() {
        return "Add " + element.getType();
    }
}

```

### `src/main/java/circuitsim/commands/MoveCommand.java`

```
package circuitsim.commands;

import circuitsim.model.Circuit;
import circuitsim.model.CircuitElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Concrete Command that moves one or more selected CircuitElements by (dx, dy)
 * and reverses the translation on undo().
 */
public class MoveCommand implements Command {
    private final Circuit circuit;
    private final List<CircuitElement> targets;
    private final int dx;
    private final int dy;

    public MoveCommand(Circuit circuit, CircuitElement target, int dx, int dy) {
        this(circuit, Collections.singletonList(target), dx, dy);
    }

    public MoveCommand(Circuit circuit, List<CircuitElement> targets, int dx, int dy) {
        this.circuit = circuit;
        this.targets = new ArrayList<>(targets);
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    public void execute() {
        for (CircuitElement el : targets) {
            el.move(dx, dy);
        }
        circuit.notifyListeners();
    }

    @Override
    public void undo() {
        for (CircuitElement el : targets) {
            el.move(-dx, -dy);
        }
        circuit.notifyListeners();
    }

    @Override
    public String getName() {
        return "Move Element(s)";
    }
}

```

### `src/main/java/circuitsim/commands/ResizeCommand.java`

```
package circuitsim.commands;

import circuitsim.exceptions.InvalidShapeException;
import circuitsim.model.Circuit;
import circuitsim.model.CircuitElement;

/**
 * Concrete Command that resizes a CircuitElement and restores its previous dimensions on undo().
 */
public class ResizeCommand implements Command {
    private final Circuit circuit;
    private final CircuitElement element;
    private final int oldWidth;
    private final int oldHeight;
    private final int newWidth;
    private final int newHeight;

    public ResizeCommand(Circuit circuit, CircuitElement element, int oldW, int oldH, int newW, int newH) {
        this.circuit = circuit;
        this.element = element;
        this.oldWidth = oldW;
        this.oldHeight = oldH;
        this.newWidth = newW;
        this.newHeight = newH;
    }

    @Override
    public void execute() {
        try {
            element.resize(newWidth, newHeight);
            circuit.notifyListeners();
        } catch (InvalidShapeException ignored) {
            // Validated prior to creating command
        }
    }

    @Override
    public void undo() {
        try {
            element.resize(oldWidth, oldHeight);
            circuit.notifyListeners();
        } catch (InvalidShapeException ignored) {
        }
    }

    @Override
    public String getName() {
        return "Resize " + element.getType();
    }
}

```

### `src/main/java/circuitsim/commands/DeleteCommand.java`

```
package circuitsim.commands;

import circuitsim.model.Circuit;
import circuitsim.model.CircuitElement;
import circuitsim.model.Wire;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete Command that removes selected CircuitElement(s) along with any attached Wires,
 * and cleanly restores both the elements and their wires on undo().
 */
public class DeleteCommand implements Command {
    private final Circuit circuit;
    private final List<CircuitElement> deletedElements = new ArrayList<>();
    private final List<Wire> attachedWires = new ArrayList<>();

    public DeleteCommand(Circuit circuit, List<CircuitElement> targets) {
        this.circuit = circuit;
        this.deletedElements.addAll(targets);
        for (CircuitElement el : targets) {
            for (Wire w : circuit.getWiresConnectedTo(el)) {
                if (!deletedElements.contains(w) && !attachedWires.contains(w)) {
                    attachedWires.add(w);
                }
            }
        }
    }

    @Override
    public void execute() {
        for (Wire w : attachedWires) {
            circuit.removeElement(w);
        }
        for (CircuitElement el : deletedElements) {
            circuit.removeElement(el);
        }
    }

    @Override
    public void undo() {
        for (CircuitElement el : deletedElements) {
            circuit.addElement(el);
        }
        for (Wire w : attachedWires) {
            circuit.addElement(w);
        }
    }

    @Override
    public String getName() {
        return "Delete Element(s)";
    }
}

```

### `src/main/java/circuitsim/commands/RecolorCommand.java`

```
package circuitsim.commands;

import circuitsim.model.Circuit;
import circuitsim.model.CircuitElement;
import circuitsim.model.ElementStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Concrete Command that updates the ElementStyle (fill color, stroke color, stroke width)
 * of selected CircuitElements and restores their individual original styles on undo().
 */
public class RecolorCommand implements Command {
    private final Circuit circuit;
    private final List<CircuitElement> targets;
    private final Map<CircuitElement, ElementStyle> oldStyles = new HashMap<>();
    private final ElementStyle newStyle;

    public RecolorCommand(Circuit circuit, List<CircuitElement> targets, ElementStyle newStyle) {
        this.circuit = circuit;
        this.targets = new ArrayList<>(targets);
        this.newStyle = newStyle.clone();
        for (CircuitElement el : targets) {
            oldStyles.put(el, el.getStyle().clone());
        }
    }

    @Override
    public void execute() {
        for (CircuitElement el : targets) {
            el.setStyle(newStyle.clone());
        }
        circuit.notifyListeners();
    }

    @Override
    public void undo() {
        for (CircuitElement el : targets) {
            ElementStyle prev = oldStyles.get(el);
            if (prev != null) {
                el.setStyle(prev.clone());
            }
        }
        circuit.notifyListeners();
    }

    @Override
    public String getName() {
        return "Update Style";
    }
}

```

### `src/main/java/circuitsim/commands/GroupCommand.java`

```
package circuitsim.commands;

import circuitsim.exceptions.InvalidShapeException;
import circuitsim.model.Circuit;
import circuitsim.model.CircuitElement;
import circuitsim.model.ElementStyle;
import circuitsim.model.GroupShape;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete Command that groups multiple CircuitElements into a Composite GroupShape
 * (or ungroups an existing GroupShape back into individual elements).
 */
public class GroupCommand implements Command {
    private final Circuit circuit;
    private final List<CircuitElement> members;
    private final GroupShape groupShape;
    private final boolean ungroupMode;

    public GroupCommand(Circuit circuit, List<CircuitElement> members) throws InvalidShapeException {
        this.circuit = circuit;
        this.members = new ArrayList<>(members);
        this.groupShape = new GroupShape(this.members, ElementStyle.createDefault());
        this.ungroupMode = false;
    }

    public GroupCommand(Circuit circuit, GroupShape existingGroup) {
        this.circuit = circuit;
        this.groupShape = existingGroup;
        this.members = new ArrayList<>(existingGroup.getChildren());
        this.ungroupMode = true;
    }

    @Override
    public void execute() {
        if (!ungroupMode) {
            for (CircuitElement m : members) {
                m.setSelected(false);
                circuit.removeElement(m);
            }
            groupShape.setSelected(true);
            circuit.addElement(groupShape);
        } else {
            circuit.removeElement(groupShape);
            for (CircuitElement m : members) {
                m.setSelected(true);
                circuit.addElement(m);
            }
        }
    }

    @Override
    public void undo() {
        if (!ungroupMode) {
            circuit.removeElement(groupShape);
            for (CircuitElement m : members) {
                m.setSelected(true);
                circuit.addElement(m);
            }
        } else {
            for (CircuitElement m : members) {
                m.setSelected(false);
                circuit.removeElement(m);
            }
            groupShape.setSelected(true);
            circuit.addElement(groupShape);
        }
    }

    @Override
    public String getName() {
        return ungroupMode ? "Ungroup Subcircuit" : "Group Subcircuit";
    }
}

```

## 5. Package `circuitsim.tools`

### `src/main/java/circuitsim/tools/Tool.java`

```
package circuitsim.tools;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

/**
 * Strategy / State Pattern interface representing the active interactive tool on the canvas.
 *
 * Design Pattern: STRATEGY / STATE PATTERN
 * SOLID (ISP & OCP): Lean mouse-event interface allowing new tools to be plugged in seamlessly.
 */
public interface Tool {
    void mousePressed(MouseEvent e);
    void mouseDragged(MouseEvent e);
    void mouseReleased(MouseEvent e);
    void drawPreview(Graphics2D g2d);
}

```

### `src/main/java/circuitsim/tools/SelectTool.java`

```
package circuitsim.tools;

import circuitsim.commands.CommandManager;
import circuitsim.commands.MoveCommand;
import circuitsim.commands.ResizeCommand;
import circuitsim.exceptions.InvalidShapeException;
import circuitsim.model.Circuit;
import circuitsim.model.CircuitElement;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Concrete Tool strategy for selecting (single or Shift-multi-select), dragging to move,
 * and dragging the bottom-right handle to resize circuit elements.
 *
 * OOM Relationship:
 * - ASSOCIATION: Associates with Circuit and CommandManager.
 */
public class SelectTool implements Tool {
    private final Circuit circuit;
    private final CommandManager commandManager;

    private Point startPoint;
    private Point lastPoint;
    private CircuitElement activeElement;
    private boolean resizing;
    private int initialWidth;
    private int initialHeight;

    public SelectTool(Circuit circuit, CommandManager commandManager) {
        this.circuit = circuit;
        this.commandManager = commandManager;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        startPoint = e.getPoint();
        lastPoint = startPoint;
        resizing = false;

        // Check if user clicked on a selected element's resize handle
        for (CircuitElement sel : circuit.getSelectedElements()) {
            if (sel.isOnResizeHandle(startPoint)) {
                activeElement = sel;
                resizing = true;
                initialWidth = sel.getWidth();
                initialHeight = sel.getHeight();
                return;
            }
        }

        CircuitElement hit = circuit.findElementAt(startPoint);
        if (hit != null) {
            if (!e.isShiftDown() && !hit.isSelected()) {
                circuit.clearSelection();
            }
            hit.setSelected(true);
            activeElement = hit;
            circuit.notifyListeners();
        } else {
            circuit.clearSelection();
            activeElement = null;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (activeElement == null || lastPoint == null) {
            return;
        }
        Point current = e.getPoint();
        if (resizing) {
            int newW = Math.max(30, initialWidth + (current.x - startPoint.x));
            int newH = Math.max(24, initialHeight + (current.y - startPoint.y));
            try {
                activeElement.resize(newW, newH);
                circuit.notifyListeners();
            } catch (InvalidShapeException ignored) {
            }
        } else {
            int dx = current.x - lastPoint.x;
            int dy = current.y - lastPoint.y;
            for (CircuitElement sel : circuit.getSelectedElements()) {
                sel.move(dx, dy);
            }
            lastPoint = current;
            circuit.notifyListeners();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (activeElement == null || startPoint == null) {
            return;
        }
        Point end = e.getPoint();
        if (resizing) {
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
        } else {
            int totalDx = end.x - startPoint.x;
            int totalDy = end.y - startPoint.y;
            if (totalDx != 0 || totalDy != 0) {
                List<CircuitElement> moved = circuit.getSelectedElements();
                // Revert live drag delta so MoveCommand.execute() applies it cleanly onto undo stack
                for (CircuitElement sel : moved) {
                    sel.move(-totalDx, -totalDy);
                }
                commandManager.executeCommand(new MoveCommand(circuit, moved, totalDx, totalDy));
            }
        }
        activeElement = null;
        resizing = false;
    }

    @Override
    public void drawPreview(Graphics2D g2d) {
        // SelectTool modifies elements live during drag
    }
}

```

### `src/main/java/circuitsim/tools/GatePlacementTool.java`

```
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
 * onto the canvas by clicking or dragging a bounding box.
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
        int x = Math.min(startPoint.x, end.x);
        int y = Math.min(startPoint.y, end.y);
        int w = Math.abs(end.x - startPoint.x);
        int h = Math.abs(end.y - startPoint.y);

        // Provide sensible default size if user simply clicks without dragging
        if (w < 30 || h < 24) {
            w = (elementType == ElementType.LED) ? 46 : 84;
            h = (elementType == ElementType.LED) ? 46 : 50;
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
                statusConsumer.accept("Placed " + elementType + " at (" + x + ", " + y + ")");
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
            int x = Math.min(startPoint.x, currentPoint.x);
            int y = Math.min(startPoint.y, currentPoint.y);
            int w = Math.max(40, Math.abs(currentPoint.x - startPoint.x));
            int h = Math.max(30, Math.abs(currentPoint.y - startPoint.y));
            float[] dash = {4.0f, 4.0f};
            g2d.setColor(new Color(37, 99, 235));
            g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
            g2d.drawRect(x, y, w, h);
        }
    }
}

```

### `src/main/java/circuitsim/tools/WireTool.java`

```
package circuitsim.tools;

import circuitsim.commands.AddShapeCommand;
import circuitsim.commands.CommandManager;
import circuitsim.exceptions.InvalidShapeException;
import circuitsim.factory.ShapeFactory;
import circuitsim.model.Circuit;
import circuitsim.model.ElementStyle;
import circuitsim.model.Pin;
import circuitsim.model.Wire;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Concrete Tool strategy for wiring an output Pin of one element to an input Pin of another.
 */
public class WireTool implements Tool {
    private final Circuit circuit;
    private final CommandManager commandManager;
    private final Supplier<ElementStyle> styleSupplier;
    private final Consumer<String> statusConsumer;

    private Pin startPin;
    private Point currentMouse;

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
        Pin pin = circuit.findPinAt(e.getPoint(), 12);
        if (pin != null) {
            startPin = pin;
            currentMouse = e.getPoint();
        } else if (statusConsumer != null) {
            statusConsumer.accept("Click directly on a component Pin (small circle) to start a wire.");
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (startPin != null) {
            currentMouse = e.getPoint();
            circuit.notifyListeners();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (startPin == null) {
            return;
        }
        Pin endPin = circuit.findPinAt(e.getPoint(), 14);
        if (endPin != null && endPin != startPin) {
            // Allow dragging from output->input or input->output seamlessly
            Pin source = startPin.isOutput() ? startPin : endPin;
            Pin target = startPin.isOutput() ? endPin : startPin;
            try {
                if (!target.isOutput() && circuit.isInputPinAlreadyWired(target)) {
                    throw new InvalidShapeException("Target input pin already has a connected wire.");
                }
                Wire wire = ShapeFactory.createWire(source, target, styleSupplier.get());
                commandManager.executeCommand(new AddShapeCommand(circuit, wire));
                if (statusConsumer != null) {
                    statusConsumer.accept("Connected wire from " + source.getId() + " to " + target.getId());
                }
            } catch (InvalidShapeException ex) {
                if (statusConsumer != null) {
                    statusConsumer.accept("Wire Error: " + ex.getMessage());
                }
            }
        }
        startPin = null;
        currentMouse = null;
        circuit.notifyListeners();
    }

    @Override
    public void drawPreview(Graphics2D g2d) {
        if (startPin != null && currentMouse != null) {
            Point p1 = startPin.getAbsolutePosition();
            g2d.setColor(new Color(22, 163, 74));
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawLine(p1.x, p1.y, currentMouse.x, currentMouse.y);
        }
    }
}

```

### `src/main/java/circuitsim/tools/InteractTool.java`

```
package circuitsim.tools;

import circuitsim.model.Circuit;
import circuitsim.model.CircuitElement;
import circuitsim.model.InputSwitch;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * Concrete Tool strategy for live circuit simulation interaction: clicking an InputSwitch
 * toggles its boolean signal (0 <-> 1) and immediately propagates signals across the circuit.
 */
public class InteractTool implements Tool {
    private final Circuit circuit;
    private final Consumer<String> statusConsumer;

    public InteractTool(Circuit circuit, Consumer<String> statusConsumer) {
        this.circuit = circuit;
        this.statusConsumer = statusConsumer;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        CircuitElement hit = circuit.findElementAt(e.getPoint());
        if (hit instanceof InputSwitch) {
            InputSwitch sw = (InputSwitch) hit;
            sw.toggle();
            circuit.evaluate();
            if (statusConsumer != null) {
                statusConsumer.accept("Toggled switch " + sw.getId() + " to " + (sw.isState() ? "HIGH (1)" : "LOW (0)"));
            }
        } else if (statusConsumer != null) {
            statusConsumer.accept("Interact Mode: Click any Input Switch (SW) to toggle its HIGH/LOW signal.");
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void drawPreview(Graphics2D g2d) {
    }
}

```

## 6. Package `circuitsim.export`

### `src/main/java/circuitsim/export/CircuitVisitor.java`

```
package circuitsim.export;

import circuitsim.model.GroupShape;
import circuitsim.model.InputSwitch;
import circuitsim.model.LogicGate;
import circuitsim.model.OutputLed;
import circuitsim.model.TextLabel;
import circuitsim.model.Wire;

/**
 * Visitor Pattern interface for traversing concrete CircuitElement instances
 * without polluting domain classes with format-specific serialization logic.
 *
 * Design Pattern: VISITOR PATTERN
 * SOLID (OCP & SRP): Keeps SVG and JSON export logic decoupled from model classes.
 */
public interface CircuitVisitor {
    void visitGate(LogicGate gate);
    void visitSwitch(InputSwitch sw);
    void visitLed(OutputLed led);
    void visitWire(Wire wire);
    void visitText(TextLabel label);
    void visitGroup(GroupShape group);
}

```

### `src/main/java/circuitsim/export/SvgExportVisitor.java`

```
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
                "<svg xmlns="http://www.w3.org/2000/svg" width="%d" height="%d" viewBox="0 0 %d %d">%n",
                width, height, width, height));
        svg.append("  <rect width="100%" height="100%" fill="#ffffff"/>
");
        circuit.accept(this);
        svg.append("</svg>
");
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
                "  <rect x="%d" y="%d" width="%d" height="%d" rx="8" fill="%s" stroke="%s" stroke-width="%.1f"/>%n",
                gate.getX(), gate.getY(), gate.getWidth(), gate.getHeight(),
                ElementStyle.toHex(s.getFillColor()), ElementStyle.toHex(s.getStrokeColor()), s.getStrokeWidth()));
        svg.append(String.format(
                "  <text x="%d" y="%d" font-family="sans-serif" font-size="12" font-weight="bold" text-anchor="middle">%s</text>%n",
                gate.getX() + gate.getWidth() / 2, gate.getY() + gate.getHeight() / 2 + 4,
                escapeXml(gate.getGateSymbol())));
    }

    @Override
    public void visitSwitch(InputSwitch sw) {
        ElementStyle s = sw.getStyle();
        String fill = sw.isState() ? "#bbf7d0" : ElementStyle.toHex(s.getFillColor());
        svg.append(String.format(
                "  <rect x="%d" y="%d" width="%d" height="%d" fill="%s" stroke="%s" stroke-width="%.1f"/>%n",
                sw.getX(), sw.getY(), sw.getWidth(), sw.getHeight(),
                fill, ElementStyle.toHex(s.getStrokeColor()), s.getStrokeWidth()));
        svg.append(String.format(
                "  <text x="%d" y="%d" font-family="sans-serif" font-size="12" font-weight="bold" text-anchor="middle">SW: %s</text>%n",
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
                "  <ellipse cx="%d" cy="%d" rx="%d" ry="%d" fill="%s" stroke="%s" stroke-width="%.1f"/>%n",
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
                "  <line x1="%d" y1="%d" x2="%d" y2="%d" stroke="%s" stroke-width="%.1f"/>%n",
                p1.x, p1.y, p2.x, p2.y, stroke, wire.getStyle().getStrokeWidth()));
    }

    @Override
    public void visitText(TextLabel label) {
        svg.append(String.format(
                "  <text x="%d" y="%d" font-family="sans-serif" font-size="13" fill="%s">%s</text>%n",
                label.getX() + 6, label.getY() + label.getHeight() / 2 + 4,
                ElementStyle.toHex(label.getStyle().getStrokeColor()), escapeXml(label.getText())));
    }

    @Override
    public void visitGroup(GroupShape group) {
        svg.append("  <g>
");
        for (CircuitElement child : group.getChildren()) {
            child.accept(this);
        }
        svg.append("  </g>
");
    }

    private String escapeXml(String input) {
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace(""", "&quot;");
    }
}

```

### `src/main/java/circuitsim/export/JsonExportVisitor.java`

```
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
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete Visitor that serializes all CircuitElements (including wires, switch states,
 * styles, and composite groups) to a clean, human-readable JSON format without
 * external library dependencies.
 */
public class JsonExportVisitor implements CircuitVisitor {
    private final List<String> entries = new ArrayList<>();

    public String exportToString(Circuit circuit) {
        entries.clear();
        circuit.accept(this);
        StringBuilder sb = new StringBuilder();
        sb.append("{
  "version": "1.0",
  "elements": [
");
        for (int i = 0; i < entries.size(); i++) {
            sb.append("    ").append(entries.get(i));
            if (i < entries.size() - 1) {
                sb.append(",");
            }
            sb.append("
");
        }
        sb.append("  ]
}
");
        return sb.toString();
    }

    public void exportToFile(Circuit circuit, File file) throws ExportFailedException {
        if (file == null) {
            throw new ExportFailedException("Target JSON file cannot be null.");
        }
        try {
            String json = exportToString(circuit);
            Files.writeString(file.toPath(), json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ExportFailedException("Failed to write JSON file: " + e.getMessage(), e);
        }
    }

    private String baseJson(CircuitElement el, String extraFields) {
        ElementStyle s = el.getStyle();
        return String.format(
                "{"id":"%s","type":"%s","x":%d,"y":%d,"width":%d,"height":%d,"fill":"%s","stroke":"%s","strokeWidth":%.1f%s}",
                el.getId(), el.getType().name(), el.getX(), el.getY(), el.getWidth(), el.getHeight(),
                ElementStyle.toHex(s.getFillColor()), ElementStyle.toHex(s.getStrokeColor()), s.getStrokeWidth(),
                extraFields);
    }

    @Override
    public void visitGate(LogicGate gate) {
        entries.add(baseJson(gate, ""));
    }

    @Override
    public void visitSwitch(InputSwitch sw) {
        entries.add(baseJson(sw, String.format(","state":%b", sw.isState())));
    }

    @Override
    public void visitLed(OutputLed led) {
        entries.add(baseJson(led, ""));
    }

    @Override
    public void visitWire(Wire wire) {
        String extra = String.format(
                ","sourceOwnerId":"%s","targetOwnerId":"%s","targetPinIndex":%d",
                wire.getSourcePin().getOwner().getId(),
                wire.getTargetPin().getOwner().getId(),
                 findPinIndex(wire));
        entries.add(baseJson(wire, extra));
    }

    private int findPinIndex(Wire wire) {
        CircuitElement targetOwner = wire.getTargetPin().getOwner();
        if (targetOwner instanceof LogicGate) {
            return ((LogicGate) targetOwner).getInputPins().indexOf(wire.getTargetPin());
        }
        return 0;
    }

    @Override
    public void visitText(TextLabel label) {
        String safeText = label.getText().replace(""", "\"");
        entries.add(baseJson(label, String.format(","text":"%s"", safeText)));
    }

    @Override
    public void visitGroup(GroupShape group) {
        for (CircuitElement child : group.getChildren()) {
            child.accept(this);
        }
    }
}

```

### `src/main/java/circuitsim/export/JsonCircuitLoader.java`

```
package circuitsim.export;

import circuitsim.exceptions.FileFormatException;
import circuitsim.exceptions.InvalidShapeException;
import circuitsim.factory.ShapeFactory;
import circuitsim.model.Circuit;
import circuitsim.model.CircuitElement;
import circuitsim.model.ElementStyle;
import circuitsim.model.ElementType;
import circuitsim.model.InputSwitch;
import circuitsim.model.LogicGate;
import circuitsim.model.OutputLed;
import circuitsim.model.Pin;
import circuitsim.model.Wire;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads and reconstructs a Circuit from a JSON string or file produced by JsonExportVisitor.
 * Throws FileFormatException if the JSON syntax or schema is invalid.
 */
public class JsonCircuitLoader {

    private static final Pattern OBJECT_PATTERN = Pattern.compile("\{([^{}]+)\}");
    private static final Pattern KV_PATTERN = Pattern.compile(""([^"]+)"\s*:\s*("[^"]*"|[^,}]+)");

    public void loadFromFile(Circuit circuit, File file) throws FileFormatException {
        if (file == null || !file.exists()) {
            throw new FileFormatException("JSON file does not exist.");
        }
        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            loadFromString(circuit, content);
        } catch (IOException e) {
            throw new FileFormatException("Could not read JSON file: " + e.getMessage(), e);
        }
    }

    public void loadFromString(Circuit circuit, String json) throws FileFormatException {
        if (json == null || !json.contains(""elements"")) {
            throw new FileFormatException("Invalid circuit JSON: missing 'elements' array.");
        }

        List<Map<String, String>> rawObjects = new ArrayList<>();
        Matcher objMatcher = OBJECT_PATTERN.matcher(json);
        while (objMatcher.find()) {
            String body = objMatcher.group(1);
            Map<String, String> fields = new HashMap<>();
            Matcher kvMatcher = KV_PATTERN.matcher(body);
            while (kvMatcher.find()) {
                String key = kvMatcher.group(1).trim();
                String val = kvMatcher.group(2).trim();
                if (val.startsWith(""") && val.endsWith(""") && val.length() >= 2) {
                    val = val.substring(1, val.length() - 1);
                }
                fields.put(key, val);
            }
            if (fields.containsKey("type")) {
                rawObjects.add(fields);
            }
        }

        circuit.clear();
        Map<String, CircuitElement> byId = new ArrayList<CircuitElement>().stream()
                .collect(HashMap::new, (m, v) -> {}, HashMap::putAll);

        List<Map<String, String>> deferredWires = new ArrayList<>();

        try {
            for (Map<String, String> map : rawObjects) {
                ElementType type = ElementType.valueOf(map.get("type"));
                if (type == ElementType.WIRE) {
                    deferredWires.add(map);
                    continue;
                }
                String id = map.get("id");
                int x = Integer.parseInt(map.getOrDefault("x", "50"));
                int y = Integer.parseInt(map.getOrDefault("y", "50"));
                int w = Integer.parseInt(map.getOrDefault("width", "80"));
                int h = Integer.parseInt(map.getOrDefault("height", "50"));
                Color fill = ElementStyle.fromHex(map.getOrDefault("fill", "#f0f4ff"));
                Color stroke = ElementStyle.fromHex(map.getOrDefault("stroke", "#1e293b"));
                float sw = Float.parseFloat(map.getOrDefault("strokeWidth", "2.0"));
                String text = map.getOrDefault("text", "Label");

                ElementStyle style = new ElementStyle(fill, stroke, sw);
                CircuitElement el = ShapeFactory.createElement(type, x, y, w, h, style, text);
                if (id != null) {
                    el.setId(id);
                }
                if (el instanceof InputSwitch && map.containsKey("state")) {
                    ((InputSwitch) el).setState(Boolean.parseBoolean(map.get("state")));
                }
                circuit.addElement(el);
                byId.put(el.getId(), el);
            }

            // Reconnect wires
            for (Map<String, String> wMap : deferredWires) {
                CircuitElement srcEl = byId.get(wMap.get("sourceOwnerId"));
                CircuitElement tgtEl = byId.get(wMap.get("targetOwnerId"));
                if (srcEl == null || tgtEl == null) {
                    throw new FileFormatException("Wire references non-existent element ID.");
                }
                Pin srcPin = extractOutputPin(srcEl);
                int idx = Integer.parseInt(wMap.getOrDefault("targetPinIndex", "0"));
                Pin tgtPin = extractInputPin(tgtEl, idx);

                Color stroke = ElementStyle.fromHex(wMap.getOrDefault("stroke", "#1e293b"));
                float sw = Float.parseFloat(wMap.getOrDefault("strokeWidth", "2.0"));
                Wire wire = ShapeFactory.createWire(srcPin, tgtPin, new ElementStyle(Color.WHITE, stroke, sw));
                circuit.addElement(wire);
            }
            circuit.evaluate();
        } catch (IllegalArgumentException | InvalidShapeException e) {
            throw new FileFormatException("Malformed element data in JSON: " + e.getMessage(), e);
        }
    }

    private Pin extractOutputPin(CircuitElement el) throws FileFormatException {
        if (el instanceof LogicGate) {
            return ((LogicGate) el).getOutputPin();
        } else if (el instanceof InputSwitch) {
            return ((InputSwitch) el).getOutputPin();
        }
        throw new FileFormatException("Element " + el.getId() + " has no output pin.");
    }

    private Pin extractInputPin(CircuitElement el, int index) throws FileFormatException {
        if (el instanceof LogicGate) {
            List<Pin> pins = ((LogicGate) el).getInputPins();
            if (index >= 0 && index < pins.size()) {
                return pins.get(index);
            }
        } else if (el instanceof OutputLed) {
            return ((OutputLed) el).getInputPin();
        }
        throw new FileFormatException("Element " + el.getId() + " has no valid input pin at index " + index);
    }
}

```

### `src/main/java/circuitsim/export/PngExporter.java`

```
package circuitsim.export;

import circuitsim.exceptions.ExportFailedException;
import circuitsim.model.Circuit;
import circuitsim.model.CircuitElement;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Exports the current Circuit schematic to a high-resolution PNG raster image.
 */
public class PngExporter {

    public void exportToFile(Circuit circuit, File file, int width, int height)
            throws ExportFailedException {
        if (file == null) {
            throw new ExportFailedException("Target PNG file cannot be null.");
        }
        if (width <= 0 || height <= 0) {
            throw new ExportFailedException("Image dimensions must be positive.");
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);

            for (CircuitElement el : circuit.getElements()) {
                el.draw(g2d);
            }
        } finally {
            g2d.dispose();
        }

        try {
            boolean written = ImageIO.write(image, "png", file);
            if (!written) {
                throw new ExportFailedException("No PNG ImageWriter found.");
            }
        } catch (IOException e) {
            throw new ExportFailedException("Failed to write PNG file: " + e.getMessage(), e);
        }
    }
}

```

## 7. Package `circuitsim.ui`

### `src/main/java/circuitsim/ui/CircuitCanvasPanel.java`

```
package circuitsim.ui;

import circuitsim.model.Circuit;
import circuitsim.model.CircuitElement;
import circuitsim.model.CircuitListener;
import circuitsim.tools.Tool;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;

/**
 * Swing canvas panel that renders the background grid, all CircuitElements,
 * and active tool previews.
 *
 * Design Pattern:
 * - OBSERVER PATTERN (Concrete Observer): Implements CircuitListener and calls
 *   repaint() automatically whenever the Circuit model changes.
 */
public class CircuitCanvasPanel extends JPanel implements CircuitListener {
    private final Circuit circuit;
    private Tool activeTool;

    public CircuitCanvasPanel(Circuit circuit) {
        this.circuit = circuit;
        this.circuit.addListener(this);
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(960, 620));

        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (activeTool != null) {
                    activeTool.mousePressed(e);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (activeTool != null) {
                    activeTool.mouseDragged(e);
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (activeTool != null) {
                    activeTool.mouseReleased(e);
                    repaint();
                }
            }
        };
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
    }

    public void setActiveTool(Tool tool) {
        this.activeTool = tool;
    }

    public Tool getActiveTool() {
        return activeTool;
    }

    @Override
    public void onCircuitChanged() {
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g2d);

        for (CircuitElement element : circuit.getElements()) {
            element.draw(g2d);
        }

        if (activeTool != null) {
            activeTool.drawPreview(g2d);
        }

        g2d.dispose();
    }

    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(new Color(241, 245, 249));
        int step = 20;
        for (int x = 0; x < getWidth(); x += step) {
            g2d.drawLine(x, 0, x, getHeight());
        }
        for (int y = 0; y < getHeight(); y += step) {
            g2d.drawLine(0, y, getWidth(), y);
        }
    }
}

```

### `src/main/java/circuitsim/ui/MainFrame.java`

```
package circuitsim.ui;

import circuitsim.commands.AddShapeCommand;
import circuitsim.commands.CommandManager;
import circuitsim.commands.DeleteCommand;
import circuitsim.commands.GroupCommand;
import circuitsim.commands.RecolorCommand;
import circuitsim.exceptions.EmptyUndoStackException;
import circuitsim.exceptions.ExportFailedException;
import circuitsim.exceptions.FileFormatException;
import circuitsim.exceptions.InvalidShapeException;
import circuitsim.export.JsonCircuitLoader;
import circuitsim.export.JsonExportVisitor;
import circuitsim.export.PngExporter;
import circuitsim.export.SvgExportVisitor;
import circuitsim.model.Circuit;
import circuitsim.model.CircuitElement;
import circuitsim.model.ElementStyle;
import circuitsim.model.ElementType;
import circuitsim.model.GroupShape;
import circuitsim.tools.GatePlacementTool;
import circuitsim.tools.InteractTool;
import circuitsim.tools.SelectTool;
import circuitsim.tools.WireTool;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;

/**
 * Main Swing application window providing toolbars for placing gates/switches/LEDs/wires,
 * simulating boolean signals, editing styles, grouping subcircuits, copying/pasting,
 * undoing/redoing, and saving/exporting to JSON, SVG, and PNG.
 */
public class MainFrame extends JFrame {
    private final Circuit circuit = new Circuit();
    private final CommandManager commandManager = new CommandManager();
    private final CircuitCanvasPanel canvasPanel = new CircuitCanvasPanel(circuit);
    private final JLabel statusLabel = new JLabel("Ready — Select a gate or tool to begin building your logic circuit.");

    private final ElementStyle currentStyle = ElementStyle.createDefault();
    private final List<CircuitElement> clipboard = new ArrayList<>();

    public MainFrame() {
        super("Logic Circuit Simulator — OOM Course Project (Java 17 Swing)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(buildComponentToolbar(), BorderLayout.NORTH);
        topContainer.add(buildActionToolbar(), BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);
        add(canvasPanel, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);

        // Default active tool is SelectTool
        canvasPanel.setActiveTool(new SelectTool(circuit, commandManager));

        pack();
        setLocationRelativeTo(null);
    }

    private JToolBar buildComponentToolbar() {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);

        addButton(bar, "Select / Move", () -> {
            canvasPanel.setActiveTool(new SelectTool(circuit, commandManager));
            setStatus("Select Tool: Click to select, Shift+Click for multi-select, drag to move or resize.");
        });

        addButton(bar, "Interact (Toggle SW)", () -> {
            canvasPanel.setActiveTool(new InteractTool(circuit, this::setStatus));
            setStatus("Interact Tool: Click any Input Switch (SW) to toggle HIGH (1) / LOW (0).");
        });

        addButton(bar, "Wire Tool", () -> {
            canvasPanel.setActiveTool(new WireTool(circuit, commandManager, currentStyle::clone, this::setStatus));
            setStatus("Wire Tool: Drag from an OUTPUT pin (right circle) to an INPUT pin (left circle).");
        });

        bar.addSeparator();

        for (ElementType type : new ElementType[]{
                ElementType.SWITCH, ElementType.AND, ElementType.OR, ElementType.NOT,
                ElementType.XOR, ElementType.NAND, ElementType.NOR, ElementType.LED, ElementType.TEXT}) {
            addButton(bar, "+ " + type.name(), () -> {
                canvasPanel.setActiveTool(new GatePlacementTool(
                        circuit,
                        commandManager,
                        type,
                        currentStyle::clone,
                        () -> JOptionPane.showInputDialog(this, "Enter label text:", "Half Adder"),
                        this::setStatus));
                setStatus("Placement Tool (" + type + "): Click or drag on the canvas to place.");
            });
        }

        return bar;
    }

    private JToolBar buildActionToolbar() {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);

        addButton(bar, "Undo", this::handleUndo);
        addButton(bar, "Redo", this::handleRedo);
        addButton(bar, "Delete", this::handleDelete);
        bar.addSeparator();

        addButton(bar, "Copy", this::handleCopy);
        addButton(bar, "Paste", this::handlePaste);
        addButton(bar, "Group", this::handleGroup);
        addButton(bar, "Ungroup", this::handleUngroup);
        bar.addSeparator();

        addButton(bar, "Fill Color", this::handleFillColor);
        addButton(bar, "Stroke Color", this::handleStrokeColor);

        JComboBox<String> widthBox = new JComboBox<>(new String[]{"1.5 px", "2.0 px", "3.0 px", "4.0 px"});
        widthBox.setSelectedIndex(1);
        widthBox.addActionListener(e -> {
            String sel = (String) widthBox.getSelectedItem();
            if (sel != null) {
                float w = Float.parseFloat(sel.replace(" px", ""));
                currentStyle.setStrokeWidth(w);
                applyStyleToSelection();
            }
        });
        bar.add(new JLabel(" Width: "));
        bar.add(widthBox);
        bar.addSeparator();

        addButton(bar, "Save JSON", this::handleSaveJson);
        addButton(bar, "Load JSON", this::handleLoadJson);
        addButton(bar, "Export SVG", this::handleExportSvg);
        addButton(bar, "Export PNG", this::handleExportPng);

        return bar;
    }

    private void addButton(JToolBar bar, String label, Runnable action) {
        JButton btn = new JButton(label);
        btn.addActionListener(e -> action.run());
        bar.add(btn);
    }

    private void handleUndo() {
        try {
            commandManager.undo();
            setStatus("Undid last action.");
        } catch (EmptyUndoStackException ex) {
            setStatus(ex.getMessage());
        }
    }

    private void handleRedo() {
        try {
            commandManager.redo();
            setStatus("Redid action.");
        } catch (EmptyUndoStackException ex) {
            setStatus(ex.getMessage());
        }
    }

    private void handleDelete() {
        List<CircuitElement> sel = circuit.getSelectedElements();
        if (sel.isEmpty()) {
            setStatus("Select at least one element to delete.");
            return;
        }
        commandManager.executeCommand(new DeleteCommand(circuit, sel));
        setStatus("Deleted " + sel.size() + " element(s).");
    }

    private void handleCopy() {
        List<CircuitElement> sel = circuit.getSelectedElements();
        if (sel.isEmpty()) {
            setStatus("Select at least one element to copy.");
            return;
        }
        clipboard.clear();
        for (CircuitElement el : sel) {
            if (el.getType() != ElementType.WIRE) {
                clipboard.add(el.clone()); // Prototype Pattern
            }
        }
        setStatus("Copied " + clipboard.size() + " element(s) to clipboard via Prototype clone().");
    }

    private void handlePaste() {
        if (clipboard.isEmpty()) {
            setStatus("Clipboard is empty.");
            return;
        }
        circuit.clearSelection();
        for (CircuitElement proto : clipboard) {
            CircuitElement copy = proto.clone();
            copy.move(25, 25);
            copy.setSelected(true);
            commandManager.executeCommand(new AddShapeCommand(circuit, copy));
        }
        setStatus("Pasted " + clipboard.size() + " cloned element(s).");
    }

    private void handleGroup() {
        List<CircuitElement> sel = circuit.getSelectedElements();
        if (sel.size() < 2) {
            setStatus("Select at least 2 elements (Shift+Click) to group into a Composite Subcircuit.");
            return;
        }
        try {
            commandManager.executeCommand(new GroupCommand(circuit, sel));
            setStatus("Grouped " + sel.size() + " elements into a GroupShape.");
        } catch (InvalidShapeException ex) {
            setStatus("Group error: " + ex.getMessage());
        }
    }

    private void handleUngroup() {
        List<CircuitElement> sel = circuit.getSelectedElements();
        if (sel.size() == 1 && sel.get(0) instanceof GroupShape) {
            commandManager.executeCommand(new GroupCommand(circuit, (GroupShape) sel.get(0)));
            setStatus("Ungrouped subcircuit.");
        } else {
            setStatus("Select a single GroupShape to ungroup.");
        }
    }

    private void handleFillColor() {
        Color chosen = JColorChooser.showDialog(this, "Choose Fill Color", currentStyle.getFillColor());
        if (chosen != null) {
            currentStyle.setFillColor(chosen);
            applyStyleToSelection();
        }
    }

    private void handleStrokeColor() {
        Color chosen = JColorChooser.showDialog(this, "Choose Stroke Color", currentStyle.getStrokeColor());
        if (chosen != null) {
            currentStyle.setStrokeColor(chosen);
            applyStyleToSelection();
        }
    }

    private void applyStyleToSelection() {
        List<CircuitElement> sel = circuit.getSelectedElements();
        if (!sel.isEmpty()) {
            commandManager.executeCommand(new RecolorCommand(circuit, sel, currentStyle.clone()));
            setStatus("Updated style on selected element(s).");
        }
    }

    private void handleSaveJson() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = ensureExtension(chooser.getSelectedFile(), ".json");
            try {
                new JsonExportVisitor().exportToFile(circuit, file);
                setStatus("Saved circuit JSON to " + file.getName());
            } catch (ExportFailedException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleLoadJson() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                new JsonCircuitLoader().loadFromFile(circuit, file);
                commandManager.clear();
                setStatus("Loaded circuit from " + file.getName());
            } catch (FileFormatException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleExportSvg() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = ensureExtension(chooser.getSelectedFile(), ".svg");
            try {
                new SvgExportVisitor().exportToFile(circuit, file, canvasPanel.getWidth(), canvasPanel.getHeight());
                setStatus("Exported SVG to " + file.getName());
            } catch (ExportFailedException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleExportPng() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = ensureExtension(chooser.getSelectedFile(), ".png");
            try {
                new PngExporter().exportToFile(circuit, file, canvasPanel.getWidth(), canvasPanel.getHeight());
                setStatus("Exported PNG to " + file.getName());
            } catch (ExportFailedException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private File ensureExtension(File f, String ext) {
        if (!f.getName().toLowerCase().endsWith(ext)) {
            return new File(f.getParentFile(), f.getName() + ext);
        }
        return f;
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }
}

```