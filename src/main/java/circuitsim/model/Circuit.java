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
