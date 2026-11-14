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

        commandManager.executeCommand(new AddShapeCommand(circuit, gate));
        assertEquals(1, circuit.getElements().size());

        commandManager.executeCommand(new MoveCommand(circuit, gate, 50, -20));
        assertEquals(150, gate.getX());
        assertEquals(80, gate.getY());

        commandManager.executeCommand(new ResizeCommand(circuit, gate, 80, 50, 120, 70));
        assertEquals(120, gate.getWidth());
        assertEquals(70, gate.getHeight());

        ElementStyle newStyle = new ElementStyle(Color.YELLOW, Color.BLUE, 3.5f);
        commandManager.executeCommand(new RecolorCommand(circuit, Collections.singletonList(gate), newStyle));
        assertEquals(Color.YELLOW, gate.getStyle().getFillColor());

        commandManager.undo();
        assertEquals(style.getFillColor(), gate.getStyle().getFillColor());

        commandManager.undo();
        assertEquals(80, gate.getWidth());

        commandManager.undo();
        assertEquals(100, gate.getX());

        commandManager.undo();
        assertTrue(circuit.getElements().isEmpty());

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
