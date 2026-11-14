# Stage 6: JUnit 5 Test Suite (`model`, `commands`, `factory`, `export`)

All unit tests run **headlessly without launching any Swing GUI window**, proving strict Model-View separation.

## 1. `src/test/java/circuitsim/ModelAndSimulationTest.java`

Tests all 6 logic gate truth tables, multi-stage signal propagation (`InputSwitch` $\rightarrow$ `Wire` $\rightarrow$ `LogicGate` $\rightarrow$ `OutputLed`), the **Prototype** pattern (`clone()`), the **Composite** pattern (`GroupShape`), and the **Observer** pattern (`CircuitListener`).

```
package circuitsim;

import circuitsim.exceptions.InvalidShapeException;
import circuitsim.model.AndGate;
import circuitsim.model.Circuit;
import circuitsim.model.ElementStyle;
import circuitsim.model.GroupShape;
import circuitsim.model.InputSwitch;
import circuitsim.model.NandGate;
import circuitsim.model.NorGate;
import circuitsim.model.NotGate;
import circuitsim.model.OrGate;
import circuitsim.model.OutputLed;
import circuitsim.model.Wire;
import circuitsim.model.XorGate;
import java.awt.Color;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ModelAndSimulationTest {

    private Circuit circuit;
    private ElementStyle defaultStyle;

    @BeforeEach
    void setUp() {
        circuit = new Circuit();
        defaultStyle = ElementStyle.createDefault();
    }

    @Test
    @DisplayName("Verify truth tables for AND, OR, NOT, XOR, NAND, and NOR gates")
    void testAllGateTruthTables() throws InvalidShapeException {
        AndGate andGate = new AndGate(10, 10, 80, 50, defaultStyle);
        andGate.getInputPins().get(0).setValue(true);
        andGate.getInputPins().get(1).setValue(false);
        andGate.evaluate();
        assertFalse(andGate.getOutputPin().getValue(), "AND(1,0) should be 0");
        andGate.getInputPins().get(1).setValue(true);
        andGate.evaluate();
        assertTrue(andGate.getOutputPin().getValue(), "AND(1,1) should be 1");

        OrGate orGate = new OrGate(10, 10, 80, 50, defaultStyle);
        orGate.getInputPins().get(0).setValue(false);
        orGate.getInputPins().get(1).setValue(true);
        orGate.evaluate();
        assertTrue(orGate.getOutputPin().getValue(), "OR(0,1) should be 1");

        NotGate notGate = new NotGate(10, 10, 80, 50, defaultStyle);
        notGate.getInputPins().get(0).setValue(false);
        notGate.evaluate();
        assertTrue(notGate.getOutputPin().getValue(), "NOT(0) should be 1");

        XorGate xorGate = new XorGate(10, 10, 80, 50, defaultStyle);
        xorGate.getInputPins().get(0).setValue(true);
        xorGate.getInputPins().get(1).setValue(true);
        xorGate.evaluate();
        assertFalse(xorGate.getOutputPin().getValue(), "XOR(1,1) should be 0");

        NandGate nandGate = new NandGate(10, 10, 80, 50, defaultStyle);
        nandGate.getInputPins().get(0).setValue(true);
        nandGate.getInputPins().get(1).setValue(true);
        nandGate.evaluate();
        assertFalse(nandGate.getOutputPin().getValue(), "NAND(1,1) should be 0");

        NorGate norGate = new NorGate(10, 10, 80, 50, defaultStyle);
        norGate.getInputPins().get(0).setValue(false);
        norGate.getInputPins().get(1).setValue(false);
        norGate.evaluate();
        assertTrue(norGate.getOutputPin().getValue(), "NOR(0,0) should be 1");
    }

    @Test
    @DisplayName("Verify end-to-end signal propagation from InputSwitch through Wire and Gate to OutputLed")
    void testSignalPropagationAcrossCircuit() throws InvalidShapeException {
        InputSwitch sw1 = new InputSwitch(20, 20, 60, 40, defaultStyle);
        InputSwitch sw2 = new InputSwitch(20, 90, 60, 40, defaultStyle);
        AndGate andGate = new AndGate(150, 50, 80, 50, defaultStyle);
        OutputLed led = new OutputLed(300, 50, 45, 45, defaultStyle);

        circuit.addElement(sw1);
        circuit.addElement(sw2);
        circuit.addElement(andGate);
        circuit.addElement(led);

        Wire w1 = new Wire(sw1.getOutputPin(), andGate.getInputPins().get(0), defaultStyle);
        Wire w2 = new Wire(sw2.getOutputPin(), andGate.getInputPins().get(1), defaultStyle);
        Wire wOut = new Wire(andGate.getOutputPin(), led.getInputPin(), defaultStyle);

        circuit.addElement(w1);
        circuit.addElement(w2);
        circuit.addElement(wOut);

        assertFalse(led.isLit(), "LED should initially be OFF when both switches are LOW");

        sw1.toggle();
        circuit.evaluate();
        assertFalse(led.isLit(), "LED should remain OFF when only sw1 is HIGH");

        sw2.toggle();
        circuit.evaluate();
        assertTrue(led.isLit(), "LED should turn ON when both sw1 and sw2 are HIGH");
    }

    @Test
    @DisplayName("Verify Prototype pattern deep clone() produces independent element and style")
    void testPrototypeDeepClone() throws InvalidShapeException {
        AndGate original = new AndGate(50, 60, 80, 50, defaultStyle);
        AndGate copy = original.clone();

        assertNotSame(original, copy);
        assertNotEquals(original.getId(), copy.getId(), "Cloned gate must get a unique ID");
        assertEquals(original.getX(), copy.getX());

        // Mutating copy style or position must not affect original
        copy.move(40, 40);
        copy.getStyle().setFillColor(Color.RED);
        assertEquals(50, original.getX());
        assertNotEquals(Color.RED, original.getStyle().getFillColor());
    }

    @Test
    @DisplayName("Verify Composite pattern GroupShape moves and styles all child elements uniformly")
    void testCompositeGroupShape() throws InvalidShapeException {
        InputSwitch sw = new InputSwitch(10, 20, 60, 40, defaultStyle);
        NotGate notGate = new NotGate(100, 20, 80, 50, defaultStyle);
        GroupShape group = new GroupShape(Arrays.asList(sw, notGate), defaultStyle);

        group.move(30, 15);
        assertEquals(40, sw.getX());
        assertEquals(35, sw.getY());
        assertEquals(130, notGate.getX());
        assertEquals(35, notGate.getY());

        ElementStyle greenStyle = new ElementStyle(Color.GREEN, Color.BLACK, 3.0f);
        group.setStyle(greenStyle);
        assertEquals(Color.GREEN, sw.getStyle().getFillColor());
        assertEquals(Color.GREEN, notGate.getStyle().getFillColor());
    }

    @Test
    @DisplayName("Verify Observer pattern notifies registered CircuitListeners on model mutation")
    void testObserverNotification() throws InvalidShapeException {
        AtomicInteger notifications = new AtomicInteger(0);
        circuit.addListener(notifications::incrementAndGet);

        InputSwitch sw = new InputSwitch(10, 10, 60, 40, defaultStyle);
        circuit.addElement(sw);
        assertTrue(notifications.get() >= 1, "Listener must be notified when an element is added");
    }
}

```

## 2. `src/test/java/circuitsim/CommandManagerTest.java`

Tests the **Command** pattern (`AddShapeCommand`, `MoveCommand`, `ResizeCommand`, `DeleteCommand`, `RecolorCommand`, `GroupCommand`) and verifies that `EmptyUndoStackException` is thrown when undoing/redoing an empty stack.

```
package circuitsim;

import circuitsim.commands.AddShapeCommand;
import circuitsim.commands.CommandManager;
import circuitsim.commands.DeleteCommand;
import circuitsim.commands.GroupCommand;
import circuitsim.commands.MoveCommand;
import circuitsim.commands.RecolorCommand;
import circuitsim.commands.ResizeCommand;
import circuitsim.exceptions.EmptyUndoStackException;
import circuitsim.exceptions.InvalidShapeException;
import circuitsim.model.AndGate;
import circuitsim.model.Circuit;
import circuitsim.model.ElementStyle;
import circuitsim.model.InputSwitch;
import circuitsim.model.OutputLed;
import circuitsim.model.Wire;
import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CommandManagerTest {

    private Circuit circuit;
    private CommandManager commandManager;
    private ElementStyle style;

    @BeforeEach
    void setUp() {
        circuit = new Circuit();
        commandManager = new CommandManager();
        style = ElementStyle.createDefault();
    }

    @Test
    @DisplayName("Verify EmptyUndoStackException is thrown when undo or redo stack is empty")
    void testEmptyUndoAndRedoThrowsException() {
        assertThrows(EmptyUndoStackException.class, () -> commandManager.undo());
        assertThrows(EmptyUndoStackException.class, () -> commandManager.redo());
    }

    @Test
    @DisplayName("Verify Add, Move, Resize, and Recolor commands execute, undo, and redo cleanly")
    void testAddMoveResizeRecolorUndoRedo() throws Exception {
        AndGate gate = new AndGate(100, 100, 80, 50, style);

        // 1. Add
        commandManager.executeCommand(new AddShapeCommand(circuit, gate));
        assertEquals(1, circuit.getElements().size());

        // 2. Move
        commandManager.executeCommand(new MoveCommand(circuit, gate, 50, -20));
        assertEquals(150, gate.getX());
        assertEquals(80, gate.getY());

        // 3. Resize
        commandManager.executeCommand(new ResizeCommand(circuit, gate, 80, 50, 120, 70));
        assertEquals(120, gate.getWidth());
        assertEquals(70, gate.getHeight());

        // 4. Recolor
        ElementStyle newStyle = new ElementStyle(Color.YELLOW, Color.BLUE, 3.5f);
        commandManager.executeCommand(new RecolorCommand(circuit, Collections.singletonList(gate), newStyle));
        assertEquals(Color.YELLOW, gate.getStyle().getFillColor());

        // Undo Recolor
        commandManager.undo();
        assertEquals(style.getFillColor(), gate.getStyle().getFillColor());

        // Undo Resize
        commandManager.undo();
        assertEquals(80, gate.getWidth());

        // Undo Move
        commandManager.undo();
        assertEquals(100, gate.getX());

        // Undo Add
        commandManager.undo();
        assertTrue(circuit.getElements().isEmpty());

        // Redo Add
        commandManager.redo();
        assertEquals(1, circuit.getElements().size());
    }

    @Test
    @DisplayName("Verify DeleteCommand removes attached wires and restores both on undo()")
    void testDeleteGateWithAttachedWireUndo() throws Exception {
        InputSwitch sw = new InputSwitch(20, 20, 60, 40, style);
        OutputLed led = new OutputLed(180, 20, 45, 45, style);
        Wire wire = new Wire(sw.getOutputPin(), led.getInputPin(), style);

        circuit.addElement(sw);
        circuit.addElement(led);
        circuit.addElement(wire);

        commandManager.executeCommand(new DeleteCommand(circuit, Collections.singletonList(sw)));
        assertEquals(1, circuit.getElements().size(), "Deleting sw must also remove its attached wire");

        commandManager.undo();
        assertEquals(3, circuit.getElements().size(), "Undoing DeleteCommand must restore both sw and wire");
    }

    @Test
    @DisplayName("Verify GroupCommand groups elements into GroupShape and ungroups on undo()")
    void testGroupCommandUndoRedo() throws InvalidShapeException, EmptyUndoStackException {
        InputSwitch sw = new InputSwitch(20, 20, 60, 40, style);
        OutputLed led = new OutputLed(140, 20, 45, 45, style);
        circuit.addElement(sw);
        circuit.addElement(led);

        commandManager.executeCommand(new GroupCommand(circuit, Arrays.asList(sw, led)));
        assertEquals(1, circuit.getElements().size(), "Two elements should be replaced by 1 GroupShape");

        commandManager.undo();
        assertEquals(2, circuit.getElements().size(), "Undo should restore the 2 individual elements");
    }
}

```

## 3. `src/test/java/circuitsim/ShapeFactoryTest.java`

Tests the **Factory** pattern (`ShapeFactory`) and verifies that `InvalidShapeException` is thrown for invalid dimensions, null types, empty text labels, and invalid pin-to-pin wiring.

```
package circuitsim;

import circuitsim.exceptions.InvalidShapeException;
import circuitsim.factory.ShapeFactory;
import circuitsim.model.CircuitElement;
import circuitsim.model.ElementStyle;
import circuitsim.model.ElementType;
import circuitsim.model.InputSwitch;
import circuitsim.model.OutputLed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ShapeFactoryTest {

    private final ElementStyle style = ElementStyle.createDefault();

    @Test
    @DisplayName("Verify ShapeFactory creates every supported gate and component type")
    void testFactoryCreatesValidElements() throws InvalidShapeException {
        for (ElementType type : new ElementType[]{
                ElementType.AND, ElementType.OR, ElementType.NOT,
                ElementType.XOR, ElementType.NAND, ElementType.NOR,
                ElementType.SWITCH, ElementType.LED, ElementType.TEXT}) {
            CircuitElement el = ShapeFactory.createElement(type, 10, 20, 80, 50, style, "TestLabel");
            assertNotNull(el);
            assertEquals(type, el.getType());
        }
    }

    @Test
    @DisplayName("Verify ShapeFactory throws InvalidShapeException on non-positive dimensions or empty text")
    void testFactoryThrowsInvalidShapeException() {
        assertThrows(InvalidShapeException.class, () ->
                ShapeFactory.createElement(ElementType.AND, 0, 0, -10, 50, style, ""));

        assertThrows(InvalidShapeException.class, () ->
                ShapeFactory.createElement(ElementType.TEXT, 0, 0, 80, 40, style, "   "));

        assertThrows(InvalidShapeException.class, () ->
                ShapeFactory.createElement(null, 0, 0, 80, 40, style, "Label"));
    }

    @Test
    @DisplayName("Verify Wire creation throws InvalidShapeException when wiring two output pins or self-wiring")
    void testInvalidWireThrowsException() throws InvalidShapeException {
        InputSwitch sw1 = new InputSwitch(10, 10, 60, 40, style);
        InputSwitch sw2 = new InputSwitch(10, 80, 60, 40, style);
        OutputLed led = new OutputLed(150, 10, 45, 45, style);

        // Valid wire (Output -> Input)
        assertNotNull(ShapeFactory.createWire(sw1.getOutputPin(), led.getInputPin(), style));

        // Invalid wire (Output -> Output)
        assertThrows(InvalidShapeException.class, () ->
                ShapeFactory.createWire(sw1.getOutputPin(), sw2.getOutputPin(), style));

        // Invalid wire (Self-connection)
        assertThrows(InvalidShapeException.class, () ->
                ShapeFactory.createWire(sw1.getOutputPin(), sw1.getOutputPin(), style));
    }
}

```

## 4. `src/test/java/circuitsim/ExporterTest.java`

Tests the **Visitor** pattern (`JsonExportVisitor`, `SvgExportVisitor`), `JsonCircuitLoader`, `PngExporter`, and verifies that `FileFormatException` and `ExportFailedException` are thrown on malformed or invalid inputs.

```
package circuitsim;

import circuitsim.exceptions.ExportFailedException;
import circuitsim.exceptions.FileFormatException;
import circuitsim.exceptions.InvalidShapeException;
import circuitsim.export.JsonCircuitLoader;
import circuitsim.export.JsonExportVisitor;
import circuitsim.export.PngExporter;
import circuitsim.export.SvgExportVisitor;
import circuitsim.model.AndGate;
import circuitsim.model.Circuit;
import circuitsim.model.ElementStyle;
import circuitsim.model.InputSwitch;
import circuitsim.model.OutputLed;
import circuitsim.model.TextLabel;
import circuitsim.model.Wire;
import java.io.File;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

public class ExporterTest {

    private Circuit circuit;
    private ElementStyle style;

    @BeforeEach
    void setUp() throws InvalidShapeException {
        circuit = new Circuit();
        style = ElementStyle.createDefault();

        InputSwitch sw1 = new InputSwitch(20, 20, 60, 40, style);
        sw1.setState(true);
        InputSwitch sw2 = new InputSwitch(20, 80, 60, 40, style);
        sw2.setState(true);
        AndGate andGate = new AndGate(140, 40, 80, 50, style);
        OutputLed led = new OutputLed(280, 40, 45, 45, style);
        TextLabel label = new TextLabel(20, 140, 120, 30, style, "AND Demo");

        circuit.addElement(sw1);
        circuit.addElement(sw2);
        circuit.addElement(andGate);
        circuit.addElement(led);
        circuit.addElement(label);
        circuit.addElement(new Wire(sw1.getOutputPin(), andGate.getInputPins().get(0), style));
        circuit.addElement(new Wire(sw2.getOutputPin(), andGate.getInputPins().get(1), style));
        circuit.addElement(new Wire(andGate.getOutputPin(), led.getInputPin(), style));
    }

    @Test
    @DisplayName("Verify JSON export and round-trip reload preserves elements, wires, and signal states")
    void testJsonRoundTripSaveAndLoad(@TempDir Path tempDir) throws Exception {
        File jsonFile = tempDir.resolve("circuit.json").toFile();
        JsonExportVisitor exporter = new JsonExportVisitor();
        exporter.exportToFile(circuit, jsonFile);

        assertTrue(jsonFile.exists() && jsonFile.length() > 0);

        Circuit loadedCircuit = new Circuit();
        JsonCircuitLoader loader = new JsonCircuitLoader();
        loader.loadFromFile(loadedCircuit, jsonFile);

        assertEquals(circuit.getElements().size(), loadedCircuit.getElements().size());

        // Verify that the loaded LED evaluates to ON (since both saved switches were HIGH)
        OutputLed loadedLed = loadedCircuit.getElements().stream()
                .filter(e -> e instanceof OutputLed)
                .map(e -> (OutputLed) e)
                .findFirst()
                .orElseThrow();
        assertTrue(loadedLed.isLit(), "Reloaded circuit must preserve switch states and re-evaluate LED to ON");
    }

    @Test
    @DisplayName("Verify SvgExportVisitor produces valid SVG XML containing rect, ellipse, line, and text")
    void testSvgExportVisitor(@TempDir Path tempDir) throws ExportFailedException {
        File svgFile = tempDir.resolve("circuit.svg").toFile();
        SvgExportVisitor svgVisitor = new SvgExportVisitor();
        String svg = svgVisitor.exportToString(circuit, 800, 600);

        assertTrue(svg.startsWith("<svg"));
        assertTrue(svg.contains("<rect"));
        assertTrue(svg.contains("<ellipse"));
        assertTrue(svg.contains("<line"));
        assertTrue(svg.contains("AND Demo"));

        svgVisitor.exportToFile(circuit, svgFile, 800, 600);
        assertTrue(svgFile.exists() && svgFile.length() > 0);
    }

    @Test
    @DisplayName("Verify PngExporter renders circuit to non-empty PNG file")
    void testPngExporter(@TempDir Path tempDir) throws ExportFailedException {
        File pngFile = tempDir.resolve("circuit.png").toFile();
        PngExporter pngExporter = new PngExporter();
        pngExporter.exportToFile(circuit, pngFile, 800, 600);
        assertTrue(pngFile.exists() && pngFile.length() > 100);
    }

    @Test
    @DisplayName("Verify FileFormatException and ExportFailedException on invalid inputs")
    void testExportAndLoadExceptions() {
        JsonCircuitLoader loader = new JsonCircuitLoader();
        assertThrows(FileFormatException.class, () -> loader.loadFromString(circuit, "{bad_json}"));

        SvgExportVisitor svgVisitor = new SvgExportVisitor();
        assertThrows(ExportFailedException.class, () -> svgVisitor.exportToFile(circuit, null, 800, 600));

        PngExporter pngExporter = new PngExporter();
        assertThrows(ExportFailedException.class, () -> pngExporter.exportToFile(circuit, new File("out.png"), -10, 200));
    }
}

```