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
