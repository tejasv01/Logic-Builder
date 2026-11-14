package circuitsim.ui;

import circuitsim.exceptions.InvalidShapeException;
import circuitsim.model.AndGate;
import circuitsim.model.Circuit;
import circuitsim.model.ElementStyle;
import circuitsim.model.InputSwitch;
import circuitsim.model.NandGate;
import circuitsim.model.NorGate;
import circuitsim.model.NotGate;
import circuitsim.model.OrGate;
import circuitsim.model.OutputLed;
import circuitsim.model.TextLabel;
import circuitsim.model.Wire;
import circuitsim.model.XorGate;

/**
 * Provides built-in educational and demonstration circuits.
 */
public class CircuitPresets {

    public static void loadHalfAdder(Circuit circuit) {
        circuit.clear();
        ElementStyle style = ElementStyle.createDefault();
        try {
            TextLabel title = new TextLabel(40, 20, 260, 36, style, "HALF ADDER (A, B -> Sum, Carry)");
            circuit.addElement(title);

            // Inputs
            InputSwitch swA = new InputSwitch(40, 80, 70, 44, style);
            InputSwitch swB = new InputSwitch(40, 180, 70, 44, style);
            swA.setState(true); // default A=1
            circuit.addElement(swA);
            circuit.addElement(swB);

            // Gates
            XorGate xor = new XorGate(180, 75, 90, 56, style);
            AndGate and = new AndGate(180, 175, 90, 56, style);
            circuit.addElement(xor);
            circuit.addElement(and);

            // Outputs
            OutputLed ledSum = new OutputLed(340, 80, 48, 48, style);
            OutputLed ledCarry = new OutputLed(340, 180, 48, 48, style);
            circuit.addElement(ledSum);
            circuit.addElement(ledCarry);

            TextLabel lblSum = new TextLabel(400, 90, 90, 30, style, "SUM (S)");
            TextLabel lblCarry = new TextLabel(400, 190, 100, 30, style, "CARRY (C)");
            circuit.addElement(lblSum);
            circuit.addElement(lblCarry);

            // Wires
            circuit.addElement(new Wire(swA.getOutputPin(), xor.getInputPins().get(0), style));
            circuit.addElement(new Wire(swB.getOutputPin(), xor.getInputPins().get(1), style));
            circuit.addElement(new Wire(swA.getOutputPin(), and.getInputPins().get(0), style));
            circuit.addElement(new Wire(swB.getOutputPin(), and.getInputPins().get(1), style));

            circuit.addElement(new Wire(xor.getOutputPin(), ledSum.getInputPin(), style));
            circuit.addElement(new Wire(and.getOutputPin(), ledCarry.getInputPin(), style));

            circuit.evaluate();
        } catch (InvalidShapeException e) {
            e.printStackTrace();
        }
    }

    public static void loadGateShowcase(Circuit circuit) {
        circuit.clear();
        ElementStyle style = ElementStyle.createDefault();
        try {
            TextLabel title = new TextLabel(40, 20, 300, 34, style, "LOGIC GATES INTERACTIVE SHOWCASE");
            circuit.addElement(title);

            // Row 1: AND
            InputSwitch a1 = new InputSwitch(40, 70, 60, 36, style); a1.setState(true);
            InputSwitch a2 = new InputSwitch(40, 115, 60, 36, style); a2.setState(true);
            AndGate and = new AndGate(140, 80, 84, 52, style);
            OutputLed ledAnd = new OutputLed(260, 82, 44, 44, style);
            TextLabel lblAnd = new TextLabel(320, 88, 70, 30, style, "AND: 1&1");
            circuit.addElement(a1); circuit.addElement(a2); circuit.addElement(and);
            circuit.addElement(ledAnd); circuit.addElement(lblAnd);
            circuit.addElement(new Wire(a1.getOutputPin(), and.getInputPins().get(0), style));
            circuit.addElement(new Wire(a2.getOutputPin(), and.getInputPins().get(1), style));
            circuit.addElement(new Wire(and.getOutputPin(), ledAnd.getInputPin(), style));

            // Row 2: OR
            InputSwitch o1 = new InputSwitch(40, 170, 60, 36, style); o1.setState(false);
            InputSwitch o2 = new InputSwitch(40, 215, 60, 36, style); o2.setState(true);
            OrGate or = new OrGate(140, 180, 84, 52, style);
            OutputLed ledOr = new OutputLed(260, 182, 44, 44, style);
            TextLabel lblOr = new TextLabel(320, 188, 70, 30, style, "OR: 0|1");
            circuit.addElement(o1); circuit.addElement(o2); circuit.addElement(or);
            circuit.addElement(ledOr); circuit.addElement(lblOr);
            circuit.addElement(new Wire(o1.getOutputPin(), or.getInputPins().get(0), style));
            circuit.addElement(new Wire(o2.getOutputPin(), or.getInputPins().get(1), style));
            circuit.addElement(new Wire(or.getOutputPin(), ledOr.getInputPin(), style));

            // Row 3: NOT
            InputSwitch n1 = new InputSwitch(40, 280, 60, 36, style); n1.setState(false);
            NotGate not = new NotGate(140, 275, 84, 52, style);
            OutputLed ledNot = new OutputLed(260, 278, 44, 44, style);
            TextLabel lblNot = new TextLabel(320, 284, 70, 30, style, "NOT: ~0=1");
            circuit.addElement(n1); circuit.addElement(not);
            circuit.addElement(ledNot); circuit.addElement(lblNot);
            circuit.addElement(new Wire(n1.getOutputPin(), not.getInputPins().get(0), style));
            circuit.addElement(new Wire(not.getOutputPin(), ledNot.getInputPin(), style));

            // Row 4: XOR
            InputSwitch x1 = new InputSwitch(40, 350, 60, 36, style); x1.setState(true);
            InputSwitch x2 = new InputSwitch(40, 395, 60, 36, style); x2.setState(false);
            XorGate xor = new XorGate(140, 360, 84, 52, style);
            OutputLed ledXor = new OutputLed(260, 362, 44, 44, style);
            TextLabel lblXor = new TextLabel(320, 368, 70, 30, style, "XOR: 1^0");
            circuit.addElement(x1); circuit.addElement(x2); circuit.addElement(xor);
            circuit.addElement(ledXor); circuit.addElement(lblXor);
            circuit.addElement(new Wire(x1.getOutputPin(), xor.getInputPins().get(0), style));
            circuit.addElement(new Wire(x2.getOutputPin(), xor.getInputPins().get(1), style));
            circuit.addElement(new Wire(xor.getOutputPin(), ledXor.getInputPin(), style));

            circuit.evaluate();
        } catch (InvalidShapeException e) {
            e.printStackTrace();
        }
    }

    public static void loadSRLatch(Circuit circuit) {
        circuit.clear();
        ElementStyle style = ElementStyle.createDefault();
        try {
            TextLabel title = new TextLabel(40, 20, 280, 34, style, "SR LATCH BISTABLE MULTIVIBRATOR");
            circuit.addElement(title);

            // Inputs: Reset (R) on top, Set (S) on bottom
            InputSwitch swR = new InputSwitch(40, 75, 70, 44, style);
            InputSwitch swS = new InputSwitch(40, 195, 70, 44, style);
            swR.setState(false);
            swS.setState(false);
            circuit.addElement(swR);
            circuit.addElement(swS);

            TextLabel lblR = new TextLabel(120, 82, 40, 28, style, "R");
            TextLabel lblS = new TextLabel(120, 202, 40, 28, style, "S");
            circuit.addElement(lblR);
            circuit.addElement(lblS);

            // Two NOR Gates
            NorGate nor1 = new NorGate(180, 70, 84, 52, style);
            NorGate nor2 = new NorGate(180, 190, 84, 52, style);
            circuit.addElement(nor1);
            circuit.addElement(nor2);

            // Outputs: Q on top, Q' on bottom
            OutputLed ledQ = new OutputLed(340, 74, 46, 46, style);
            OutputLed ledQBar = new OutputLed(340, 194, 46, 46, style);
            circuit.addElement(ledQ);
            circuit.addElement(ledQBar);

            TextLabel lblQ = new TextLabel(400, 82, 60, 28, style, "Q");
            TextLabel lblQBar = new TextLabel(400, 202, 60, 28, style, "Q' (NOT Q)");
            circuit.addElement(lblQ);
            circuit.addElement(lblQBar);

            // Wires: External Inputs to NOR gates
            circuit.addElement(new Wire(swR.getOutputPin(), nor1.getInputPins().get(0), style));
            circuit.addElement(new Wire(swS.getOutputPin(), nor2.getInputPins().get(1), style));

            // Outputs to LEDs
            circuit.addElement(new Wire(nor1.getOutputPin(), ledQ.getInputPin(), style));
            circuit.addElement(new Wire(nor2.getOutputPin(), ledQBar.getInputPin(), style));

            // Cross-coupling feedback wires
            circuit.addElement(new Wire(nor1.getOutputPin(), nor2.getInputPins().get(0), style));
            circuit.addElement(new Wire(nor2.getOutputPin(), nor1.getInputPins().get(1), style));

            circuit.evaluate();
        } catch (InvalidShapeException e) {
            e.printStackTrace();
        }
    }
}
