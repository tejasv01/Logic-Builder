package circuitsim.export;

import circuitsim.exceptions.FileFormatException;
import circuitsim.exceptions.InvalidShapeException;
import circuitsim.factory.ShapeFactory;
import circuitsim.model.Circuit;
import circuitsim.model.CircuitElement;
import circuitsim.model.ElementStyle;
import circuitsim.model.ElementType;
import circuitsim.model.InputSwitch;
import circuitsim.model.LogicGate;
import circuitsim.model.OutputLed;
import circuitsim.model.Pin;
import circuitsim.model.Wire;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads and reconstructs a Circuit from a JSON string or file produced by JsonExportVisitor.
 * Throws FileFormatException if the JSON syntax or schema is invalid.
 */
public class JsonCircuitLoader {

    private static final Pattern OBJECT_PATTERN = Pattern.compile("\\{([^{}]+)\\}");
    private static final Pattern KV_PATTERN = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(\"[^\"]*\"|[^,}]+)");

    public void loadFromFile(Circuit circuit, File file) throws FileFormatException {
        if (file == null || !file.exists()) {
            throw new FileFormatException("JSON file does not exist.");
        }
        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            loadFromString(circuit, content);
        } catch (IOException e) {
            throw new FileFormatException("Could not read JSON file: " + e.getMessage(), e);
        }
    }

    public void loadFromString(Circuit circuit, String json) throws FileFormatException {
        if (json == null || !json.contains("\"elements\"")) {
            throw new FileFormatException("Invalid circuit JSON: missing 'elements' array.");
        }

        List<Map<String, String>> rawObjects = new ArrayList<>();
        Matcher objMatcher = OBJECT_PATTERN.matcher(json);
        while (objMatcher.find()) {
            String body = objMatcher.group(1);
            Map<String, String> fields = new HashMap<>();
            Matcher kvMatcher = KV_PATTERN.matcher(body);
            while (kvMatcher.find()) {
                String key = kvMatcher.group(1).trim();
                String val = kvMatcher.group(2).trim();
                if (val.startsWith("\"") && val.endsWith("\"") && val.length() >= 2) {
                    val = val.substring(1, val.length() - 1);
                }
                fields.put(key, val);
            }
            if (fields.containsKey("type")) {
                rawObjects.add(fields);
            }
        }

        circuit.clear();
        Map<String, CircuitElement> byId = new HashMap<>();

        List<Map<String, String>> deferredWires = new ArrayList<>();

        try {
            for (Map<String, String> map : rawObjects) {
                ElementType type = ElementType.valueOf(map.get("type"));
                if (type == ElementType.WIRE) {
                    deferredWires.add(map);
                    continue;
                }
                String id = map.get("id");
                int x = Integer.parseInt(map.getOrDefault("x", "50"));
                int y = Integer.parseInt(map.getOrDefault("y", "50"));
                int w = Integer.parseInt(map.getOrDefault("width", "80"));
                int h = Integer.parseInt(map.getOrDefault("height", "50"));
                Color fill = ElementStyle.fromHex(map.getOrDefault("fill", "#f0f4ff"));
                Color stroke = ElementStyle.fromHex(map.getOrDefault("stroke", "#1e293b"));
                float sw = Float.parseFloat(map.getOrDefault("strokeWidth", "2.0"));
                String text = map.getOrDefault("text", "Label");

                ElementStyle style = new ElementStyle(fill, stroke, sw);
                CircuitElement el = ShapeFactory.createElement(type, x, y, w, h, style, text);
                if (id != null) {
                    el.setId(id);
                }
                if (el instanceof InputSwitch && map.containsKey("state")) {
                    ((InputSwitch) el).setState(Boolean.parseBoolean(map.get("state")));
                }
                circuit.addElement(el);
                byId.put(el.getId(), el);
            }

            // Reconnect wires
            for (Map<String, String> wMap : deferredWires) {
                CircuitElement srcEl = byId.get(wMap.get("sourceOwnerId"));
                CircuitElement tgtEl = byId.get(wMap.get("targetOwnerId"));
                if (srcEl == null || tgtEl == null) {
                    throw new FileFormatException("Wire references non-existent element ID.");
                }
                Pin srcPin = extractOutputPin(srcEl);
                int idx = Integer.parseInt(wMap.getOrDefault("targetPinIndex", "0"));
                Pin tgtPin = extractInputPin(tgtEl, idx);

                Color stroke = ElementStyle.fromHex(wMap.getOrDefault("stroke", "#1e293b"));
                float sw = Float.parseFloat(wMap.getOrDefault("strokeWidth", "2.0"));
                Wire wire = ShapeFactory.createWire(srcPin, tgtPin, new ElementStyle(Color.WHITE, stroke, sw));
                circuit.addElement(wire);
            }
            circuit.evaluate();
        } catch (IllegalArgumentException | InvalidShapeException e) {
            throw new FileFormatException("Malformed element data in JSON: " + e.getMessage(), e);
        }
    }

    private Pin extractOutputPin(CircuitElement el) throws FileFormatException {
        if (el instanceof LogicGate) {
            return ((LogicGate) el).getOutputPin();
        } else if (el instanceof InputSwitch) {
            return ((InputSwitch) el).getOutputPin();
        }
        throw new FileFormatException("Element " + el.getId() + " has no output pin.");
    }

    private Pin extractInputPin(CircuitElement el, int index) throws FileFormatException {
        if (el instanceof LogicGate) {
            List<Pin> pins = ((LogicGate) el).getInputPins();
            if (index >= 0 && index < pins.size()) {
                return pins.get(index);
            }
        } else if (el instanceof OutputLed) {
            return ((OutputLed) el).getInputPin();
        }
        throw new FileFormatException("Element " + el.getId() + " has no valid input pin at index " + index);
    }
}
