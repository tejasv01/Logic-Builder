package circuitsim.ui;

import circuitsim.model.Circuit;
import circuitsim.model.CircuitElement;
import circuitsim.model.CircuitListener;
import circuitsim.model.InputSwitch;
import circuitsim.model.Pin;
import circuitsim.tools.InteractTool;
import circuitsim.tools.Tool;
import circuitsim.tools.WireTool;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import javax.swing.JPanel;

/**
 * High-performance Swing canvas panel providing smooth rendering, modern CAD dot grid,
 * interactive zoom & pan, high-DPI antialiasing, and responsive cursor cues.
 *
 * Design Pattern:
 * - OBSERVER PATTERN: Automatically repaints on Circuit mutation notifications.
 */
public class CircuitCanvasPanel extends JPanel implements CircuitListener {
    private final Circuit circuit;
    private Tool activeTool;

    // Viewport Zoom & Pan
    private double zoom = 1.0;
    private double panX = 0;
    private double panY = 0;
    private Point panStartScreen = null;
    private boolean isPanning = false;

    // Cursor tracking
    private Point lastMouseCanvas = new Point(0, 0);

    public CircuitCanvasPanel(Circuit circuit) {
        this.circuit = circuit;
        this.circuit.addListener(this);
        setPreferredSize(new Dimension(1000, 680));

        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Middle click (button 2) or right click with Shift triggers pan
                if (e.getButton() == MouseEvent.BUTTON2 || (e.getButton() == MouseEvent.BUTTON3 && e.isShiftDown())) {
                    isPanning = true;
                    panStartScreen = e.getPoint();
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    return;
                }

                Point cp = screenToCanvas(e.getPoint());
                lastMouseCanvas = cp;
                if (activeTool != null) {
                    MouseEvent transformed = transformEvent(e, cp);
                    activeTool.mousePressed(transformed);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isPanning && panStartScreen != null) {
                    panX += (e.getX() - panStartScreen.x);
                    panY += (e.getY() - panStartScreen.y);
                    panStartScreen = e.getPoint();
                    repaint();
                    return;
                }

                Point cp = screenToCanvas(e.getPoint());
                lastMouseCanvas = cp;
                if (activeTool != null) {
                    MouseEvent transformed = transformEvent(e, cp);
                    activeTool.mouseDragged(transformed);
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isPanning) {
                    isPanning = false;
                    panStartScreen = null;
                    updateHoverCursor(e.getPoint());
                    return;
                }

                Point cp = screenToCanvas(e.getPoint());
                lastMouseCanvas = cp;
                if (activeTool != null) {
                    MouseEvent transformed = transformEvent(e, cp);
                    activeTool.mouseReleased(transformed);
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Point cp = screenToCanvas(e.getPoint());
                lastMouseCanvas = cp;
                updateHoverCursor(e.getPoint());
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                // Zoom centered around mouse location
                double oldZoom = zoom;
                if (e.getWheelRotation() < 0) {
                    zoom = Math.min(2.5, zoom * 1.1);
                } else {
                    zoom = Math.max(0.5, zoom / 1.1);
                }

                double factor = zoom / oldZoom;
                panX = e.getX() - (e.getX() - panX) * factor;
                panY = e.getY() - (e.getY() - panY) * factor;

                repaint();
            }
        };

        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        addMouseWheelListener(adapter);
    }

    public void setActiveTool(Tool tool) {
        this.activeTool = tool;
        repaint();
    }

    public Tool getActiveTool() {
        return activeTool;
    }

    public Point getLastMouseCanvas() {
        return lastMouseCanvas;
    }

    public double getZoom() {
        return zoom;
    }

    public void zoomIn() {
        zoom = Math.min(2.5, zoom * 1.2);
        repaint();
    }

    public void zoomOut() {
        zoom = Math.max(0.5, zoom / 1.2);
        repaint();
    }

    public void resetView() {
        zoom = 1.0;
        panX = 0;
        panY = 0;
        repaint();
    }

    public Point screenToCanvas(Point screenPt) {
        try {
            AffineTransform at = getTransform();
            Point2D pt = at.inverseTransform(screenPt, null);
            return new Point((int) Math.round(pt.getX()), (int) Math.round(pt.getY()));
        } catch (NoninvertibleTransformException ex) {
            return screenPt;
        }
    }

    private AffineTransform getTransform() {
        AffineTransform at = new AffineTransform();
        at.translate(panX, panY);
        at.scale(zoom, zoom);
        return at;
    }

    private MouseEvent transformEvent(MouseEvent orig, Point canvasPt) {
        return new MouseEvent(
                orig.getComponent(),
                orig.getID(),
                orig.getWhen(),
                orig.getModifiersEx(),
                canvasPt.x,
                canvasPt.y,
                orig.getClickCount(),
                orig.isPopupTrigger(),
                orig.getButton()
        );
    }

    private void updateHoverCursor(Point screenPt) {
        Point cp = screenToCanvas(screenPt);
        CircuitElement hit = circuit.findElementAt(cp);
        if (hit instanceof InputSwitch) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return;
        }
        if (activeTool instanceof WireTool) {
            Pin pin = circuit.findPinAt(cp, 12);
            if (pin != null) {
                setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                return;
            }
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public void onCircuitChanged() {
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        // High-Quality Antialiasing and Rendering Hints
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // Fill Canvas Background
        g2d.setColor(UITheme.getCanvasBg());
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Apply Viewport Transform (Zoom & Pan)
        AffineTransform origAt = g2d.getTransform();
        g2d.transform(getTransform());

        // Draw CAD Dot Grid
        drawGrid(g2d);

        // Render Circuit Elements (Wires first so gates sit on top of wire leads)
        for (CircuitElement element : circuit.getElements()) {
            if (element.getType() == circuitsim.model.ElementType.WIRE) {
                element.draw(g2d);
            }
        }
        for (CircuitElement element : circuit.getElements()) {
            if (element.getType() != circuitsim.model.ElementType.WIRE) {
                element.draw(g2d);
            }
        }

        // Draw Tool Preview (e.g., active wire drag line or gate outline)
        if (activeTool != null) {
            activeTool.drawPreview(g2d);
        }

        // Restore screen transform for HUD overlays
        g2d.setTransform(origAt);
        drawCanvasHUD(g2d);

        g2d.dispose();
    }

    private void drawGrid(Graphics2D g2d) {
        // Compute visible canvas rectangle
        Point topLeft = screenToCanvas(new Point(0, 0));
        Point botRight = screenToCanvas(new Point(getWidth(), getHeight()));

        int startX = (topLeft.x / 20) * 20 - 20;
        int endX = (botRight.x / 20) * 20 + 20;
        int startY = (topLeft.y / 20) * 20 - 20;
        int endY = (botRight.y / 20) * 20 + 20;

        Color dotColor = UITheme.getGridColor();
        Color majorDotColor = UITheme.isDarkMode() ? new Color(100, 116, 139, 180) : new Color(148, 163, 184, 200);

        // Modern CAD dot grid
        for (int x = startX; x <= endX; x += 20) {
            boolean majorX = (x % 100 == 0);
            for (int y = startY; y <= endY; y += 20) {
                boolean majorY = (y % 100 == 0);
                if (majorX && majorY) {
                    g2d.setColor(majorDotColor);
                    g2d.fillRect(x - 1, y - 1, 3, 3);
                } else if (zoom >= 0.75) {
                    g2d.setColor(dotColor);
                    g2d.fillRect(x, y, 1, 1);
                }
            }
        }
    }

    private void drawCanvasHUD(Graphics2D g2d) {
        // Floating Mini Zoom HUD at bottom-right
        int hudW = 110;
        int hudH = 28;
        int hudX = getWidth() - hudW - 16;
        int hudY = getHeight() - hudH - 16;

        g2d.setColor(UITheme.isDarkMode() ? new Color(30, 41, 59, 210) : new Color(255, 255, 255, 220));
        g2d.fillRoundRect(hudX, hudY, hudW, hudH, 14, 14);

        g2d.setColor(UITheme.isDarkMode() ? new Color(71, 85, 105) : new Color(203, 213, 225));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRoundRect(hudX, hudY, hudW, hudH, 14, 14);

        g2d.setFont(UITheme.FONT_SMALL);
        g2d.setColor(UITheme.getTextColor());
        String zoomStr = String.format("Zoom: %d%%", (int) Math.round(zoom * 100));
        g2d.drawString(zoomStr, hudX + 18, hudY + 18);
    }
}
