# Logic-Builder
A Logical Circuit Building Utility built from scratch by Tejasv and Sarthak. Chosen as an open-topic Object Oriented Methodologies project.

---

## 1. Project Structure (Ordered by Stage & Package)
```text
logic-circuit-simulator/
├── README.md
├── pom.xml
├── run.sh
├── docs/
│   ├── 04_Complete_Source_Code_Reference.md
│   ├── 06_JUnit5_Test_Suite_Reference.md
│   └── 07_Project_Report.md
└── src/
    ├── main/java/circuitsim/
    │   ├── Main.java
    │   ├── exceptions/  (InvalidShapeException, FileFormatException, ExportFailedException, EmptyUndoStackException)
    │   ├── model/       (Circuit, CircuitElement, LogicGate, And/Or/Not/Xor/Nand/NorGate, InputSwitch, OutputLed, Wire, Pin, TextLabel, GroupShape, ElementStyle, ElementType, CircuitListener)
    │   ├── factory/     (ShapeFactory)
    │   ├── commands/    (Command, CommandManager, AddShapeCommand, MoveCommand, ResizeCommand, DeleteCommand, RecolorCommand, GroupCommand)
    │   ├── tools/       (Tool, SelectTool, GatePlacementTool, WireTool, InteractTool)
    │   ├── export/      (CircuitVisitor, SvgExportVisitor, JsonExportVisitor, JsonCircuitLoader, PngExporter)
    │   └── ui/          (MainFrame, CircuitCanvasPanel, ModernButton, UITheme, CircuitPresets)
    └── test/java/circuitsim/
        ├── ModelAndSimulationTest.java
        ├── CommandManagerTest.java
        ├── ShapeFactoryTest.java
        └── ExporterTest.java
```

## 2. Quick Compile & Run (Java 17 LTS)
### Option A: Direct Shell Script (Auto-compile and launch)
```bash
./run.sh
```

### Option B: Pure JDK (`javac` + `java`)
```bash
mkdir -p out
javac --release 17 -d out $(find src/main/java -name "*.java")
java -cp out circuitsim.Main
```

### Option C: Maven (Compile, Run & Execute JUnit 5 Tests)
```bash
mvn test
mvn compile exec:java -Dexec.mainClass="circuitsim.Main"
```

## 3. Key Features
- **Studio Interface:** Modern Dark Studio & Light Studio theme toggle with a compact 2-column sidebar.
- **Direct Switch Interaction:** Click any Input Switch on the canvas to toggle its boolean state immediately (`0` ↔ `1`).
- **Interactive Wiring:** Drag from any pin terminal with magnetic snapping and live signal glow (green neon for HIGH).
- **Realistic Schematics:** IEEE/ANSI logic gate silhouettes, radial-glow LEDs with specular reflection, and tactile toggle switches.
- **Marquee Selection:** Box-select multiple components simultaneously on the canvas.
- **Command Pattern & History:** Full Undo/Redo stack for add, move, resize, recolor, delete, and grouping actions.
- **Design Patterns Implemented:** Observer, Visitor (JSON/SVG export), Factory, Prototype (clone), Command, and Composite (Subcircuits).
