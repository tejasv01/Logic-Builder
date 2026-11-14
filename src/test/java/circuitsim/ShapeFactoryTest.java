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

        assertNotNull(ShapeFactory.createWire(sw1.getOutputPin(), led.getInputPin(), style));

        assertThrows(InvalidShapeException.class, () ->
                ShapeFactory.createWire(sw1.getOutputPin(), sw2.getOutputPin(), style));

        assertThrows(InvalidShapeException.class, () ->
                ShapeFactory.createWire(sw1.getOutputPin(), sw1.getOutputPin(), style));
    }
}
