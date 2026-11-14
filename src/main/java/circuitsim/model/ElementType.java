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
