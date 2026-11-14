package circuitsim.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

/**
 * Premium custom-rendered Swing button with rounded corners, smooth hover effects,
 * active state highlights, and modern typography.
 */
public class ModernButton extends JButton {
    private boolean hovered = false;
    private boolean active = false;
    private Color customAccent = null;
    private int cornerRadius = 8;

    public ModernButton(String text) {
        super(text);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setFont(UITheme.FONT_BODY);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    public ModernButton(String text, Color accent) {
        this(text);
        this.customAccent = accent;
    }

    public void setActive(boolean active) {
        this.active = active;
        repaint();
    }

    public boolean isActive() {
        return active;
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int w = getWidth();
        int h = getHeight();

        Color bg;
        Color border;
        Color fg;

        boolean dark = UITheme.isDarkMode();

        if (active) {
            bg = (customAccent != null) ? customAccent : UITheme.getAccentColor();
            border = bg.brighter();
            fg = Color.WHITE;
        } else if (getModel().isPressed()) {
            bg = dark ? new Color(51, 65, 85) : new Color(203, 213, 225);
            border = dark ? new Color(71, 85, 105) : new Color(148, 163, 184);
            fg = UITheme.getTextColor();
        } else if (hovered) {
            bg = dark ? new Color(39, 53, 76) : new Color(226, 232, 240);
            border = dark ? new Color(71, 85, 105) : new Color(148, 163, 184);
            fg = UITheme.getTextColor();
        } else {
            bg = dark ? new Color(30, 41, 59) : new Color(241, 245, 249);
            border = dark ? new Color(51, 65, 85) : new Color(226, 232, 240);
            fg = UITheme.getTextColor();
        }

        // Paint background
        g2.setColor(bg);
        g2.fillRoundRect(1, 1, w - 2, h - 2, cornerRadius, cornerRadius);

        // Paint border
        g2.setColor(border);
        g2.setStroke(new BasicStroke(active ? 1.5f : 1.0f));
        g2.drawRoundRect(1, 1, w - 3, h - 3, cornerRadius, cornerRadius);

        // Paint text
        g2.setFont(getFont());
        g2.setColor(fg);
        FontMetrics fm = g2.getFontMetrics();
        int textX = (w - fm.stringWidth(getText())) / 2;
        int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }
}
