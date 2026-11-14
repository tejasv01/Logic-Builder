package circuitsim.export;

import circuitsim.exceptions.ExportFailedException;
import circuitsim.model.Circuit;
import circuitsim.model.CircuitElement;
import circuitsim.model.ElementStyle;
import circuitsim.model.GroupShape;
import circuitsim.model.InputSwitch;
import circuitsim.model.LogicGate;
import circuitsim.model.OutputLed;
import circuitsim.model.TextLabel;
import circuitsim.model.Wire;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete Visitor that serializes all CircuitElements (including wires, switch states,
 * styles, and composite groups) to a clean, human-readable JSON format without
 * external library dependencies.
 */
public class JsonExportVisitor implements CircuitVisitor {
    private final List<String> entries = new ArrayList<>();

    public String exportToString(Circuit circuit) {
        entries.clear();
        circuit.accept(this);
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"version\": \"1.0\",\n  \"elements\": [\n");
        for (int i = 0; i < entries.size(); i++) {
            sb.append("    ").append(entries.get(i));
            if (i < entries.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("  ]\n}\n");
        return sb.toString();
    }

    public void exportToFile(Circuit circuit, File file) throws ExportFailedException {
        if (file == null) {
            throw new ExportFailedException("Target JSON file cannot be null.");
        }
        try {
            String json = exportToString(circuit);
            Files.writeString(file.toPath(), json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ExportFailedException("Failed to write JSON file: " + e.getMessage(), e);
        }
    }

    private String baseJson(CircuitElement el, String extraFields) {
        ElementStyle s = el.getStyle();
        return String.format(
                "{\"id\":\"%s\",\"type\":\"%s\",\"x\":%d,\"y\":%d,\"width\":%d,\"height\":%d,\"fill\":\"%s\",\"stroke\":\"%s\",\"strokeWidth\":%.1f%s}",
                el.getId(), el.getType().name(), el.getX(), el.getY(), el.getWidth(), el.getHeight(),
                ElementStyle.toHex(s.getFillColor()), ElementStyle.toHex(s.getStrokeColor()), s.getStrokeWidth(),
                extraFields);
    }

    @Override
    public void visitGate(LogicGate gate) {
        entries.add(baseJson(gate, ""));
    }

    @Override
    public void visitSwitch(InputSwitch sw) {
        entries.add(baseJson(sw, String.format(",\"state\":%b", sw.isState())));
    }

    @Override
    public void visitLed(OutputLed led) {
        entries.add(baseJson(led, ""));
    }

    @Override
    public void visitWire(Wire wire) {
        String extra = String.format(
                ",\"sourceOwnerId\":\"%s\",\"targetOwnerId\":\"%s\",\"targetPinIndex\":%d",
                wire.getSourcePin().getOwner().getId(),
                wire.getTargetPin().getOwner().getId(),
                findPinIndex(wire));
        entries.add(baseJson(wire, extra));
    }

    private int findPinIndex(Wire wire) {
        CircuitElement targetOwner = wire.getTargetPin().getOwner();
        if (targetOwner instanceof LogicGate) {
            return ((LogicGate) targetOwner).getInputPins().indexOf(wire.getTargetPin());
        }
        return 0;
    }

    @Override
    public void visitText(TextLabel label) {
        String safeText = label.getText().replace("\"", "\\\"");
        entries.add(baseJson(label, String.format(",\"text\":\"%s\"", safeText)));
    }

    @Override
    public void visitGroup(GroupShape group) {
        for (CircuitElement child : group.getChildren()) {
            child.accept(this);
        }
    }
}
