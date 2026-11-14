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
