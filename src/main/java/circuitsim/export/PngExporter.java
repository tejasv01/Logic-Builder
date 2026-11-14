package circuitsim.export;

import circuitsim.exceptions.ExportFailedException;
import circuitsim.model.Circuit;
import circuitsim.model.CircuitElement;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Exports the current Circuit schematic to a high-resolution PNG raster image.
 */
public class PngExporter {

    public void exportToFile(Circuit circuit, File file, int width, int height)
            throws ExportFailedException {
        if (file == null) {
            throw new ExportFailedException("Target PNG file cannot be null.");
        }
        if (width <= 0 || height <= 0) {
            throw new ExportFailedException("Image dimensions must be positive.");
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);

            for (CircuitElement el : circuit.getElements()) {
                el.draw(g2d);
            }
        } finally {
            g2d.dispose();
        }

        try {
            boolean written = ImageIO.write(image, "png", file);
            if (!written) {
                throw new ExportFailedException("No PNG ImageWriter found.");
            }
        } catch (IOException e) {
            throw new ExportFailedException("Failed to write PNG file: " + e.getMessage(), e);
        }
    }
}
