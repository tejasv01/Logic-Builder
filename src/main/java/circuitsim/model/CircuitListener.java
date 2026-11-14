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
