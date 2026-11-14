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
