package circuitsim;

import circuitsim.exceptions.ExportFailedException;
import circuitsim.exceptions.FileFormatException;
import circuitsim.exceptions.InvalidShapeException;
import circuitsim.export.JsonCircuitLoader;
import circuitsim.export.JsonExportVisitor;
import circuitsim.export.PngExporter;
import circuitsim.export.SvgExportVisitor;
import circuitsim.model.AndGate;
import circuitsim.model.Circuit;
import circuitsim.model.ElementStyle;
import circuitsim.model.InputSwitch;
import circuitsim.model.OutputLed;
import circuitsim.model.TextLabel;
import circuitsim.model.Wire;
import java.io.File;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

public class ExporterTest {

    private Circuit circuit;
    private ElementStyle style;

    @BeforeEach
    void setUp() throws InvalidShapeException {
        circuit = new Circuit();
        style = ElementStyle.createDefault();

        InputSwitch sw1 = new InputSwitch(20, 20, 60, 40, style);
        sw1.setState(true);
        InputSwitch sw2 = new InputSwitch(20, 80, 60, 40, style);
        sw2.setState(true);
        AndGate andGate = new AndGate(140, 40, 80, 50, style);
        OutputLed led = new OutputLed(280, 40, 45, 45, style);
        TextLabel label = new TextLabel(20, 140, 120, 30, style, "AND Demo");

        circuit.addElement(sw1);
        circuit.addElement(sw2);
        circuit.addElement(andGate);
        circuit.addElement(led);
        circuit.addElement(label);
        circuit.addElement(new Wire(sw1.getOutputPin(), andGate.getInputPins().get(0), style));
        circuit.addElement(new Wire(sw2.getOutputPin(), andGate.getInputPins().get(1), style));
        circuit.addElement(new Wire(andGate.getOutputPin(), led.getInputPin(), style));
    }

    @Test
    @DisplayName("Verify JSON export and round-trip reload preserves elements, wires, and signal states")
    void testJsonRoundTripSaveAndLoad(@TempDir Path tempDir) throws Exception {
        File jsonFile = tempDir.resolve("circuit.json").toFile();
        JsonExportVisitor exporter = new JsonExportVisitor();
        exporter.exportToFile(circuit, jsonFile);

        assertTrue(jsonFile.exists() && jsonFile.length() > 0);

        Circuit loadedCircuit = new Circuit();
        JsonCircuitLoader loader = new JsonCircuitLoader();
        loader.loadFromFile(loadedCircuit, jsonFile);

        assertEquals(circuit.getElements().size(), loadedCircuit.getElements().size());

        OutputLed loadedLed = loadedCircuit.getElements().stream()
                .filter(e -> e instanceof OutputLed)
                .map(e -> (OutputLed) e)
                .findFirst()
                .orElseThrow();
        assertTrue(loadedLed.isLit(), "Reloaded circuit must preserve switch states and re-evaluate LED to ON");
    }

    @Test
    @DisplayName("Verify SvgExportVisitor produces valid SVG XML containing rect, ellipse, line, and text")
    void testSvgExportVisitor(@TempDir Path tempDir) throws ExportFailedException {
        File svgFile = tempDir.resolve("circuit.svg").toFile();
        SvgExportVisitor svgVisitor = new SvgExportVisitor();
        String svg = svgVisitor.exportToString(circuit, 800, 600);

        assertTrue(svg.startsWith("<svg"));
        assertTrue(svg.contains("<rect"));
        assertTrue(svg.contains("<ellipse"));
        assertTrue(svg.contains("<line"));
        assertTrue(svg.contains("AND Demo"));

        svgVisitor.exportToFile(circuit, svgFile, 800, 600);
        assertTrue(svgFile.exists() && svgFile.length() > 0);
    }

    @Test
    @DisplayName("Verify PngExporter renders circuit to non-empty PNG file")
    void testPngExporter(@TempDir Path tempDir) throws ExportFailedException {
        File pngFile = tempDir.resolve("circuit.png").toFile();
        PngExporter pngExporter = new PngExporter();
        pngExporter.exportToFile(circuit, pngFile, 800, 600);
        assertTrue(pngFile.exists() && pngFile.length() > 100);
    }

    @Test
    @DisplayName("Verify FileFormatException and ExportFailedException on invalid inputs")
    void testExportAndLoadExceptions() {
        JsonCircuitLoader loader = new JsonCircuitLoader();
        assertThrows(FileFormatException.class, () -> loader.loadFromString(circuit, "{bad_json}"));

        SvgExportVisitor svgVisitor = new SvgExportVisitor();
        assertThrows(ExportFailedException.class, () -> svgVisitor.exportToFile(circuit, null, 800, 600));

        PngExporter pngExporter = new PngExporter();
        assertThrows(ExportFailedException.class, () -> pngExporter.exportToFile(circuit, new File("out.png"), -10, 200));
    }
}
