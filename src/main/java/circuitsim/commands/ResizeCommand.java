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
