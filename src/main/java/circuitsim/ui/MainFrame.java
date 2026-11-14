package circuitsim.ui;

import circuitsim.commands.AddShapeCommand;
import circuitsim.commands.CommandManager;
import circuitsim.commands.DeleteCommand;
import circuitsim.commands.GroupCommand;
import circuitsim.commands.RecolorCommand;
import circuitsim.exceptions.EmptyUndoStackException;
import circuitsim.exceptions.ExportFailedException;
import circuitsim.exceptions.FileFormatException;
import circuitsim.exceptions.InvalidShapeException;
import circuitsim.export.JsonCircuitLoader;
import circuitsim.export.JsonExportVisitor;
import circuitsim.export.PngExporter;
import circuitsim.export.SvgExportVisitor;
import circuitsim.model.Circuit;
import circuitsim.model.CircuitElement;
import circuitsim.model.ElementStyle;
import circuitsim.model.ElementType;
import circuitsim.model.GroupShape;
import circuitsim.model.LogicGate;
import circuitsim.model.OutputLed;
import circuitsim.model.Wire;
import circuitsim.tools.GatePlacementTool;
import circuitsim.tools.InteractTool;
import circuitsim.tools.SelectTool;
import circuitsim.tools.WireTool;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

/**
 * Modern High-Quality Main Application Window for the Logic Circuit Simulator.
 * Features a balanced 3-zone Studio Header Bar and a compact, 2-column Left Tool Palette.
 */
public class MainFrame extends JFrame {
    private final Circuit circuit = new Circuit();
    private final CommandManager commandManager = new CommandManager();
    private final CircuitCanvasPanel canvasPanel = new CircuitCanvasPanel(circuit);

    private final JLabel statusLabel = new JLabel("Ready — Select a tool or gate from the left palette to begin.");
    private final JLabel metricsLabel = new JLabel("Elements: 0");

    private final ElementStyle currentStyle = ElementStyle.createDefault();
    private final List<CircuitElement> clipboard = new ArrayList<>();

    // Palette button references for active state toggle
    private final List<ModernButton> toolButtons = new ArrayList<>();
    private JPanel headerPanel;
    private JPanel sidebarPanel;
    private JPanel statusPanel;

    public MainFrame() {
        super("Logic Circuit Simulator Pro (Java 17 Swing)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Build UI Layout
        headerPanel = buildHeaderPanel();
        sidebarPanel = buildSidebarPanel();
        statusPanel = buildStatusPanel();

        add(headerPanel, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(sidebarPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        add(scrollPane, BorderLayout.WEST);
        add(canvasPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);

        // Circuit change observer updates element counts
        circuit.addListener(this::updateMetrics);

        // Register Global Keyboard Shortcuts (Delete, Undo, Redo, Copy, Paste, Select All)
        setupKeyBindings();

        // Load Default Starter Circuit (Half Adder)
        CircuitPresets.loadHalfAdder(circuit);

        // Default tool: SelectTool
        selectTool(0, () -> canvasPanel.setActiveTool(new SelectTool(circuit, commandManager, this::setStatus)),
                "Select Tool: Click switch to toggle, drag to move/resize, drag empty canvas to box-select.");

        pack();
        setMinimumSize(new Dimension(1120, 750));
        setLocationRelativeTo(null);
    }

    private void setupKeyBindings() {
        int shortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        JComponent root = getRootPane();

        // Delete / Backspace
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteAction");
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "deleteAction");
        root.getActionMap().put("deleteAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleDelete();
            }
        });

        // Undo (Cmd+Z / Ctrl+Z)
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, shortcutKeyMask), "undoAction");
        root.getActionMap().put("undoAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleUndo();
            }
        });

        // Redo (Cmd+Y / Ctrl+Y or Cmd+Shift+Z / Ctrl+Shift+Z)
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, shortcutKeyMask), "redoAction");
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, shortcutKeyMask | InputEvent.SHIFT_DOWN_MASK), "redoAction");
        root.getActionMap().put("redoAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRedo();
            }
        });

        // Copy (Cmd+C / Ctrl+C)
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_C, shortcutKeyMask), "copyAction");
        root.getActionMap().put("copyAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleCopy();
            }
        });

        // Paste (Cmd+V / Ctrl+V)
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_V, shortcutKeyMask), "pasteAction");
        root.getActionMap().put("pasteAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handlePaste();
            }
        });

        // Select All (Cmd+A / Ctrl+A)
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_A, shortcutKeyMask), "selectAllAction");
        root.getActionMap().put("selectAllAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (CircuitElement el : circuit.getElements()) {
                    el.setSelected(true);
                }
                circuit.notifyListeners();
                setStatus("Selected all " + circuit.getElements().size() + " element(s).");
            }
        });

        // Group (Cmd+G / Ctrl+G)
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_G, shortcutKeyMask), "groupAction");
        root.getActionMap().put("groupAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleGroup();
            }
        });

        // Ungroup (Cmd+Shift+G / Ctrl+Shift+G)
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_G, shortcutKeyMask | InputEvent.SHIFT_DOWN_MASK), "ungroupAction");
        root.getActionMap().put("ungroupAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleUngroup();
            }
        });
    }

    /**
     * Balanced 3-Zone Header Bar:
     * - Left: App Branding & Live Simulation Status Badge
     * - Center: Educational Presets Dropdown & Clear Canvas
     * - Right: Segmented Zoom Controls (- / 100% / +) & Theme Toggle
     */
    private JPanel buildHeaderPanel() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UITheme.getPanelBg());
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.isDarkMode() ? new Color(51, 65, 85) : new Color(226, 232, 240)));
        bar.setPreferredSize(new Dimension(getWidth(), 48));

        // 1. Left Zone: Branding
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        brandPanel.setOpaque(false);

        JLabel logo = new JLabel("⚡");
        logo.setFont(new Font("SansSerif", Font.BOLD, 18));
        logo.setForeground(new Color(59, 130, 246));

        JLabel title = new JLabel("LOGIC CIRCUIT SIMULATOR");
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        title.setForeground(UITheme.getTextColor());

        JLabel liveBadge = new JLabel(" ● LIVE ");
        liveBadge.setFont(new Font("SansSerif", Font.BOLD, 10));
        liveBadge.setForeground(new Color(34, 197, 94));
        liveBadge.setOpaque(true);
        liveBadge.setBackground(UITheme.isDarkMode() ? new Color(20, 83, 45, 120) : new Color(220, 252, 231));
        liveBadge.setBorder(BorderFactory.createLineBorder(new Color(34, 197, 94, 160), 1, true));

        brandPanel.add(logo);
        brandPanel.add(title);
        brandPanel.add(liveBadge);

        // 2. Center Zone: Presets & Canvas Actions
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        centerPanel.setOpaque(false);

        JLabel lblPreset = new JLabel("📚 Presets:");
        lblPreset.setFont(UITheme.FONT_SMALL);
        lblPreset.setForeground(UITheme.getTextMutedColor());

        JComboBox<String> presetBox = new JComboBox<>(new String[]{
                "Half Adder", "All Gates Showcase", "SR Latch (Flip-Flop)"
        });
        presetBox.setFont(UITheme.FONT_SMALL);
        presetBox.addActionListener(e -> {
            int sel = presetBox.getSelectedIndex();
            if (sel == 0) CircuitPresets.loadHalfAdder(circuit);
            else if (sel == 1) CircuitPresets.loadGateShowcase(circuit);
            else if (sel == 2) CircuitPresets.loadSRLatch(circuit);
            commandManager.clear();
            setStatus("Loaded preset circuit: " + presetBox.getSelectedItem());
        });

        ModernButton btnClear = new ModernButton("🗑 Clear Canvas");
        btnClear.setPreferredSize(new Dimension(110, 28));
        btnClear.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Clear all circuit elements from canvas?", "Confirm Clear", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                circuit.clear();
                commandManager.clear();
                setStatus("Canvas cleared.");
            }
        });

        centerPanel.add(lblPreset);
        centerPanel.add(presetBox);
        centerPanel.add(Box.createHorizontalStrut(6));
        centerPanel.add(btnClear);

        // 3. Right Zone: Zoom & Theme
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 8));
        rightPanel.setOpaque(false);

        ModernButton btnZoomOut = new ModernButton(" - ");
        btnZoomOut.setPreferredSize(new Dimension(34, 28));
        btnZoomOut.addActionListener(e -> canvasPanel.zoomOut());

        ModernButton btnZoomReset = new ModernButton("100%");
        btnZoomReset.setPreferredSize(new Dimension(54, 28));
        btnZoomReset.addActionListener(e -> canvasPanel.resetView());

        ModernButton btnZoomIn = new ModernButton(" + ");
        btnZoomIn.setPreferredSize(new Dimension(34, 28));
        btnZoomIn.addActionListener(e -> canvasPanel.zoomIn());

        ModernButton btnTheme = new ModernButton(UITheme.isDarkMode() ? "☀️ Light" : "🌙 Dark");
        btnTheme.setPreferredSize(new Dimension(78, 28));
        btnTheme.addActionListener(e -> {
            UITheme.setDarkMode(!UITheme.isDarkMode());
            btnTheme.setText(UITheme.isDarkMode() ? "☀️ Light" : "🌙 Dark");
            refreshTheme();
        });

        rightPanel.add(btnZoomOut);
        rightPanel.add(btnZoomReset);
        rightPanel.add(btnZoomIn);
        rightPanel.add(Box.createHorizontalStrut(6));
        rightPanel.add(btnTheme);

        bar.add(brandPanel, BorderLayout.WEST);
        bar.add(centerPanel, BorderLayout.CENTER);
        bar.add(rightPanel, BorderLayout.EAST);
        return bar;
    }

    /**
     * Compact, 2-Column Left Sidebar Tool Palette:
     * Fits cleanly on screen without awkward vertical scrolling.
     */
    private JPanel buildSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.getSidebarBg());
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.isDarkMode() ? new Color(51, 65, 85) : new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        sidebar.setPreferredSize(new Dimension(230, 680));

        // 1. INTERACTION TOOLS
        sidebar.add(createSectionLabel("INTERACTION TOOLS"));
        sidebar.add(Box.createVerticalStrut(4));

        ModernButton btnSelect = createToolButton("↖ Select / Move", 0, () -> {
            canvasPanel.setActiveTool(new SelectTool(circuit, commandManager, this::setStatus));
            setStatus("Select Tool: Click switch to toggle, drag to move/resize, drag empty canvas to box-select.");
        });
        btnSelect.setMaximumSize(new Dimension(210, 30));
        btnSelect.setPreferredSize(new Dimension(210, 30));
        btnSelect.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(btnSelect);
        sidebar.add(Box.createVerticalStrut(4));

        JPanel toolRow = new JPanel(new GridLayout(1, 2, 6, 0));
        toolRow.setOpaque(false);
        toolRow.setMaximumSize(new Dimension(210, 30));
        toolRow.setAlignmentX(LEFT_ALIGNMENT);

        ModernButton btnWire = createToolButton("🔌 Wire Tool", 1, () -> {
            canvasPanel.setActiveTool(new WireTool(circuit, commandManager, currentStyle::clone, this::setStatus));
            setStatus("Wire Tool: Drag between pins with smart magnetic snapping.");
        });
        ModernButton btnInteract = createToolButton("⚡ Toggle SW", 2, () -> {
            canvasPanel.setActiveTool(new InteractTool(circuit, this::setStatus));
            setStatus("Interact Tool: Click any Input Switch (SW) to toggle HIGH (1) / LOW (0).");
        });
        toolRow.add(btnWire);
        toolRow.add(btnInteract);
        sidebar.add(toolRow);
        sidebar.add(Box.createVerticalStrut(10));

        // 2. LOGIC GATES (2-Column Grid: 3 rows of 2)
        sidebar.add(createSectionLabel("LOGIC GATES"));
        sidebar.add(Box.createVerticalStrut(4));

        JPanel gateGrid = new JPanel(new GridLayout(3, 2, 6, 4));
        gateGrid.setOpaque(false);
        gateGrid.setMaximumSize(new Dimension(210, 96));
        gateGrid.setAlignmentX(LEFT_ALIGNMENT);

        ElementType[] gates = {
                ElementType.AND, ElementType.OR,
                ElementType.NOT, ElementType.XOR,
                ElementType.NAND, ElementType.NOR
        };

        int toolIdx = 3;
        for (ElementType type : gates) {
            final int idx = toolIdx++;
            ModernButton btn = createToolButton("⊞ " + type.name(), idx, () -> {
                canvasPanel.setActiveTool(new GatePlacementTool(
                        circuit,
                        commandManager,
                        type,
                        currentStyle::clone,
                        () -> JOptionPane.showInputDialog(this, "Enter label text:", "Half Adder"),
                        this::setStatus));
                setStatus("Placement Tool (" + type + "): Click on canvas to place (snaps to grid).");
            });
            gateGrid.add(btn);
        }
        sidebar.add(gateGrid);
        sidebar.add(Box.createVerticalStrut(10));

        // 3. INPUT / OUTPUT & ANNOTATIONS
        sidebar.add(createSectionLabel("COMPONENTS"));
        sidebar.add(Box.createVerticalStrut(4));

        JPanel ioGrid = new JPanel(new GridLayout(1, 2, 6, 0));
        ioGrid.setOpaque(false);
        ioGrid.setMaximumSize(new Dimension(210, 30));
        ioGrid.setAlignmentX(LEFT_ALIGNMENT);

        final int swIdx = toolIdx++;
        ModernButton btnSwitch = createToolButton("🔘 Switch", swIdx, () -> {
            canvasPanel.setActiveTool(new GatePlacementTool(
                    circuit, commandManager, ElementType.SWITCH, currentStyle::clone, null, this::setStatus));
            setStatus("Place Switch: Click on canvas to place an interactive input switch.");
        });
        final int ledIdx = toolIdx++;
        ModernButton btnLed = createToolButton("💡 LED", ledIdx, () -> {
            canvasPanel.setActiveTool(new GatePlacementTool(
                    circuit, commandManager, ElementType.LED, currentStyle::clone, null, this::setStatus));
            setStatus("Place LED: Click on canvas to place an output signal LED.");
        });
        ioGrid.add(btnSwitch);
        ioGrid.add(btnLed);
        sidebar.add(ioGrid);
        sidebar.add(Box.createVerticalStrut(4));

        final int textIdx = toolIdx++;
        ModernButton btnText = createToolButton("🏷 Text Label", textIdx, () -> {
            canvasPanel.setActiveTool(new GatePlacementTool(
                    circuit, commandManager, ElementType.TEXT, currentStyle::clone,
                    () -> JOptionPane.showInputDialog(this, "Enter label text:", "Schematic Label"),
                    this::setStatus));
            setStatus("Place Label: Click on canvas to place an annotation label.");
        });
        btnText.setMaximumSize(new Dimension(210, 28));
        btnText.setPreferredSize(new Dimension(210, 28));
        btnText.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(btnText);
        sidebar.add(Box.createVerticalStrut(10));

        // 4. STYLING & APPEARANCE
        sidebar.add(createSectionLabel("STYLING & APPEARANCE"));
        sidebar.add(Box.createVerticalStrut(4));

        JPanel colorRow = new JPanel(new GridLayout(1, 2, 6, 0));
        colorRow.setOpaque(false);
        colorRow.setMaximumSize(new Dimension(210, 28));
        colorRow.setAlignmentX(LEFT_ALIGNMENT);
        ModernButton btnFill = new ModernButton("🎨 Fill");
        btnFill.addActionListener(e -> handleFillColor());
        ModernButton btnStroke = new ModernButton("✏️ Stroke");
        btnStroke.addActionListener(e -> handleStrokeColor());
        colorRow.add(btnFill);
        colorRow.add(btnStroke);
        sidebar.add(colorRow);
        sidebar.add(Box.createVerticalStrut(4));

        JPanel widthPanel = new JPanel(new BorderLayout(6, 0));
        widthPanel.setOpaque(false);
        widthPanel.setMaximumSize(new Dimension(210, 26));
        widthPanel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lblWidth = new JLabel("Stroke Width: ");
        lblWidth.setFont(UITheme.FONT_SMALL);
        lblWidth.setForeground(UITheme.getTextColor());
        JComboBox<String> widthBox = new JComboBox<>(new String[]{"1.5 px", "2.0 px", "3.0 px", "4.0 px"});
        widthBox.setSelectedIndex(1);
        widthBox.setFont(UITheme.FONT_SMALL);
        widthBox.addActionListener(e -> {
            String selected = (String) widthBox.getSelectedItem();
            if (selected != null) {
                float w = Float.parseFloat(selected.replace(" px", ""));
                currentStyle.setStrokeWidth(w);
                applyStyleToSelection();
            }
        });
        widthPanel.add(lblWidth, BorderLayout.WEST);
        widthPanel.add(widthBox, BorderLayout.CENTER);
        sidebar.add(widthPanel);
        sidebar.add(Box.createVerticalStrut(10));

        // 5. EDIT & CLIPBOARD (2-Column Grid)
        sidebar.add(createSectionLabel("EDIT & CLIPBOARD"));
        sidebar.add(Box.createVerticalStrut(4));

        JPanel editGrid1 = new JPanel(new GridLayout(2, 2, 6, 4));
        editGrid1.setOpaque(false);
        editGrid1.setMaximumSize(new Dimension(210, 60));
        editGrid1.setAlignmentX(LEFT_ALIGNMENT);

        ModernButton btnUndo = new ModernButton("Undo");
        btnUndo.addActionListener(e -> handleUndo());
        ModernButton btnRedo = new ModernButton("Redo");
        btnRedo.addActionListener(e -> handleRedo());
        ModernButton btnCopy = new ModernButton("Copy");
        btnCopy.addActionListener(e -> handleCopy());
        ModernButton btnPaste = new ModernButton("Paste");
        btnPaste.addActionListener(e -> handlePaste());

        editGrid1.add(btnUndo);
        editGrid1.add(btnRedo);
        editGrid1.add(btnCopy);
        editGrid1.add(btnPaste);
        sidebar.add(editGrid1);
        sidebar.add(Box.createVerticalStrut(4));

        JPanel editGrid2 = new JPanel(new GridLayout(2, 2, 6, 4));
        editGrid2.setOpaque(false);
        editGrid2.setMaximumSize(new Dimension(210, 60));
        editGrid2.setAlignmentX(LEFT_ALIGNMENT);

        ModernButton btnGroup = new ModernButton("Group");
        btnGroup.addActionListener(e -> handleGroup());
        ModernButton btnUngroup = new ModernButton("Ungroup");
        btnUngroup.addActionListener(e -> handleUngroup());
        ModernButton btnSelectAll = new ModernButton("Select All");
        btnSelectAll.addActionListener(e -> {
            for (CircuitElement el : circuit.getElements()) {
                el.setSelected(true);
            }
            circuit.notifyListeners();
            setStatus("Selected all " + circuit.getElements().size() + " element(s).");
        });
        ModernButton btnDelete = new ModernButton("Delete", new Color(239, 68, 68));
        btnDelete.addActionListener(e -> handleDelete());

        editGrid2.add(btnGroup);
        editGrid2.add(btnUngroup);
        editGrid2.add(btnSelectAll);
        editGrid2.add(btnDelete);
        sidebar.add(editGrid2);
        sidebar.add(Box.createVerticalStrut(10));

        // 6. FILE & EXPORT (2-Column Grid)
        sidebar.add(createSectionLabel("FILE & EXPORT"));
        sidebar.add(Box.createVerticalStrut(4));

        JPanel fileGrid = new JPanel(new GridLayout(2, 2, 6, 4));
        fileGrid.setOpaque(false);
        fileGrid.setMaximumSize(new Dimension(210, 60));
        fileGrid.setAlignmentX(LEFT_ALIGNMENT);

        ModernButton btnSave = new ModernButton("💾 Save");
        btnSave.addActionListener(e -> handleSaveJson());
        ModernButton btnLoad = new ModernButton("📂 Load");
        btnLoad.addActionListener(e -> handleLoadJson());
        ModernButton btnSvg = new ModernButton("SVG");
        btnSvg.addActionListener(e -> handleExportSvg());
        ModernButton btnPng = new ModernButton("PNG");
        btnPng.addActionListener(e -> handleExportPng());

        fileGrid.add(btnSave);
        fileGrid.add(btnLoad);
        fileGrid.add(btnSvg);
        fileGrid.add(btnPng);
        sidebar.add(fileGrid);

        return sidebar;
    }

    private JLabel createSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setForeground(UITheme.getTextMutedColor());
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private ModernButton createToolButton(String label, int index, Runnable action) {
        ModernButton btn = new ModernButton(label);
        btn.setFont(UITheme.FONT_SMALL);
        btn.addActionListener(e -> selectTool(index, action, null));
        toolButtons.add(btn);
        return btn;
    }

    private void selectTool(int activeIndex, Runnable action, String statusMsg) {
        for (int i = 0; i < toolButtons.size(); i++) {
            toolButtons.get(i).setActive(i == activeIndex);
        }
        if (action != null) {
            action.run();
        }
        if (statusMsg != null) {
            setStatus(statusMsg);
        }
    }

    private JPanel buildStatusPanel() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UITheme.getPanelBg());
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.isDarkMode() ? new Color(51, 65, 85) : new Color(226, 232, 240)));
        bar.setPreferredSize(new Dimension(getWidth(), 30));

        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.getTextColor());
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

        metricsLabel.setFont(UITheme.FONT_SMALL);
        metricsLabel.setForeground(UITheme.getTextMutedColor());
        metricsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16));

        bar.add(statusLabel, BorderLayout.WEST);
        bar.add(metricsLabel, BorderLayout.EAST);
        return bar;
    }

    private void updateMetrics() {
        int gates = 0, wires = 0, switches = 0, leds = 0;
        for (CircuitElement el : circuit.getElements()) {
            if (el instanceof LogicGate) gates++;
            else if (el instanceof Wire) wires++;
            else if (el instanceof circuitsim.model.InputSwitch) switches++;
            else if (el instanceof OutputLed) leds++;
        }
        metricsLabel.setText(String.format("Gates: %d | Wires: %d | Switches: %d | LEDs: %d",
                gates, wires, switches, leds));
    }

    private void refreshTheme() {
        headerPanel.setBackground(UITheme.getPanelBg());
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.isDarkMode() ? new Color(51, 65, 85) : new Color(226, 232, 240)));

        sidebarPanel.setBackground(UITheme.getSidebarBg());
        sidebarPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.isDarkMode() ? new Color(51, 65, 85) : new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        statusPanel.setBackground(UITheme.getPanelBg());
        statusPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.isDarkMode() ? new Color(51, 65, 85) : new Color(226, 232, 240)));
        statusLabel.setForeground(UITheme.getTextColor());
        metricsLabel.setForeground(UITheme.getTextMutedColor());

        canvasPanel.repaint();
        repaint();
    }

    private void handleUndo() {
        try {
            commandManager.undo();
            setStatus("Undid last action.");
        } catch (EmptyUndoStackException ex) {
            setStatus(ex.getMessage());
        }
    }

    private void handleRedo() {
        try {
            commandManager.redo();
            setStatus("Redid action.");
        } catch (EmptyUndoStackException ex) {
            setStatus(ex.getMessage());
        }
    }

    private void handleDelete() {
        List<CircuitElement> sel = circuit.getSelectedElements();
        if (sel.isEmpty()) {
            setStatus("Select at least one element to delete.");
            return;
        }
        commandManager.executeCommand(new DeleteCommand(circuit, sel));
        setStatus("Deleted " + sel.size() + " element(s).");
    }

    private void handleCopy() {
        List<CircuitElement> sel = circuit.getSelectedElements();
        if (sel.isEmpty()) {
            setStatus("Select at least one element to copy.");
            return;
        }
        clipboard.clear();
        for (CircuitElement el : sel) {
            if (el.getType() != ElementType.WIRE) {
                clipboard.add(el.clone()); // Prototype Pattern
            }
        }
        setStatus("Copied " + clipboard.size() + " element(s) to clipboard.");
    }

    private void handlePaste() {
        if (clipboard.isEmpty()) {
            setStatus("Clipboard is empty.");
            return;
        }
        circuit.clearSelection();
        for (CircuitElement proto : clipboard) {
            CircuitElement copy = proto.clone();
            copy.move(25, 25);
            copy.setSelected(true);
            commandManager.executeCommand(new AddShapeCommand(circuit, copy));
        }
        setStatus("Pasted " + clipboard.size() + " cloned element(s).");
    }

    private void handleGroup() {
        List<CircuitElement> sel = circuit.getSelectedElements();
        if (sel.size() < 2) {
            setStatus("Select at least 2 elements (box-select or Shift+Click) to group into a Subcircuit.");
            return;
        }
        try {
            commandManager.executeCommand(new GroupCommand(circuit, sel));
            setStatus("Grouped " + sel.size() + " elements into a GroupShape.");
        } catch (InvalidShapeException ex) {
            setStatus("Group error: " + ex.getMessage());
        }
    }

    private void handleUngroup() {
        List<CircuitElement> sel = circuit.getSelectedElements();
        if (sel.size() == 1 && sel.get(0) instanceof GroupShape) {
            commandManager.executeCommand(new GroupCommand(circuit, (GroupShape) sel.get(0)));
            setStatus("Ungrouped subcircuit.");
        } else {
            setStatus("Select a single GroupShape to ungroup.");
        }
    }

    private void handleFillColor() {
        Color chosen = JColorChooser.showDialog(this, "Choose Component Fill Color", currentStyle.getFillColor());
        if (chosen != null) {
            currentStyle.setFillColor(chosen);
            applyStyleToSelection();
        }
    }

    private void handleStrokeColor() {
        Color chosen = JColorChooser.showDialog(this, "Choose Component Border/Stroke Color", currentStyle.getStrokeColor());
        if (chosen != null) {
            currentStyle.setStrokeColor(chosen);
            applyStyleToSelection();
        }
    }

    private void applyStyleToSelection() {
        List<CircuitElement> sel = circuit.getSelectedElements();
        if (!sel.isEmpty()) {
            commandManager.executeCommand(new RecolorCommand(circuit, sel, currentStyle.clone()));
            setStatus("Updated styling on " + sel.size() + " selected element(s).");
        } else {
            setStatus("Style updated for future elements. Select elements to apply immediately.");
        }
    }

    private void handleSaveJson() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = ensureExtension(chooser.getSelectedFile(), ".json");
            try {
                new JsonExportVisitor().exportToFile(circuit, file);
                setStatus("Saved circuit JSON to " + file.getName());
            } catch (ExportFailedException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleLoadJson() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                new JsonCircuitLoader().loadFromFile(circuit, file);
                commandManager.clear();
                setStatus("Loaded circuit from " + file.getName());
            } catch (FileFormatException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleExportSvg() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = ensureExtension(chooser.getSelectedFile(), ".svg");
            try {
                new SvgExportVisitor().exportToFile(circuit, file, canvasPanel.getWidth(), canvasPanel.getHeight());
                setStatus("Exported SVG to " + file.getName());
            } catch (ExportFailedException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleExportPng() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = ensureExtension(chooser.getSelectedFile(), ".png");
            try {
                new PngExporter().exportToFile(circuit, file, canvasPanel.getWidth(), canvasPanel.getHeight());
                setStatus("Exported PNG to " + file.getName());
            } catch (ExportFailedException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private File ensureExtension(File f, String ext) {
        if (!f.getName().toLowerCase().endsWith(ext)) {
            return new File(f.getParentFile(), f.getName() + ext);
        }
        return f;
    }

    public void setStatus(String message) {
        statusLabel.setText(message);
    }
}
