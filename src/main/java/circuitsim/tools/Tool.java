package circuitsim.tools;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

/**
 * Strategy / State Pattern interface representing the active interactive tool on the canvas.
 *
 * Design Pattern: STRATEGY / STATE PATTERN
 * SOLID (ISP & OCP): Lean mouse-event interface allowing new tools to be plugged in seamlessly.
 */
public interface Tool {
    void mousePressed(MouseEvent e);
    void mouseDragged(MouseEvent e);
    void mouseReleased(MouseEvent e);
    void drawPreview(Graphics2D g2d);
}
