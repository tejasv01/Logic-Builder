package circuitsim;

import circuitsim.ui.MainFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Application entry point for the Logic Circuit Simulator (Java 17 LTS).
 * Launches the Swing GUI safely on the Event Dispatch Thread (EDT).
 *
 * SOLID (SRP): Sole responsibility is bootstrapping the look-and-feel and MainFrame.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fallback to default Swing CrossPlatformLookAndFeel
            }
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}