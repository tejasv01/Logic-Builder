package circuitsim.ui;

import java.awt.Color;
import java.awt.Font;

/**
 * Encapsulates the visual theme tokens for the Logic Circuit Simulator.
 * Supports both a sleek modern Dark Studio mode (with neon logic glow)
 * and a crisp Light Blueprint mode.
 */
public class UITheme {
    private static boolean darkMode = true;

    // Dark Mode Palette (Modern Slate / Neon Cyberpunk)
    public static final Color DARK_BG = new Color(15, 23, 42);          // Slate 900
    public static final Color DARK_PANEL_BG = new Color(30, 41, 59);    // Slate 800
    public static final Color DARK_SIDEBAR_BG = new Color(24, 32, 47);  // Darker slate
    public static final Color DARK_GRID_DOT = new Color(51, 65, 85, 120); // Slate 700
    public static final Color DARK_GRID_LINE = new Color(30, 41, 59, 100);
    public static final Color DARK_TEXT = new Color(241, 245, 249);     // Slate 100
    public static final Color DARK_TEXT_MUTED = new Color(148, 163, 184); // Slate 400
    public static final Color DARK_CARD_BG = new Color(30, 41, 59);     // Gate fill
    public static final Color DARK_CARD_BORDER = new Color(71, 85, 105); // Gate stroke
    public static final Color DARK_ACCENT = new Color(59, 130, 246);    // Blue 500

    // Light Mode Palette (Modern Clean Studio)
    public static final Color LIGHT_BG = new Color(248, 250, 252);       // Slate 50
    public static final Color LIGHT_PANEL_BG = new Color(255, 255, 255);
    public static final Color LIGHT_SIDEBAR_BG = new Color(241, 245, 249);
    public static final Color LIGHT_GRID_DOT = new Color(203, 213, 225, 160);
    public static final Color LIGHT_GRID_LINE = new Color(226, 232, 240, 140);
    public static final Color LIGHT_TEXT = new Color(15, 23, 42);
    public static final Color LIGHT_TEXT_MUTED = new Color(100, 116, 139);
    public static final Color LIGHT_CARD_BG = new Color(255, 255, 255);
    public static final Color LIGHT_CARD_BORDER = new Color(148, 163, 184);
    public static final Color LIGHT_ACCENT = new Color(37, 99, 235);

    // Common High-Quality Colors
    public static final Color SIGNAL_HIGH = new Color(34, 197, 94);     // Emerald 500
    public static final Color SIGNAL_HIGH_GLOW = new Color(74, 222, 128, 80); // Neon glow
    public static final Color SIGNAL_LOW_DARK = new Color(71, 85, 105); // Slate 600
    public static final Color SIGNAL_LOW_LIGHT = new Color(148, 163, 184);
    public static final Color SELECTION_COLOR = new Color(56, 189, 248); // Sky 400
    public static final Color SELECTION_HALO = new Color(56, 189, 248, 60);

    // Fonts
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_HEADER = new Font("SansSerif", Font.BOLD, 12);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_GATE_LABEL = new Font("SansSerif", Font.BOLD, 12);

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void setDarkMode(boolean dark) {
        darkMode = dark;
    }

    public static Color getCanvasBg() {
        return darkMode ? DARK_BG : LIGHT_BG;
    }

    public static Color getPanelBg() {
        return darkMode ? DARK_PANEL_BG : LIGHT_PANEL_BG;
    }

    public static Color getSidebarBg() {
        return darkMode ? DARK_SIDEBAR_BG : LIGHT_SIDEBAR_BG;
    }

    public static Color getGridColor() {
        return darkMode ? DARK_GRID_DOT : LIGHT_GRID_DOT;
    }

    public static Color getTextColor() {
        return darkMode ? DARK_TEXT : LIGHT_TEXT;
    }

    public static Color getTextMutedColor() {
        return darkMode ? DARK_TEXT_MUTED : LIGHT_TEXT_MUTED;
    }

    public static Color getCardBg() {
        return darkMode ? DARK_CARD_BG : LIGHT_CARD_BG;
    }

    public static Color getCardBorder() {
        return darkMode ? DARK_CARD_BORDER : LIGHT_CARD_BORDER;
    }

    public static Color getAccentColor() {
        return darkMode ? DARK_ACCENT : LIGHT_ACCENT;
    }

    public static Color getSignalLowColor() {
        return darkMode ? SIGNAL_LOW_DARK : SIGNAL_LOW_LIGHT;
    }
}
