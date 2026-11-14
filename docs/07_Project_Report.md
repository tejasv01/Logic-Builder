# Object Oriented Methodology (OOM) Course Project Report

## Project Title: Interactive Logic Circuit Simulator in Java (Swing)

**Target Platform:** Java 17 LTS | **Testing Framework:** JUnit 5 | **Architecture:** Model-GUI Separated Object-Oriented Design

## 1. Introduction

### 1.1 Project Overview

The **Interactive Logic Circuit Simulator** is a desktop CAD and boolean simulation application engineered in **Java 17 LTS** using **Java Swing** (`javax.swing` and `java.awt`). It enables students and digital-logic designers to visually place boolean logic gates (**AND, OR, NOT, XOR, NAND, NOR**), interactive **Input Switches**, circular **Output LEDs**, and **Text Labels**, wire their input/output pins together, and simulate live digital signal propagation in real time. Beyond schematic capture and simulation, the application supports full vector-editing capabilities: single and multi-selection, dragging, corner-handle resizing, custom styling (fill color, stroke color, and line width), multi-level undo/redo history, composite subcircuit grouping/ungrouping, prototype clipboard copy/paste, JSON persistence, and vector/raster export to **SVG** and **PNG**.

### 1.2 Architectural Philosophy: Strict Model-GUI Separation

A core engineering requirement of this project is maintaining strict independence between the **domain/simulation model** and the **Swing graphical user interface**:

* The packages `circuitsim.model`, `circuitsim.commands`, `circuitsim.factory`, `circuitsim.export`, and `circuitsim.exceptions` contain **zero imports from `javax.swing`**.

* The domain model communicates state changes to the presentation layer exclusively through the `CircuitListener` interface (**Observer Pattern**), allowing the entire simulation engine, command stack, factory, and exporters to be executed headlessly inside automated JUnit 5 test suites or CLI pipelines.

## 2. Requirements Specification

### 2.1 Functional Requirements

| Req ID | Module | Requirement Statement | Target Classes | 
| ----- | ----- | ----- | ----- | 
| **FR-01** | Component Placement | Place **AND**, **OR**, **NOT**, **XOR**, **NAND**, **NOR** gates, **InputSwitch**, **OutputLed**, and **TextLabel** elements on the canvas. | `ShapeFactory`, `GatePlacementTool`, `AddShapeCommand` | 
| **FR-02** | Pin-to-Pin Wiring | Connect an output `Pin` of a source component to an input `Pin` of a target component via a directed `Wire` while preventing short-circuits or self-wiring. | `Pin`, `Wire`, `WireTool` | 
| **FR-03** | Live Boolean Simulation | Toggle `InputSwitch` states (`HIGH=1` / `LOW=0`) interactively and propagate boolean signals across connected wires and gates to update `OutputLed` probes. | `Circuit.evaluate()`, `LogicGate.evaluate()`, `InteractTool` | 
| **FR-04** | Selection, Move, Resize & Delete | Select single or multiple elements, drag to reposition (with connected wires following automatically), drag resize handles to scale, or delete elements and incident wires. | `SelectTool`, `MoveCommand`, `ResizeCommand`, `DeleteCommand` | 
| **FR-05** | Visual Styling | Customize fill color, stroke color, and stroke width (`1.5 px` – `4.0 px`) for selected components or default toolbar settings. | `ElementStyle`, `RecolorCommand` | 
| **FR-06** | Undo / Redo History | Record every mutating operation on an undo stack and support multi-step `undo()` and `redo()`. | `Command`, `CommandManager` | 
| **FR-07** | Subcircuit Grouping | Group multiple selected components into a single `GroupShape` composite unit and ungroup them back into individual components. | `GroupShape`, `GroupCommand` | 
| **FR-08** | Clipboard Copy / Paste | Deep-clone selected elements via Prototype `clone()` and paste independent duplicates with a `(+25, +25)` pixel offset. | `CircuitElement.clone()`, `ElementStyle.clone()` | 
| **FR-09** | Save/Load & Export | Save and reload complete circuit topologies and switch states in `.json` format, and export schematics to `.svg` and `.png`. | `JsonExportVisitor`, `JsonCircuitLoader`, `SvgExportVisitor`, `PngExporter` | 

### 2.2 Non-Functional Requirements

* **Extensibility (Open/Closed Compliance):** Adding a new logic gate (e.g., `XnorGate`) or a new export format (e.g., `VerilogExportVisitor`) requires zero modification to existing `CommandManager` or `Circuit` classes.

* **Defensive Validation:** All invalid geometric dimensions, illegal pin connections, empty undo/redo operations, and malformed file inputs raise descriptive custom checked exceptions.

* **Portability:** Compiles and runs out-of-the-box on any standard **JDK 17+** installation without third-party runtime libraries.

## 3. Object-Oriented Methodology (OOM) Concepts & UML Architecture

### 3.1 Explicit Mapping of OOM Relationships in Code

| OOM Concept | Where Applied in Codebase | Code-Level Explanation | 
| ----- | ----- | ----- | 
| **Encapsulation** | `ElementStyle`, `Pin`, `CircuitElement`, `Circuit`, `CommandManager` | All fields (`x`, `y`, `width`, `height`, `fillColor`, `undoStack`, `elements`) are `private` or `protected` and mutated only through validated methods (`resize()`, `setStyle()`, `executeCommand()`), returning unmodifiable views (`Collections.unmodifiableList()`). | 
| **Abstraction** | `CircuitElement`, `LogicGate`, `Tool`, `Command`, `CircuitVisitor`, `CircuitListener` | Abstract classes and interfaces define high-level contracts (`evaluate()`, `draw()`, `accept()`, `execute()`, `undo()`) while hiding implementation details from callers. | 
| **Inheritance** | `CircuitElement` $\rightarrow$ `LogicGate` $\rightarrow$ `{AndGate, OrGate, NotGate, XorGate, NandGate, NorGate}` | Shared coordinate, bounding-box, pin-management, and rendering logic resides in `CircuitElement` and `LogicGate`; concrete gate subclasses inherit this structure and implement only gate-specific boolean truth tables. | 
| **Polymorphism** | `Circuit.evaluate()`, `CircuitCanvasPanel.paintComponent()`, `Circuit.accept()` | Dynamic method dispatch invokes `el.evaluate()`, `el.draw(g2d)`, and `el.accept(visitor)` uniformly across heterogeneous `List<CircuitElement>` collections without `instanceof` chains. | 
| **Composition (Strong Lifecycle `has-a`)** | `LogicGate *-- Pin`, `InputSwitch *-- Pin`, `OutputLed *-- Pin`, `CircuitElement *-- ElementStyle` | A `LogicGate` instantiates its own `Pin` objects inside its constructor (`initPins()`), and each `CircuitElement` owns a cloned `ElementStyle`. If a gate is garbage-collected or cloned, its pins die or are recreated with it. | 
| **Aggregation (Weak Lifecycle `has-a`)** | `GroupShape o-- CircuitElement`, `Circuit o-- CircuitElement`, `CommandManager o-- Command` | `GroupShape` aggregates existing `CircuitElement` instances passed into its constructor. When ungrouped via `GroupCommand`, the child elements continue to exist independently inside `Circuit`. | 
| **Association (`uses-a` / Reference)** | `Wire --> Pin`, `SelectTool --> Circuit`, `Circuit --> CircuitListener` | A `Wire` holds references to `sourcePin` and `targetPin` belonging to two separate components without owning their lifecycles; tools associate with `Circuit` and `CommandManager` to dispatch user commands. | 

### 3.2 Class Diagram (Mermaid)

```
classDiagram
    direction TB

    class Circuit {
        -List~CircuitElement~ elements
        -List~CircuitListener~ listeners
        +addElement(CircuitElement el) void
        +removeElement(CircuitElement el) void
        +evaluate() void
        +accept(CircuitVisitor v) void
        +addListener(CircuitListener l) void
        +notifyListeners() void
    }

    class CircuitListener {
        <<interface>>
        +onCircuitChanged() void
    }

    class CircuitElement {
        <<abstract>>
        #String id
        #int x
        #int y
        #int width
        #int height
        #ElementStyle style
        #boolean selected
        +draw(Graphics2D g2d)* void
        +evaluate()* void
        +accept(CircuitVisitor v)* void
        +clone()* CircuitElement
    }

    class LogicGate {
        <<abstract>>
        #List~Pin~ inputPins
        #Pin outputPin
        +getGateSymbol()* String
    }

    class Pin {
        -String id
        -CircuitElement owner
        -boolean output
        -boolean value
        +getAbsolutePosition() Point
    }

    class GroupShape {
        -List~CircuitElement~ children
        +getChildren() List~CircuitElement~
        +recalculateBounds() void
    }

    class Wire {
        -Pin sourcePin
        -Pin targetPin
    }

    Circuit "1" o-- "*" CircuitElement : Aggregation
    Circuit "1" --> "*" CircuitListener : Association (Observer)
    CircuitElement "1" *-- "1" ElementStyle : Composition
    CircuitElement <|-- LogicGate : Inheritance
    CircuitElement <|-- InputSwitch : Inheritance
    CircuitElement <|-- OutputLed : Inheritance
    CircuitElement <|-- Wire : Inheritance
    CircuitElement <|-- TextLabel : Inheritance
    CircuitElement <|-- GroupShape : Inheritance
    LogicGate <|-- AndGate
    LogicGate <|-- OrGate
    LogicGate <|-- NotGate
    LogicGate <|-- XorGate
    LogicGate <|-- NandGate
    LogicGate <|-- NorGate
    LogicGate "1" *-- "1..*" Pin : Composition
    GroupShape "1" o-- "*" CircuitElement : Aggregation (Composite)
    Wire "1" --> "2" Pin : Association

```

### 3.3 Sequence Diagram: Mouse Drag $\rightarrow$ Tool $\rightarrow$ Command $\rightarrow$ Circuit $\rightarrow$ Repaint

```
sequenceDiagram
    actor User
    participant Canvas as CircuitCanvasPanel
    participant Tool as GatePlacementTool
    participant Factory as ShapeFactory
    participant CmdMgr as CommandManager
    participant Cmd as AddShapeCommand
    participant Circuit as Circuit

    User->>Canvas: mousePressed(e) & mouseDragged(e)
    Canvas->>Tool: mousePressed(e) / mouseDragged(e)
    Tool->>Canvas: repaint() (renders dashed preview box)
    User->>Canvas: mouseReleased(e)
    Canvas->>Tool: mouseReleased(e)
    Tool->>Factory: createElement(AND, x, y, w, h, style, "")
    Factory-->>Tool: AndGate instance
    Tool->>Cmd: new AddShapeCommand(circuit, andGate)
    Tool->>CmdMgr: executeCommand(cmd)
    CmdMgr->>Cmd: execute()
    Cmd->>Circuit: addElement(andGate)
    Circuit->>Circuit: evaluate()
    Circuit->>Canvas: onCircuitChanged()
    Canvas->>Canvas: repaint()

```

## 4. Design Patterns Implemented

| Design Pattern | Category | Participating Classes | Purpose & Benefit in Logic Circuit Simulator | 
| ----- | ----- | ----- | ----- | 
| **1. Composite** | Structural | `CircuitElement` (Component), `GroupShape` (Composite), `LogicGate` / `InputSwitch` / `OutputLed` (Leaves) | Allows users to group multiple logic gates and switches into a reusable subcircuit block (`GroupShape`) that moves, styles, evaluates, and renders uniformly alongside primitive elements. | 
| **2. Command** | Behavioral | `Command`, `CommandManager`, `AddShapeCommand`, `MoveCommand`, `ResizeCommand`, `DeleteCommand`, `RecolorCommand`, `GroupCommand` | Encapsulates every circuit modification as an object with `execute()` and `undo()` methods, enabling unlimited multi-step Undo and Redo history. | 
| **3. Factory** | Creational | `ShapeFactory`, `ElementType`, `CircuitElement` subclasses | Centralizes object instantiation and dimension/text validation so `GatePlacementTool` and `JsonCircuitLoader` never couple directly to concrete gate constructors. | 
| **4. Prototype** | Creational | `CircuitElement.clone()`, `ElementStyle.clone()`, concrete `clone()` overrides | Enables deep-copying of selected gates, switches, LEDs, and composite `GroupShape` instances for clipboard **Copy & Paste** without knowing their concrete classes. | 
| **5. Strategy / State** | Behavioral | `Tool` interface, `SelectTool`, `GatePlacementTool`, `WireTool`, `InteractTool` | Allows `CircuitCanvasPanel` to delegate `mousePressed`, `mouseDragged`, `mouseReleased`, and `drawPreview` behavior dynamically based on the currently selected toolbar mode. | 
| **6. Visitor** | Behavioral | `CircuitVisitor`, `SvgExportVisitor`, `JsonExportVisitor`, `CircuitElement.accept()` | Separates `.svg` and `.json` serialization algorithms from the `model` classes via double dispatch (`element.accept(visitor)`). | 
| **7. Observer** | Behavioral | `Circuit` (Subject), `CircuitListener` (Observer interface), `CircuitCanvasPanel` (Concrete Observer) | Decouples the `Circuit` simulation model from `javax.swing`; whenever gates are added, moved, or toggled, `Circuit` notifies registered listeners to trigger `repaint()`. | 

## 5. SOLID Principles Mapping

| Principle | Full Name | How & Where Applied in Codebase | 
| ----- | ----- | ----- | 
| **S — SRP** | Single Responsibility Principle | Every class has a single reason to change: `AndGate` only computes boolean AND; `ElementStyle` only manages visual styling; `SvgExportVisitor` only formats SVG XML; `CommandManager` only manages undo/redo stacks. | 
| **O — OCP** | Open/Closed Principle | Classes are open for extension but closed for modification: adding a new `XnorGate` (extending `LogicGate`), a new `RotateCommand` (implementing `Command`), or a new `VerilogExportVisitor` (implementing `CircuitVisitor`) requires zero changes to `CommandManager` or `Circuit`. | 
| **L — LSP** | Liskov Substitution Principle | Every subclass of `CircuitElement` (`AndGate`, `OrGate`, `InputSwitch`, `OutputLed`, `Wire`, `TextLabel`, `GroupShape`) honors the base contract (`draw`, `evaluate`, `move`, `accept`, `clone`) and can substitute for `CircuitElement` in any collection or command without breaking invariants. | 
| **I — ISP** | Interface Segregation Principle | Interfaces are small and focused: `CircuitListener` has 1 method (`onCircuitChanged()`), `Command` has 3 methods (`execute()`, `undo()`, `getName()`), and `Tool` has 4 mouse/preview methods—no client is forced to implement unused methods. | 
| **D — DIP** | Dependency Inversion Principle | High-level modules depend on abstractions: `Circuit` depends on `CircuitListener` (not `JPanel`), `CircuitCanvasPanel` depends on `Tool` (not concrete tool classes), and `CommandManager` depends on `Command` (not specific command subclasses). | 

## 6. Exception Handling Architecture

All four required custom checked exceptions reside in `circuitsim.exceptions` and enforce fail-safe behavior across the application:

| Custom Exception Class | Where Thrown | Trigger Condition | How Handled in UI / Tests | 
| ----- | ----- | ----- | ----- | 
| **`InvalidShapeException`** | `CircuitElement` constructor, `resize()`, `TextLabel`, `Wire`, `GroupShape`, `ShapeFactory` | Non-positive width/height ($\le 0$), resize below $20\times 20$ px, null/empty text label, connecting two output pins together, self-wiring, or empty group. | Caught in `GatePlacementTool`, `WireTool`, and `MainFrame` to display a clear status bar warning without crashing or corrupting the undo stack. | 
| **`EmptyUndoStackException`** | `CommandManager.undo()`, `CommandManager.redo()` | User clicks **Undo** when `undoStack` is empty, or clicks **Redo** when `redoStack` is empty. | Caught in `MainFrame.handleUndo()` / `handleRedo()` and shown as a friendly status bar message. | 
| **`FileFormatException`** | `JsonCircuitLoader.loadFromFile()`, `loadFromString()` | Loaded `.json` file is missing the `"elements"` array, contains unknown gate types, has unparseable coordinates, or references missing pin owner IDs. | Caught in `MainFrame.handleLoadJson()` to show a `JOptionPane.ERROR_MESSAGE` dialog while preserving application stability. | 
| **`ExportFailedException`** | `SvgExportVisitor.exportToFile()`, `JsonExportVisitor.exportToFile()`, `PngExporter.exportToFile()` | Target file is null, image dimensions are non-positive, or disk I/O permission/write failure occurs. | Caught in `MainFrame` export handlers and surfaced via an error dialog. | 

## 7. Verification & Unit Testing Summary

The JUnit 5 test suite (`src/test/java/circuitsim/`) verifies the entire non-GUI engine across 4 test classes and 15 test methods:

1. **`ModelAndSimulationTest`:** Validates truth tables for `AND`, `OR`, `NOT`, `XOR`, `NAND`, and `NOR`; tests end-to-end signal propagation from `InputSwitch` through `Wire` and `AndGate` to `OutputLed`; verifies deep `clone()` independence (**Prototype**), `GroupShape` composite operations (**Composite**), and `CircuitListener` callbacks (**Observer**).

2. **`CommandManagerTest`:** Tests `execute()`, `undo()`, and `redo()` for `AddShapeCommand`, `MoveCommand`, `ResizeCommand`, `RecolorCommand`, `GroupCommand`, and `DeleteCommand` (verifying that deleting a gate also removes and restores its attached wires), plus `EmptyUndoStackException`.

3. **`ShapeFactoryTest`:** Confirms factory creation of all `ElementType` values and asserts that `InvalidShapeException` is thrown for negative dimensions, blank text labels, output-to-output wiring, and self-wiring.

4. **`ExporterTest`:** Uses JUnit `@TempDir` to test round-trip JSON save and reload (confirming switch states and LED outputs survive serialization), SVG XML generation, PNG raster rendering, `FileFormatException`, and `ExportFailedException`.

## 8. Screenshots Section (Guide for Your Submission Document)

Include the following 5 screenshots when compiling your final submission PDF/Word handout:

* **Figure 8.1 — Half-Adder Schematic on Canvas:** Place two `InputSwitch` nodes (`SW A`, `SW B`), an `XorGate` (Sum), an `AndGate` (Carry), two `OutputLed` indicators, and a `TextLabel` reading `"Half Adder Circuit"`, wired together with `InteractTool` toggling `SW A = 1, SW B = 1` (showing green energized wires and the Carry LED lit `ON`).

* **Figure 8.2 — Composite Grouping & Custom Styling:** Show two gates selected and grouped into a dashed `GroupShape` bounding box with custom fill/stroke colors applied via `JColorChooser`.

* **Figure 8.3 — Undo / Redo & Custom Exception Status Message:** Show the bottom status bar displaying `"Nothing to undo: undo stack is empty."` when clicking **Undo** on an empty stack (`EmptyUndoStackException`) or `"Wire Error: Wire must connect an OUTPUT pin to an INPUT pin."` (`InvalidShapeException`).

* **Figure 8.4 — Exported SVG & JSON Output:** Show the exported `.svg` file opened in a web browser alongside the formatted `.json` file in a text editor.

* **Figure 8.5 — JUnit 5 Green Test Bar:** Capture the IDE or Maven test runner showing all 4 test classes (`ModelAndSimulationTest`, `CommandManagerTest`, `ShapeFactoryTest`, `ExporterTest`) passing with 0 failures.

## 9. Conclusion

The **Logic Circuit Simulator** demonstrates how Object-Oriented Methodology principles—strict encapsulation, inheritance hierarchies, polymorphism, clearly distinguished composition/aggregation/association relationships, SOLID design rules, custom exception hierarchies, and 7 Gang-of-Four design patterns—work together to produce a modular, extensible, and testable desktop application. By isolating the `model`, `commands`, `factory`, and `export` packages from `javax.swing`, the simulator achieves both responsive interactive graphics and 100% headless unit testability.

## 10. Future Scope

1. **Sequential Logic & Clock Generators:** Introduce a `ClockSource` element along with edge-triggered flip-flops (`DFlipFlop`, `JKFlipFlop`) and propagation delay timers.

2. **Reusable Custom IC Packaging:** Allow a `GroupShape` subcircuit (such as a 1-bit Full Adder) to be collapsed into a single named IC chip with external input/output pins and saved to a user component library.

3. **Automatic Truth Table & Karnaugh Map Generator:** Add a `TruthTableVisitor` implementing `CircuitVisitor` that enumerates all $2^n$ input switch combinations, evaluates the circuit, and exports a Markdown/LaTeX truth table and simplified boolean expression.

4. **Orthogonal Manhattan Wire Routing:** Enhance `Wire.draw()` with an A\* grid-routing strategy so wires automatically route around intermediate gates with crisp $90^\circ$ bends.